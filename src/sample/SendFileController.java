package sample;

import Client.CP2Client;
import Client.Client;
import Server.ServerClassCP2MultiThread;
import Server.UserInfo;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendFileController implements Initializable {

    ArrayList<File> fileList;
    String user;

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
        this.user = UserInfo.getInstance().getUser();

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
        Image uploadGif = new Image(getClass().getResourceAsStream("upload.gif"));
        this.imageView.setImage(uploadGif);
        Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Thread.sleep(3500);
                } catch (InterruptedException e) {
                }
                return null;
            }
        };
        sleeper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                Image uploadImg = new Image(getClass().getResourceAsStream("upload.png"));
                imageView.setImage(uploadImg);
            }
        });
        new Thread(sleeper).start();


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
//        Runnable client = new CP2Client(this.fileList);
        String rawData = HomeController.enteredUser;
        Pattern IPPattern = Pattern.compile("[\\d]{1,3}.[\\d]{1,3}.[\\d]{1,3}.[\\d]{1,3}$");
        Matcher m = IPPattern.matcher(rawData);
        String IPAddress = null;
        while (m.find()){
            IPAddress = m.group(0);
        }
        Runnable client = new Client(user,fileList,IPAddress);
        new Thread(client).start();

    }

    // call the server code


}


