package sample;

import Client.CP2Client;
import Server.ServerClassCP2MultiThread;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SendFileController implements Initializable {

    ArrayList<File> fileList;

    @FXML
    private ImageView imageView;

    @FXML
    private JFXButton homeButton;

    @FXML
    private ListView<Label> fileListView;

    @FXML
    private Label receiver;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // instantiate fileList
        this.receiver.setText(HomeController.enteredUser);
        this.fileList = new ArrayList<>();

        homeButton.setOnAction(e -> {
            try {
                goHome();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

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
        System.out.println("something dropped");

        //TODO: load gif for one cycle
        imageView = new ImageView(new Image(getClass().getResourceAsStream("upload.gif")));



        String fileName;
        List<File> files = event.getDragboard().getFiles();
        for (int i = 0; i < files.size(); i++) {
            this.fileList.add(files.get(i));
            fileName = files.get(i).getName();
            Label lbl = new Label("File Name: " + fileName);
            System.out.println(fileName);
            this.fileListView.getItems().add(lbl);

        }
        System.out.println(files);

//        this.fileList.add(files.get(0));
//        String fileName = files.get(0).getName();

//        ObservableList<Label> fileList;
//        fileList = this.fileListView.getSelectionModel().getSelectedItems();
//        for (Label l: fileList){
//            System.out.println(l);
//        }

        // the code below can change the image
//        this.imageView.setImage(img);
    }



    private void goHome() throws IOException {
        Parent send_file_page_parent = FXMLLoader.load(getClass().getResource("home.fxml"));
        Scene send_file_page_scene = new Scene(send_file_page_parent);
        Stage app_stage = (Stage) this.homeButton.getScene().getWindow();
        app_stage.setScene(send_file_page_scene);
        app_stage.show();
    }
    @FXML
    void sendFiles(ActionEvent event) {
        System.out.println(this.fileList.toString());
        // get files from fileList and send them out
//        Runnable client = new CP2Client(this.fileList);
        Runnable client = new CP2Client(this.fileList);
        new Thread(client).start();

    }

    // call the server code


}


