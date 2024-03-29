package lk.ijse.mychatapp.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lk.ijse.mychatapp.client.ClientHandler;
import lk.ijse.mychatapp.emoji.EmojiPicker;
import lk.ijse.mychatapp.server.Server;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class ClientFormController {

    public AnchorPane pane;
    public ScrollPane scrollPane;
    public VBox vBox;
    public Button btnCamera;
    public Button btnEmoji;
    public Button btnSend;
    public Label txtLable;
    public TextField txtMsg;
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String clientName = "client";
    private Server server;
    public void initialize(){
        txtLable.setText(clientName);

        //me code eka mama aluthen damma
        new Thread(() -> {
            try {
                server = Server.getInstance();
                server.makeSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket("localhost", 3001);
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    System.out.println("Client Connected");

                    while (socket.isConnected()){
                        String receivingMsg = dataInputStream.readUTF();
                        receiveMassege(receivingMsg,ClientFormController.this.vBox);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();

        this.vBox.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                scrollPane.setVvalue((Double) newValue);
            }
        });
        emoji();
    }

    private void emoji() {
        // Create the EmojiPicker
        EmojiPicker emojiPicker = new EmojiPicker();

        VBox vBox = new VBox(emojiPicker);
        vBox.setPrefSize(150,300);
        vBox.setLayoutX(400);
        vBox.setLayoutY(95);
        vBox.setStyle("-fx-font-size: 30");

        pane.getChildren().add(vBox);

        // Set the emoji picker as hidden initially
        emojiPicker.setVisible(false);

        // Show the emoji picker when the button is clicked
        btnEmoji.setOnAction(event -> {
            if (emojiPicker.isVisible()){
                emojiPicker.setVisible(false);
            }else {
                emojiPicker.setVisible(true);
            }
        });

        // Set the selected emoji from the picker to the text field
        emojiPicker.getEmojiListView().setOnMouseClicked(event -> {
            String selectedEmoji = emojiPicker.getEmojiListView().getSelectionModel().getSelectedItem();
            if (selectedEmoji != null) {
                txtMsg.setText(txtMsg.getText()+selectedEmoji);
            }
            emojiPicker.setVisible(false);
        });
    }

    public void btnSendOnAction(ActionEvent actionEvent) {
        sendMsg(txtMsg.getText());
    }
    private void sendMsg(String msgToSend) {
        if (!msgToSend.isEmpty()){
            if (!msgToSend.matches(".*\\.(png|jpe?g|gif)$")){

                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER_RIGHT);
                hBox.setPadding(new Insets(5, 5, 0, 10));

                Text text = new Text(msgToSend);
                text.setStyle("-fx-font-size: 14");
                TextFlow textFlow = new TextFlow(text);

//              #0693e3 #37d67a #40bf75
                textFlow.setStyle("-fx-background-color: #0693e3; -fx-font-weight: bold; -fx-color: white; -fx-background-radius: 20px");
                textFlow.setPadding(new Insets(5, 10, 5, 10));
                text.setFill(Color.color(1, 1, 1));

                hBox.getChildren().add(textFlow);

                HBox hBoxTime = new HBox();
                hBoxTime.setAlignment(Pos.CENTER_RIGHT);
                hBoxTime.setPadding(new Insets(0, 5, 5, 10));
                String stringTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                Text time = new Text(stringTime);
                time.setStyle("-fx-font-size: 8");

                hBoxTime.getChildren().add(time);

                vBox.getChildren().add(hBox);
                vBox.getChildren().add(hBoxTime);


                try {
                    dataOutputStream.writeUTF(clientName + "-" + msgToSend);
                    dataOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                txtMsg.clear();
            }
        }
    }

    private void sendImage(String filePath) {
        try {
            File file = new File(filePath);

                if (!file.exists()) {
                    System.out.println("File does not exist: " + filePath);
                    return;
                }

                // Read the image file as bytes
                byte[] fileBytes = new byte[(int) file.length()];
                FileInputStream fileInputStream = new FileInputStream(file);
                fileInputStream.read(fileBytes);
                fileInputStream.close();

                // Send the image bytes through the DataOutputStream
                dataOutputStream.writeUTF("image:" + clientName);  // Assuming you want to identify the message as an image
                dataOutputStream.writeInt(fileBytes.length);
                dataOutputStream.write(fileBytes);
                dataOutputStream.flush();

                // Display the image in the UI
                Image image = new Image("file:" + filePath);
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(200);
                imageView.setFitWidth(200);

                HBox hBox = new HBox();
                hBox.setPadding(new Insets(5, 5, 5, 10));
                hBox.getChildren().add(imageView);
                hBox.setAlignment(Pos.CENTER_RIGHT);

                vBox.getChildren().add(hBox);

                System.out.println("Image sent: " + filePath);
        } catch (IOException e) {
                e.printStackTrace();
        }
    }


   public static void receiveMassege(String msg, VBox vBox) {
       /*if(msg.matches(".*\\.(png|jpe?g|gif)$")){
            HBox hBoxName = new HBox();
            hBoxName.setAlignment(Pos.CENTER_LEFT);
            Text textName = new Text(msg.split("[-]")[0]);
            TextFlow textFlowName = new TextFlow(textName);
            hBoxName.getChildren().add(textFlowName);

            Image image = new Image(msg.split("[-]")[1]);
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(200);
            imageView.setFitWidth(200);
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.setPadding(new Insets(5,5,5,10));
            hBox.getChildren().add(imageView);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    vBox.getChildren().add(hBoxName);
                    vBox.getChildren().add(hBox);
                }
            });*/

        /*if (msg.startsWith("image:")) {
            String[] parts = msg.split(":");
            String senderName = parts[1];
            String imagePath = parts[2];

            Platform.runLater(() -> {
                HBox hBoxName = new HBox();
                hBoxName.setAlignment(Pos.CENTER_LEFT);
                Text textName = new Text(senderName);
                TextFlow textFlowName = new TextFlow(textName);
                hBoxName.getChildren().add(textFlowName);

                Image image = new Image("file:" + imagePath);
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(200);
                imageView.setFitWidth(200);
                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER_LEFT);
                hBox.setPadding(new Insets(5, 5, 5, 10));
                hBox.getChildren().add(imageView);

                vBox.getChildren().add(hBoxName);
                vBox.getChildren().add(hBox);
            });*/
       if (msg.startsWith("image:")) {
           String[] parts = msg.split(":");
           if (parts.length >= 3) {
               String senderName = parts[1];
               String imagePath = parts[2];

               HBox hBoxName = new HBox();
               hBoxName.setAlignment(Pos.CENTER_LEFT);
               Text textName = new Text(senderName);
               TextFlow textFlowName = new TextFlow(textName);
               hBoxName.getChildren().add(textFlowName);

               Image image = new Image("file:" + imagePath);
               ImageView imageView = new ImageView(image);
               imageView.setFitHeight(200);
               imageView.setFitWidth(200);
               HBox hBox = new HBox();
               hBox.setAlignment(Pos.CENTER_LEFT);
               hBox.setPadding(new Insets(5, 5, 5, 10));
               hBox.getChildren().add(imageView);

               vBox.getChildren().add(hBoxName);
               vBox.getChildren().add(hBox);
           }

       } else {
           String name = msg.split("-")[0];
           String msgFromServer = msg.split("-")[1];
           //System.out.println(msgFromServer);
           HBox hBox = new HBox();
           hBox.setAlignment(Pos.CENTER_LEFT);
           hBox.setPadding(new Insets(5, 5, 5, 10));

           HBox hBoxName = new HBox();
           hBoxName.setAlignment(Pos.CENTER_LEFT);
           Text textName = new Text(name);
           TextFlow textFlowName = new TextFlow(textName);
           hBoxName.getChildren().add(textFlowName);

           Text text = new Text(msgFromServer);
           TextFlow textFlow = new TextFlow(text);
           textFlow.setStyle("-fx-background-color: #abb8c3; -fx-font-weight: bold; -fx-background-radius: 20px");
           textFlow.setPadding(new Insets(5, 10, 5, 10));
           text.setFill(Color.color(0, 0, 0));

           hBox.getChildren().add(textFlow);

           Platform.runLater(new Runnable() {
               @Override
               public void run() {
                   vBox.getChildren().add(hBoxName);
                   vBox.getChildren().add(hBox);
               }
           });
       }
   }

    public void btnCameraOnAction(ActionEvent actionEvent) {
        // Create a FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Open");

        // Set extension filters if you want to restrict to certain file types
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        // Show open file dialog
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            // Process the selected file
            String filePath = selectedFile.getAbsolutePath();
            sendImage(filePath);
            System.out.println(filePath + " chosen.");
        } else {
            System.out.println("File selection cancelled.");
        }

    }

    public void btnEmojiOnAction(ActionEvent actionEvent) {
       // emoji();
    }


    public void setClentName(String name){
        clientName = name;

    }
}
