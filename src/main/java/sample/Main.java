package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setResources(ResourceBundle.getBundle("sample.theResources"));
        StackPane pane = fxmlLoader.load(this.getClass().getResource("theScene.fxml").openStream());
        final ObservableList<Node> children = ((VBox) fxmlLoader.getNamespace().get("VBox")).getChildren();
        ObservableList<String> options =
                FXCollections.observableArrayList(
                        "Option 1",
                        "Option 2",
                        "Option 3"
                );
        children.add(
                new ComboBox<String>(options)
        );
        final FileChooser fileChooser = new FileChooser();

        final Button openButton = new Button("Open a Picture...");
        final Button openMultipleButton = new Button("Open Pictures...");

        openButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = fileChooser.showOpenDialog(primaryStage);
                        if (file != null) {
                            openFile(file);
                        }
                    }
                });

        openMultipleButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        List<File> list =
                                fileChooser.showOpenMultipleDialog(primaryStage);
                        if (list != null) {
                            for (File file : list) {
                                openFile(file);
                            }
                        }
                    }
                });
        children.add(
                openButton
        );
        children.add(
                openMultipleButton
        );
        primaryStage.setTitle("Hello World");
        final Scene scene = new Scene(pane);
        scene.getStylesheets().setAll(
                getClass().getResource("theStyles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    private void openFile(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            Logger.getLogger(
                    getClass().getName()).log(
                    Level.SEVERE, null, ex
            );
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
