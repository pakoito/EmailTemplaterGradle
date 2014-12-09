
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
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.dialog.Dialogs;

import java.io.*;
import java.util.*;

public class PersonOverviewController {
    private HashSet<String> mLanguages = new HashSet<String>();

    private ArrayList<Translator> mTranslators = new ArrayList<Translator>();

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

    @FXML
    public Button returnButton;

    // Reference to the main application.
    private Main mainApp;

    @FXML
    private GridPane filterGrid;

    private TextField languageFilter;

    private TextField nameFilter;

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
        returnButton.setText("<== Return to email spitter");
        returnButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
				if (mTranslators.size() > 0) {
					showExitDialog();
				} else {
					mainApp.showMailSpitter();
				}
            }
        });
        final javafx.scene.control.Button spitButton = new javafx.scene.control.Button(
                "Save database...");
        filterGrid.setPadding(new Insets(10, 10, 10, 10));
        filterGrid.setVgap(10);
        filterGrid.setHgap(10);
        languageFilter = new TextField();
        nameFilter = new TextField();
        filterGrid.add(new Label("Name filter"), 0, 0);
        filterGrid.add(nameFilter, 1, 0);
        filterGrid.add(new Label("Language filter"), 0, 1);
        filterGrid.add(languageFilter, 1, 1);
        filterGrid.add(spitButton, 4, 1);
        spitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                spitDatabase();
            }
        });
        languageFilter.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case ENTER:
                        nameFilter.setText("");
                        getTranslatorsForFilter(languageFilter.getText());
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
                        languageFilter.setText("");
                        getTranslatorsForName(nameFilter.getText());
                        break;
                    default:
                        break;
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
            mTranslators.clear();
            translatorObservableList.clear();
            Set<String> names = new HashSet<String>();
            for (Translator translator : database.getTranslators()) {
                mTranslators.add(translator);
                names.add(translator.getName());
                for (String language : translator.getLanguages()) {
                    mLanguages.add(language);
                }
            }
            mLanguages.addAll(database.getAllLanguages());
            personTable.setItems(translatorObservableList);
            translatorObservableList.addAll(mTranslators);
            languageFilter.setText("");
            nameFilter.setText("");
            TextFields.bindAutoCompletion(languageFilter, mLanguages);
            TextFields.bindAutoCompletion(nameFilter, names);
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

    private void showExitDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Leaving database editor!");
        alert.setHeaderText("Do you want to save before leaving?");
        alert.setContentText("Choose your option.");
        ButtonType buttonTypeOne = new ButtonType("Save and leave");
        ButtonType buttonTypeTwo = new ButtonType("Leave without saving");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne) {
            spitDatabase();
            mainApp.showMailSpitter();
        } else if (result.get() == buttonTypeTwo) {
            mainApp.showMailSpitter();
        } else {
            // ... user chose CANCEL or closed the dialog
        }
    }

    private void getTranslatorsForName(String text) {
        translatorObservableList.clear();
        for (Translator translator : mTranslators) {
            if (text == null
                    || text.equalsIgnoreCase("")
                    || (translator.getName().replace(" ", "").toLowerCase().contains(text.replace(
                            " ", "").toLowerCase()))) {
                translatorObservableList.add(translator);
            }
        }
        translatorObservableList.sort(new Comparator<Translator>() {
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

    private void getTranslatorsForFilter(String textField) {
        translatorObservableList.clear();
        for (Translator translator : mTranslators) {
            if (textField == null || textField.equalsIgnoreCase("")
                    || (translator.getLanguages().contains(textField))) {
                translatorObservableList.add(translator);
            }
        }
        translatorObservableList.sort(new Comparator<Translator>() {
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

    private void spitDatabase() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose destination");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON file(*.json)", "*.json"));
        File file = fileChooser.showSaveDialog(mainApp.getStage());
        if (file != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Set<String> languages = new HashSet<String>();
                languages.addAll(mLanguages);
                for (Translator translator : translatorObservableList) {
                    for (String language : translator.getLanguages()) {
                        languages.add(language);
                    }
                }
                TranslatorDatabase database = new TranslatorDatabase().withAllLanguages(
                        new ArrayList<String>(languages)).withTranslators(translatorObservableList);
                String databaseSerialized = mapper.writeValueAsString(database);
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(databaseSerialized);
                bw.close();
                loadDatabase(file);
				showInformation("Database save", "Saved correctly!");
            } catch (FileNotFoundException e1) {
                showErrorDialog(mainApp.getStage(), "Failed to open file\n" + e1);
            } catch (IOException e1) {
                showErrorDialog(mainApp.getStage(), "File type unknown, please open it externally");
            }
        }
    }

	private void showInformation(String title, String text) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(text);

		alert.showAndWait();
	}
}
