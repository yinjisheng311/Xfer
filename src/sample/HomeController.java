package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
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
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class HomeController implements Initializable {

    @FXML
    private JFXListView<Label> listView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        for (int i = 0; i < 4; i++){
            try {
                Label lbl = new Label("Item" + i);
                lbl.setGraphic(new ImageView(new Image(new FileInputStream("/Users/G/IdeaProjects/CSEDesignCompetition/src/sample/shield.png"))));
                this.listView.getItems().add(lbl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.listView.setExpanded(true);


    }

}
