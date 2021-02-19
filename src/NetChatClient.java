import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
       // connectScreen().getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Scene connectScreen() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("grid");
        grid.setStyle("-fx-background-color:linear-gradient(#61a2b1, #2A5058) ");
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text scenetitle = new Text("Welcome to NetChat");
        scenetitle.setStyle("-fx-font: 35px Verdana; -fx-fill: #9bc9d4; -fx-stroke: black; -fx-stroke-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0), 10, 0.6, 1 , 1 ); ");
        scenetitle.setTranslateY(-20);
        grid.add(scenetitle, 1, 0);

        hostInput = new TextField("localhost"); // clear this parameter after testing
        hostInput.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 14; -fx-border-radius: 15; -fx-border-color: linear-gradient(from 0% 0% to 100% 200%, repeat, #9bc9d4 0%, #61a2b1 50%); -fx-border-width: 2; -fx-border: gone; -fx-text-fill: #e0cc19; -fx-background-color: transparent; -fx-alignment: center; ");
        grid.add(hostInput,1,1);
        hostInput.setPromptText("Enter Host");

        portInput = new TextField(String.valueOf(33030)); // clear this parameter after testing
        portInput.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 14; -fx-border-radius: 15; -fx-border-color: linear-gradient(from 0% 0% to 100% 200%, repeat, #9bc9d4 0%, #61a2b1 50%); -fx-border-width: 2; -fx-border: gone; -fx-text-fill: #e0cc19; -fx-background-color: transparent; -fx-alignment: center; ");
        grid.add(portInput, 1, 2);
        portInput.setPromptText("Enter Port");

        usernameInput = new TextField("testuser"); // clear this parameter after testing
        usernameInput.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 14; -fx-border-radius: 15; -fx-border-color: linear-gradient(from 0% 0% to 100% 200%, repeat, #9bc9d4 0%, #61a2b1 50%); -fx-border-width: 2; -fx-border: gone; -fx-text-fill: #e0cc19; -fx-background-color: transparent; -fx-alignment: center; ");
        grid.add(usernameInput,1,3);
        usernameInput.setPromptText("Enter Username");

        // for connection errors
        Text errorMsg = new Text();
        errorMsg.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 16; -fx-fill: #c90a0a ;");
        grid.add(errorMsg,1,5);
        errorMsg.setTranslateX(70);

        Button btnQuit = new Button("Quit");
        buttonHover(btnQuit);

        Button btnConnect = new Button("Connect to NetChat");
        buttonHover(btnConnect);


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
              //      e.printStackTrace();
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
        return new Scene(grid, 800, 400);
    }

    private Scene NetChatView() {

        VBox vBox = new VBox(); // Vertikal-box to hold hbox1 and hbox2
        vBox.setPadding(new Insets(12,15,12,15));
        vBox.setSpacing(10);
        vBox.setStyle("-fx-background-color:linear-gradient(#61a2b1, #2A5058)");

        HBox hbox1 = new HBox(); // Horizontal-box to hold message input and send button
        HBox hbox2 = new HBox(); // Horizontal-box to hold chatarea and userlist
        HBox hbox3 = new HBox(); // Horizontal-box to hold headline and serverinfo
       // hbox1.setEffect(new DropShadow(  10, 0.6, 1 , Color.BLACK));
        hbox2.setEffect(new DropShadow(  10, 0.6, 1 , Color.BLACK));
        hbox1.setBackground(Background.EMPTY);
        hbox2.setBackground(Background.EMPTY);
        hbox1.setSpacing(10);
        hbox2.setSpacing(10);
        hbox3.setSpacing(15);

        ScrollPane scrollPane = new ScrollPane(); //pane to display text messages
        ScrollPane userPane = new ScrollPane();

        Text headline = new Text("Project NetChat");
        headline.setStyle("-fx-font: 35px Verdana; -fx-fill: #9bc9d4; -fx-stroke: black;-fx-text-alignment: center; -fx-stroke-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0), 10, 0.6, 1 , 1 ); ");

        // define textarea for chatmessages
        chatArea = new TextArea();
        chatArea.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 16; -fx-font-weight: 700; -fx-text-fill: #e0cc19; -fx-background-color: transparent; -fx-control-inner-background: rgba(53,89,119,0.8);");
        chatArea.setEditable(false); // makes that text in the chatArea can not be edited after display

        // sets size for scrollPane to fit chatArea
        scrollPane.setContent(chatArea);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinWidth(600);
        scrollPane.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0), 10, 0.6, 1 , 1 );");

        userList = new TextArea("Connected Users: " );
        userList.setEditable(false);
        userList.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 14; -fx-text-fill: #e0cc19; -fx-background-color: transparent; -fx-control-inner-background: rgba(53,89,119,0.9);");

        // set size for userPane to fit userlist
        userPane.setContent(userList);
        userPane.setFitToHeight(true);
        userPane.setPrefWidth(200);
        userPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        userPane.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0), 10, 0.6, 1 , 1 );");

        // define textfield for message input and styling
        msgInput = new TextField();
        msgInput.setPrefHeight(30);
        msgInput.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 14;-fx-background-radius:10; -fx-effect: dropshadow(gaussian, rgba(0,0,0), 10, 0.6, 1 , 1 ); -fx-border-radius: 13; -fx-border-color: lightgrey; -fx-border-width: 2; -fx-border: gone; -fx-text-fill: #e0cc19; -fx-background-color: linear-gradient(#61a2b1, #2A5058);");
        msgInput.setPromptText("New message....");
        msgInput.setMinWidth(600);

        // define textfield for Server status info
        serverInfo = new TextArea();
        serverInfo.setPrefHeight(35);
        serverInfo.setStyle("-fx-control-inner-background: black; -fx-effect: dropshadow(gaussian, rgba(0,0,0), 10, 0.6, 1 , 1 ); -fx-font-family: 'Quicksand'; -fx-font-size: 13; -fx-border-color: lightgrey; -fx-border-width: 2; -fx-text-fill: #00ff00; ");
        serverInfo.setEditable(false);

        // define Send-button and styling
        Button btnSend = new Button("Send");
        btnSend.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 14;-fx-font-weight: 700; -fx-border-radius: 10; -fx-background-radius:10; -fx-effect: dropshadow(gaussian, rgba(0,0,0), 10, 0.6, 1 , 1 ); -fx-border-color: lightgrey; -fx-border-width: 2; -fx-text-fill: #e0cc19; -fx-background-color: linear-gradient(#61a2b1, #2A5058); -fx-control-inner-background: #149ac9;");
        btnSend.setCursor(Cursor.HAND);
        btnSend.setPrefSize(135,30);

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
                   // Prime.setScene(connectScreen());
                    Platform.exit();
                }

                if (message.equalsIgnoreCase("/help")) {
                    chatArea.appendText("\n Commands:  \n /exit ---> leaves the Chat \n <*username*> ---> Private Message to selected username");
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

        hbox3.getChildren().addAll(headline,serverInfo);
        HBox.setHgrow(serverInfo,Priority.ALWAYS);

        //set center and bottom of the borderPane with scrollPane and hBox
        vBox.getChildren().addAll(hbox3, hbox2, hbox1);
        VBox.setVgrow(hbox2, Priority.ALWAYS);

        //create a scene and display
        Prime.setMinWidth(800);
        Prime.setMinHeight(600);
        return new Scene(vBox, 800, 600);
    }

    public void buttonHover(Button button) {
        button.setCursor(Cursor.HAND);
        button.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 14; -fx-border-radius: 15; -fx-border-color: linear-gradient(from 0% 0% to 100% 200%, repeat, #9bc9d4 0%, #61a2b1 50%); -fx-border-width: 2; -fx-text-fill: #e0cc19; -fx-background-color: transparent; ");
        button.setOnMouseEntered(mouseEvent -> {
            button.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 14; -fx-border-radius: 15; -fx-border-color: linear-gradient(from 0% 0% to 100% 200%, repeat, #00ff00 0%, #6aadbd 50%); -fx-border-width: 2; -fx-text-fill: #00ff00; -fx-background-color: transparent; ");
        });
        button.setOnMouseExited(mouseEvent -> {
            button.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 14; -fx-border-radius: 15; -fx-border-color: linear-gradient(from 0% 0% to 100% 200%, repeat, #9bc9d4 0%, #61a2b1 50%); -fx-border-width: 2; -fx-text-fill: #e0cc19; -fx-background-color: transparent; ");
        });

    }

}
