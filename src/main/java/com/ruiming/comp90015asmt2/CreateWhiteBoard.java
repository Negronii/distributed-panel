package com.ruiming.comp90015asmt2;

import com.ruiming.comp90015asmt2.Messages.CreateRequestMessage;
import com.ruiming.comp90015asmt2.Messages.MessageFactory;
import com.ruiming.comp90015asmt2.Messages.QuitMessage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.ruiming.comp90015asmt2.Messages.MessageFactory.*;

public class CreateWhiteBoard extends Application {
    public static Stage window;
    public static BufferedReader bufferedReader;
    public static BufferedWriter bufferedWriter;
    public static String username;

    static Socket socket;

    @Override
    public void start(Stage stage) throws IOException {
        WhiteBoardController.isManager = true;
        // main stage
        window = stage;
        // load fxml
        FXMLLoader fxmlLoader = new FXMLLoader(CreateWhiteBoard.class.getResource("WhiteBoardView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        window.setTitle("Distributed Shared White Board");
        window.setScene(scene);
        window.show();
    }

    public static void main(String[] args) throws UnknownHostException {
        username = "hello";
        InetAddress idxAddress = InetAddress.getByName("localhost");
        int idxPort = 3201;
        try {
            socket = new Socket(idxAddress, idxPort);
            // set up reader and writer for IO stream
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

            writeMsg(bufferedWriter, new CreateRequestMessage(username, WhiteBoardController.date.getTime()));


        } catch (IOException e) {
            e.printStackTrace();
        }

        launch();
    }

    @Override
    public void stop() throws IOException {
        writeMsg(bufferedWriter, new QuitMessage(username, WhiteBoardController.date.getTime()));
        socket.close();
    }
}