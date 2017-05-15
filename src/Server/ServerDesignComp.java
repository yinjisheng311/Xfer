package Server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import AuthenticationConstants.ACs;
import CSV.CSVUtils;

public class ServerDesignComp {
	
	protected final static String rootAddr = "D:\\Backup\\SUTD\\ISTD\\Computer Systems Engineering\\CSE-Programming-Assignments\\CSE-Programming-Assignment-2\\Eclipse Project";
	protected final static File rootDir = new File(rootAddr);
	
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
	
	private static String authenticationProtocol(BufferedReader in, PrintWriter out, Cipher rsaECipher, String serverCertPath) throws Exception{
		
		System.out.println("Starting authentication protocol");
		
		byte[] clientNonceInBytes = new byte[32];
		String clientNonce = in.readLine();
		clientNonceInBytes = DatatypeConverter.parseBase64Binary(clientNonce);	    
		
		byte[] encryptedNonce = rsaECipher.doFinal(clientNonceInBytes);
		sendMsg(out, DatatypeConverter.printBase64Binary(encryptedNonce));

		
		if(!(in.readLine().equals(ACs.REQUESTSIGNEDCERT ))){
			System.out.println("Request Signed Certificate Error!");
			terminateConnection(out);
			return ACs.AUTHFAILED;
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
		
		// "Waiting for client to confirm my identity" 
		if(!ACs.SERVERIDENTIFIED.equals(in.readLine())){
			System.out.println("Client did not verify my ID properly");
			terminateConnection(out);
			return ACs.AUTHFAILED;
		}
		/////////////////////////////////////////////// CHANGE BELOW ////////////////
		
		// Server request for Client ID
		sendMsg(out,ACs.SERVERREQUESTCLIENTID);
		
		// Server takes in client's ID
		String clientID = in.readLine();
		String clientAddr = rootAddr + "//" + clientID;
		File clientDir = new File(clientAddr);
		if(clientDir.mkdir() ){
			// Folder does not exist
			sendMsg(out, ACs.NEWCLIENTCONNECTION);
			if(!AuthenticationProtocolNewClient(in,out,clientAddr)){
				return ACs.AUTHFAILED;
			}
		}
		else{
			// Folder already exists
			sendMsg(out, ACs.REPEATCLIENTCONNECTION);
			try{
				String userPublicKey = new String(Files.readAllBytes(Paths.get(clientAddr+"\\PublicKey")));
				if(!AuthenticationProtocolRepeatClient(in,out,userPublicKey)){
					return ACs.AUTHFAILED;
				}
			}catch(IOException e){
				// This is caused by another thread trying to read the same file 
				// - Client trying to log in using another browser?
				// For now, block that connection - Only allow client to access server using one browser
				return ACs.AUTHFAILED;
			}
			
		}

		 System.out.println("Completed authentication protocol, server ready to receive files");
		 sendMsg(out,ACs.SERVERREADYTORECEIVE);

		 return clientAddr;
	}
	
	private static boolean AuthenticationProtocolRepeatClient(BufferedReader in, PrintWriter out, String clientPublicKeyString) throws Exception{
		// Generate nonce to ensure that client is a valid requester, and not a playback attacker
		byte[] serverNonce = new byte[32];
		Random randGen = SecureRandom.getInstanceStrong();
		randGen.nextBytes(serverNonce);
		String serverNonceString = new String(serverNonce, "UTF-16");
	
		 // "Sending nonce to client" 
		 out.println(DatatypeConverter.printBase64Binary(serverNonce));
		 
		 byte[] encryptedServerNonce = new byte[128];
		// Receive nonce encrypted with client's private key
		 encryptedServerNonce = DatatypeConverter.parseBase64Binary(in.readLine());
		 
		 Cipher rsaDCipherClientPublic = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		 Key clientPublicKey = getPublicKey(clientPublicKeyString);
		 rsaDCipherClientPublic.init(Cipher.DECRYPT_MODE, clientPublicKey);
		
		 byte[] decryptedServerNonce = rsaDCipherClientPublic.doFinal(encryptedServerNonce);
		 String decryptedNonceString = new String(decryptedServerNonce, "UTF-16");
		 if(!decryptedNonceString.equals(serverNonceString)){
		   	  System.out.println("Client authentication failed!");
		   	  return terminateConnection(out);
		 }
		 
		 return true;
	}
	
	private static boolean AuthenticationProtocolNewClient(BufferedReader in, PrintWriter out, String clientAddr) throws Exception{
		// Generate nonce to ensure that client is a valid requester, and not a playback attacker
		byte[] serverNonce = new byte[32];
		Random randGen = SecureRandom.getInstanceStrong();
		randGen.nextBytes(serverNonce);
		String serverNonceString = new String(serverNonce, "UTF-16");
	
		 // "Sending nonce to client" 
		 out.println(DatatypeConverter.printBase64Binary(serverNonce));
		 
		 byte[] encryptedServerNonce = new byte[128];
		// Receive nonce encrypted with client's private key
		 encryptedServerNonce = DatatypeConverter.parseBase64Binary(in.readLine());
		
		 // Requesting for client public key 
		 sendMsg(out,ACs.REQUESTCLIENTPUBLICKEY);
		
		 // Receiving client public key
		 String clientPublicKeyString = in.readLine();
		 
		 // Save the client Public key to a Folder for future re-use
		 FileWriter writer = new FileWriter(clientAddr+"\\PublicKey", false);
		 writer.write(clientPublicKeyString);
		 writer.flush();
		 writer.close();
		 
		 Cipher rsaDCipherClientPublic = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		 Key clientPublicKey = getPublicKey(clientPublicKeyString);
		 rsaDCipherClientPublic.init(Cipher.DECRYPT_MODE, clientPublicKey);
		
		 byte[] decryptedServerNonce = rsaDCipherClientPublic.doFinal(encryptedServerNonce);
		 String decryptedNonceString = new String(decryptedServerNonce, "UTF-16");
		 if(!decryptedNonceString.equals(serverNonceString)){
		   	  System.out.println("Client authentication failed!");
		   	  return terminateConnection(out);
		 }
		 
		 return true;
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
		
		String clientAddr = authenticationProtocol(in,out,rsaECipherPrivate, serverCertPath);
		
		if(clientAddr.equals(ACs.AUTHFAILED)){
			System.out.println("Authentication protocol failed!");
			return;
		}
		
		System.out.println("Waiting for encrypted AES Key from client");
		String AESKeyString = in.readLine();
		//Key AESKey = getAESKey(AESKeyString,rsaDCipherPrivate);
		
		//Cipher AESCipher = Cipher.getInstance("AES");
		//AESCipher.init(Cipher.DECRYPT_MODE, AESKey);
		
		System.out.println("Waiting for encrypted file from client");
		Map<String,Long> fileUploadTimings = new ConcurrentHashMap<String, Long>();
		boolean clientDone = false;
		
		String csvFile = "CompTimings.csv";
        FileWriter writer = new FileWriter(csvFile,true);
        CSVUtils.writeLine(writer, Arrays.asList("File Size", "Time Taken (ms)"));

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
			
			File receivedFileDir = new File(clientAddr+"\\"+clientsFileName.split(".")[0]); // Remove the file extension
			if(!receivedFileDir.mkdir()){
				// Overwrite previous file and save new version
				receivedFileDir.delete();
				receivedFileDir.mkdir();
			}
			
			FileWriter filewriter = new FileWriter(clientAddr+"\\"+clientsFileName.split(".")[0]+"\\"+clientsFileName);
			filewriter.write(clientEncryptedFileString);
			filewriter.flush();
			filewriter.close();
			
			FileWriter AESWriter = new FileWriter(clientAddr+"\\"+clientsFileName.split(".")[0]+"\\"+"AES");
			AESWriter.write(AESKeyString);
			AESWriter.flush();
			AESWriter.close();
			System.out.println("Successfully saved client's file : "+clientsFileName);
			fileUploadTimings.put(clientsFileName,System.currentTimeMillis()-startTime);
			
	        clientDone = ACs.CLIENTDONE.equals(in.readLine());
	        System.out.println("Is client Done? " + clientDone);
			long endTime = System.currentTimeMillis();
			long elapsedTime = endTime-startTime;
//			CSVUtils.writeLine(writer, Arrays.asList(Integer.toString(clientFileSize), Long.toString(elapsedTime)));
		}while(!clientDone);
		
		writer.close();
		
		System.out.println(fileUploadTimings.toString());
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
