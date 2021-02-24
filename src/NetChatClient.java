import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
    TextField hostInput;
    TextField portInput;
    TextArea serverInfo;
    String portNr;
    String username;
    Stage Prime;
    ArrayList<String> connectedUsers = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        Prime = primaryStage;
      //  primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("Project NetChat");
        primaryStage.setScene(connectScreen());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Scene connectScreen() {
        GridPane grid = new GridPane();
        grid.setId("grid");
        grid.setHgap(10);
        grid.setVgap(12);

        Text welcomeToNetChat = new Text("Welcome to NetChat");
        welcomeToNetChat.setId("title");
        welcomeToNetChat.setTranslateY(-20);
        grid.add(welcomeToNetChat, 1, 0);

        hostInput = new TextField("localhost"); // clear this parameter after testing
        hostInput.setId("input");
        hostInput.setFocusTraversable(false);
        hostInput.setPromptText("Enter Host");
        textfieldHover(hostInput);
        grid.add(hostInput,1,1);

        portInput = new TextField(String.valueOf(33030)); // clear this parameter after testing
        portInput.setId("input");
        portInput.setFocusTraversable(false);
        portInput.setPromptText("Enter Port");
        textfieldHover(portInput);
        grid.add(portInput, 1, 2);

        usernameInput = new TextField("Tester"); // clear this parameter after testing
        usernameInput.setId("input");
        usernameInput.setFocusTraversable(false);
        usernameInput.setPromptText("Enter Username");
        textfieldHover(usernameInput);
        grid.add(usernameInput,1,3);

        // for connection errors
        Text errorMsg = new Text();
        errorMsg.setId("error");
        errorMsg.setTranslateX(70);
        grid.add(errorMsg,1,5);

        Button btnQuit = new Button("Quit");
        btnQuit.setOnAction(actionEvent -> System.exit(0));

        Button btnConnect = new Button("Connect to NetChat");
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

            } else { // Starts connection to server when login is valid

                try {
                    // Create a socket to connect to the server
                    Socket socket = new Socket(hostAddress, Integer.parseInt(portNr));

                    // Creates an output stream to send data to the server
                    output = new DataOutputStream(socket.getOutputStream());
                    input = new DataInputStream(socket.getInputStream());

                    Prime.setScene(NetChatView());

                    // creates thread to read message from server continuously
                    ReadThread readMsg = new ReadThread(socket, this);
                    Thread thread = new Thread(readMsg);
                    thread.start();
                    output.writeUTF("Protocol: username: " + username);
                    output.flush();

                } catch (IOException e) {
                    errorMsg.setText("Connecting to Server failed");
                    e.printStackTrace();
                }
            }
        });

        ButtonBar btnbar = new ButtonBar();
        btnbar.getButtons().addAll(btnConnect,btnQuit);
        btnConnect.setTranslateX(-58);
        grid.add(btnbar,1,4);

        // Login when ENTER key is pressed
        usernameInput.setOnKeyPressed(actionEvent -> {
            if (actionEvent.getCode() == KeyCode.ENTER)
                btnConnect.fire();
            });
        Scene scene = new Scene(grid,1280,720);
        scene.getStylesheets().add("style.css");
        return scene;
    }

    private Scene NetChatView() {

        VBox vBox = new VBox(); // Vertikal-box to hold hbox1 and hbox2
        vBox.setSpacing(10);

        HBox hbox1 = new HBox(); // Horizontal-box to hold message input and send button
        HBox hbox2 = new HBox(); // Horizontal-box to hold chatarea and userlist
        HBox hbox3 = new HBox(); // Horizontal-box to hold headline and serverinfo
        hbox1.setEffect(new DropShadow(  10, 0.6, 1 , Color.BLACK));
        hbox2.setEffect(new DropShadow(  10, 0.6, 1 , Color.BLACK));
        hbox3.setEffect(new DropShadow(  10, 0.6, 1 , Color.BLACK));
        hbox1.setSpacing(10);
        hbox2.setSpacing(10);
        hbox3.setSpacing(15);

        ScrollPane messagePane = new ScrollPane(); //pane to display text messages
        ScrollPane userPane = new ScrollPane(); // pane for Users Online List

        Text headline = new Text("Project NetChat");
        headline.setId("headline");

        // define textarea for chatmessages
        chatArea = new TextArea();
        chatArea.setEditable(false); // makes that text in the chatArea can not be edited after display
        chatArea.setFocusTraversable(false);
        chatArea.setMouseTransparent(true);

        // sets size for scrollPane to fit chatArea
        messagePane.setContent(chatArea);
        messagePane.setFitToHeight(true);
        messagePane.setFitToWidth(true);
        messagePane.setMinWidth(600);

        userList = new TextArea();
        userList.setEditable(false);
        privateMsgClick(userList);

        // set size for userPane to fit userlist
        userPane.setContent(userList);
        userPane.setFitToHeight(true);
        userPane.setFitToWidth(true);
        userPane.setMaxWidth(200);
        userPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // define textfield for message input and styling
        msgInput = new TextField();
        msgInput.setId("msginput");
        msgInput.setPrefHeight(30);
        textfieldHover(msgInput);
        msgInput.setPromptText("New message....");
        msgInput.setMinWidth(600);

        // define textfield for Server status info
        serverInfo = new TextArea();
        serverInfo.setId("serverinfo");
        serverInfo.setPrefHeight(35);
        serverInfo.setEditable(false);

        // define Send-button and styling
        Button btnSend = new Button("Send");
        btnSend.setCursor(Cursor.HAND);
        btnSend.setPrefSize(200,30);

        serverInfo.appendText("Connected to Server " + hostInput.getText() + " @ " + new Date() + "\n" + "Logged in as: " + username);
        chatArea.appendText("Welcome to NetChat " + username + " !   Type: /help for commands." );

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

                //if message is empty, just return : don't send the message
                if (message.length() == 0) {
                    return;
                }

                if (message.equalsIgnoreCase("/exit")) {
                    Platform.exit();
                }

                if (message.equals("/logout")) {
                    Prime.setScene(connectScreen()); // <--- Works but resolution buggy
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
        hbox2.getChildren().addAll(messagePane, userPane);
        HBox.setHgrow(messagePane,Priority.ALWAYS);

        hbox3.getChildren().addAll(headline,serverInfo);
        HBox.setHgrow(serverInfo,Priority.ALWAYS);

        //set center and bottom of the borderPane with scrollPane and hBox
        vBox.getChildren().addAll(hbox3, hbox2, hbox1);
        VBox.setVgrow(hbox2, Priority.ALWAYS);

        //create a scene and display
        Prime.setMinWidth(1024);
        Prime.setMinHeight(600);
        Scene scene2 = new Scene(vBox,1280,720);
        scene2.getStylesheets().add("style.css");
        return scene2;
    }

    public void textfieldHover(TextField textfield) {
        textfield.setCursor(Cursor.HAND);
        textfield.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 14; -fx-border-radius: 15; -fx-border-color: linear-gradient(from 0% 0% to 100% 200%, repeat, #9bc9d4 0%, #61a2b1 80%); -fx-border-width: 2; -fx-border: gone; -fx-text-fill: #17e8ff; -fx-background-color: transparent;");
        textfield.setOnMouseEntered(mouseEvent -> textfield.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 14; -fx-border-radius: 15; -fx-border-color: linear-gradient(from 0% 0% to 100% 200%, repeat, #17e8ff 0%, #61a2b1 80%); -fx-border-width: 2; -fx-border: gone; -fx-text-fill: #17e8ff; -fx-background-color: transparent;"));
        textfield.setOnMouseExited(mouseEvent -> textfield.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 14; -fx-border-radius: 15; -fx-border-color: linear-gradient(from 0% 0% to 100% 200%, repeat, #9bc9d4 0%, #61a2b1 80%); -fx-border-width: 2; -fx-border: gone; -fx-text-fill: #17e8ff; -fx-background-color: transparent;"));
    }

    public void privateMsgClick(TextArea textArea) {
        textArea.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if(mouseEvent.getClickCount() == 2){
                    msgInput.clear();
                    msgInput.appendText("<" + textArea.getSelectedText() + ">");
                    textArea.deselect();
                }
            }
        });
    }
}
