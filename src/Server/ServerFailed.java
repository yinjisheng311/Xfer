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
import java.security.*;
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
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import AuthenticationConstants.ACs;	// Authentication Constants
import CSV.CSVUtils;

public class ServerFailed implements Runnable {
    private final String user;
    private final String password;

    ServerFailed(String user, String password){
        this.user = user;
        this.password = password;
    }

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

    private static Key getAESKey(byte[] AESKeyByte, Cipher rsaDCipher) throws IllegalBlockSizeException, BadPaddingException {
//        byte[] byteKey = DatatypeConverter.parseBase64Binary(AESKeyString);
        byte[] decryptedByteKey = rsaDCipher.doFinal(AESKeyByte);
        SecretKey sessionKey = new SecretKeySpec(decryptedByteKey, 0, decryptedByteKey.length, "AES");
        return sessionKey;
    }

    private static boolean terminateConnection(PrintWriter out){
        out.println(ACs.TERMINATEMSG);
        return false;
    }

    private static boolean authenticationProtocol(BufferedReader in, PrintWriter out, Cipher rsaECipher, Cipher rsaDCipher, String serverCertPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{

        System.out.println("Starting authentication protocol");

        byte[] clientNonceInBytes = new byte[32];
        String clientNonce = in.readLine();
        clientNonceInBytes = DatatypeConverter.parseBase64Binary(clientNonce);

        byte[] encryptedNonce = rsaECipher.doFinal(clientNonceInBytes);
        sendMsg(out, DatatypeConverter.printBase64Binary(encryptedNonce));

//        if(!(in.readLine().equals(ACs.REQUESTSIGNEDCERT ))){
//            System.out.println("Request Signed Certificate Error!");
//            return terminateConnection(out);
//        }
//
//        File certFile = new File(serverCertPath);
//        byte[] certBytes = new byte[(int) certFile.length()];
//        BufferedInputStream certFileInput = new BufferedInputStream(new FileInputStream(certFile));
//        certFileInput.read(certBytes,0,certBytes.length);
//        certFileInput.close();
//
//        // Prepping client to receive certificate in bytes
//        sendMsg(out, Integer.toString(certBytes.length) );
//        // Sending signed cert of server - includes public key of client
//        sendMsg(out, DatatypeConverter.printBase64Binary(certBytes));

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

//        // "Requesting for client public key"
//        sendMsg(out,ACs.REQUESTCLIENTPUBLICKEY);
//
//        // Receiving client's public key
//        String clientPublicKeyString = in.readLine();
//
//        Cipher rsaDCipherClientPublic = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//
//        Key clientPublicKey = getPublicKey(clientPublicKeyString);
//        rsaDCipherClientPublic.init(Cipher.DECRYPT_MODE, clientPublicKey);

        byte[] decryptedServerNonce = rsaDCipher .doFinal(encryptedServerNonce);
        String decryptedNonceString = new String(decryptedServerNonce, "UTF-16");
        if(!decryptedNonceString.equals(serverNonceString)){
            System.out.println("Client authentication failed!");
            return terminateConnection(out);
        }

        System.out.println("Completed authentication protocol, server ready to receive files");
//        sendMsg(out,ACs.SERVERREADYTORECEIVE);

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

    protected static void handleRequest(Socket clientSocket, String user, String password) throws Exception{
        BackgroundFireBase firebase = BackgroundFireBase.getInstance(); // Prolly need to tweak for performance
        String[] PubPriv = firebase.QueryPubPriv(user);
        final String privateKeyFileName = "src\\Server\\privateServerNic.der";
        final String serverCertPath = "src\\Server\\1001490.crt";
        final Path keyPath = Paths.get(privateKeyFileName);
        final byte[] privateKeyByteArray = Files.readAllBytes(keyPath);
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyByteArray);

        final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        final PrivateKey privateAppKey = keyFactory.generatePrivate(keySpec);

        // Create encryption cipher
        final Cipher rsaAppECipherPrivate = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        final Cipher rsaAppDCipherPrivate = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        rsaAppECipherPrivate.init(Cipher.ENCRYPT_MODE, privateAppKey);
        rsaAppDCipherPrivate.init(Cipher.DECRYPT_MODE, privateAppKey);



        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new DataInputStream(clientSocket.getInputStream())));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        /*
        Right now authentication is only to authenticate that the other party is using a
        Legitimate Application that has been produced by us.
         */
        boolean proceed = authenticationProtocol(in,out,rsaAppECipherPrivate, rsaAppDCipherPrivate, serverCertPath);

        if(!proceed){
            System.out.println("Authentication protocol failed!");
            return;
        }

        byte[] key = password.getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher Dcipher = Cipher.getInstance("AES");
        Dcipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] EpublicKeyBytes = DatatypeConverter.parseBase64Binary(PubPriv[0]);
        byte[] EprivateKeyBytes = DatatypeConverter.parseBase64Binary(PubPriv[1]);
        byte[] DpublicKeyBytes = Dcipher.doFinal(EpublicKeyBytes);
        byte[] DprivateKeyBytes = Dcipher.doFinal(EprivateKeyBytes);
        final PrivateKey privateKey = keyFactory.generatePrivate(new SecretKeySpec(DprivateKeyBytes,0,DprivateKeyBytes.length,"AES"));

        sendMsg(out, DatatypeConverter.printBase64Binary(DpublicKeyBytes) );

        // Create encryption cipher
        final Cipher rsaDCipherPrivate = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaDCipherPrivate.init(Cipher.DECRYPT_MODE, privateKey);


        // "Waiting for encrypted AES Key from client"
        String AESKeyString = in.readLine();
        byte[] AESKeyByte = Dcipher.doFinal(DatatypeConverter.parseBase64Binary(AESKeyString));
        Key AESKey = getAESKey(AESKeyByte,rsaDCipherPrivate);

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
            // "Received client's file name");
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
    private void main() throws Exception{

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
                        handleRequest(clientSocket, user, password);
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


