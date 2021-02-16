import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ReadThread implements Runnable {
    // Fields
    Socket socket;
    NetChatClient client;
    DataInputStream input;
    List <String> connectedUsers = new ArrayList<>();

    // Constructor
    public ReadThread(Socket socket, NetChatClient client) {
        this.socket = socket;
        this.client = client;
    }

    @Override
    public void run() {

        while(true) {

            try {
                // Input Stream
                input = new DataInputStream(socket.getInputStream());

                // get Input from Client
                String message = input.readUTF();

                // Works but output not Good !!!!
                if(message.startsWith("\n" + "--> ")) {
                    client.chatArea.appendText("\n" + message);
                }

                else if(message.startsWith("ListCall:")) {
                    String[] userdata = message.split(":");
                    connectedUsers.add(userdata[1]);
                    client.userList.appendText(userdata[1]);
                    System.out.println(userdata[1]);

                } else {
                    client.chatArea.appendText("\n" + message); // show the "has joined" message from server broadcast
                }

            } catch (IOException e) {
                client.serverInfo.setText("Error reading from Server: " + e.getMessage());
                client.chatArea.appendText("\n" + "Disconnected from Server");
                e.printStackTrace();
                break;
            }
        }
    }

}
