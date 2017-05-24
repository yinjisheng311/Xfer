package sample;

import Server.BackgroundFireBase;
import Server.Server2;
import Server.UserInfo;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXToggleButton;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Phaser;


public class HomeController implements Initializable {

    @FXML
    private StackPane stackPane;

    @FXML
    private ListView<Label> listView;

    @FXML

    private JFXToggleButton toggle;

    public static String enteredUser;

    private static boolean accept;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        accept = false;
//        receiveFiles();

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

//        receiveFiles();
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

    public void goToLoginScene() throws IOException {
        //TODO: logs the user out of the firebase as well

//        BackgroundFireBase.getInstance().offline(UserInfo.getInstance().getUser());
        UserInfo.getInstance().resetInstance();

        Parent login_page_parent = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene login_page_scene = new Scene(login_page_parent);
        login_page_scene.getStylesheets().add(getClass().getResource("lisStyles.css").toExternalForm());
        Stage app_stage = (Stage) this.listView.getScene().getWindow();
        app_stage.setScene(login_page_scene);
        app_stage.show();
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
//
//    public void receiveFiles(){
////        Runnable server = new Server();
////        new Thread(server).start();
//        sendRequestPopup();
//    }

    JFXDialog dialog;
    boolean[] proceed = new boolean[2];

    public void sendRequestPopup(){
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("File Received"));
        // TODO: make the name of the sender appear
        Text[] bodyText = {new Text("Someone has sent you files")};
        content.setBody(bodyText);
        dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);
        JFXButton button2 = new JFXButton("Okay");
        proceed[0] = true;
//
//        button2.setStyle("-fx-background-color: red");
//        button2.setStyle("-fx-text-fill: white");
//        Phaser phaser = new Phaser();
//        phaser.bulkRegister(2);
        button2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
                proceed[0] = false;
                proceed[1] = true;
//                phaser.arriveAndDeregister();
            }
        });
        content.setActions(button2);
        dialog.show();

    }

//
//
//
//    public void checkDialogHasPoppedUp(){
////        dialog.
//        while(!proceed[1]){}
//
//    }

    public boolean getProceed(){
        return this.proceed[0];
    }

    public void refresh(){
        BackgroundFireBase firebaseSingleton = BackgroundFireBase.getInstance();

        this.listView.getItems().clear();
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
    }

    public void startServer() throws Exception {

        if (this.toggle.isSelected()){
            this.toggle.setText("Accepting files");
            String[] whatever = {""};
            Runnable server = new Server2(this);
            new Thread(server).start();


        } else {
            //TODO: close server socket

            this.toggle.setText("Click to accept files");
        }
    }


}
