package sample;

import com.coolchick.translatortemplater.model.Translator;
import com.coolchick.translatortemplater.model.TranslatorDatabase;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.control.textfield.TextFields;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    private ArrayList<Translator> mTranslators;

    @Override
    public void start(final Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setResources(ResourceBundle.getBundle("sample.theResources"));
        StackPane pane = fxmlLoader.load(this.getClass().getResource("theScene.fxml").openStream());
        mTranslators = new ArrayList<Translator>();
        final ObservableList<Translator> translators =
                FXCollections.observableArrayList();
        final ObservableList<Translator> translatorsTarget =
                FXCollections.observableArrayList();
        final ObservableList<Node> children = ((VBox) fxmlLoader.getNamespace().get("VBox")).getChildren();

        GridPane filterGrid = new GridPane();
        filterGrid.setVgap(10);
        filterGrid.setHgap(10);
        filterGrid.setPadding(new Insets(30, 30, 0, 30));
        final TextField textField = new TextField();
        filterGrid.add(new Label("Language filter"), 0, 0);
        filterGrid.add(textField, 1, 0);
        textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case ENTER:
                        getTranslatorsForLanguage(textField, translators, translatorsTarget);
                        break;
                    default:
                        break;
                }
            }
        });
        GridPane.setHgrow(textField, Priority.ALWAYS);

        final ListSelectionView<Translator> listSelectionView = new ListSelectionView<Translator>();
        listSelectionView.setSourceItems(translators);
        listSelectionView.setTargetItems(translatorsTarget);

        final FileChooser fileChooser = new FileChooser();

        final javafx.scene.control.Button openButton = new javafx.scene.control.Button("Load JSON database...");
        openButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = fileChooser.showOpenDialog(primaryStage);
                        if (file != null) {
                            ObjectMapper mapper = new ObjectMapper();
                            try {
                                TranslatorDatabase database = mapper.readValue(file, TranslatorDatabase.class);
                                HashSet<String> languages = new HashSet<String>();
                                for (Translator translator : database.getTranslators()) {
                                    mTranslators.add(translator);
                                    for (String language : translator.getLanguages()) {
                                        languages.add(language);
                                    }
                                }
                                TextFields.bindAutoCompletion(textField, languages);
                                getTranslatorsForLanguage(textField, translators, translatorsTarget);
                            } catch (IOException e1) {
                                showErrorDialog(primaryStage, "Bad translator database");
                            }
                        }
                    }
                });
        children.add(
                openButton
        );
        children.add(filterGrid);
        children.add(
                listSelectionView
        );
        primaryStage.setTitle("Hello World");
        final Scene scene = new Scene(pane);
        scene.getStylesheets().setAll(
                getClass().getResource("theStyles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    private void getTranslatorsForLanguage(TextField textField, ObservableList<Translator> translators, ObservableList<Translator> translatorsTarget) {
        translators.clear();
        for (Translator translator : mTranslators) {
            if (textField.getText() == null || textField.getText().equalsIgnoreCase("") || (translator.getLanguages().contains(textField.getText()) && !translatorsTarget.contains(translator))) {
                translators.add(translator);
            }
        }
    }

    private void showErrorDialog(Stage primaryStage, String text) {
        Alert dlg = new Alert(Alert.AlertType.ERROR);
        dlg.setTitle("NOPE.JPG");
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.WINDOW_MODAL);
        dlg.getDialogPane().setContentText(text);
        dlg.show();
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
