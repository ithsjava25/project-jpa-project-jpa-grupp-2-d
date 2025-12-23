package org.example.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.dto.MovieDTO;
import org.example.service.MovieServices;
import org.example.movie.entity.Movie;

import java.util.List;
import java.util.Objects;

public class MainController {

   /* @FXML
    private ListView<String> movieList;

    private final MovieService movieService;

    public MainController(MovieServices movieServices) {
            this.movieServices = movieServices;
    }*/

    private final MovieDataSource dataSource;

    public MainController(MovieDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @FXML
    private FlowPane movieContainer;

    @FXML
    public void initialize() {
        try {
            dataSource.getTopRatedMovies().forEach(movie ->
                movieContainer.getChildren().add(createMovieCard(movie))
            );
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

    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private static final double CARD_WIDTH = 160;
    private static final double CARD_HEIGHT = 280;
    private static final double POSTER_HEIGHT = 220;

    private VBox createMovieCard(MovieDTO movie) {
        VBox card = new VBox(6);
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setMinSize(CARD_WIDTH, CARD_HEIGHT);
        card.setMaxSize(CARD_WIDTH, CARD_HEIGHT);

        card.setStyle("""
            -fx-background-color: #1e1e1e;
            -fx-padding: 6;
            -fx-border-radius: 6;
            -fx-background-radius: 6;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);
         """);


        ImageView poster = new ImageView();
        poster.setFitWidth(CARD_WIDTH - 12);
        poster.setFitHeight(POSTER_HEIGHT);
        poster.setPreserveRatio(false);

        String posterPath = movie.posterPath();

        if (posterPath != null && !posterPath.isBlank()) {
            poster.setImage(new Image(IMAGE_BASE_URL + posterPath, true));
        } else {
            poster.setImage(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/images/no-poster.jpg"))));
        }


        Label title = new Label(movie.title());
        title.setWrapText(true);
        title.setMaxHeight(40); // ≈ 2 lines
        title.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 12px;
            -fx-font-weight: bold;
        """);

        Label overview = new Label(movie.overview());
        overview.setWrapText(true);
        overview.setMaxWidth(150);

        VBox.setVgrow(title, Priority.NEVER);

        card.getChildren().addAll(poster, title);
        return card;
    }

    @FXML
    private TextField searchField;


  /*  @FXML
    private void loadMovies() {
        movieList.getItems().clear();

      *//*  List<Movie> movies = movieServices.getAllMovies();*//* //Placeholder - här lägger vi in metoder som byggs i MovieServices, skickas till repos och JPA

       *//* for (Movie movie : movies) {
            movieList.getItems().add(movie.getTitle());
        }*//*
    }*/
}
