package Server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import AuthenticationConstants.ACs;	// Authentication Constants
import CSV.CSVUtils;

public class ServerClassCP2MultiThread {
	
	private static boolean sendMsg(PrintWriter out,String msg){
		out.println(msg);
		out.flush();
		return true;
	}
	
	private static PublicKey getPublicKey(String key){
	    try{
	        byte[] byteKey = Base64.getDecoder().decode(key);
	        X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
	        KeyFactory kf = KeyFactory.getInstance("RSA");

	        return kf.generatePublic(X509publicKey);
	    }
	    catch(Exception e){
	        e.printStackTrace();
	    }

	    return null;
	}
	
	private static Key getAESKey(String AESKeyString, Cipher rsaDCipher) throws IllegalBlockSizeException, BadPaddingException {
		byte[] byteKey = DatatypeConverter.parseBase64Binary(AESKeyString);
		byte[] decryptedByteKey = rsaDCipher.doFinal(byteKey);
		SecretKey sessionKey = new SecretKeySpec(decryptedByteKey, 0, decryptedByteKey.length, "AES");
		return sessionKey;
	}
	
	private static boolean terminateConnection(PrintWriter out){
		out.println(ACs.TERMINATEMSG);
		return false;
	}

	private static boolean authenticationProtocol(BufferedReader in, PrintWriter out, Cipher rsaECipher, String serverCertPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{

		System.out.println("Starting authentication protocol");
		
		byte[] clientNonceInBytes = new byte[32];
		String clientNonce = in.readLine();
		clientNonceInBytes = DatatypeConverter.parseBase64Binary(clientNonce);	    
		
		byte[] encryptedNonce = rsaECipher.doFinal(clientNonceInBytes);
		sendMsg(out, DatatypeConverter.printBase64Binary(encryptedNonce));
		
		if(!(in.readLine().equals(ACs.REQUESTSIGNEDCERT ))){
			System.out.println("Request Signed Certificate Error!");
			return terminateConnection(out);
		}

		File certFile = new File(serverCertPath);
		byte[] certBytes = new byte[(int) certFile.length()];
		BufferedInputStream certFileInput = new BufferedInputStream(new FileInputStream(certFile));
		certFileInput.read(certBytes,0,certBytes.length);
		certFileInput.close();
		
		// Prepping client to receive certificate in bytes
		sendMsg(out, Integer.toString(certBytes.length) );
		// Sending signed cert of server - includes public key of client
		sendMsg(out, DatatypeConverter.printBase64Binary(certBytes));
		
		System.out.println("Waiting for client to confirm my identity");
		if(!ACs.SERVERIDENTIFIED.equals(in.readLine())){
			System.out.println("Client did not verify my ID properly");
			return terminateConnection(out);
		}
		
		// Generate nonce to ensure that client is a valid requester, and not a playback attacker
		byte[] serverNonce = new byte[32];
		Random randGen = SecureRandom.getInstanceStrong();
		randGen.nextBytes(serverNonce);
		String serverNonceString = new String(serverNonce, "UTF-16");
	
		// "Sending nonce to client"
		sendMsg(out,DatatypeConverter.printBase64Binary(serverNonce));
		 
		byte[] encryptedServerNonce = new byte[128];
		// "Receiving nonce encrypted with client's private key"
		encryptedServerNonce = DatatypeConverter.parseBase64Binary(in.readLine());
		
		// "Requesting for client public key"
		sendMsg(out,ACs.REQUESTCLIENTPUBLICKEY);

		// Receiving client's public key
		String clientPublicKeyString = in.readLine();
		
		Cipher rsaDCipherClientPublic = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		
		Key clientPublicKey = getPublicKey(clientPublicKeyString);
		rsaDCipherClientPublic.init(Cipher.DECRYPT_MODE, clientPublicKey);
		
		byte[] decryptedServerNonce = rsaDCipherClientPublic.doFinal(encryptedServerNonce);
		String decryptedNonceString = new String(decryptedServerNonce, "UTF-16");
		if(!decryptedNonceString.equals(serverNonceString)){
		   	 System.out.println("Client authentication failed!");
		   	 return terminateConnection(out);
		}
		
		System.out.println("Completed authentication protocol, server ready to receive files");
		sendMsg(out,ACs.SERVERREADYTORECEIVE);

		return true;
	}
	
	private static byte[] decryptFile(byte[] encryptedData, Cipher rsaDecryptionCipher) throws Exception{
		
		System.out.println("Decrypting client's files ... ");
		
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

	    int start = 0;
	    int fileSize = encryptedData.length;
	    while (start < fileSize) {
	        byte[] tempBuff;
	        if (fileSize - start >= 128) {
	            tempBuff = rsaDecryptionCipher.doFinal(encryptedData, start, 128);
	        } else {
	            tempBuff = rsaDecryptionCipher.doFinal(encryptedData, start, fileSize - start);
	        }
	        byteOutput.write(tempBuff, 0, tempBuff.length);
	        start += 128;
	    }
	    byte[] decryptedFileBytes = byteOutput.toByteArray();
	    byteOutput.close();
	    
	    System.out.println("Decryption complete");
	    return decryptedFileBytes;


	  }
	
	protected static void handleRequest(Socket clientSocket) throws Exception{
		final String privateKeyFileName = "D:\\Backup\\SUTD\\ISTD\\Computer Systems Engineering\\CSE-Programming-Assignments\\CSE-Programming-Assignment-2\\privateServerNic.der";
		final String serverCertPath = "D:\\Backup\\SUTD\\ISTD\\Computer Systems Engineering\\CSE-Programming-Assignments\\CSE-Programming-Assignment-2\\1001490.crt";
		final Path keyPath = Paths.get(privateKeyFileName);
	    final byte[] privateKeyByteArray = Files.readAllBytes(keyPath);
	    final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyByteArray);
		
		final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		final PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		
		// Create encryption cipher
		final Cipher rsaECipherPrivate = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		final Cipher rsaDCipherPrivate = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		
		rsaECipherPrivate.init(Cipher.ENCRYPT_MODE, privateKey);
		rsaDCipherPrivate.init(Cipher.DECRYPT_MODE, privateKey);
		
		

		BufferedReader in = new BufferedReader(
								new InputStreamReader(
										new DataInputStream(clientSocket.getInputStream())));
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		
		boolean proceed = authenticationProtocol(in,out,rsaECipherPrivate, serverCertPath);
		
		if(!proceed){
			System.out.println("Authentication protocol failed!");
			return;
		}
		
		// "Waiting for encrypted AES Key from client"
		String AESKeyString = in.readLine();
		Key AESKey = getAESKey(AESKeyString,rsaDCipherPrivate);
		
		Cipher AESCipher = Cipher.getInstance("AES");
		AESCipher.init(Cipher.DECRYPT_MODE, AESKey);
		
		// "Waiting for encrypted file from client"
		Map<String,Long> fileUploadTimings = new ConcurrentHashMap<String, Long>();
		boolean clientDone = false;
		
		String csvFile = "CP2Timings.csv";
        FileWriter writer = new FileWriter(csvFile,true);
        CSVUtils.writeLine(writer, Arrays.asList("File Size", "Time Taken (ms)"));
        
        final int NCPU = Runtime.getRuntime().availableProcessors();
    	final double TCPU = 0.8;
    	final double WCRATIO = 0.5;
    	final int NTHREADS = (int) (NCPU*TCPU * (1+WCRATIO));
    	final Executor threadExec = Executors.newFixedThreadPool(NTHREADS);
		final Phaser phaser = new Phaser();
		phaser.register();	// Registers current thread
		do{
			long startTime = System.currentTimeMillis();
			
			String clientsFileName = in.readLine();
			System.out.println("Received client's file name");
	        int clientFileSize = Integer.parseInt(in.readLine());
	        System.out.println("File size " + clientFileSize);
	        byte[] encryptedDataFile = new byte[clientFileSize];

	        // Read in encrypted file String representation
	        
			String clientEncryptedFileString = in.readLine();
			System.out.println("Received client's encrypted file : "+clientsFileName);
			phaser.register(); // Registers a new file to decrypt
			Runnable decryptionWorker = new Runnable(){
				public void run(){
					try {
						final Cipher rsaDCipherPrivate = Cipher.getInstance("RSA/ECB/PKCS1Padding");
						rsaDCipherPrivate.init(Cipher.DECRYPT_MODE, privateKey);
						handleDecryption(encryptedDataFile, clientEncryptedFileString, clientsFileName, AESCipher, fileUploadTimings, startTime, phaser);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			
			threadExec.execute(decryptionWorker);
			
	        clientDone = ACs.CLIENTDONE.equals(in.readLine());
	        System.out.println("Is client Done? " + clientDone);
			long endTime = System.currentTimeMillis();
			long elapsedTime = endTime-startTime;
//			CSVUtils.writeLine(writer, Arrays.asList(Integer.toString(clientFileSize), Long.toString(elapsedTime)));
		}while(!clientDone);
		
		writer.close();
		
		
		phaser.arriveAndAwaitAdvance();
		System.out.println(fileUploadTimings.toString());
	}
	
	protected static void handleDecryption(byte[] encryptedDataFile, String clientEncryptedFileString, String clientsFileName, Cipher AESDCipher, Map<String, Long> fileUploadTimings, long startTime, Phaser phaser) throws Exception{	
		encryptedDataFile = DatatypeConverter.parseBase64Binary(clientEncryptedFileString);
		
		byte[] clientDecryptedFileBytes = decryptFile(encryptedDataFile,AESDCipher);
		FileOutputStream fileOutput = new FileOutputStream("ReceivedFile\\"+clientsFileName);
        fileOutput.write(clientDecryptedFileBytes, 0, clientDecryptedFileBytes.length);
        fileOutput.close();
        System.out.println("Successfully saved client's file : "+clientsFileName);
        phaser.arriveAndDeregister();
        fileUploadTimings.put(clientsFileName,System.currentTimeMillis()-startTime);
	}
	
	/*
	 * args:
	 * 	portNumber
	 * 	private key location
	 * 	Certificate Location
	 * 	
	 */
	public static void main(String[] args) throws Exception{
		
		//String hostName = args[0];
//		int portNum = Integer.parseInt(args[0]);
		
		int portNum = 7777;	// socket address
		ServerSocket serverSocket;		
		serverSocket = new ServerSocket(portNum);
		
		final Executor exec = Executors.newCachedThreadPool();
		
		while(true){
			System.out.println("Accepting client connections now ...");
			final Socket clientSocket = serverSocket.accept();
			System.out.println("Client connection established!");
			Runnable OpenConnections = new Runnable(){
				public void run(){
					try {
						handleRequest(clientSocket);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			
			exec.execute(OpenConnections);
		}
		
	}
	
	
}


