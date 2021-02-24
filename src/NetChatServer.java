import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class NetChatServer extends Application {

    TextArea serverStatusInfo;
    ScrollPane scrollPane;
    ArrayList<String> userList = new ArrayList<>();
    ArrayList<ClientConnection> clientList = new ArrayList<>();
    DataOutputStream output;

    private Scene serverGUI() {
        VBox serverbox = new VBox();

        // Text area for displaying Server info
        serverStatusInfo = new TextArea();
        serverStatusInfo.setEditable(false);

        scrollPane = new ScrollPane();  //pane to display text messages
        scrollPane.setContent(serverStatusInfo);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        serverbox.getChildren().add(scrollPane);

        return new Scene(serverbox, 450, 150);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("NetChatServer"); // Set the stage title
        primaryStage.setScene(serverGUI()); // Place the scene in the stage
        primaryStage.show(); // Display the stage

        new Thread(() -> {
            try {
                // Creates ServerSocketClass object with listening-port
                ServerSocket chatServer = new ServerSocket(33030);

                serverStatusInfo.appendText("New server started at " + new Date() + '\n');

                while (true) {

                    // creates socket object, with .accept() the server is listening for Client request (accepts requests)
                    Socket socket = chatServer.accept();

                    //add new connection to the list
                    ClientConnection connection = new ClientConnection(socket, this);
                    clientList.add(connection);
                    output = new DataOutputStream(socket.getOutputStream());

                    // creates thread for ClientConnection to handle multiple Clients connected to server
                    Thread thread = new Thread(connection);
                    thread.start();
                }

            } catch (IOException e) { // exception is thrown when server cant start up i.e: ports not defined etc
                serverStatusInfo.appendText("Something went wrong: " + e + "\n");
                e.printStackTrace();
            }
        }).start();
    }

    public static void main (String[] args) {
        launch(args);
    }

    //sends message to all connected clients
    public void broadcast(String message) {
        for (ClientConnection clientConnection : this.clientList) {
            clientConnection.sendMessage(message);
        }
    }

    //sends message to specified User
    public void privateMessage(String privateMessage, String targetUser) {
        for(ClientConnection aUser:clientList) {
            if(aUser.username.equals(targetUser)) {
                aUser.sendMessage(privateMessage);
            }
        }
    }
}




