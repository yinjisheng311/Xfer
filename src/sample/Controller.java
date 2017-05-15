package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private JFXPasswordField password;

    @FXML
    private JFXButton login;

    @FXML
    private JFXButton signup;

    @FXML
    private JFXTextField username;


    @FXML
    void authenticate(ActionEvent event) throws IOException {
        String username = this.username.getText();
        String password = this.password.getText();
        Parent home_page_parent = FXMLLoader.load(getClass().getResource("home.fxml"));
        Scene home_page_scene = new Scene(home_page_parent);
        Stage app_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (username.equals("user") && password.equals("user")){
            System.out.println("succeeded");
            //app_stage.hide();
            app_stage.setScene(home_page_scene);
            app_stage.show();


        } else {
            System.out.println("failed");
            this.username.clear();
            this.password.clear();

        }
    }

    public void loginButtonClicked(){
        System.out.println("whatever");
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
    @FXML
    private void handleDragOver(DragEvent event){
        //got the plus sign when you hover over it
        //only if it is a file, not html etc
        if(event.getDragboard().hasFiles()){
            event.acceptTransferModes(TransferMode.ANY);
        }

    }
    @FXML
    private void handleDrop(DragEvent event) throws FileNotFoundException {
        List<File> files = event.getDragboard().getFiles();
        Image img = new Image(new FileInputStream(files.get(0)));

    }
}
