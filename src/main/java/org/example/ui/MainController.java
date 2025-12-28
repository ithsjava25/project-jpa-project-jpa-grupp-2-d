package org.example.ui;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.movie.entity.Movie;
import org.example.service.MovieService;

import java.util.*;
import java.util.stream.Collectors;

public class MainController {

    private final MovieService movieService;

    public MainController(MovieService movieService) {
        this.movieService = movieService;
    }

    @FXML
    private FlowPane movieContainer;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> genreComboBox;


    private List<UIMovie> allMovies = new ArrayList<>();
    private List<UIMovie> displayedMovies = new ArrayList<>();


    @FXML
    private void loadHome() {
        applyFilters();
    }

    @FXML
    private void search() {
        applyFilters();
    }

    @FXML
    private void loadTopRated() {
        loadFromDatabase();
    }

    @FXML
    private void loadNewReleases() {
        loadFromDatabase();
    }

    @FXML
    private void filterByGenre() {
        applyFilters();
    }

    @FXML
    public void initialize() {
        loadFromDatabase();
    }

    private void loadFromDatabase() {
        List<Movie> movies = movieService.getAllMovies();

        allMovies = movies.stream()
            .map(UIMovie::fromEntity)
            .toList();

        setupGenresFromDb();
        applyFilters();
    }

    private void setupGenresFromDb() {
        Set<String> genres = allMovies.stream()
            .flatMap(movie -> movie.getGenre().stream())
            .collect(Collectors.toCollection(TreeSet::new));

        genreComboBox.getItems().setAll("All");
        genreComboBox.getItems().addAll(genres);
        genreComboBox.getSelectionModel().select("All");
    }

    private void applyFilters() {
        String query = searchField.getText().toLowerCase();
        String selectedGenre = genreComboBox.getValue();

        displayedMovies = allMovies.stream()
            .filter(movie ->
                query.isBlank() ||
                    movie.getTitle().toLowerCase().contains(query)
            )
            .filter(movie ->
                selectedGenre == null ||
                    selectedGenre.equals("All") ||
                    movie.getGenre().contains(selectedGenre)
            )
            .toList();

        refreshGrid();
    }

    private void refreshGrid() {
        movieContainer.getChildren().clear();

        displayedMovies.forEach(movie ->
            movieContainer.getChildren().add(createMovieCard(movie))
        );
    }

    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private static final double CARD_WIDTH = 160;
    private static final double CARD_HEIGHT = 280;
    private static final double POSTER_HEIGHT = 220;

    private VBox createMovieCard(UIMovie movie) {
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

        String posterPath = movie.getPosterPath();

        if (posterPath != null && !posterPath.isBlank()) {
            poster.setImage(new Image(IMAGE_BASE_URL + posterPath, true));
        } else {
            poster.setImage(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/images/no-poster.jpg"))));
        }

        Label title = new Label(movie.getTitle());
        Label meta = new Label(
            "⭐ " + String.format("%.1f", movie.getRating()) +
                "  •  " + movie.getReleaseYear()
        );

        meta.setStyle("""
            -fx-text-fill: #cccccc;
            -fx-font-size: 11px;
        """);

        title.setWrapText(true);
        title.setMaxHeight(40);
        title.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 12px;
            -fx-font-weight: bold;
        """);

        VBox.setVgrow(title, Priority.NEVER);
        card.getChildren().addAll(poster, title, meta);
        return card;
    }

}
