package sample;

import Server.BackgroundFireBase;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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

public class LoginController implements Initializable {
    @FXML
    private JFXPasswordField password;

    @FXML
    private JFXButton login;

    @FXML
    private JFXButton signup;

    @FXML
    private JFXTextField username;




//    @FXML
//    void authenticate(ActionEvent event) throws IOException {
//        String username = this.username.getText();
//        String password = this.password.getText();
//        Parent home_page_parent = FXMLLoader.load(getClass().getResource("home.fxml"));
//        Scene home_page_scene = new Scene(home_page_parent);
//        Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
//        if (username.equals("user") && password.equals("user")){
//            System.out.println("succeeded");
//            //app_stage.hide();
//            app_stage.setScene(home_page_scene);
//            app_stage.show();
//
//
//        } else {
//            System.out.println("failed");
//            this.username.clear();
//            this.password.clear();
//
//        }
//    }

    @FXML
    void authenticate(ActionEvent event) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        String username = this.username.getText();
        String password = this.password.getText();



        //AES the password
        // Generate the secret key specs.
//        byte[] user = username.getBytes();
//        byte[] key = password.getBytes();
//        MessageDigest sha = MessageDigest.getInstance("SHA-1");
//        key = sha.digest(key);
//        key = Arrays.copyOf(key, 16);
//
//        user = sha.digest(user);
//        user = Arrays.copyOf(user, 16);
//
//
//        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
//        // Instantiate the cipher
//        Cipher cipher = Cipher.getInstance("AES");
//        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
//
//        // Encrypt the username using the hashed password AES key
//        byte[] encryptedBytes = cipher.doFinal(user);
//        String base64format = DatatypeConverter.printBase64Binary(encryptedBytes);
//        System.out.println(base64format);

//        // Fetch from firebase and compare the string
//        BackgroundFireBase firebaseSingleton = BackgroundFireBase.getInstance();
//        String firebaseData = firebaseSingleton.Authenticate(username);
//        System.out.println(firebaseData);

        Parent home_page_parent = FXMLLoader.load(getClass().getResource("home.fxml"));
        Scene home_page_scene = new Scene(home_page_parent);
        Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

//        if (firebaseData.equals(base64format)){
        if (username.equals("user")){
            System.out.println("succeeded");
            //app_stage.hide();
            app_stage.setScene(home_page_scene);
            app_stage.show();
        }
        else {
            System.out.println("failed");
            this.username.clear();
            this.password.clear();
        }
    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

}
