package org.example.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.movie.entity.Movie;
import org.example.movie.entity.RoleType;
import org.example.service.MovieService;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.IOException;


public class MovieDetailsController {
    private final MovieService movieService;
    private MovieDetailsUI movie;
    private static final String IMAGE_BASE_URL =
        "https://image.tmdb.org/t/p/w500";


    @FXML
    private Label titleLabel;

    @FXML
    private Label ratingLabel;
    @FXML
    private Label yearLabel;

    @FXML private Label overviewLabel;

    @FXML
    private ImageView posterImage;

    @FXML
    private Label metaLabel;

    @FXML
    private Label directorLabel;

    @FXML
    private Label actorsLabel;



    public MovieDetailsController(MovieService movieService) {
        this.movieService = movieService;
    }

    public void loadMovie(int tmdbId) {
        this.movie = movieService.getMovieDetails(tmdbId);
        populateView();
    }


    private void populateView() {

        titleLabel.setText(
            movie.getTitle() + " (" + movie.getReleaseYear() + ")"
        );

        ratingLabel.setText("⭐ " + movie.getRating());

        overviewLabel.setText(
            movie.getOverview() == null
                ? "No description available."
                : movie.getOverview()
        );

        if (movie.getPosterPath() != null) {
            posterImage.setImage(
                new Image(IMAGE_BASE_URL + movie.getPosterPath(), true)
            );
        }

        directorLabel.setText(
            movie.getDirectors().isEmpty()
                ? "Unknown"
                : movie.getDirectors().get(0)
        );

        actorsLabel.setText(
            String.join(", ", movie.getActors())
        );

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

            scene.getStylesheets().add(
                getClass().getResource("/styles/app.css").toExternalForm()
            );

            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
