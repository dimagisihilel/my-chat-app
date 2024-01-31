package lk.ijse.mychatapp.client;

import javafx.application.Platform;
import lk.ijse.mychatapp.controller.ClientFormController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.net.Socket;
import java.util.List;

public class ClientHandler {
    private Socket socket;
    private List<ClientHandler> clients;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String msg  = "";


    public ClientHandler(Socket socket, List<ClientHandler> clients)  {

        try{
            this.socket = socket;
            this.clients = clients;
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());

        } catch (IOException e){
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while(socket.isConnected()){
                        msg = dataInputStream.readUTF();
                        for (ClientHandler clientHandler : clients) {
                            if (clientHandler.socket.getPort() != socket.getPort()) {
                                clientHandler.dataOutputStream.writeUTF(msg);
                                clientHandler.dataOutputStream.flush();
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        /*new Thread(() -> {
            try {
                while (socket.isConnected()) {
                    String receivedMsg = dataInputStream.readUTF();
                    handleReceivedMessage(receivedMsg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();*/
    }
   /* public ClientHandler(Socket socket, List<ClientHandler> clients)  {

        try {
            this.socket = socket;
            this.clients = clients;
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            try {
                while (socket.isConnected()) {
                    try {
                        String receivingMsg = dataInputStream.readUTF();
                        if (receivingMsg.startsWith("image:")) {
                            handleReceivedImage(receivingMsg);
                        } else if (!receivingMsg.isEmpty()) {
                            handleReceivedText(receivingMsg);
                        }
                    } catch (UTFDataFormatException e) {
                        // Handle the UTFDataFormatException gracefully
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void handleReceivedText(String msg) {
        // Handle text messages
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.socket.getPort() != socket.getPort()) {
                try {
                    clientHandler.dataOutputStream.writeUTF(msg);
                    clientHandler.dataOutputStream.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void handleReceivedImage(String msg) {
        // Handle image messages
        String[] parts = msg.split(":");
        if (parts.length >= 3) {
            String senderName = parts[1];
            String imagePath = parts[2];

            for (ClientHandler clientHandler : clients) {
                if (clientHandler.socket.getPort() != socket.getPort()) {
                    try {
                        clientHandler.dataOutputStream.writeUTF(msg);
                        clientHandler.dataOutputStream.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }*/
}


