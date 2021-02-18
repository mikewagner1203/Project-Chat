import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientConnection implements Runnable {
    // Fields
    Socket socket;
    NetChatServer server;
    String username = "";

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
            server.userList.add(this.username);
            server.broadcast("\n" + "--> "+ this.username + " <-- has joined");
            server.broadcast("ListCall:" + server.userList);
        }
    }

    public void chatListener(String message) {
        if(message.startsWith("<")) {
            String targetUser = message.substring(message.indexOf("<") + 1, message.indexOf(">"));
            server.privateMessage("\n" + "[" + username + " flüstert]--> " + message.substring(message.indexOf(">") + 1), targetUser);
            if(!server.userList.contains(targetUser)) {
                server.privateMessage("\n" + targetUser + " is not available ! ", username);
            }

        } else if(message.equals("/exit")) {
            server.userList.remove(this.username);
            server.broadcast("ListCall:" + server.userList);
            server.broadcast("\n" + "--> "+ this.username + " <-- has left");

        } else {
            // send Message via broadcast method from server
            server.broadcast("\n" + "[" + username + "]: " + message);
        }

        // not necessary just displays Message from Client in Server Window
        server.serverStatusInfo.appendText(message + "\n");
    }
}
