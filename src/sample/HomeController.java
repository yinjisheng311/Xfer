package sample;

import Server.BackgroundFireBase;
import Server.ServerClassCP2MultiThread;
import com.jfoenix.controls.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.RunnableFuture;


public class HomeController implements Initializable {

    @FXML
    private StackPane stackPane;

    @FXML
    private JFXListView<Label> listView;

    public static String enteredUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        enteredUser = "";
        BackgroundFireBase firebaseSingleton = BackgroundFireBase.getInstance();
        System.out.println(firebaseSingleton.onlineUsers.toString());
        System.out.println("While initialising home controller, firebase ref : " + firebaseSingleton.numReferences);

        // creating a deep copy of the firebase list
        Map<String, String> firebaseCopy = new HashMap<>(firebaseSingleton.onlineUsers);

        Iterator it = firebaseCopy.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            try {
                Label lbl = new Label(pair.getKey().toString() + ": " + pair.getValue().toString());
                this.listView.getItems().add(lbl);
                it.remove();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        this.listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Label>() {
            //ran when the selected list item is clicked
            @Override
            public void changed(ObservableValue<? extends Label> observable, Label oldValue, Label newValue) {
                // Your action here
                System.out.println("Selected item: " + newValue.getText());
                System.out.println("Going to send file scene");
                try {
                    enteredUser = newValue.getText();
                    goToSendFileScene();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private void populateList( Map<String, String> onlineUsers) {
        System.out.println(onlineUsers.toString());
        for (int i = 0; i < onlineUsers.size(); i++) {
            System.out.println(onlineUsers.get(i));
        }

        //testing
        Iterator it = onlineUsers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            try {
                Label lbl = new Label(pair.getKey().toString() + ": " + pair.getValue().toString());
                this.listView.getItems().add(lbl);
                it.remove();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void goToSendFileScene() throws IOException {
        Parent send_file_page_parent = FXMLLoader.load(getClass().getResource("send-file.fxml"));
        Scene send_file_page_scene = new Scene(send_file_page_parent);
        send_file_page_scene.getStylesheets().add(getClass().getResource("lisStyles.css").toExternalForm());
        Stage app_stage = (Stage) this.listView.getScene().getWindow();
        app_stage.setScene(send_file_page_scene);
        app_stage.show();
    }

    @FXML
    void loadDialog(ActionEvent event) {
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("Send Request"));
        content.setBody(new Text("Someone wants to send you files"));
        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);
        JFXButton button = new JFXButton("Okay");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
            }
        });
        content.setActions(button);
        dialog.show();
    }


    // to fetch the list of all online users currently
    public void populateList() {

        BackgroundFireBase firebaseSingleton = BackgroundFireBase.getInstance();
//        String user1 = firebaseSingleton.onlineUsers.get(0);
//        System.out.println(user1);
        System.out.println(firebaseSingleton.onlineUsers.toString());
        for (int i = 0; i < firebaseSingleton.onlineUsers.size(); i++) {
            System.out.println(firebaseSingleton.onlineUsers.get(i));
        }

    }

    public void receiveFiles(){

        Runnable server = new ServerClassCP2MultiThread();
        new Thread(server).start();

        // TODO: if there is a request to send, pop up the dialog

        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("Send Request"));
        // TODO: make the name of the sender appear
        content.setBody(new Text("Someone wants to send you files"));
        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);
        JFXButton button = new JFXButton("Okay");
        JFXButton button2 = new JFXButton("No thanks");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //TODO: continue with the rest of the procedure
            }
        });
        button2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
            }
        });
        content.setActions(button);
        content.setActions(button2);
        dialog.show();


    }






}
