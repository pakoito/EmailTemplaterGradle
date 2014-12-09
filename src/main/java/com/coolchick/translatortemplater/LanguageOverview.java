
package com.coolchick.translatortemplater;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Callback;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;

public class LanguageOverview {
    private TableView<StringWrapper> table = new TableView<StringWrapper>();

    final HBox hb = new HBox();

    private Main main;

    private HashSet<String> mLanguages;

    private ObservableList<StringWrapper> observableList = FXCollections.observableArrayList();

    public LanguageOverview(Main main) {
        this.main = main;
        this.mLanguages = new HashSet<String>(main.getLanguages());
    }

    public static class StringWrapper {
        private final String language;

        public String getLanguage() {
            return language;
        }

        private StringWrapper(String string) {
            this.language = string;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StringWrapper that = (StringWrapper) o;

            if (language != null ? !language.equals(that.language) : that.language != null) return false;

            return true;
        }
    }

    public Scene getRootScene() {
        Scene scene = new Scene(new Group(), 800, 600);
        final Label label = new Label("Language List");
        label.setFont(new Font("Arial", 20));
        table.setEditable(true);
        Callback<TableColumn, TableCell> cellFactory = new Callback<TableColumn, TableCell>() {
            public TableCell call(TableColumn p) {
                return new EditingCell();
            }
        };
        TableColumn firstNameCol = new TableColumn("Language");
        firstNameCol.setMinWidth(100);
        firstNameCol.setCellValueFactory(new PropertyValueFactory<StringWrapper, String>("language"));
        firstNameCol.setCellFactory(cellFactory);
        firstNameCol.setOnEditCommit(new EventHandler<CellEditEvent<StringWrapper, String>>() {
            @Override
            public void handle(CellEditEvent<StringWrapper, String> t) {
                StringWrapper rowValue = t.getTableView().getItems().get(t.getTablePosition().getRow());
                mLanguages.remove(rowValue.getLanguage());
                if(t.getNewValue() != null && !"".equalsIgnoreCase(t.getNewValue())){
                    mLanguages.add(t.getNewValue());
                }
                updateObservable();
            }
        });
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(firstNameCol);
        final TextField addLanguage = new TextField();
        addLanguage.setPromptText("Language");
        addLanguage.setMaxWidth(firstNameCol.getPrefWidth());
        final Button addButton = new Button("Add");
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                final String text = addLanguage.getText();
                if (text != null && !"".equalsIgnoreCase(text)) {
                    mLanguages.add(text);
                    updateObservable();
                    addLanguage.clear();
                }
            }
        });
        hb.getChildren().addAll(addLanguage, addButton);
        hb.setSpacing(3);
        Button returnButton = new Button();
        returnButton.setText("<== Return to email spitter");
        returnButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                showExitDialog();
            }
        });
        final javafx.scene.control.Button spitButton = new javafx.scene.control.Button(
                "Save database...");
        spitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                updateAndSaveDb();
            }
        });
        final VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(returnButton, label, table, hb, spitButton);
        ((Group)scene.getRoot()).getChildren().addAll(vbox);
        updateObservable();
        table.setItems(observableList);
        return scene;
    }

    private void showExitDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Leaving languages editor!");
        alert.setHeaderText("Do you want to save before leaving?");
        alert.setContentText("Choose your option.");
        ButtonType buttonTypeOne = new ButtonType("Save and leave");
        ButtonType buttonTypeTwo = new ButtonType("Leave without saving");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeCancel);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne) {
            updateAndSaveDb();
            main.showTranslatorOverview();
        } else if (result.get() == buttonTypeTwo) {
            main.showTranslatorOverview();
        } else {
            // ... user chose CANCEL or closed the dialog
        }
    }

    private void updateAndSaveDb() {
        main.getLanguages().clear();
        main.getLanguages().addAll(mLanguages);
        main.spitDatabase();
    }

    private void updateObservable() {
        observableList.clear();
        for (String lang : mLanguages) {
            observableList.add(new StringWrapper(lang));
        }
        observableList.sort(new Comparator<StringWrapper>() {
            @Override
            public int compare(StringWrapper o1, StringWrapper o2) {
                int compare = o1.getLanguage().compareToIgnoreCase(o2.getLanguage());
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

    class EditingCell extends TableCell<String, String> {
        private TextField textField;

        public EditingCell() {
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem());
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1,
                        Boolean arg2) {
                    if (!arg2) {
                        commitEdit(textField.getText());
                    }
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }
}
