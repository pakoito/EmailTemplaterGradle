
package com.coolchick.translatortemplater;

import com.coolchick.translatortemplater.model.Translator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.dialog.Dialogs;

import java.io.IOException;
import java.util.HashSet;

public class Main extends Application {
    private Stage primaryStage;

    public Stage getStage() {
        return primaryStage;
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
            controller.setMainApp(this);
            primaryStage.setScene(new Scene(personOverview, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens a dialog to edit details for the specified person. If the user clicks OK, the changes
     * are saved into the provided person object and true is returned.
     *
     * @param person the person object to be edited
     * @param languages
     * @return true if the user clicked OK, false otherwise.
     */
    public boolean showTranslatorEditDialog(Translator person, HashSet<String> languages) {
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
            controller.setTranslator(person, languages);
            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();
            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void showMailSpitter() {
        try {
            primaryStage
                    .setScene(new Scene(new EmailSpitter(this).getRoot(primaryStage), 800, 600));
        } catch (IOException e) {
            Dialogs.create().title("ERROR").masthead("BIG ERROR").message("LIKE WTF").showWarning();
        }
    }
}
