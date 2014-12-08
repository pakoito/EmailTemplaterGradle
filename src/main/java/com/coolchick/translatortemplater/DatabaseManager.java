
package com.coolchick.translatortemplater;

import com.coolchick.translatortemplater.model.Translator;
import com.coolchick.translatortemplater.model.TranslatorDatabase;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.TextFields;

import java.io.*;
import java.util.*;

/**
 * Created by Paco on 08/12/2014. See LICENSE.md
 */
// TODO FIXME class -.-
public class DatabaseManager {
    private ArrayList<Translator> mTranslators;

    private HashSet<String> mLanguages;

    private ObservableList<Translator> translatorObservableList;

    private ObservableList<Translator> translatorsTarget;

    private ObservableList<String> languagesObservableList;

    private ObservableList<String> languagesTarget;

    private TextField emailField;

    public Parent getRoot(final Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setResources(ResourceBundle
                .getBundle("com.coolchick.translatortemplater.theResources"));
        StackPane pane = fxmlLoader.load(DatabaseManager.class.getResource("theScene.fxml")
                .openStream());
        pane.setPadding(new Insets(10, 10, 10, 10));
        mTranslators = new ArrayList<Translator>();
        mLanguages = new HashSet<String>();
        translatorObservableList = FXCollections.observableArrayList();
        translatorsTarget = FXCollections.observableArrayList();
        languagesObservableList = FXCollections.observableArrayList();
        languagesTarget = FXCollections.observableArrayList();
        final ObservableList<Node> children = ((VBox)fxmlLoader.getNamespace().get("VBox"))
                .getChildren();
        GridPane filterGrid = new GridPane();
        filterGrid.setVgap(10);
        filterGrid.setHgap(10);
        filterGrid.setPadding(new Insets(30, 60, 0, 30));
        final TextField languageFilter = new TextField();
        final TextField nameFilter = new TextField();
        filterGrid.add(new Label("Name filter"), 0, 0);
        filterGrid.add(nameFilter, 1, 0);
        filterGrid.add(new Label("Language filter"), 0, 1);
        filterGrid.add(languageFilter, 1, 1);
        languageFilter.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case ENTER:
                        getTranslatorsForFilter(languageFilter.getText(), translatorObservableList,
                                translatorsTarget);
                        break;
                    default:
                        break;
                }
            }
        });
        nameFilter.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case ENTER:
                        getTranslatorsForName(nameFilter.getText(), translatorObservableList);
                        break;
                    default:
                        break;
                }
            }
        });
        TableView<Translator> table = new TableView<Translator>();
        TableColumn firstNameCol = new TableColumn("Name");
        firstNameCol.setMinWidth(100);
        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<Translator, String>("name"));
        TableColumn secondEmailCol = new TableColumn("Email");
        secondEmailCol.setMinWidth(100);
        secondEmailCol.setCellValueFactory(
                new PropertyValueFactory<Translator, String>("email"));
        TableColumn thirdLangCol = new TableColumn("Languages");
        thirdLangCol.setMinWidth(100);
        thirdLangCol.setCellValueFactory(
                new PropertyValueFactory<Translator, List<String>>("languages"));
        table.setItems(translatorObservableList);
        table.getColumns().addAll(firstNameCol, secondEmailCol, thirdLangCol);
//        final ListSelectionView<Translator> listSelectionView = new ListSelectionView<Translator>();
//        listSelectionView.setSourceItems(translatorObservableList);
//        listSelectionView.setTargetItems(translatorsTarget);
//        final ListSelectionView<String> languagesSelection = new ListSelectionView<String>();
//        languagesSelection.setSourceItems(languagesObservableList);
//        languagesSelection.setTargetItems(languagesTarget);
        final javafx.scene.control.Button openButton = new javafx.scene.control.Button(
                "Load JSON database...");
        openButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Choose your JSON database");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("JSON file(*.json)", "*.json"));
                File file = fileChooser.showOpenDialog(stage);
                if (file != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        Set<String> names = new HashSet<String>();
                        TranslatorDatabase database = mapper.readValue(file,
                                TranslatorDatabase.class);
                        for (Translator translator : database.getTranslators()) {
                            mTranslators.add(translator);
                            names.add(translator.getName());
                            for (String language : translator.getLanguages()) {
                                mLanguages.add(language);
                            }
                        }
                        mLanguages.addAll(database.getAllLanguages());
                        TextFields.bindAutoCompletion(languageFilter, mLanguages);
                        TextFields.bindAutoCompletion(nameFilter, names);
                        getTranslatorsForFilter("", translatorObservableList,
                                translatorsTarget);
                    } catch (IOException e1) {
                        showErrorDialog(stage, "Bad translator database");
                    }
                }
            }
        });
        GridPane emailGrid = new GridPane();
        emailGrid.setVgap(10);
        emailGrid.setHgap(10);
        emailField = new TextField();
        emailField.setPadding(new Insets(5, 200, 5, 0));
        emailGrid.add(new Label("From Email"), 0, 0);
        emailGrid.add(emailField, 1, 0);
        final javafx.scene.control.Button spitButton = new javafx.scene.control.Button(
                "Spit database...");
        spitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Choose destination");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("JSON file(*.json)", "*.json"));
                File file = fileChooser.showSaveDialog(stage);
                if (file != null) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        TranslatorDatabase database = new TranslatorDatabase().withAllLanguages(
                                new ArrayList<String>(mLanguages)).withTranslators(
                                translatorObservableList);
                        String databaseSerialized = mapper.writeValueAsString(database);
                        FileWriter fw = new FileWriter(file.getAbsoluteFile());
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(databaseSerialized);
                        bw.close();
                        showOkayDialog(stage, "Saved as " + file.getAbsolutePath());
                    } catch (FileNotFoundException e1) {
                        showErrorDialog(stage, "Failed to open file\n" + e1);
                    } catch (IOException e1) {
                        showErrorDialog(stage, "File type unknown, please open it externally");
                    }
                }
            }
        });
        children.add(openButton);
        children.add(filterGrid);
        children.add(table);
        children.add(spitButton);
        return pane;
    }

    private void getTranslatorsForName(String text,
                                         ObservableList<Translator> translators) {
        translators.clear();
        for (Translator translator : mTranslators) {
            if (text == null
                    || text.equalsIgnoreCase("")
                    || (translator.getName().toLowerCase().contains(text.toLowerCase()))) {
                translators.add(translator);
            }
        }
        translators.sort(new Comparator<Translator>() {
            @Override
            public int compare(Translator o1, Translator o2) {
                int compare = o1.getName().compareToIgnoreCase(o2.getName());
                if (compare < 0) {
                    return -1;
                } else if (compare > 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    private void getTranslatorsForFilter(String textField,
            ObservableList<Translator> translators, ObservableList<Translator> translatorsTarget) {
        translators.clear();
        for (Translator translator : mTranslators) {
            if (textField == null
                    || textField.equalsIgnoreCase("")
                    || (translator.getLanguages().contains(textField) && !translatorsTarget
                            .contains(translator))) {
                translators.add(translator);
            }
        }
        translators.sort(new Comparator<Translator>() {
            @Override
            public int compare(Translator o1, Translator o2) {
                int compare = o1.getName().compareToIgnoreCase(o2.getName());
                if (compare < 0) {
                    return -1;
                } else if (compare > 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    private void geLanguageForFilter(TextField textField, ObservableList<String> languages,
            ObservableList<String> languagesTarget) {
        languages.clear();
        for (String language : mLanguages) {
            if (textField.getText() == null
                    || textField.getText().equalsIgnoreCase("")
                    || (textField.getText().equalsIgnoreCase(language) && !languagesTarget
                            .contains(language))) {
                languages.add(language);
            }
        }
        languages.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int compare = o1.compareToIgnoreCase(o2);
                if (compare < 0) {
                    return -1;
                } else if (compare > 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    private void showErrorDialog(Stage primaryStage, String text) {
        Alert dlg = new Alert(Alert.AlertType.ERROR);
        dlg.setTitle("NOPE.JPG");
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.WINDOW_MODAL);
        dlg.getDialogPane().setContentText(text);
        dlg.show();
    }

    private void showOkayDialog(Stage primaryStage, String text) {
        Alert dlg = new Alert(Alert.AlertType.INFORMATION);
        dlg.setTitle("OK.WEBP");
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.WINDOW_MODAL);
        dlg.getDialogPane().setContentText(text);
        dlg.show();
    }
}
