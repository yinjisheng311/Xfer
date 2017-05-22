package Server;

/**
 * Created by nicholas on 21-May-17.
 */

import AuthenticationConstants.ACs;
import CSV.CSVUtils;
import sample.HomeController;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import javax.xml.crypto.Data;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
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

public class Server implements Runnable {

    private static boolean sendMsg(PrintWriter out, String msg){
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

    private static boolean authenticationProtocol(BufferedReader in, PrintWriter out, Cipher rsaECipher, String serverCertPath) throws Exception{

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

        // Receiving client's Certificate
        String sizeInString = in.readLine();
        int certificateSize = Integer.parseInt(sizeInString);
        byte[] clientCert = new byte[certificateSize];
        String clientCertString = in.readLine();
        clientCert = DatatypeConverter.parseBase64Binary(clientCertString);

        // Extrat public key from client cert
        FileOutputStream fileOutput = new FileOutputStream("ClientAppCert.crt");
        fileOutput.write(clientCert,0,clientCert.length);
        FileInputStream clientAppCertFileInput = new FileInputStream("ClientAppCert.crt");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate clientAppCert = (X509Certificate) cf.generateCertificate(clientAppCertFileInput);
        PublicKey clientAppPublicKey = clientAppCert.getPublicKey();
        clientAppCert.checkValidity();

        Cipher rsaDCipherClientPublic = Cipher.getInstance("RSA/ECB/PKCS1Padding");

//        Key clientPublicKey = getPublicKey(clientPublicKeyString);
        rsaDCipherClientPublic.init(Cipher.DECRYPT_MODE, clientAppPublicKey);

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
//        final Cipher rsaDCipherPrivate = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        rsaECipherPrivate.init(Cipher.ENCRYPT_MODE, privateKey);
//        rsaDCipherPrivate.init(Cipher.DECRYPT_MODE, privateKey);



        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new DataInputStream(clientSocket.getInputStream())));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        // Read in client's ID
        String clientID = in.readLine();
        // TODO: Prompt the user if they want to accept the files
        boolean accept = HomeController.sendRequestPopup();
        if(!accept){
            sendMsg(out,"");
            System.out.println("User rejected transfer!");
            return;
        }

        sendMsg(out,"accept");

        boolean proceed = authenticationProtocol(in,out,rsaECipherPrivate, serverCertPath);

        if(!proceed){
            System.out.println("Authentication protocol failed!");
            return;
        }

        // Generate keypair here
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair keyPair = keyGen.generateKeyPair();
        Key serverPublicKey = keyPair.getPublic();
        Key serverPrivateKey = keyPair.getPrivate();

        // Send client my public key
        sendMsg(out,Base64.getEncoder().encodeToString(serverPublicKey.getEncoded()));

        // "Waiting for encrypted AES Key from client"
        String AESKeyString = in.readLine();
        final Cipher serverPublicDCipher = Cipher.getInstance("RSA/ECB/PKCS1PAdding");
        serverPublicDCipher.init(Cipher.DECRYPT_MODE, serverPrivateKey);

        Key AESKey = getAESKey(AESKeyString,serverPublicDCipher);

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
        Integer numFilesExpected = Integer.parseInt(in.readLine());
        do{
            if(numFilesExpected==0){
                break;
            }
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
        System.out.println(clientsFileName);
        clientsFileName = clientsFileName.replace("\\",",");
        System.out.println(clientsFileName);
        String [] temp = clientsFileName.split(",");
        File directory = new File("ReceivedFile");
        if(!directory.exists()){
            directory.mkdir();
        }
        FileOutputStream fileOutput = new FileOutputStream("ReceivedFile\\"+temp[temp.length-1]);
        fileOutput.write(clientDecryptedFileBytes, 0, clientDecryptedFileBytes.length);
        fileOutput.close();
        System.out.println("Successfully saved client's file : "+clientsFileName);
        phaser.arriveAndDeregister();
        fileUploadTimings.put(clientsFileName,System.currentTimeMillis()-startTime);
    }

    private void main() throws Exception{

        int portNum = 6667;	// socket address
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


    @Override
    public void run() {
        try {
            main();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



