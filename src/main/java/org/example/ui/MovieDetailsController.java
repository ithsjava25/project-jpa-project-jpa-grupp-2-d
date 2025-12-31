package org.example.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.service.MovieService;
import javafx.scene.control.Label;
import java.io.IOException;



public class MovieDetailsController {
    private final MovieService movieService;
    private MovieDetailsUI movie;
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    @FXML
    private Label titleLabel;

    @FXML private Label overviewLabel;

    @FXML
    private ImageView posterImage;

    @FXML
    private Label directorLabel;

    @FXML
    private Label actorsLabel;
    @FXML
    private Label runtimeLabel;

    @FXML
    private Label releaseDateLabel;

    @FXML
    private Label genreLabel;

    @FXML
    private Label ratingLabel;

    @FXML
    private Label taglineLabel;

    @FXML
    private Label voteCountLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label languagesLabel;

    @FXML
    private Hyperlink homepageLink;


    public MovieDetailsController(MovieService movieService) {
        this.movieService = movieService;
    }

    public void loadMovie(int tmdbId) {
        this.movie = movieService.getMovieDetails(tmdbId);
        populateView();
    }

    private void populateView() {

        titleLabel.setText(
            movie.getTitle() +
                (movie.getReleaseYear() > 0 ? " (" + movie.getReleaseYear() + ")" : "")
        );

        ratingLabel.setText(
            String.format("%.1f / 10", movie.getRating())
        );

        if (movie.getTagline() != null && !movie.getTagline().isBlank()) {
            taglineLabel.setText(movie.getTagline());
            taglineLabel.setVisible(true);
        } else {
            taglineLabel.setVisible(false);
        }

        if (movie.getVoteCount() != null) {
            voteCountLabel.setText("Total votes: " + movie.getVoteCount());
            voteCountLabel.setVisible(true);
        } else {
            voteCountLabel.setVisible(false);
        }


        if (movie.getStatus() != null) {
            statusLabel.setText("Status: " + movie.getStatus());
            statusLabel.setVisible(true);
        } else {
            statusLabel.setVisible(false);
        }

        if (movie.getSpokenLanguages() != null && !movie.getSpokenLanguages().isBlank()) {
            languagesLabel.setText(movie.getSpokenLanguages());
            languagesLabel.setVisible(true);
        } else {
            languagesLabel.setVisible(false);
        }

        if (movie.getHomepage() != null && !movie.getHomepage().isBlank()) {
            homepageLink.setVisible(true);
            homepageLink.setText("🔗 Movie Page");
            homepageLink.setOnAction(e ->
                org.example.App.HOST_SERVICES.showDocument(movie.getHomepage())
            );


        } else {
            homepageLink.setVisible(false);
        }

        // Runtime
        runtimeLabel.setText(formatRuntime(movie.getRuntime()));

        if (movie.getReleaseDate() != null) {
            releaseDateLabel.setText(
                movie.getReleaseDate().toString()
            );
        } else {
            releaseDateLabel.setText("—");
        }


        genreLabel.setText(
            movie.getGenre() != null && !movie.getGenre().isBlank()
                ? movie.getGenre()
                : "—"
        );

        // Overview
        overviewLabel.setText(
            movie.getOverview() == null || movie.getOverview().isBlank()
                ? "No description available."
                : movie.getOverview()
        );

        // Poster
        if (movie.getPosterPath() != null) {
            String imageUrl = IMAGE_BASE_URL + movie.getPosterPath();
            posterImage.setImage(ImageCache.get(imageUrl));

        }

        // Director
        directorLabel.setText(
            movie.getDirectors().isEmpty()
                ? "Unknown"
                : String.join(", ", movie.getDirectors())
        );


        // Actors
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

    private String formatRuntime(Integer runtime) {
        if (runtime == null || runtime <= 0) {
            return "—";
        }
        int hours = runtime / 60;
        int minutes = runtime % 60;
        return hours + "h " + minutes + "min";
    }

}
