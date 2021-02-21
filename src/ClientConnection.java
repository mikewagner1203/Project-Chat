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
            server.broadcast("\n" + "--> "+ this.username + " <-- has joined \n");
            server.broadcast("ListCall:" + server.userList);
        }
    }

    public void chatListener(String message) {
        if(message.startsWith("<")) {
            String targetUser = message.substring(message.indexOf("<") + 1, message.indexOf(">"));
            server.privateMessage("[" + username + " flüstert]--> " + message.substring(message.indexOf(">") + 1) +"\n", targetUser);
            server.privateMessage("[Du flüsterst an: " + targetUser + "]--> " + message.substring(message.indexOf(">") + 1) +"\n", this.username);
            if(!server.userList.contains(targetUser)) {
                server.privateMessage(targetUser + " is not available ! \n", username);
            }

        } else if(message.equals("/exit")) {
            server.userList.remove(this.username);
            server.broadcast("ListCall:" + server.userList);
            server.broadcast("\n" + "--> "+ this.username + " <-- has left \n");

        } else if(message.equals("/help")) {
            server.privateMessage("\n Commands: \n /help ---> shows this  \n /exit ---> leaves the Chat \n <*username*> ---> Private Message to selected username",this.username);

        } else {
            // send Message via broadcast method from server
            server.broadcast("[" + this.username + "]> " + message);
        }

        // not necessary just displays Message from Client in Server Window
        server.serverStatusInfo.appendText(message + "\n");
    }
}
