package org.example.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.api.TmdbClient;
import org.example.dto.GenreDTO;
import org.example.dto.MovieDTO;
import org.example.service.MovieService;
import org.example.movie.entity.Movie;


import java.util.*;
import java.util.stream.Collectors;

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
    private TextField searchField;

    @FXML
    private ComboBox<String> genreComboBox;

    private final TmdbClient tmdbClient = new TmdbClient();

    private Map<Integer, String> genreMap;
    private List<UIMovie> allMovies = new ArrayList<>();
    private List<UIMovie> displayedMovies = new ArrayList<>();

    private void loadGenres() {
        List<GenreDTO> genres = tmdbClient.getGenres(); // GenreResponseDTO
        genreMap = genres.stream()
            .collect(Collectors.toMap(
                GenreDTO::id,
                GenreDTO::name
            ));

        genreComboBox.getItems().setAll("All");
        genreComboBox.getItems().addAll(genreMap.values());
    }

    @FXML
    private void loadHome() {
        loadTopRated(); // Home = top rated for now
    }

    @FXML
    private void search() {
        applyFilters();
    }

    @FXML
    private void loadTopRated() {
        var response = tmdbClient.getTopRatedMovies();
        allMovies = response.results().stream()
            .map(dto -> UIMovie.fromDto(dto, genreMap))
            .toList();

        applyFilters();
    }

    @FXML
    private void filterByGenre() {
        applyFilters();
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

    @FXML
    private void loadNewReleases() {
        var response = tmdbClient.getNowPlayingMovies(); // teammate can add this
        allMovies = response.results().stream()
            .map(dto -> UIMovie.fromDto(dto, genreMap))
            .toList();

        applyFilters();
    }

    private void setupGenres() {
        loadGenres();
        genreComboBox.getItems().add("All");
        genreComboBox.getSelectionModel().select("All");
    }

    private void updateGenres() {
        Set<String> genres = allMovies.stream()
            .flatMap(movie -> movie.getGenre().stream())
            .collect(Collectors.toSet());

        genreComboBox.getItems().setAll("All");
        genreComboBox.getItems().addAll(genres);
    }


    private void refreshGrid() {
        movieContainer.getChildren().clear();

        displayedMovies.forEach(movie ->
            movieContainer.getChildren().add(createMovieCard(movie))
        );
    }


    @FXML
    public void initialize() {
        setupGenres();
        loadHome();

        //Old Version
        /*try {
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
        }*/
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
        title.setWrapText(true);
        title.setMaxHeight(40); // ≈ 2 lines
        title.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 12px;
            -fx-font-weight: bold;
        """);

        Label overview = new Label(movie.getOverview());
        overview.setWrapText(true);
        overview.setMaxWidth(150);

        VBox.setVgrow(title, Priority.NEVER);

        card.getChildren().addAll(poster, title);
        return card;
    }






  /*  @FXML
    private void loadMovies() {
        movieList.getItems().clear();

      *//*  List<Movie> movies = movieServices.getAllMovies();*//* //Placeholder - här lägger vi in metoder som byggs i MovieServices, skickas till repos och JPA

       *//* for (Movie movie : movies) {
            movieList.getItems().add(movie.getTitle());
        }*//*
    }*/
}
