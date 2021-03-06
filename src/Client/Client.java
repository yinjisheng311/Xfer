package Client;

/**
 * Created by nicholas on 21-May-17.
 */

import AuthenticationConstants.ACs;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;

public class Client implements Runnable {

    public final String user;
    public final ArrayList<File> fileArrayList;
    public final String hostName;
    private int port;

    public Client(String user, ArrayList<File> fileArrayList, String hostName, int port){
        this.user = user;
        this.fileArrayList = fileArrayList;
        this.hostName = hostName;
        this.port = port;
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

    private void main() throws Exception {
        System.out.println("CP2: trying to connect");
//        String hostName = "10.12.21.29";
//        String hostName = "localhost";
//        int portNumber = 6666;
        int portNumber = port;
//		String hostName = args[0];
//		int portNumber = Integer.parseInt(args[1]);
        Socket echoSocket = new Socket();
        System.out.println(hostName);
        System.out.println(hostName.equals("10.12.145.110"));
        SocketAddress sockaddr = new InetSocketAddress(hostName, portNumber);
        echoSocket.connect(sockaddr, 18080);
        System.out.println("connected");
        PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

        // Tell Server my Identity
        out.println(user);
        out.flush();

        // Wait for server to accept my send request, terminate if rejected
        String serverAccept = in.readLine();
        // TODO: handle server response
        // Send response to user if the server denys connection and stop client
        if(serverAccept.equals("")){
            System.out.println("Server rejected connection!");
            return;
        }

        //send nonce as the message for the server to encrypt, to make sure no playback attack can take place
        byte[] nonce = new byte[32];
        Random rand;
        rand = SecureRandom.getInstance ("SHA1PRNG");
        rand.nextBytes(nonce);
        String nonceString = new String(nonce, "UTF-16");
        // Send over nonce
        System.out.println("sending over nonce");
        out.println(DatatypeConverter.printBase64Binary(nonce));
        out.flush();

        //receive encrypted nonce from server
        String serverInitialReply = in.readLine();
        System.out.println("gave me secret message: " + serverInitialReply);

        //send request for cert and receive signed cert
        out.println(ACs.REQUESTSIGNEDCERT);
        out.flush();
        String sizeInString = in.readLine();

        int certificateSize = Integer.parseInt(sizeInString);
        byte[] signedCertificate = new byte[certificateSize];
        String signedCertificateInString = in.readLine();
        signedCertificate = DatatypeConverter.parseBase64Binary(signedCertificateInString);
        System.out.println("gave me signed certificate");

        //extract public key from signed certificate
        //creating X509 certificate object
        FileOutputStream fileOutput = new FileOutputStream("CA.crt");
        fileOutput.write(signedCertificate, 0, signedCertificate.length);
        FileInputStream ServercertFileInput = new FileInputStream("CA.crt");

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate CAcert = (X509Certificate) cf.generateCertificate(ServercertFileInput);

        //extract public key from the certificate
        PublicKey CAkey = CAcert.getPublicKey();
        CAcert.checkValidity();
        System.out.println("public key of CA extracted");


        //use public key to decrypt signed certificate to extract public key of server
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, CAkey);
        byte[] decryptedBytes = cipher.doFinal(DatatypeConverter.parseBase64Binary(serverInitialReply));
        String decryptedMessage = new String (decryptedBytes, "UTF-16");
        System.out.println("decryptedMessage: " + decryptedMessage);

        //if serverInitialReply is correct, then proceed to give my encrypted client ID
        if (!decryptedMessage.equals(nonceString)){
            out.println(ACs.TERMINATEMSG);
            out.flush();
            out.close();
            in.close();
            echoSocket.close();
            System.out.println("authentication failed");
            return;
        }
        out.println(ACs.SERVERIDENTIFIED);
        out.flush();
        System.out.println("successfully authenticated the server");

        // Read in Client's Certificate in preparation for Server Nonce
        final String privateKeyFileName = "src/Client/privateServerNic.der";
        final String clientCertPath = "src/Client/1001490.crt";
        final Path keyPath = Paths.get(privateKeyFileName);
        final byte[] privateKeyByteArray = Files.readAllBytes(keyPath);
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyByteArray);

        final KeyFactory AppkeyFactory = KeyFactory.getInstance("RSA");
        final PrivateKey AppprivateKey = AppkeyFactory.generatePrivate(keySpec);

        // Create encryption cipher
        final Cipher ApprsaECipherPrivate = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        final Cipher ApprsaDCipherPrivate = Cipher.getInstance("RSA/ECB/PKCS1Padding" );

        ApprsaECipherPrivate.init(Cipher.ENCRYPT_MODE, AppprivateKey);
        ApprsaDCipherPrivate.init(Cipher.DECRYPT_MODE, AppprivateKey);
        File certFile = new File(clientCertPath);
        byte[] certBytes = new byte[(int) certFile.length()];
        BufferedInputStream ClientCertFileInput = new BufferedInputStream(new FileInputStream(certFile));
        ClientCertFileInput.read(certBytes,0,certBytes.length);
        ClientCertFileInput.close();


//        //generate keypair here
//        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
//        keyGen.initialize(1024);
//        KeyPair keyPair = keyGen.generateKeyPair();
//        Key publicKey = keyPair.getPublic();
//        Key privateKey = keyPair.getPrivate();

        //receive nonce from server
        byte[] serverNonceInBytes = new byte[32];
        String serverNonce = in.readLine();
        serverNonceInBytes = DatatypeConverter.parseBase64Binary(serverNonce);
        System.out.println("received nonce from server: " + serverNonce);

        //encrypt nonce using client App private key and send it back to server
//        Cipher Ecipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//        Ecipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] encryptedServerNonce = ApprsaECipherPrivate.doFinal(serverNonceInBytes);
        out.println(DatatypeConverter.printBase64Binary(encryptedServerNonce));
        out.flush();
        System.out.println("sent encrypted nonce to server");


        //wait for server to ask for App public key, send App public key to server
        String requestForPublic = in.readLine();
        if (!requestForPublic.equals(ACs.REQUESTCLIENTPUBLICKEY)){
            out.println("you didn't ask for the public key");
            out.flush();
            out.close();
            in.close();
            echoSocket.close();
            System.out.println("failed to request public key");
            return;
        }


//        String encodedKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        // Tell Server the size of client app cert
        out.println(Integer.toString(certBytes.length));
        out.flush();
        // Send the actual cert as a string
        String encodedKey = DatatypeConverter.printBase64Binary(certBytes);
        out.println(encodedKey);
        out.flush();
        System.out.println("sent public key to server");

        //receive success message and initialise handshake
        String successMessage = in.readLine();
        if (!successMessage.equals(ACs.SERVERREADYTORECEIVE)){
            out.println("you didn't tell me you're ready to receive my files");
            out.flush();
            out.close();
            in.close();
            echoSocket.close();
            return;
        }

        System.out.println("initialising handshake");

        // Read in Server public key
        String serverPublicKeyString = in.readLine();
        Cipher rsaECipherServerPublic = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        Key serverPublicKey = getPublicKey(serverPublicKeyString);
        rsaECipherServerPublic.init(Cipher.ENCRYPT_MODE, serverPublicKey);

        //generate secret key using AES algorithm, encrypt it with server's public key, send it to server
        SecretKey key = KeyGenerator.getInstance("AES").generateKey();
//        Cipher aesCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//        aesCipher.init(Cipher.ENCRYPT_MODE, CAkey);
        byte[] encryptedKey = rsaECipherServerPublic.doFinal(key.getEncoded());
        out.println(DatatypeConverter.printBase64Binary(encryptedKey));
        out.flush();
        System.out.println("finished sending secret symmetric key");

        //use server's public key to encrypt the clients files and send it back to server
//		for (int i = 2; i < args.length; i++){
//			//tell server this is the starting time
//			File fileToBeSent = new File(args[i]);
//			byte[] fileBytes = new byte[(int)fileToBeSent.length()];
//			BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(fileToBeSent));
//			fileInput.read(fileBytes,0,fileBytes.length);
//			fileInput.close();
//
//			//encrypt this file
//			Cipher Ecipher2 = Cipher.getInstance("AES");
//			Ecipher2.init(Cipher.ENCRYPT_MODE, key);
//			byte[] encryptedFile = encryptFile(fileBytes, Ecipher2);
//
//			out.println(args[i]);
//			out.println(Integer.toString(encryptedFile.length));
//			out.println(DatatypeConverter.printBase64Binary(encryptedFile));
//			System.out.println("successfully sent over " + args[i]);
//			if((i+1)<args.length) {
//				out.println(ACs.CLIENTONEFILESENT);
//			}else{
//				out.println(ACs.CLIENTDONE);
//			}
//		}

//        String[] fileList = {}; //{"largeFile.txt","medianFile.txt","smallFile.txt"};

        //Inform server about number of files sent
        out.println(fileArrayList.size());
        for(int i = 0; i <fileArrayList.size();i++){
            //tell server this is the starting time
            File fileToBeSent = fileArrayList.get(i);
            byte[] fileBytes = new byte[(int)fileToBeSent.length()];
            BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(fileToBeSent));
            fileInput.read(fileBytes,0,fileBytes.length);
            fileInput.close();

            //encrypt this file
            Cipher Ecipher2 = Cipher.getInstance("AES");
            Ecipher2.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedFile = encryptFile(fileBytes, Ecipher2);

            out.println(fileArrayList.get(i));
            out.println(Integer.toString(encryptedFile.length));
            out.println(DatatypeConverter.printBase64Binary(encryptedFile));
            System.out.println("successfully sent over " + fileArrayList.get(i));
            if((i+1)<fileArrayList.size()) {
                out.println(ACs.CLIENTONEFILESENT);
            }else{
                break;
            }
        }
        out.println(ACs.CLIENTDONE);

        System.out.println("told server all ecnrypted files are sent");

    }
    public static byte[] encryptFile(byte[] fileBytes, Cipher rsaECipher) throws Exception{

        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

        int start = 0;
        int fileLength = fileBytes.length;
        while (start < fileLength) {
            byte[] tempBuff;
            if (fileLength - start >= 117) {
                tempBuff = rsaECipher.doFinal(fileBytes, start, 117);
                //System.out.println(Arrays.toString(tempBuff));
            } else {
                tempBuff = rsaECipher.doFinal(fileBytes, start, fileLength - start);
            }
            byteOutput.write(tempBuff, 0, tempBuff.length);
            start += 117;
        }
        byte[] encryptedFileBytes = byteOutput.toByteArray();
        byteOutput.close();
        return encryptedFileBytes;

    }

    @Override
    public void run() {
        if(hostName==null){
            System.out.println("INVALID HOSTNAME");
            return;
        }
        try {
            main();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

