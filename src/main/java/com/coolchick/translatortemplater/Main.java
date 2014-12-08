
package com.coolchick.translatortemplater;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private Scene mEmailSpitterScene;

    private Scene mDatabaseManagerScene;

    @Override
    public void start(final Stage primaryStage) throws Exception {
        mEmailSpitterScene = new Scene(new EmailSpitter().getRoot(primaryStage), 800, 600);
        mDatabaseManagerScene = new Scene(new DatabaseManager().getRoot(primaryStage), 800, 600);
        primaryStage.setTitle("Hello Cel");
        primaryStage.setScene(mEmailSpitterScene);
        primaryStage.show();
        primaryStage.setScene(mDatabaseManagerScene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
