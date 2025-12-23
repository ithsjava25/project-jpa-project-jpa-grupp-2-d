package org.example.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.api.TmdbClient;
import org.example.service.MovieServices;

public class MainApp {

    private final MovieServices movieServices = new MovieServices();

    public void start(Stage stage) {
        try {
                TmdbClient tmdbClient = new TmdbClient();
                MovieDataSource dataSource = new ApiMovieDataSource(tmdbClient);
                var fxmlUrl = getClass().getResource("/MainView.fxml");
                if (fxmlUrl == null) {
                throw new IllegalStateException("Cannot find FXML resource: /MainView.fxml");
                }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            loader.setControllerFactory(type -> {
                if (type == MainController.class) {
                    return new MainController(dataSource);
                }
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Scene scene = new Scene(loader.load());

            stage.setScene(scene);
            stage.setTitle("Movie Database App");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR,
                "Failed to load the application UI: " + e.getMessage()
            );
            alert.showAndWait();
            throw new RuntimeException("UI initialization failed", e);
        }
    }
}
