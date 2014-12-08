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
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
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
 * Created by Paco on 08/12/2014.
 * See LICENSE.md
 */
// TODO FIXME class -.-
public class EmailSpitter {

    private ArrayList<Translator> mTranslators;

    private HashSet<String> mLanguages;

    private ObservableList<Translator> translatorObservableList;

    private ObservableList<Translator> translatorsTarget;

    private ObservableList<String> languagesObservableList;

    private ObservableList<String> languagesTarget;

    private TextField emailField;

    private WeakReference<Stage> mStage;

    public Parent getRoot(final Stage stageRef) throws IOException {
        mStage = new WeakReference<Stage>(stageRef);
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setResources(ResourceBundle
                .getBundle("com.coolchick.translatortemplater.theResources"));
        StackPane pane = fxmlLoader.load(EmailSpitter.class.getResource("theScene.fxml").openStream());
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
        final TextField textField = new TextField();
        filterGrid.add(new Label("Language filter"), 0, 0);
        filterGrid.add(textField, 1, 0);
        textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case ENTER:
                        getTranslatorsForFilter(textField, translatorObservableList,
                                translatorsTarget);
                        geLanguageForFilter(textField, languagesObservableList, languagesTarget);
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
        openButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Choose your JSON database");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("JSON file(*.json)", "*.json"));
                File file = fileChooser.showOpenDialog(mStage.get());
                if (file != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        TranslatorDatabase database = mapper.readValue(file,
                                TranslatorDatabase.class);
                        for (Translator translator : database.getTranslators()) {
                            mTranslators.add(translator);
                            for (String language : translator.getLanguages()) {
                                mLanguages.add(language);
                            }
                        }
                        mLanguages.addAll(database.getAllLanguages());
                        TextFields.bindAutoCompletion(textField, mLanguages);
                        getTranslatorsForFilter(textField, translatorObservableList,
                                translatorsTarget);
                        geLanguageForFilter(textField, languagesObservableList, languagesTarget);
                    } catch (IOException e1) {
                        showErrorDialog(mStage.get(), "Bad translator database");
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
        children.add(openButton);
        children.add(filterGrid);
        children.add(listSelectionView);
        children.add(languagesSelection);
        children.add(emailGrid);
        children.add(spitButton);
        return pane;
    }

    private void getTranslatorsForFilter(TextField textField,
                                         ObservableList<Translator> translators, ObservableList<Translator> translatorsTarget) {
        translators.clear();
        for (Translator translator : mTranslators) {
            if (textField.getText() == null
                    || textField.getText().equalsIgnoreCase("")
                    || (translator.getLanguages().contains(textField.getText()) && !translatorsTarget
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

    private void openFile(File file) throws IOException {
        Desktop.getDesktop().open(file);
    }
}
