
package com.coolchick.translatortemplater;

import com.coolchick.translatortemplater.model.Translator;
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
import org.controlsfx.dialog.Dialogs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PersonOverviewController {
    private List<Translator> mTranslators;

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

    // @FXML
    // private Button openButton;
    @FXML
    public Button returnButton;

    // Reference to the main application.
    private Main main;

    @FXML
    private GridPane filterGrid;

    @FXML
    public Button editLanguages;

    private TextField languageFilter;

    private TextField nameFilter;

    public void setMain(Main main) {
        this.main = main;
    }

    public void setTranslators(List<Translator> translators) {
        mTranslators = new ArrayList<Translator>(translators);
        getTranslatorsForName("");
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
        personTable.setItems(translatorObservableList);
        returnButton.setText("<== Return to email spitter");
        returnButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                showExitDialog();
            }
        });
        editLanguages.setText("Edit languages");
        editLanguages.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                main.showLanguageOverview();
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
                updateAndSaveDb();
            }
        });
        languageFilter.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case ENTER:
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
                        getTranslatorsForName(nameFilter.getText());
                        break;
                    default:
                        break;
                }
            }
        });
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
            mTranslators.remove(personTable.getItems().remove(selectedIndex));
            getTranslatorsForFilter("");
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
        boolean okClicked = main.showTranslatorEditDialog(tempTranslator);
        if (okClicked) {
            mTranslators.add(tempTranslator);
            getTranslatorsForFilter("");
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
            boolean okClicked = main.showTranslatorEditDialog(selectedTranslator);
            if (okClicked) {
                int addedAt = 0;
                // FIXME force update
                mTranslators.remove(selectedTranslator);
                mTranslators.add(selectedTranslator);
                for (int i = 0; i < translatorObservableList.size(); i++) {
                    Translator trans = translatorObservableList.get(i);
                    if (selectedTranslator == trans) {
                        if (i == translatorObservableList.size() - 1) {
                            translatorObservableList.remove(i);
                            translatorObservableList.add(translatorObservableList.size() - 1,
                                    selectedTranslator);
                            addedAt = translatorObservableList.size() - 2;
                        } else {
                            translatorObservableList.remove(i);
                            translatorObservableList.add(selectedTranslator);
                            addedAt = translatorObservableList.size() - 1;
                        }
                        break;
                    }
                }
                personTable.getSelectionModel().select(addedAt);
            }
        } else {
            // Nothing selected.
            Dialogs.create().title("No Selection").masthead("No Translator Selected")
                    .message("Please select a person in the table.").showWarning();
        }
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
            updateAndSaveDb();
            main.showMailSpitter();
        } else if (result.get() == buttonTypeTwo) {
            main.showMailSpitter();
        } else {
            // ... user chose CANCEL or closed the dialog
        }
    }

    private void updateAndSaveDb() {
        main.getTranslators().clear();
        main.getTranslators().addAll(mTranslators);
        main.spitDatabaseIfAvailable();
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
        languageFilter.setText("");
        nameFilter.setText(text);
    }

    private void getTranslatorsForFilter(String textField) {
        translatorObservableList.clear();
        for (Translator translator : mTranslators) {
            if (textField != null && !textField.equalsIgnoreCase("")) {
                final String sanitizedText = textField.toLowerCase().replace(" ", "");
                for(String lang: translator.getLanguages()){
                    if (lang.toLowerCase().replace(" ", "").contains(sanitizedText)) {
                        translatorObservableList.add(translator);
                        break;
                    }
                }
            } else {
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
        languageFilter.setText(textField);
        nameFilter.setText("");
    }
}
