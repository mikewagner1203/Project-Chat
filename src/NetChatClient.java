import javafx.application.Application;
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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    List<String> connectedUsers = new ArrayList<String>();

    @Override
    public void start(Stage primaryStage) {
        Prime = primaryStage;
       // primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("Project NetChat");
        primaryStage.setScene(connectScreen());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Scene connectScreen() {
        GridPane grid = new GridPane();
        grid.setStyle("-fx-background-color:linear-gradient(#61a2b1, #2A5058)");
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

        TextField portInput = new TextField(String.valueOf(33030)); // clear this parameter after testing
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
        btnQuit.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 14; -fx-border-radius: 15; -fx-border-color: linear-gradient(from 0% 0% to 100% 200%, repeat, #9bc9d4 0%, #61a2b1 50%); -fx-border-width: 2; -fx-text-fill: #e0cc19; -fx-background-color: transparent; ");
        btnQuit.setOnAction(actionEvent ->{
            System.exit(0);
        });

        Button btnConnect = new Button("Connect to NetChat");
        btnConnect.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 14; -fx-border-radius: 15; -fx-border-color: linear-gradient(from 0% 0% to 100% 200%, repeat, #9bc9d4 0%, #61a2b1 50%); -fx-border-width: 2; -fx-text-fill: #e0cc19; -fx-background-color: transparent; ");
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

        ButtonBar btnbar = new ButtonBar();
        btnbar.getButtons().addAll(btnConnect,btnQuit);
      //  btnbar.translateXProperty().setValue(-30);
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
        vBox.setPadding(new Insets(12,12,12,12));
        vBox.setSpacing(10);
        vBox.setStyle("-fx-background-color:#549bab;");

        HBox hbox1 = new HBox(); // Horizontal-box to hold message input and send button
        HBox hbox2 = new HBox(); // Horizontal-box to hold chatarea and userlist
        HBox hbox3 = new HBox();
        hbox1.setEffect(new DropShadow(  10, 0.6, 1 , Color.BLACK));
        hbox2.setEffect(new DropShadow(  10, 0.6, 1 , Color.BLACK));
        hbox1.setBackground(Background.EMPTY);
        hbox2.setBackground(Background.EMPTY);
        hbox1.setSpacing(5);
        hbox2.setSpacing(5);
        hbox3.setSpacing(15);

        ScrollPane scrollPane = new ScrollPane(); //pane to display text messages
        ScrollPane userPane = new ScrollPane();

        Text headline = new Text("Project NetChat");
        headline.setStyle("-fx-font: 35px Verdana; -fx-fill: #9bc9d4; -fx-stroke: black;-fx-text-alignment: center; -fx-stroke-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0), 10, 0.6, 1 , 1 ); ");
        // define textarea for chatmessages and properties for responsive size
        chatArea = new TextArea();
     //   chatArea.setFont(Font.font("Verdana", FontWeight.BOLD, 12));

        chatArea.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 15; -fx-border-radius: 10; -fx-font-weight: 700; -fx-border-color: lightgrey; -fx-border-width: 1; -fx-border: gone; -fx-text-fill: #000000; -fx-background-color: transparent; -fx-control-inner-background: #549bab;");
        //  chatArea.setStyle("-fx-control-inner-background:#61a2b1; -fx-text-fill: #000000;");
        chatArea.setEditable(false); // makes that text in the chatArea can not be edited after display

        // sets size for scrollPane to fit chatArea
        scrollPane.setContent(chatArea);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinWidth(600);

        userList = new TextArea("Connected Users: " );
        userList.setEditable(false);
       // userList.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        userList.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 14; -fx-border-radius: 5; -fx-border-color: lightgrey; -fx-border-width: 0; -fx-border: gone; -fx-text-fill: #000000; -fx-background-color: transparent; -fx-control-inner-background: #549bab; ");

        // set size for userPane to fit userlist
        userPane.setContent(userList);
        userPane.setFitToHeight(true);
        userPane.setPrefWidth(200);
       // userPane.setFitToWidth(true);
        userPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // define textfield for message input and styling
        msgInput = new TextField();
        msgInput.setPrefHeight(30);
      //  msgInput.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        msgInput.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 14; -fx-border-radius: 10; -fx-border-color: lightgrey; -fx-border-width: 2; -fx-border: gone; -fx-text-fill: #e0cc19; -fx-background-color: transparent; ");
        msgInput.setPromptText("New message....");

        // define textfield for Server status info
        serverInfo = new TextArea();
        serverInfo.setPrefHeight(35);
       // serverInfo.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
        serverInfo.setStyle("-fx-control-inner-background: black; -fx-font-family: 'Quicksand'; -fx-font-size: 13; -fx-border-color: lightgrey; -fx-border-width: 2; -fx-text-fill: #00ff00; ");
        serverInfo.setEditable(false);

        // define Send-button and styling
        Button btnSend = new Button("Send");
       // btnSend.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        btnSend.setStyle("-fx-font-family: 'Quicksand'; -fx-font-size: 14;-fx-font-weight: 700; -fx-border-radius: 10; -fx-border-color: lightgrey; -fx-border-width: 2; -fx-text-fill: #e0cc19; -fx-background-color: transparent; -fx-control-inner-background: #149ac9;");
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

                //if message is empty, just return : don't send the message
                if (message.length() == 0) {
                    return;
                }

                if (message.equalsIgnoreCase("/exit")) {
                    Prime.setScene(connectScreen());
                   // System.exit(0);
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

}
