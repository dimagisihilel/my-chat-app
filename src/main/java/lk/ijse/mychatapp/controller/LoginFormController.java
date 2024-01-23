package lk.ijse.mychatapp.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginFormController {

    public AnchorPane root;
    public TextField txtName;
    public Button btnLogin;

    public void initialize(){


    }

    public void btnLoginOnAction(ActionEvent actionEvent) throws IOException {
        if (!txtName.getText().isEmpty()&&txtName.getText().matches("[A-Za-z0-9]+")){
            Stage primaryStage = new Stage();
            //FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/client_form.fxml"));

            //ClientFormController controller = new ClientFormController();
            //controller.setClentName(txtName.getText());
            //fxmlLoader.setController(controller);

            //primaryStage.setScene(new Scene(fxmlLoader.load()));
            primaryStage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/view/client_form.fxml"))));

            ClientFormController controller = new ClientFormController();
            controller.setClentName(txtName.getText());

            primaryStage.setTitle(txtName.getText());
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            //window code ekk enawa

            primaryStage.show();
            txtName.clear();

        }else {
            new Alert(Alert.AlertType.ERROR,"Please Enter Your Name").show();
        }
    }
}
