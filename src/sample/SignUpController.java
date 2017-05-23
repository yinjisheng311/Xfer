package sample;

import Server.BackgroundFireBase;
import com.jfoenix.controls.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.ResourceBundle;

public class SignUpController implements Initializable {
    @FXML
    private JFXPasswordField password;

    @FXML
    private JFXButton login;

    @FXML
    private JFXButton signup;

    @FXML
    private JFXTextField username;

    @FXML
    private StackPane stackPane;


    @FXML
    void createUser(ActionEvent event) throws UnknownHostException {
        System.out.println("CREATE USER!!!!!");
        if(!BackgroundFireBase.getInstance().userList.contains(username)){
            //AES the password
            // Generate the secret key specs.
            byte[] user = this.username.getText().getBytes();
            byte[] key = this.password.getText().getBytes();
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
            BackgroundFireBase.getInstance().createUser(username.getText(),base64format);

            JFXDialogLayout content = new JFXDialogLayout();
            content.setHeading(new Text("User created"));
            content.setBody(new Text("Your account: "+ this.username.getText() + " has been successfully created"));
            JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);
            JFXButton button = new JFXButton("Okay");
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    dialog.close();
                    try {
                        goBackLogin();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
            content.setActions(button);
            dialog.show();
            this.username.clear();
            this.password.clear();

        }
    }

    public void goBackLogin() throws IOException {
        Parent login_page_parent = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene login_page_scene = new Scene(login_page_parent);
        Stage app_stage = (Stage) this.username.getScene().getWindow();
        app_stage.setScene(login_page_scene);
        app_stage.show();

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

}
