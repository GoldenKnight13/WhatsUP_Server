package com.example.whatsup_server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


//Main class: allows to run the app
public class Main extends Application {

    //Function will run when the app starts
    public void start(Stage stage) throws IOException {

        //Loads the fxml document that contains the server log window elements
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("ServerWindow.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 400);   //Creates a window of 500x400 px
        stage.setTitle("WhatsUP Server");
        stage.setScene(scene);
        stage.show();
    }

    //
    public static void main(String[] args) { launch(); }

}