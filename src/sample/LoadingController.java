package sample;

import Server.BackgroundFireBase;
import com.google.firebase.internal.Log;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXListView;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.Phaser;


public class LoadingController extends Application implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        String username = LoginController.userN;
        String password = LoginController.passW;
        final boolean[] authenticated = {false};

//
//        //AES the password
//        // Generate the secret key specs.
//        byte[] user = username.getBytes();
//        byte[] key = password.getBytes();
//        MessageDigest sha = null;
//        try {
//            sha = MessageDigest.getInstance("SHA-1");
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        key = sha.digest(key);
//        key = Arrays.copyOf(key, 16);
//
//        user = sha.digest(user);
//        user = Arrays.copyOf(user, 16);
//
//
//        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
//        // Instantiate the cipher
//        Cipher cipher = null;
//        try {
//            cipher = Cipher.getInstance("AES");
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (NoSuchPaddingException e) {
//            e.printStackTrace();
//        }
//        try {
//            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//        }
//
//        // Encrypt the username using the hashed password AES key
//        byte[] encryptedBytes = new byte[0];
//        try {
//            encryptedBytes = cipher.doFinal(user);
//        } catch (IllegalBlockSizeException e) {
//            e.printStackTrace();
//        } catch (BadPaddingException e) {
//            e.printStackTrace();
//        }
//        String base64format = DatatypeConverter.printBase64Binary(encryptedBytes);
//        System.out.println(base64format);
//
////        Phaser phaser = new Phaser();
////        phaser.bulkRegister(2); // Register this thread and firebase thread
//
//        Runnable firebaseAuthentication = new Runnable() {
//            @Override
//            public void run() {
//                // Fetch from firebase and compare the string
//                BackgroundFireBase firebaseSingleton = BackgroundFireBase.getInstance();
//                String firebaseData = firebaseSingleton.Authenticate(username);
//
//                if (firebaseData.equals(base64format)){
//                    System.out.println("succeeded");
//                    authenticated[0] = true;
////                    phaser.arrive();
////                    notifyAll();
////                    throw new RuntimeException("Thread interrupted...");
//                }
//                else {
//                    System.out.println("failed");
////                    phaser.arrive();
////                    notifyAll();
////                    throw new RuntimeException("Thread interrupted...");
//
//                }
//            }
//        };
//
//        Thread firebaseAuthenticationThread = new Thread(firebaseAuthentication);
//        firebaseAuthenticationThread.start();
//        try {
//            firebaseAuthenticationThread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        phaser.arriveAndAwaitAdvance();
        System.out.println("Finished authentication");

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("hello");

    }
}
