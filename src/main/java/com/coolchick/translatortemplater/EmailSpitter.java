
package com.coolchick.translatortemplater;

import com.coolchick.translatortemplater.model.Translator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.control.textfield.TextFields;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Created by Paco on 08/12/2014. See LICENSE.md
 */
// TODO FIXME class -.-
public class EmailSpitter {
    private final Main main;

    private ObservableList<Translator> translatorObservableList;

    private ObservableList<Translator> translatorsTarget;

    private ObservableList<String> languagesObservableList;

    private ObservableList<String> languagesTarget;

    private TextField emailField;

    private WeakReference<Stage> mStage;

    private TextField languageFilter;

    private TextField nameFilter;

    public EmailSpitter(Main main) {
        this.main = main;
    }

    public Parent getRoot(final Stage stageRef) throws IOException {
        mStage = new WeakReference<Stage>(stageRef);
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setResources(ResourceBundle
                .getBundle("com.coolchick.translatortemplater.theResources"));
        StackPane pane = fxmlLoader.load(EmailSpitter.class.getResource("theScene.fxml")
                .openStream());
        translatorObservableList = FXCollections.observableArrayList();
        translatorsTarget = FXCollections.observableArrayList();
        languagesObservableList = FXCollections.observableArrayList();
        languagesTarget = FXCollections.observableArrayList();
        final ObservableList<Node> children = ((VBox)fxmlLoader.getNamespace().get("VBox"))
                .getChildren();
        GridPane filterGrid = new GridPane();
        filterGrid.setVgap(10);
        filterGrid.setHgap(10);
        filterGrid.setPadding(new Insets(10, 10, 10, 10));
        nameFilter = new TextField();
        filterGrid.add(new Label("Name filter"), 0, 0);
        filterGrid.add(nameFilter, 1, 0);
        languageFilter = new TextField();
        filterGrid.add(new Label("Language filter"), 0, 1);
        filterGrid.add(languageFilter, 1, 1);
        languageFilter.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case ENTER:
                        nameFilter.setText("");
                        getTranslatorsForFilter(languageFilter.getText());
                        geLanguageForFilter(languageFilter.getText());
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
        final ListSelectionView<Translator> listSelectionView = new ListSelectionView<Translator>();
        listSelectionView.setSourceItems(translatorObservableList);
        listSelectionView.setTargetItems(translatorsTarget);
        final ListSelectionView<String> languagesSelection = new ListSelectionView<String>();
        languagesSelection.setSourceItems(languagesObservableList);
        languagesSelection.setTargetItems(languagesTarget);
        final javafx.scene.control.Button openButton = new javafx.scene.control.Button(
                "Load JSON database...");
        final javafx.scene.control.Button saveButton = new javafx.scene.control.Button(
                "Save new JSON database...");
        final javafx.scene.control.Button appendButton = new javafx.scene.control.Button(
                "Append JSON database...");
        final javafx.scene.control.Button manageDatabaseButton = new javafx.scene.control.Button(
                "<== Edit JSON Database");
        openButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                if (main.openDatabase()) {
                    startDatabase(false);
                    manageDatabaseButton.setVisible(true);
                    appendButton.setVisible(true);
                }
            }
        });
        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (main.spitNewDatabase()) {
                    startDatabase(false);
                    manageDatabaseButton.setVisible(true);
                    appendButton.setVisible(true);
                }
            }
        });
        appendButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (main.appendDatabase()) {
                    startDatabase(true);
                    manageDatabaseButton.setVisible(false);
                }
            }
        });
        manageDatabaseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                main.showTranslatorOverview();
            }
        });
        final HBox buttonBox = new HBox(manageDatabaseButton, openButton, saveButton, appendButton);
        buttonBox.setPadding(new Insets(10, 10, 10, 10));
        buttonBox.setSpacing(30);
        buttonBox.setPrefWidth(400);
        GridPane emailGrid = new GridPane();
        emailGrid.setVgap(10);
        emailGrid.setHgap(10);
        emailField = new TextField();
        emailField.setPadding(new Insets(5, 200, 5, 0));
        emailGrid.add(new Label("From Email"), 0, 0);
        emailGrid.add(emailField, 1, 0);
        final javafx.scene.control.Button spitButton = new javafx.scene.control.Button(
                "Spit email...");
        spitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Choose destination");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Email draft(*.eml)", "*.eml"));
                File file = fileChooser.showSaveDialog(mStage.get());
                if (file != null) {
                    try {
                        Properties properties = System.getProperties();
                        Session session = Session.getDefaultInstance(properties);
                        MimeMessage message = new MimeMessage(session);
                        final Address[] addresses = new Address[translatorsTarget.size()];
                        for (int i = 0; i < translatorsTarget.size(); i++) {
                            Translator trans = translatorsTarget.get(i);
                            addresses[i] = new InternetAddress(trans.getEmail());
                        }
                        String from = emailField.getText();
                        if (from != null && !"".equalsIgnoreCase(from)) {
                            message.addFrom(new Address[] {
                                new InternetAddress()
                            });
                        }
                        message.addRecipients(Message.RecipientType.BCC, addresses);
                        message.addHeaderLine("X-Unsent: 1");
                        message.setSubject("Sending email for languages: " + languagesTarget);
                        message.setContent("<h1>This is actual message</h1>", "text/html");
                        message.writeTo(new FileOutputStream(file));
                        openFile(file);
                    } catch (MessagingException e1) {
                        showErrorDialog(mStage.get(), "Failed to open file\n" + e1);
                    } catch (FileNotFoundException e1) {
                        showErrorDialog(mStage.get(), "Failed to open file\n" + e1);
                    } catch (IOException e1) {
                        showErrorDialog(mStage.get(),
                                "File type unknown, please open it externally");
                    }
                }
            }
        });
        children.add(buttonBox);
        children.add(filterGrid);
        children.add(listSelectionView);
        children.add(languagesSelection);
        children.add(emailGrid);
        children.add(spitButton);
        if (main.isDatabaseAvailable()) {
            /* Return from saved database */
            startDatabase(false);
            manageDatabaseButton.setVisible(true);
            appendButton.setVisible(true);
            saveButton.setVisible(true);
            openButton.setVisible(true);
        } else {
            /* Cold start */
            manageDatabaseButton.setVisible(false);
            appendButton.setVisible(false);
            saveButton.setVisible(true);
            openButton.setVisible(true);
        }
        return pane;
    }

    private void startDatabase(boolean append) {
        if (!append) {
            languagesObservableList.clear();
            translatorObservableList.clear();
        }
        languagesTarget.clear();
        translatorsTarget.clear();
        languageFilter.setText("");
        nameFilter.setText("");
        HashSet<String> names = new HashSet<String>();
        for (Translator trans : main.getTranslators()) {
            names.add(trans.getName());
        }
        TextFields.bindAutoCompletion(languageFilter, main.getLanguages());
        TextFields.bindAutoCompletion(nameFilter, names);
        languagesObservableList.addAll(main.getLanguages());
        translatorObservableList.addAll(main.getTranslators());
    }

    private void getTranslatorsForFilter(String text) {
        translatorObservableList.clear();
        for (Translator translator : main.getTranslators()) {
            if (text == null
                    || text.equalsIgnoreCase("")
                    || (translator.getLanguages().contains(text) && !translatorsTarget
                            .contains(translator))) {
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

    private void getTranslatorsForName(String filter) {
        translatorObservableList.clear();
        for (Translator translator : main.getTranslators()) {
            if (filter == null
                    || filter.equalsIgnoreCase("")
                    || (translator.getName().replace(" ", "").toLowerCase()
                            .contains(filter.replace(" ", "").toLowerCase()) && !translatorsTarget
                            .contains(translator))) {
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

    private void geLanguageForFilter(String filter) {
        languagesObservableList.clear();
        for (String language : main.getLanguages()) {
            if (filter == null || filter.equalsIgnoreCase("")
                    || (filter.equalsIgnoreCase(language) && !languagesTarget.contains(language))) {
                languagesObservableList.add(language);
            }
        }
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
    }

    private void showErrorDialog(Stage primaryStage, String text) {
        Alert dlg = new Alert(Alert.AlertType.ERROR);
        dlg.setTitle("NOPE.JPG");
        dlg.initOwner(primaryStage);
        dlg.initModality(Modality.WINDOW_MODAL);
        dlg.getDialogPane().setContentText(text);
        dlg.show();
    }

    private void openFile(File file) throws IOException {
        Desktop.getDesktop().open(file);
    }
}
