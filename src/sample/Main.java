package sample;

import Server.BackgroundFireBase;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Main extends Application {

    Stage window;
    BackgroundFireBase firebaseSingleton;

    @FXML
    private JFXTextField username;

    @FXML
    private JFXPasswordField password;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        firebaseSingleton = BackgroundFireBase.getInstance();
        window = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        window.setTitle("Xfer Without Trudy");
        window.setScene(new Scene(root, 600,400));
        window.show();

        System.out.println("hello");
        String username = LoginController.userN;
        String password = LoginController.passW;
        final boolean[] authenticated = {false};

        username = "";
        password = "";
        //AES the password
        // Generate the secret key specs.
        byte[] user = username.getBytes();
        byte[] key = password.getBytes();
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);

        user = sha.digest(user);
        user = Arrays.copyOf(user, 16);


        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        // Instantiate the cipher
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        // Encrypt the username using the hashed password AES key
        byte[] encryptedBytes = new byte[0];
        try {
            encryptedBytes = cipher.doFinal(user);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        String base64format = DatatypeConverter.printBase64Binary(encryptedBytes);
        System.out.println(base64format);

//        Phaser phaser = new Phaser();
//        phaser.bulkRegister(2); // Register this thread and firebase thread

        String finalUsername = username;
        Runnable firebaseAuthentication = new Runnable() {
            @Override
            public void run() {
                // Fetch from firebase and compare the string
                BackgroundFireBase firebaseSingleton = BackgroundFireBase.getInstance();
                String firebaseData = firebaseSingleton.Authenticate(finalUsername);

                if (firebaseData.equals(base64format)){
                    System.out.println("succeeded");
                    authenticated[0] = true;
//                    phaser.arrive();
//                    notifyAll();
//                    throw new RuntimeException("Thread interrupted...");
                }
                else {
                    System.out.println("failed");
//                    phaser.arrive();
//                    notifyAll();
//                    throw new RuntimeException("Thread interrupted...");

                }
            }
        };

        Thread firebaseAuthenticationThread = new Thread(firebaseAuthentication);
        firebaseAuthenticationThread.start();
        try {
            firebaseAuthenticationThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        phaser.arriveAndAwaitAdvance();
        System.out.println("Finished authentication");

    }

    public void authenticate(){
        System.out.println("hello");
        String username = this.username.getText();
        String password = this.password.getText();
        final boolean[] authenticated = {false};

        //AES the password
        // Generate the secret key specs.
        byte[] user = username.getBytes();
        byte[] key = password.getBytes();
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);

        user = sha.digest(user);
        user = Arrays.copyOf(user, 16);


        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        // Instantiate the cipher
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        // Encrypt the username using the hashed password AES key
        byte[] encryptedBytes = new byte[0];
        try {
            encryptedBytes = cipher.doFinal(user);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        String base64format = DatatypeConverter.printBase64Binary(encryptedBytes);
        System.out.println(base64format);

//        Phaser phaser = new Phaser();
//        phaser.bulkRegister(2); // Register this thread and firebase thread

        String finalUsername = username;
        Runnable firebaseAuthentication = new Runnable() {
            @Override
            public void run() {
                // Fetch from firebase and compare the string
                BackgroundFireBase firebaseSingleton = BackgroundFireBase.getInstance();
                String firebaseData = firebaseSingleton.Authenticate(finalUsername);

                if (firebaseData.equals(base64format)){
                    System.out.println("succeeded");
                    authenticated[0] = true;
                    Parent root = null;
                    try {
                        root = FXMLLoader.load(getClass().getResource("home.fxml"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    window.setTitle("Xfer Without Trudy");
                    window.setScene(new Scene(root, 600,400));
                    window.show();
//                    phaser.arrive();
//                    notifyAll();
//                    throw new RuntimeException("Thread interrupted...");
                }
                else {
                    System.out.println("failed");
//                    phaser.arrive();
//                    notifyAll();
//                    throw new RuntimeException("Thread interrupted...");

                }
            }
        };

        Thread firebaseAuthenticationThread = new Thread(firebaseAuthentication);
        firebaseAuthenticationThread.start();
        try {
            firebaseAuthenticationThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        phaser.arriveAndAwaitAdvance();
        System.out.println("Finished authentication");
        populateList();


    }

    public void populateList(){
        BackgroundFireBase firebaseSingleton = BackgroundFireBase.getInstance();
//        String user1 = firebaseSingleton.onlineUsers.get(0);
//        System.out.println(user1);
        System.out.println(firebaseSingleton.onlineUsers.toString());
        for(int i=0 ; i < firebaseSingleton.onlineUsers.size(); i ++){
            System.out.println(firebaseSingleton.onlineUsers.get(i));
        }
    }


}
