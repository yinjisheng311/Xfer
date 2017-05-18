package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SendFileController implements Initializable {

    List<File> fileList;

    @FXML
    private ImageView imageView;

    @FXML
    private JFXButton homeButton;

    @FXML
    private JFXListView<Label> fileListView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // instantiate fileList
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

        List<File> files = event.getDragboard().getFiles();
        this.fileList.add(files.get(0));
        String fileName = files.get(0).getName();
        System.out.println(files);
//        ObservableList<Label> fileList;
//        fileList = this.fileListView.getSelectionModel().getSelectedItems();
//        for (Label l: fileList){
//            System.out.println(l);
//        }
        Label lbl = new Label("File Name: " + fileName);
        this.fileListView.getItems().add(lbl);
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
        

    }


}
