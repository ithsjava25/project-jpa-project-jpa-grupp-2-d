package org.example;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;
import org.example.ui.MainApp;
import org.example.util.JPAUtil;

public class App extends Application {
    public static HostServices HOST_SERVICES;
    @Override
    public void start(Stage primaryStage) {
        HOST_SERVICES = getHostServices();
        MainApp mainApp = new MainApp();
        mainApp.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);

        JPAUtil.inTransaction(em -> {
            System.out.println("Database schema initialized");
        });
    }
}
