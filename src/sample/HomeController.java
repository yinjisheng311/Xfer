package sample;

import com.jfoenix.controls.*;
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
    private JFXListView<Button> listView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        for (int i = 0; i < 4; i++){
            try {
//                Label lbl = new Label("Item" + i);
                Button btn = new Button();
                btn.setText("Item" + i);
                btn.setOnAction(e -> listButtonClicked());
//                lbl.setGraphic(new ImageView(new Image(new FileInputStream("/Users/G/IdeaProjects/CSEDesignCompetition/src/sample/shield.png"))));
                this.listView.getItems().add(btn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.listView.setExpanded(true);

    }
    private void listButtonClicked(){
        System.out.println("CLICKED");
        ObservableList<Button> users;
        users = this.listView.getSelectionModel().getSelectedItems();
        System.out.println(users.toString());
    }


    @FXML
    void loadDialog(ActionEvent event) {
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("Heading"));
        content.setBody(new Text("THIS IS BODY"));
//        JFXDialog dialog = new JFXDialog(homeBordenPane, new Label("Hello"), JFXDialog.DialogTransition.CENTER));
    }

}
