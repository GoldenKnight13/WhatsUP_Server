package com.example.whatsup_server;


import javafx.scene.layout.VBox;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

//What's UP server class
public class Server{

    private final ServerSocket serverSocket;    //Server
    private Thread server;  //Allows to run the server in a separate thread, allowing to keep track of the log without interrupting the server
    private VBox logRecord; //Connects the server with the VBox of the ServerWindow
    private ArrayList<ClientHandler> clients = new ArrayList<>();   //Array containing all the connected clients


    //Constructor: Creates a new server
    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    //Starts the server; Receives the VBox of the ServerWindow
    public void startServer(VBox logRecord){


        this.logRecord = logRecord;
        server = new Thread( () -> {

            //While the server is still running, it keeps this function working
            while (!serverSocket.isClosed()){

                try {

                    Socket socket = serverSocket.accept();  //Accept the connection of a new client
                    ServerController.addLog("Client " + socket.getInetAddress() + ":" + socket.getPort() + " connected at " + java.time.LocalDate.now() + " " + java.time.LocalTime.now(),
                            logRecord); //Prints in the server log form where and when the new client connected
                    ClientHandler client = new ClientHandler(socket);   //Creates a client handler for the new client
                    Thread thread = new Thread( client );   //Creates a separate thread for the client
                    thread.start(); //Start the new client

                } catch (IOException e){
                    if (!serverSocket.isClosed()){
                        ServerController.addLog("Error connecting new client", logRecord);  //Does not connect the client
                    }
                    break;
                }
            }

        });
        server.start();

    }

    //Stops the server
    public void stopServer (){
        try {
            if (serverSocket != null) { serverSocket.close(); } //Closes the server socket
            if( server.isAlive() ){ server.interrupt(); }   //Closes the server
        } catch (IOException e){
            e.printStackTrace();
        }
    }



    //Class that helps us to manage every client individually
    private class ClientHandler implements Runnable{


        private Socket socket;  //The client connection to the server
        public BufferedReader bufferedReader;   //Allows the server to send messages to the client
        public BufferedWriter bufferedWriter;   //Allows the server to receive messages to the client
        public String clientName;   //The client username


        //The constructor: receives the connection from the server
        public ClientHandler(Socket socket){

            try {
                this.socket = socket;
                bufferedReader = new BufferedReader( new InputStreamReader( socket.getInputStream()));  //Creates the data input
                bufferedWriter = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream()));    //Creates the data output
                this.clientName = bufferedReader.readLine();    //Receives the client username
                clients.add(this);  //Adds the client to the connected clients list
                showClients();  //Lets the other clients know a new client arrived

            } catch (IOException e) {
                //If an error occurs, the client is stopped
                stopClient(socket, bufferedReader, bufferedWriter);
                e.printStackTrace();
            }
        }

        //Function that runs after the clientHandler is created
        @Override public void run(){

            listenToMessages(); //Allows the server to receive messages for the client

        }


        //Function that kills the client connection
        public void stopClient (Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){

            //Adds to the server log when the client disconnected
            ServerController.addLog("Client " + socket.getInetAddress() + ":" + socket.getPort() + " has disconnected at "+ java.time.LocalDateTime.now(), logRecord);
            clients.remove(this); //Removes the client from the online client list
            showClients();  //Les the other clients know the client disconnected

            try{

                if( socket.isConnected() ){ socket.close(); }   //Kills the client connection
                if( bufferedReader != null ){ bufferedReader.close(); } //Interrupts the data input
                if( bufferedWriter != null ){ bufferedWriter.close(); } //Interrupts the data output

            } catch (IOException e){
                //If an error occurs it le
                ServerController.addLog("Error disconnecting client: " + socket.getInetAddress() + ":" + socket.getPort(), logRecord);
                e.printStackTrace();
            }
        }

        //Function that lets all the clients know who is online
        public void showClients(){

            //If there is more than one client online the function executes
            if (clients.size() > 1){

                //Iterates through the client list
                for(ClientHandler clientHandler: clients){

                    String address = clientHandler.getCompleteAddress(clientHandler);    //Obtains the actual client IP address
                    int counter = 1;   //Let us control the star and ending transmission message

                    //Iterates through the client list
                    for(ClientHandler client: clients){
                        String clientAddress = client.getCompleteAddress(client); //Gets the second client complete address

                        //If the actual client and the second client are not the same
                        if(!clientAddress.equals(address)){
                            try {

                                //If this is the first client to be sent, the server sends a message to let the client know the client list is about to be sent
                                if (counter == 1) {
                                    clientHandler.bufferedWriter.write("200");
                                    clientHandler.bufferedWriter.newLine();
                                    clientHandler.bufferedWriter.flush();
                                }

                                //Sends the client address and username
                                clientHandler.bufferedWriter.write(clientAddress + ";" + client.clientName);
                                clientHandler.bufferedWriter.newLine();
                                clientHandler.bufferedWriter.flush();
                                counter++;

                                //If the second client id the last one to be transmitted, the sever send a code to let the client know the transmission is ending
                                if(counter == clients.size()){
                                    clientHandler.bufferedWriter.write("201");
                                    clientHandler.bufferedWriter.newLine();
                                    clientHandler.bufferedWriter.flush();
                                }

                            //If an error occurs, the server will print a warning
                            } catch (IOException e){ System.out.println("Error sending address"); }
                        }


                    }


                }
            }
        }

        //Allows the server to receive messages from the client
        public void listenToMessages(){

            //The listener is running on a separate thread in order to not interrupt the sending process
            Thread listener = new Thread( ()-> {

                //While the server is still running
                while(socket.isConnected()){
                    try {
                        //The server will try to read the incoming messages form the client
                        String message = bufferedReader.readLine();

                        //If a message is sent by the client by error, the client connection is killed
                        if(message == null){
                            stopClient(socket, bufferedReader, bufferedWriter);
                            break;
                        } else {

                            //The message is split into several parts, being ';' the identifier of every part
                            String[] redirectedMessage = message.split(";", 2);
                            System.out.println(redirectedMessage);

                            //The server searches the destination in the client list
                            for (ClientHandler client: clients){
                                if (client.getCompleteAddress(client).equals(redirectedMessage[0])){

                                    //Redirects the message to the correct destination
                                    client.bufferedWriter.write(redirectedMessage[1]);
                                    client.bufferedWriter.newLine();
                                    client.bufferedWriter.flush();
                                }
                            }

                        }

                    //If an error occurs, the client its killed
                    } catch (IOException e){
                        stopClient(socket, bufferedReader, bufferedWriter);
                        break;
                    }
                }
            });
            listener.start();   //Starts the listener
        }

        //Allows to know the client complete IP address
        public String getCompleteAddress(ClientHandler clientHandler){
            return clientHandler.socket.getInetAddress() + ":" + clientHandler.socket.getPort(); //Returns the client IP address plus the port it is connecting from
        }

    }



}
