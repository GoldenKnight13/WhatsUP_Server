package com.example.whatsup_server;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ResourceBundle;

public class ServerController implements Initializable {

    //Joins the controller variables with the ServerWindow elements
    @FXML private Button start_button;
    @FXML private Button stop_button;
    @FXML private ScrollPane Log;
    @FXML private VBox logRecord;

    //Server variables
    private Server myServer;    //The server instance
    private final String serverIP = "localhost";  //The server IP address
    private static final int port = 1000;   //The port where the server is going to be running
    private Boolean isRunning = false;  //Check if the server is running


    //Function that executes when the class is created
    @Override public void initialize(URL location, ResourceBundle resources){

        startServer();  //Starts the server

        //Allows to view the last record of the server log automatically
        logRecord.heightProperty().addListener(new ChangeListener<Number>() {

            //Updates the log record value in real time
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                Log.setVvalue( (Double) t1);
            }
        });

        //Start button action
        start_button.setOnAction(actionEvent -> {
            startServer();  //Starts the server
        });

        //Stop button action
        stop_button.setOnAction(actionEvent -> {
            stopServer();
        });

    }

    //Function that allows us to successfully start the server
    public void startServer(){

        //If the server is not running
        if (!isRunning){

            //The function will try to start the server
            try {
                ServerSocket serverSocket = new ServerSocket(); //Creates a server
                serverSocket.bind(new InetSocketAddress(serverIP, port));   //The server is tied to the selected IP address
                myServer = new Server(serverSocket);    //Creates a new server

                myServer.startServer(logRecord);    //Passes the VBox of the server log
                addLog("Server started at: " + java.time.LocalDate.now() + ", " + java.time.LocalTime.now(), logRecord);    //Add to the log record when the server is started
                isRunning = !isRunning; //Lets the class know a server is actually running

            //If an error occurs, the server is not created
            } catch(IOException e){
                e.printStackTrace();
                addLog("Error creating server", logRecord); //Adds a record into the log to let us know the server could not be created
                isRunning = false;
            }

        //If the server is actually running
        } else {
            addLog("Server is already running",logRecord);  //Add a record into the log letting us know the server is actually running
        }

    }

    //Function that allows us to kill the server
    private void stopServer(){

        //If the server is running
        if (isRunning) {
            myServer.stopServer();  //Stops the server
            addLog("Server stopped at: " + java.time.LocalDate.now() + ", " + java.time.LocalTime.now(),logRecord); //Add a record that let us know the server stopped successfully
            isRunning = !isRunning; //Lets the class know the server stopped (is not actually running)
            myServer = null;    //The stopped server is deleted

        //If the server is not running
        } else {
            addLog("Server is currently stopped",logRecord);    //Add a record that lets us know the server is not running
        }
    }

    //Function that allows us to add a record into the server log
    public static void addLog(String server_log, VBox logRecord){

        //When the message is available
        Platform.runLater(() -> {

            //Creates a new element that contains the text
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.setPadding(new Insets(2, 2, 2, 5));
            Text text = new Text(server_log);
            TextFlow textFlow = new TextFlow(text);
            textFlow.setPadding(new Insets(2, 2, 2, 5));

            //Adds the record at the end of the log
            hBox.getChildren().add(textFlow);
            logRecord.getChildren().add(hBox);

        });

    }

}
