
package com.coolchick.translatortemplater;

import com.coolchick.translatortemplater.model.Translator;
import com.coolchick.translatortemplater.model.TranslatorDatabase;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.dialog.Dialogs;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main extends Application {
    private Stage primaryStage;

    private TranslatorDatabase database = new TranslatorDatabase();

    private File databaseFile;

    public Stage getStage() {
        return primaryStage;
    }

    public Set<String> getLanguages() {
        return database.getAllLanguages();
    }

    public List<Translator> getTranslators() {
        return database.getTranslators();
    }

    public void setDatabase(TranslatorDatabase database) {
        this.database = database;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Hello Cel");
        showMailSpitter();
        primaryStage.show();
    }

    /**
     * Shows the person overview inside the root layout.
     */
    public void showTranslatorOverview() {
        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("PersonOverview.fxml"));
            StackPane personOverview = loader.load();
            PersonOverviewController controller = loader.getController();
            controller.setMain(this);
            controller.setTranslators(getTranslators());
            primaryStage.setScene(new Scene(personOverview, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the person overview inside the root layout.
     */
    public void showLanguageOverview() {
        // Load person overview.
        // FXMLLoader loader = new FXMLLoader();
        // loader.setLocation(Main.class.getResource("PersonOverview.fxml"));
        // StackPane languageOverview = loader.load();
        // PersonOverviewController controller = loader.getController();
        // controller.setMainApp(this);
        // primaryStage.setScene(new Scene(languageOverview, 800, 600));
        primaryStage.setScene(new LanguageOverview(this).getRootScene());
    }

    /**
     * Opens a dialog to edit details for the specified person. If the user clicks OK, the changes
     * are saved into the provided person object and true is returned.
     *
     * @param person the person object to be edited
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean showTranslatorEditDialog(Translator person) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("PersonEditDialog.fxml"));
            AnchorPane page = (AnchorPane)loader.load();
            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Person");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            // Set the person into the controller.
            PersonEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setMain(this);
            controller.setTranslator(person);
            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();
            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void showMailSpitter() {
        try {
            primaryStage
                    .setScene(new Scene(new EmailSpitter(this).getRoot(primaryStage), 800, 600));
        } catch (IOException e) {
            Dialogs.create().title("ERROR").masthead("BIG ERROR").message("LIKE WTF").showWarning();
        }
    }

    public boolean isDatabaseAvailable(){
        return (databaseFile != null && databaseFile.exists());
    }

    public boolean appendDatabase() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose your JSON database");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON file(*.json)", "*.json"));
        File file = fileChooser.showOpenDialog(getStage());
        if (file != null) {
            databaseFile = file;
            return appendDatabase(file);
        }
        return false;
    }

    private boolean appendDatabase(File file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            TranslatorDatabase database = mapper.readValue(file, TranslatorDatabase.class);
            getTranslators().addAll(database.getTranslators());
            getLanguages().addAll(database.getAllLanguages());
            return true;
        } catch (IOException e1) {
            showErrorDialog(getStage(), "Bad translator database");
        }
        return false;
    }

    public boolean openDatabase() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose your JSON database");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON file(*.json)", "*.json"));
        File file = fileChooser.showOpenDialog(getStage());
        if (file != null) {
            databaseFile = file;
            return openDatabase(file);
        }
        return false;
    }

    private boolean openDatabase(File file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            TranslatorDatabase database = mapper.readValue(file, TranslatorDatabase.class);
            setDatabase(database);
            return true;
        } catch (IOException e1) {
            showErrorDialog(getStage(), "Bad translator database");
        }
        return false;
    }

    public boolean spitDatabaseIfAvailable() {
        if (databaseFile != null && databaseFile.exists()) {
            return spitDatabase(databaseFile);
        } else {
            return spitNewDatabase();
        }
    }

    public boolean spitNewDatabase() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose destination");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON file(*.json)", "*.json"));
        File file = fileChooser.showSaveDialog(getStage());
        return file != null && spitDatabase(file);
    }

    private boolean spitDatabase(File file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Set<String> languages = new HashSet<String>();
            languages.addAll(getLanguages());
            // FIXME do not aggregate translator languages to global list
            // for (Translator translator : getTranslators()) {
            // for (String language : translator.getLanguages()) {
            // languages.add(language);
            // }
            // }
            TranslatorDatabase database = new TranslatorDatabase().withAllLanguages(languages)
                    .withTranslators(getTranslators());
            String databaseSerialized = mapper.writeValueAsString(database);
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(databaseSerialized);
            bw.close();
            openDatabase(file);
            showInformation("Database save", "Saved correctly!");
            return true;
        } catch (FileNotFoundException e1) {
            showErrorDialog(getStage(), "Failed to open file\n" + e1);
        } catch (IOException e1) {
            showErrorDialog(getStage(), "File type unknown, please open it externally");
        }
        return false;
    }

    private void showInformation(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }

    private void showErrorDialog(Stage primaryStage, String text) {
        Alert dlg = new Alert(Alert.AlertType.ERROR);
        dlg.setTitle("NOPE.JPG");
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.WINDOW_MODAL);
        dlg.getDialogPane().setContentText(text);
        dlg.show();
    }
}
