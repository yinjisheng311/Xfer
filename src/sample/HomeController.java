package sample;

import com.jfoenix.controls.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class HomeController implements Initializable {

    @FXML
    private BorderPane homeBordenPane;

    @FXML
    private JFXListView<Label> listView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        for (int i = 0; i < 4; i++){
            try {
                Label lbl = new Label("User " + (i+1));
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
        content.setHeading(new Text("Heading"));
        content.setBody(new Text("THIS IS BODY"));
//        JFXDialog dialog = new JFXDialog(homeBordenPane, new Label("Hello"), JFXDialog.DialogTransition.CENTER));
    }

}
