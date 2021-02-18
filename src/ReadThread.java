import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

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
                    String[] userdata = message.split(":");
                    String[] users = userdata[1].split("[,]");
                    client.connectedUsers.add(users[users.length-1]); //<--Good till here


                    client.userList.appendText("\n" + client.userList.toString());





                 //   String[] user = userdata[1].split(",");
                  //  System.out.println(Arrays.toString(user));


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
