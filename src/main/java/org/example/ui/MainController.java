package org.example.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.enums.ViewType;
import org.example.movie.entity.Movie;
import org.example.service.MovieService;



import java.io.IOException;
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

    @FXML private Button homeButton;
    @FXML private Button newReleasesButton;

    private List<UIMovie> allMovies = new ArrayList<>();
    private List<UIMovie> displayedMovies = new ArrayList<>();

    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private static final double CARD_WIDTH = 160;
    private static final double CARD_HEIGHT = 280;
    private static final double POSTER_HEIGHT = 220;
    private ViewType currentView = ViewType.TOP_RATED;
    private ViewType initialView = ViewType.TOP_RATED;


    public void setInitialView(ViewType viewType) {
        this.initialView = viewType;
    }


    @FXML
    public void loadTopRatedFromDatabase() {
        currentView = ViewType.TOP_RATED;
        setActive(homeButton);
        loadFromDatabase();
    }
    @FXML
    public void loadNewReleases() {
        currentView = ViewType.NOW_PLAYING;
        setActive(newReleasesButton);
        List<Movie> movies = movieService.getNowPlayingMoviesFromDb();

        allMovies = movies.stream()
            .map(UIMovie::fromEntity)
            .toList();

        setupGenresFromDb();
        applyFilters();
    }
    @FXML
    private void search() {
        applyFilters();
    }
    @FXML
    private void filterByGenre() {
        applyFilters();
    }
    @FXML
    public void initialize() {
        currentView = initialView;

        if (initialView == ViewType.NOW_PLAYING) {
            setActive(newReleasesButton);
            loadNowPlayingFromDatabase();
        } else {
            setActive(homeButton);
            loadFromDatabase();
        }

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            applyFilters();
        });

        setupGenresFromDb();
    }

    private void loadFromDatabase() {
        List<Movie> movies = movieService.getTopRatedMoviesFromDb();

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
    private VBox createMovieCard(UIMovie movie) {
        VBox card = new VBox(6);
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setMinSize(CARD_WIDTH, CARD_HEIGHT);
        card.setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        card.getStyleClass().add("movie-card");

        card.setOnMouseClicked(e -> {
            openMovieDetails(movie);
        });

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
            String imageUrl = IMAGE_BASE_URL + posterPath;
            poster.setImage(ImageCache.get(imageUrl));
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
    private void openMovieDetails(UIMovie movie) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/MovieDetailsView.fxml")
            );

            loader.setControllerFactory(type -> {
                if (type == MovieDetailsController.class) {
                    return new MovieDetailsController(movieService, currentView);

                }
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();

            MovieDetailsController controller = loader.getController();
            controller.loadMovie(movie.getId());

            Stage stage = (Stage) movieContainer.getScene().getWindow();

            Scene scene = new Scene(root, 1300, 800);
            stage.setWidth(1300);
            stage.setHeight(800);

            stage.setMinWidth(1300);
            stage.setMinHeight(800);
            stage.setResizable(false);

            var css = getClass().getResource("/styles/app.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            stage.setScene(scene);

        } catch (IOException e) {
            throw new RuntimeException("Failed to open movie details view", e);
        }
    }
    private void setActive(Button active) {
        homeButton.getStyleClass().remove("active");
        newReleasesButton.getStyleClass().remove("active");
        active.getStyleClass().add("active");
    }
    private void loadNowPlayingFromDatabase() {
        allMovies = movieService.getNowPlayingMoviesFromDb()
            .stream()
            .map(UIMovie::fromEntity)
            .toList();

        setupGenresFromDb();
        applyFilters();
    }


}
