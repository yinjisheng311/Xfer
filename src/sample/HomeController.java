package sample;

import Server.BackgroundFireBase;
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
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;


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

        Iterator it = firebaseSingleton.onlineUsers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
            try {
                Label lbl = new Label(pair.getKey().toString() + ": " + pair.getValue().toString());
                this.listView.getItems().add(lbl);

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
    public void goToSendFileScene() throws IOException {
        Parent send_file_page_parent = FXMLLoader.load(getClass().getResource("send-file.fxml"));
        Scene send_file_page_scene = new Scene(send_file_page_parent);
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
        populateList();
    }


    // to fetch the list of all online users currently
    public void populateList(){
        Main.firebaseSingleton = BackgroundFireBase.getInstance();
//        String user1 = firebaseSingleton.onlineUsers.get(0);
//        System.out.println(user1);
        System.out.println(Main.firebaseSingleton.onlineUsers.toString());
        for(int i=0 ; i < Main.firebaseSingleton.onlineUsers.size(); i ++){
            System.out.println(Main.firebaseSingleton.onlineUsers.get(i));
        }

        //testing
        Iterator it = firebaseSingleton.onlineUsers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
            try {
                Label lbl = new Label(pair.getKey().toString() + ": " + pair.getValue().toString());
                this.listView.getItems().add(lbl);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



}
