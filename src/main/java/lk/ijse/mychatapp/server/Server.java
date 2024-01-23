package lk.ijse.mychatapp.server;

import lk.ijse.mychatapp.client.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    private Socket socket;
    private static  Server server;
    private List<ClientHandler> clients = new ArrayList<>();
    private Server() throws IOException {
        serverSocket = new ServerSocket(3001);
    }
    public static Server getInstance() throws IOException {
        return server!=null? server:(server = new Server());
    }

    public void makeSocket(){
        while (!serverSocket.isClosed()){
            try{
                socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket,clients);
                clients.add(clientHandler);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
