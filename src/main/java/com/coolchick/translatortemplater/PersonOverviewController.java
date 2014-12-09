
package com.coolchick.translatortemplater;

import com.coolchick.translatortemplater.model.Translator;
import com.coolchick.translatortemplater.model.TranslatorDatabase;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.dialog.Dialogs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PersonOverviewController {
    private HashSet<String> mLanguages = new HashSet<String>();

    private ObservableList<Translator> translatorObservableList = FXCollections
            .observableArrayList();

    @FXML
    private TableView<Translator> personTable;

    @FXML
    private TableColumn<Translator, String> firstNameColumn;

    @FXML
    private TableColumn<Translator, String> emailColumn;

    @FXML
    private TableColumn<Translator, List<String>> languageColumn;

    @FXML
    private Label firstNameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label languagesLabel;

    @FXML
    private Button openButton;

    // Reference to the main application.
    private Main mainApp;

    /**
     * The constructor. The constructor is called before the initialize() method.
     */
    public PersonOverviewController() {
    }

    /**
     * Initializes the controller class. This method is automatically called after the fxml file has
     * been loaded.
     */
    @FXML
    private void initialize() {
        // Initialize the person table with the two columns.
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<Translator, String>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<Translator, String>("email"));
        languageColumn.setCellValueFactory(new PropertyValueFactory<Translator, List<String>>(
                "languages"));
        // Clear person details.
        showTranslatorDetails(null);
        // Listen for selection changes and show the person details when changed.
        personTable.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<Translator>() {
                    @Override
                    public void changed(ObservableValue<? extends Translator> observable,
                            Translator oldValue, Translator newValue) {
                        showTranslatorDetails(newValue);
                    }
                });
        openButton.setText("Load JSON database...");
        openButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Choose your JSON database");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("JSON file(*.json)", "*.json"));
                File file = fileChooser.showOpenDialog(mainApp.getStage());
                if (file != null) {
                    loadDatabase(file);
                }
            }
        });
    }

    /**
     * Is called by the main application to give a reference back to itself.
     * 
     * @param mainApp
     */
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Fills all text fields to show details about the person. If the specified person is null, all
     * text fields are cleared.
     * 
     * @param person the person or null
     */
    private void showTranslatorDetails(Translator person) {
        if (person != null) {
            // Fill the labels with info from the person object.
            firstNameLabel.setText(person.getName());
            emailLabel.setText(person.getEmail());
            languagesLabel.setText(person.getLanguages().toString());
        } else {
            // Translator is null, remove all the text.
            firstNameLabel.setText("");
            emailLabel.setText("");
            languagesLabel.setText(new ArrayList<String>().toString());
        }
    }

    /**
     * Called when the user clicks on the delete button.
     */
    @FXML
    private void handleDeleteTranslator() {
        int selectedIndex = personTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            personTable.getItems().remove(selectedIndex);
        } else {
            // Nothing selected.
            Dialogs.create().title("No Selection").masthead("No Translator Selected")
                    .message("Please select a person in the table.").showWarning();
        }
    }

    /**
     * Called when the user clicks the new button. Opens a dialog to edit details for a new person.
     */
    @FXML
    private void handleNewTranslator() {
        Translator tempTranslator = new Translator();
        boolean okClicked = mainApp.showTranslatorEditDialog(tempTranslator, mLanguages);
        if (okClicked) {
            translatorObservableList.add(tempTranslator);
        }
    }

    /**
     * Called when the user clicks the edit button. Opens a dialog to edit details for the selected
     * person.
     */
    @FXML
    private void handleEditTranslator() {
        Translator selectedTranslator = personTable.getSelectionModel().getSelectedItem();
        if (selectedTranslator != null) {
            boolean okClicked = mainApp.showTranslatorEditDialog(selectedTranslator, mLanguages);
            if (okClicked) {
                // FIXME force update
                translatorObservableList.remove(selectedTranslator);
                translatorObservableList.add(0, selectedTranslator);
                personTable.getSelectionModel().select(0);
            }
        } else {
            // Nothing selected.
            Dialogs.create().title("No Selection").masthead("No Translator Selected")
                    .message("Please select a person in the table.").showWarning();
        }
    }

    private void loadDatabase(File file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            TranslatorDatabase database = mapper.readValue(file, TranslatorDatabase.class);
            mLanguages.clear();
            translatorObservableList.clear();
            Set<String> names = new HashSet<String>();
            for (Translator translator : database.getTranslators()) {
                translatorObservableList.add(translator);
                names.add(translator.getName());
                for (String language : translator.getLanguages()) {
                    mLanguages.add(language);
                }
            }
            mLanguages.addAll(database.getAllLanguages());
            personTable.setItems(translatorObservableList);
            // TextFields.bindAutoCompletion(languageFilter, mLanguages);
            // TextFields.bindAutoCompletion(nameFilter, names);
            // getTranslatorsForFilter("", translatorObservableList, translatorsTarget);
        } catch (IOException e1) {
            showErrorDialog(mainApp.getStage(), "Bad translator database");
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

    private void showOkayDialog(Stage primaryStage, String text) {
        Alert dlg = new Alert(Alert.AlertType.INFORMATION);
        dlg.setTitle("OK.WEBP");
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.WINDOW_MODAL);
        dlg.getDialogPane().setContentText(text);
        dlg.show();
    }
}
