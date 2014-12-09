
package com.coolchick.translatortemplater;

import com.coolchick.translatortemplater.model.Translator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.dialog.Dialogs;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Dialog to edit details of a translator.
 * 
 * @author Marco Jakob
 */
public class PersonEditDialogController {
    @FXML
    private TextField firstNameField;

    @FXML
    private TextField emailField;

    @FXML
    private ListSelectionView<String> languagesField;

    private Stage dialogStage;

    private Translator translator;

    private HashSet<String> languages;

    private boolean okClicked = false;

    private ObservableList<String> languagesObservableList;

    private ObservableList<String> languagesTarget;

    /**
     * Initializes the controller class. This method is automatically called after the fxml file has
     * been loaded.
     */
    @FXML
    private void initialize() {
    }

    /**
     * Sets the stage of this dialog.
     * 
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets the translator to be edited in the dialog.
     *
     * @param translator
     * @param languages
     */
    public void setTranslator(Translator translator, HashSet<String> languages) {
        this.translator = translator;
        firstNameField.setText(translator.getName());
        emailField.setText(translator.getEmail());
        languagesTarget = FXCollections.observableArrayList(translator.getLanguages());
        languagesField.setTargetItems(languagesTarget);
        HashSet<String> langs = new HashSet<String>(languages);
        langs.removeAll(translator.getLanguages());
        this.languages = langs;
        languagesObservableList = FXCollections.observableArrayList(langs);
        languagesField.setSourceItems(languagesObservableList);
    }

    /**
     * Returns true if the user clicked OK, false otherwise.
     * 
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            translator.setName(firstNameField.getText());
            translator.setEmail(emailField.getText());
            translator.setLanguages(Arrays.asList(languagesTarget.toArray(new String[languagesTarget.size()])));
            okClicked = true;
            dialogStage.close();
        }
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Validates the user input in the text fields.
     * 
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        String errorMessage = "";
        if (firstNameField.getText() == null || firstNameField.getText().length() == 0) {
            errorMessage += "No valid first name!\n";
        }
        if (emailField.getText() == null || emailField.getText().length() == 0) {
            errorMessage += "No valid last name!\n";
        }
        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            Dialogs.create().title("Invalid Fields").masthead("Please correct invalid fields")
                    .message(errorMessage).showError();
            return false;
        }
    }
}
