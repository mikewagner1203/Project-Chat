import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class NetChatClient extends Application {
    DataInputStream input;
    DataOutputStream output;
    TextArea chatArea;
    TextArea userList;
    TextField msgInput;
    TextField usernameInput;
    TextArea serverInfo;
    TextField hostInput;
    String portNr;
    String username;
    Stage Prime;


    @Override
    public void start(Stage primaryStage) {
        Prime = primaryStage;
        primaryStage.setTitle("Project NetChat");
        primaryStage.setScene(connectScreen());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Scene connectScreen() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text scenetitle = new Text("Welcome to NetChat");
        scenetitle.setFont(Font.font("Verdana", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        hostInput = new TextField("localhost"); // clear this parameter after testing
        grid.add(hostInput,1,1);
        hostInput.setPromptText("Enter Host");

        TextField portInput = new TextField(String.valueOf(33030)); // clear this parameter after testing
        grid.add(portInput, 1, 2);
        portInput.setPromptText("Enter Port");

        usernameInput = new TextField("testuser"); // clear this parameter after testing
        grid.add(usernameInput,1,3);
        usernameInput.setPromptText("Enter Username");

        // for connection errors
        Text errorMsg = new Text();
        errorMsg.setFill(Color.RED);
        grid.add(errorMsg,1,5);

        Button btnConnect = new Button("Connect to NetChat");
        grid.add(btnConnect,1,4);
        btnConnect.setOnAction(actionEvent -> {
            String hostAddress = hostInput.getText();
            portNr = portInput.getText();
            username = usernameInput.getText();

            //  handles input errors when fields are skipped
            if(hostAddress.length() == 0 || username.length() == 0 || portNr.length() == 0) {
                hostInput.setPromptText("Please enter a Host address");
                portInput.setPromptText("Please enter a valid portnumber");
                usernameInput.setPromptText("Please enter a Username");
                errorMsg.setText("Invalid Login");

            } else {  // Starts connection to server when login is valid

                try {
                    // Create a socket to connect to the server
                   //Socket socket = new Socket("localhost",33030);
                    Socket socket = new Socket(hostAddress, Integer.parseInt(portNr));

                    // Creates an output stream to send data to the server
                    output = new DataOutputStream(socket.getOutputStream());
                    input = new DataInputStream(socket.getInputStream());

                    Prime.setScene(NetChatView());
                    // chatArea.appendText("Hello " + username + ", welcome to NetChat" + "\n");

                    // creates thread to read message from server continuously
                    ReadThread readMsg = new ReadThread(socket, this);
                    Thread thread = new Thread(readMsg);
                    thread.start();
                    output.writeUTF("Protocol: username: " + username);
                    output.flush();

                } catch (IOException e) {
                    errorMsg.setText("Connecting to Server failed");
              //      e.printStackTrace();
                }
            }
        });

        // Login when ENTER key is pressed
        usernameInput.setOnKeyPressed(actionEvent -> {
            if (actionEvent.getCode() == KeyCode.ENTER)
                btnConnect.fire();
        });
        return new Scene(grid, 800, 400);
    }


    private Scene NetChatView() {

        VBox vBox = new VBox(); // Vertikal-box to hold hbox1 and hbox2

        HBox hbox1 = new HBox(); // Horizontal-box to hold message input and send button
        HBox hbox2 = new HBox(); // Horizontal-box to hold chatarea and userlist

        ScrollPane scrollPane = new ScrollPane(); //pane to display text messages
        ScrollPane userPane = new ScrollPane();
        // define textarea for chatmessages and properties for responsive size
        chatArea = new TextArea();
        chatArea.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        chatArea.setStyle("-fx-control-inner-background:#CCCCFF; -fx-text-fill: #000000;");
        chatArea.setEditable(false); // makes that text in the chatArea can not be edited after display

        // sets size for scrollPane to fit chatArea
        scrollPane.setContent(chatArea);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinWidth(600);

        userList = new TextArea("Connected Users: " );
        userList.setEditable(false);
        userList.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        userList.setStyle("-fx-control-inner-background:#00CCFF; -fx-text-fill: #000000;");
        userList.setWrapText(true);

        // set size for userPane to fit userlist
        userPane.setContent(userList);
        userPane.setFitToHeight(true);
        userPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        userPane.setMinWidth(200);
        userPane.setMaxWidth(200);

        // define textfield for message input and styling
        msgInput = new TextField();
        msgInput.setPrefHeight(30);
        msgInput.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        msgInput.setStyle("-fx-control-inner-background:#F2F4F4 ; -fx-text-fill: #000000;");
        msgInput.setPromptText("New message....");

        // define textfield for Server status info
        serverInfo = new TextArea();
        serverInfo.setPrefHeight(32);
        serverInfo.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
        serverInfo.setEditable(false);

        // define Send-button and styling
        Button btnSend = new Button("Send");
        btnSend.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        btnSend.setStyle("-fx-background-color:#F2F4F4; -fx-text-fill: #000000;");
        btnSend.setCursor(Cursor.HAND);
        btnSend.setPrefSize(80,30);

        serverInfo.appendText("Connected to Server " + hostInput.getText() + " @ " + new Date() + "\n" + "Logged in as: " + username);
        chatArea.appendText("Hello " + username + " Welcome to NetChat !");


        // sends Message when ENTER key is pressed
        msgInput.setOnKeyPressed(actionEvent -> {
            if (actionEvent.getCode() == KeyCode.ENTER)
                btnSend.fire();
        });

        // action when send button is clicked
        btnSend.setOnAction(actionEvent -> {

            try {
                //get message
                String message = msgInput.getText();
                if (message.equalsIgnoreCase("/exit")) {
                    // output.writeUTF(getUsername() + "<--- has left the Chat--->");
                    System.exit(0);
                }

                //if message is empty, just return : don't send the message
                if (message.length() == 0) {
                    return;
                }

                //send message to server
                output.writeUTF(message);
                output.flush();

                // empties the Inputfield after message is sent
                msgInput.clear();

                // Throws exception when no server is found or connection is refused (ports wrong etc...)
            } catch (IOException e) {
                e.printStackTrace();
                serverInfo.setText("\n" + "Sending message failed...");
            }
        });

        // adds Inputfield and button to hBox1
        hbox1.getChildren().addAll(msgInput, btnSend);
        HBox.setHgrow(msgInput, Priority.ALWAYS);  //set textfield to grow as window size grows

        // adds chatarea and userList to Hbox2
        hbox2.getChildren().addAll(scrollPane, userPane);
        HBox.setHgrow(scrollPane,Priority.ALWAYS);

        //set center and bottom of the borderPane with scrollPane and hBox
        vBox.getChildren().addAll(serverInfo, hbox2, hbox1);
        VBox.setVgrow(hbox2, Priority.ALWAYS);

        //create a scene and display
        Prime.setMinWidth(800);
        Prime.setMinHeight(600);
        return new Scene(vBox, 800, 600);
    }

}

// to Start another Client from Terminal
/*C:\Users\codersbay\IdeaProjects\Project NetChat\out\production\Project NetChat> C:\Users\codersbay\.jdks\openjdk-15.0.1\bin\java.exe --module-path C:\Users\codersbay\javafx-sdk-15.0.1\lib --add-modules javafx.controls,javafx.fxml --a
        dd-exports javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED --add-modules javafx.base,javafx.graphics --add-reads javafx.base=ALL-UNNAMED --add-reads javafx.graphics=ALL-UNNAMED "-javaagent:C:\Program Files\JetBrains\IntelliJ IDE
        A Community Edition 2020.2.1\lib\idea_rt.jar=57032:C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2020.2.1\bin" -Dfile.encoding=UTF-8 -classpath "C:\Users\codersbay\IdeaProjects\Project NetChat\out\production\Project Net
        Chat;C:\Users\codersbay\javafx-sdk-15.0.1\lib\src.zip;C:\Users\codersbay\javafx-sdk-15.0.1\lib\javafx-swt.jar;C:\Users\codersbay\javafx-sdk-15.0.1\lib\javafx.web.jar;C:\Users\codersbay\javafx-sdk-15.0.1\lib\javafx.base.jar;C:\Users\
        codersbay\javafx-sdk-15.0.1\lib\javafx.fxml.jar;C:\Users\codersbay\javafx-sdk-15.0.1\lib\javafx.media.jar;C:\Users\codersbay\javafx-sdk-15.0.1\lib\javafx.swing.jar;C:\Users\codersbay\javafx-sdk-15.0.1\lib\javafx.controls.jar;C:\User
        s\codersbay\javafx-sdk-15.0.1\lib\javafx.graphics.jar" NetChatClient */