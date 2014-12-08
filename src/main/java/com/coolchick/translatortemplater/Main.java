
package com.coolchick.translatortemplater;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {


    @Override
    public void start(final Stage primaryStage) throws Exception {
        final Scene scene = new Scene(EmailSpitter.getRoot(primaryStage), 800, 600);
        primaryStage.setTitle("Hello Cel");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
