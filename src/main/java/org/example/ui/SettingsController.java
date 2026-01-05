package org.example.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
    private void setDarkMode() {
        ThemeManager.applyDark(rootPane.getScene());
    }

    @FXML
    private void setLightMode() {
        ThemeManager.applyLight(rootPane.getScene());
    }

    @FXML
    private void onResetDatabase() {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm database reset");
        confirm.setHeaderText("Reset database?");
        confirm.setContentText(
            "This will delete all movies and re-import data.\n" +
                "This operation can take up to 1 minute."
        );

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

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

            Throwable ex = task.getException();
            ex.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Import Failed");
            alert.setHeaderText("Database reset failed");
            alert.setContentText(
                ex.getMessage() != null
                    ? ex.getMessage()
                    : "An unexpected error occurred during import."
            );
            alert.showAndWait();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
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
