package com.ruiming.comp90015asmt2;

import com.ruiming.comp90015asmt2.Messages.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;

import static com.ruiming.comp90015asmt2.Messages.MessageFactory.*;
import static com.ruiming.comp90015asmt2.Messages.MessageFactory.writeMsg;

public class WhiteBoardController implements Initializable {
    @FXML
    public TextField textInput;

    @FXML
    public Label chatRoomLabel;

    @FXML
    public ListView<String> usersInBoard;

    @FXML
    private Slider slider;

    @FXML
    public Canvas canvas;

    @FXML
    private ComboBox<String> tool;

    @FXML
    private ColorPicker colorPicker;

    private int triangleCount = 0;
    private final double[] triangleXs = new double[3];
    private final double[] triangleYs = new double[3];

    boolean newPoint = true;
    private final double[] point = new double[2];

    public static Date date = new Date();

    BufferedWriter bufferedWriter;
    String username;


    public static boolean isManager = false;

    Stage window;

    @FXML
    public void onSavePNG() {
        try {
            Image snapShot = canvas.snapshot(null, null);
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose location to save");
            File choseDirectory = directoryChooser.showDialog(window);
            if (choseDirectory == null) return;
            ImageIO.write(SwingFXUtils.fromFXImage(snapShot, null),
                    "png", new File(choseDirectory.getPath() + "/paint.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void onExit() {
        Platform.exit();
    }

    @FXML
    public void onOpen() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files",
                "*.png", "*.jpg"));
        File selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile == null) return;
        Image img = SwingFXUtils.toFXImage(ImageIO.read(selectedFile), null);
        canvas.getGraphicsContext2D().drawImage(img, 0, 0);
        writeMsg(bufferedWriter,new ImageMessage(username,date.getTime(),img));
    }

    @FXML
    public void onNew() throws IOException {
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        writeMsg(bufferedWriter, new ClearPanelMessage(username, date.getTime()));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        bufferedWriter = (isManager) ? CreateWhiteBoard.bufferedWriter : JoinWhiteBoard.bufferedWriter;
        username = (isManager) ? CreateWhiteBoard.username : JoinWhiteBoard.username;
        window = (isManager) ? CreateWhiteBoard.window : JoinWhiteBoard.window;
        tool.getItems().addAll("Free-hand", "Eraser", "Line", "Circle", "Triangle", "Rectangle", "Text");
        textInput.setVisible(false);
        tool.getSelectionModel().selectedIndexProperty().addListener((v, oldValue, newValue) -> {
            newPoint = true;
            Arrays.fill(point, 0);
            triangleCount = 0;
            if (oldValue.equals(6)) {
                textInput.setVisible(false);
            }
            if (newValue.equals(6)) {
                textInput.setVisible(true);
            }
        });

        window.setOnCloseRequest(e -> {
            onExit();
        });

        canvas.setOnMouseDragged(e -> {
            if (tool.getValue() == null) {
                showAlert("No Tool Alert", "Please enter a tool");
            } else {
                double brushSize = slider.getValue();
                double x = e.getX() - brushSize / 2;
                double y = e.getY() - brushSize / 2;
                if (tool.getValue().equals("Free-hand")) {
                    if (newPoint)
                        newPoint = false;
                    else {
                        Color color = colorPicker.getValue();
                        double size = slider.getValue();
                        writeMsg(bufferedWriter, new DrawLineMessage(username, date.getTime(), point[0],
                                point[1], x, y, size, color));
                    }
                    point[0] = x;
                    point[1] = y;
                } else if (tool.getValue().equals("Eraser")) {
                    writeMsg(bufferedWriter, new EraseMessage(username, date.getTime(), x, y, brushSize));
                }
            }
        });

        canvas.setOnMousePressed(e -> {
            point[0] = e.getX();
            point[1] = e.getY();
        });

        canvas.setOnMouseReleased(e -> {
            if (tool.getValue() == null) {
                showAlert("No Tool Alert", "Please enter a tool");
            } else {
                double x = e.getX();
                double y = e.getY();
                if (tool.getValue().equals("Rectangle") || tool.getValue().equals("Circle")) {
                    double width = Math.abs(x - point[0]);
                    double height = Math.abs(y - point[1]);
                    double startX = Math.min(point[0], x);
                    double startY = Math.min(point[1], y);
                    Color color = colorPicker.getValue();
                    if (tool.getValue().equals("Rectangle")) {
                        writeMsg(bufferedWriter, new DrawRectMessage(username, date.getTime(),
                                startX, startY, width, height, color));
                    }
                    if (tool.getValue().equals("Circle")) {
                        writeMsg(bufferedWriter, new DrawCircleMessage(username, date.getTime(),
                                startX, startY, width, height, color));
                    }

                } else if (tool.getValue().equals("Line")) {
                    Color color = colorPicker.getValue();
                    double size = slider.getValue();
                    writeMsg(bufferedWriter, new DrawLineMessage(username, date.getTime(), point[0],
                            point[1], x, y, size, color));
                } else if (tool.getValue().equals("Triangle")) {
                    triangleXs[triangleCount] = x;
                    triangleYs[triangleCount] = y;
                    if (++triangleCount == 3) {
                        writeMsg(bufferedWriter, new DrawTriangleMessage(username, date.getTime(),
                                triangleXs, triangleYs, colorPicker.getValue()));
                        triangleCount = 0;
                    }
                } else if (tool.getValue().equals("Text")) {
                    if (textInput.getText().equals("")) {
                        showAlert("No text entered", "Please enter text field besides slider");
                    } else {
                        writeMsg(bufferedWriter, new DrawTextMessage(username, date.getTime(), x, y,
                                textInput.getText(), colorPicker.getValue(), slider.getValue()));
                    }
                } else if (tool.getValue().equals("Free-hand")) {
                    Arrays.fill(point, 0);
                    newPoint = true;
                }
            }
        });
    }

    public void showAlert(String title, String message) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL); //make user deal with alert first
        window.setTitle(title);
        window.setWidth(250);
        Label label = new Label();
        label.setText(message);
        Button closeButton = new Button();
        closeButton.setText("Got it");
        closeButton.setOnAction(e -> window.close()); //window.close() to close the stage
        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, closeButton);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait(); // wait until user close stage
    }






}