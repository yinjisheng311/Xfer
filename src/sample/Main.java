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
    public static BackgroundFireBase firebaseSingleton;

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

        // to make firebase load first
        kickstartFirebase();
        System.out.println("At main start() firebase ref : " + firebaseSingleton.numReferences);


    }

    public void kickstartFirebase(){
        System.out.println("hello");
        String username = "";
        String password = "";
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


        String finalUsername = username;
        Runnable firebaseAuthentication = new Runnable() {
            @Override
            public void run() {
                // Fetch from firebase and compare the string
//                BackgroundFireBase firebaseSingleton = BackgroundFireBase.getInstance();
                String firebaseData = firebaseSingleton.Authenticate(finalUsername);
                System.out.println("While kick starting firebase, firebase ref : "+firebaseSingleton.numReferences);

                if (firebaseData.equals(base64format)){
                    System.out.println("succeeded");
                    authenticated[0] = true;
                }
                else {
                    System.out.println("failed");
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
        System.out.println("Firebase kickstarted");

    }



}
