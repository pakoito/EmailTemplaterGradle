
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
import java.util.Comparator;
import java.util.HashSet;

/**
 * Created by Paco on 08/12/2014. See LICENSE.md
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

    private Main main;

    public void setMain(Main main) {
        this.main = main;
    }

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
     */
    public void setTranslator(Translator translator) {
        this.translator = translator;
        firstNameField.setText(translator.getName());
        emailField.setText(translator.getEmail());
        languagesTarget = FXCollections.observableArrayList(translator.getLanguages());
        languagesField.setTargetItems(languagesTarget);
        HashSet<String> langs = new HashSet<String>(main.getLanguages());
        langs.removeAll(translator.getLanguages());
        this.languages = langs;
        languagesObservableList = FXCollections.observableArrayList(langs);
        languagesObservableList.sort(new Comparator<String>() {
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
            languagesTarget.sort(new Comparator<String>() {
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
