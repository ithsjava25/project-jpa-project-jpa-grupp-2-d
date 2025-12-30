package org.example.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.example.api.TmdbClient;
import org.example.repository.MovieRepository;
import org.example.repository.MovieRepositoryImpl;
import org.example.repository.PersonRepository;
import org.example.repository.PersonRepositoryImpl;
import org.example.repository.RoleRepository;
import org.example.repository.RoleRepositoryImpl;
import org.example.service.MovieService;

public class MainApp {

    public void start(Stage stage) {
        try {
            MovieRepository movieRepository = new MovieRepositoryImpl();
            PersonRepository personRepository = new PersonRepositoryImpl();
            RoleRepository roleRepository = new RoleRepositoryImpl();
            TmdbClient tmdbClient = new TmdbClient();

            MovieService movieService = new MovieService(
                movieRepository,
                personRepository,
                roleRepository,
                tmdbClient
            );

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

            Scene scene = new Scene(loader.load());
            var css = getClass().getResource("/styles/app.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            stage.setScene(scene);
            stage.setWidth(1300);
            stage.setHeight(800);

            stage.setMinWidth(1300);
            stage.setMinHeight(800);

            stage.setTitle("Movie Database App - By Adam, Mats, Tatjana :)");

            stage.getIcons().add(
                new Image(
                    getClass().getResourceAsStream("/icons/app-icon.png")
                )
            );
            stage.show();



        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("UI initialization failed", e);
        }
    }
}
