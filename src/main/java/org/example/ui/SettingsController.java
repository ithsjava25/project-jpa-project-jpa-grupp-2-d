package org.example.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.service.MovieService;
import javafx.concurrent.Task;


import java.io.IOException;

public class SettingsController {

    private final MovieService movieService;
    @FXML
    private BorderPane rootPane;

    @FXML
    private StackPane loadingOverlay;

    @FXML
    private Button importButton;

    public SettingsController(MovieService movieService) {
        this.movieService = movieService;
    }
    @FXML
    private Button backButton;

    @FXML
    private void setDarkMode() {
        ThemeManager.applyDark(rootPane.getScene());
    }

    @FXML
    private void setLightMode() {
        ThemeManager.applyLight(rootPane.getScene());
    }

    @FXML
    private void onResetDatabase() {

        loadingOverlay.setVisible(true);
        importButton.setDisable(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                movieService.resetDatabaseAndImport();
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            loadingOverlay.setVisible(false);
            importButton.setDisable(false);
        });

        task.setOnFailed(e -> {
            loadingOverlay.setVisible(false);
            importButton.setDisable(false);
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }



    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/MainView.fxml")
            );

            loader.setControllerFactory(type -> {
                if (type == MainController.class) {
                    return new MainController(movieService);
                }
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();

            Scene scene = new Scene(root);
            ThemeManager.apply(scene);

            Stage stage = (Stage) rootPane.getScene().getWindow();

            stage.setScene(scene);

        } catch (IOException e) {
            throw new RuntimeException("Failed to go back from settings", e);
        }
    }




}
