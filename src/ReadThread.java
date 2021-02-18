import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class ReadThread implements Runnable {
    // Fields
    Socket socket;
    NetChatClient client;
    DataInputStream input;

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

                if(message.startsWith("ListCall:")) {
                    client.userList.clear();
                    fillUserList(message);

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

    private void fillUserList(String message) {
        String[] userdata = message.split(":");
        String[] users = userdata[1].split("[,]");
        client.connectedUsers.clear();
        for(int i = 0; i< users.length;i++) {
            client.connectedUsers.add(users[i]);
        }
        client.userList.appendText("Users Online: " + client.connectedUsers.size() + "\n"); // adds list headline and users online count
        for (int j = 0; j< client.connectedUsers.size(); j++) {
            client.userList.appendText(Arrays.toString(client.connectedUsers.get(j).split(",")) + "\n");
        }
    }
}
