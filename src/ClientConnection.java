import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientConnection implements Runnable {
    // Fields
    Socket socket;
    NetChatServer server;
    String username = "";
    ArrayList<String> userList = new ArrayList<>();

    // Create IO Streams
    DataInputStream input;
    DataOutputStream output;

    // Constructor
    public ClientConnection(Socket socket, NetChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            // Create IO Streams
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            // get Username from Client
            String nameCall = input.readUTF();

            // Saves the username from login when connection to server is valid
            process(nameCall);

            while (true) {

                // Get Message from Client
                String message = input.readUTF();

                 //Broadcasts Messages and handles private messages.
                chatListener(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                input.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //sends message back to client
    public void sendMessage(String message) {
        try {
            output.writeUTF(message);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void process(String nameCall) {
        if(nameCall.startsWith("Protocol: ")) {
            this.username = nameCall.substring(20);
            userList.add(this.username);
            server.broadcast("\n" + "--> "+ this.username + " <-- has joined");
            server.broadcast("ListCall:" + userList);
         //   output.flush();

         //  server.serverStatusInfo.appendText("userlist: " + server.userList + "\n");
        }
    }

    public void chatListener(String message) {
        if(message.startsWith("[")) {
            String targetUser = message.substring(message.indexOf("[") +1,message.indexOf("]"));

            System.out.println(targetUser + "<--Target User"); // for testing
            System.out.println(message + "<--Testmessage"); // for testing

            server.privateMessage("\n" + "[" + username + " flüstert]--> " + message.substring(message.indexOf("]") + 1), targetUser);
        } else {
            // send Message via broadcast method from server
            server.broadcast("\n" + "[" + username + "]: " + message);
        }

        // not necessary just displays Message from Client in Server Window
        server.serverStatusInfo.appendText(message + "\n");
    }
}
