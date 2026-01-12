package org.example.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.enums.ViewType;
import org.example.movie.entity.Movie;
import org.example.service.MovieService;
import java.text.Normalizer;
import java.util.Locale;



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
    @FXML private Button topRatedButton;

    @FXML private Button favoritesButton;
    @FXML private ImageView favoritesIcon;
    @FXML private Label favoritesCountLabel;


    private List<UIMovie> allMovies = new ArrayList<>();
    private List<UIMovie> displayedMovies = new ArrayList<>();

    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private static final double CARD_WIDTH = 160;
    private static final double CARD_HEIGHT = 280;
    private static final double POSTER_HEIGHT = 220;
    private ViewType currentView = ViewType.TOP_RATED;
    private ViewType initialView = ViewType.TOP_RATED;

    @FXML
    private ImageView settingsIcon;


    public void setInitialView(ViewType viewType) {
        this.initialView = viewType;
    }


    @FXML
    public void loadAllMoviesFromDb() {
        currentView = ViewType.HOME;
        setActive(homeButton);
        loadFromDatabase();
        clearSearchField();
    }

    @FXML
    public void loadTopRatedFromDatabase() {
        currentView = ViewType.TOP_RATED;
        setActive(topRatedButton);
        loadTopRatedFromDb();
        clearSearchField();
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
        clearSearchField();
    }

    @FXML
    private void showFavorites() {
        currentView = ViewType.FAVORITES;
        setActive(null);

        allMovies = movieService.getFavoriteMovies()
            .stream()
            .map(UIMovie::fromEntity)
            .toList();

        setupGenresFromDb();
        applyFilters();
    }


    private void clearSearchField() {
        if (searchField != null) {
            Platform.runLater(() -> searchField.clear());
        }
    }

    private void updateSettingsIcon() {
        String iconPath = ThemeManager.isLightMode()
            ? "/icons/settingsDark.png"
            : "/icons/settingsLight.png";

        var iconStream = getClass().getResourceAsStream(iconPath);
        if (iconStream == null) {
            System.err.println("Warning: Settings icon not found at " + iconPath);
            return;
        }

        settingsIcon.setImage(new Image(iconStream));
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
        updateSettingsIcon();
        updateHeartTopIcon();
        updateHeartCounter();


        switch (initialView) {

            case FAVORITES -> {
                showFavorites();
            }

            case NOW_PLAYING -> {
                setActive(newReleasesButton);
                loadNowPlayingFromDatabase();
            }

            case HOME -> {
                setActive(homeButton);
                loadAllMoviesFromDb();
            }

            case TOP_RATED -> {
                setActive(topRatedButton);
                loadFromDatabase();
            }

            default -> {
                setActive(topRatedButton);
                loadFromDatabase();
            }
        }

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            applyFilters();
        });


    }

    @FXML
    private void searchRemote() {
        String query = searchField.getText();

        if (query == null || query.isBlank()) {
            loadFromDatabase();
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                movieService.searchAndStoreMovies(query);
                return null;
            }
        };

        task.setOnRunning(e -> showLoading());

        task.setOnSucceeded(e -> {
            hideLoading();
            loadSearchResultsFromDb(query);
        });

        task.setOnFailed(e -> {
            hideLoading();
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private void loadSearchResultsFromDb(String query) {
        currentView = ViewType.SEARCH;

        List<Movie> movies = movieService.searchMoviesFromDb(query);

        allMovies = movies.stream()
            .map(UIMovie::fromEntity)
            .toList();

        setupGenresFromDb();
        applyFilters();
    }


    @FXML
    private StackPane loadingOverlay;

    private void showLoading() {
        loadingOverlay.setVisible(true);
    }

    private void hideLoading() {
        loadingOverlay.setVisible(false);
    }


    private void loadTopRatedFromDb() {
        allMovies = movieService.getTopRatedMoviesFromDb()
            .stream()
            .map(UIMovie::fromEntity)
            .toList();

        setupGenresFromDb();
        applyFilters();
    }

    private void loadFromDatabase() {
        allMovies = movieService.getAllMoviesFromDb()
            .stream()
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

    public static String normalize(String input) {
        if (input == null) return "";

        return Normalizer.normalize(input, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")   // remove diacritics
            .toLowerCase(Locale.ROOT)
            .trim();
    }

    private void applyFilters() {
        String query = normalize(searchField.getText().toLowerCase());
        String selectedGenre = genreComboBox.getValue();

        displayedMovies = allMovies.stream()
            .filter(movie -> {
                String titleNorm = normalize(movie.getTitle());
                return query.isBlank() ||
                    titleNorm.toLowerCase().contains(query);
            })
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

        card.setOnMouseClicked(e -> openMovieDetails(movie));

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
            poster.setImage(ImageCache.get(IMAGE_BASE_URL + posterPath));
        } else {
            poster.setImage(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/images/no-poster.jpg")
            )));
        }

        // ===== HEART ICON =====
        ImageView heartIcon = new ImageView();
        heartIcon.setFitWidth(15);
        heartIcon.setFitHeight(15);
        heartIcon.setPreserveRatio(true);

        updateHeartIcon(heartIcon, movie);

        StackPane heartWrapper = new StackPane(heartIcon);
        heartWrapper.getStyleClass().add("heart-wrapper");

        heartWrapper.setMinSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
        heartWrapper.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);

        StackPane.setAlignment(heartWrapper, Pos.TOP_RIGHT);
        StackPane.setMargin(heartWrapper, new Insets(6));

        // Klicka på hela cirkeln
        heartWrapper.setOnMouseClicked(e -> {
            e.consume();
            movieService.toggleFavorite(movie.getId());
            updateHeartIcon(heartIcon, movie);
            updateHeartCounter();
        });

        StackPane posterWrapper = new StackPane(poster, heartWrapper);

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
        card.getChildren().addAll(posterWrapper, title, meta);
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

            ThemeManager.apply(scene);


            stage.setScene(scene);

        } catch (IOException e) {
            throw new RuntimeException("Failed to open movie details view", e);
        }
    }
    private void setActive(Button active) {
        homeButton.getStyleClass().remove("active");
        newReleasesButton.getStyleClass().remove("active");
        topRatedButton.getStyleClass().remove("active");

        if (active != null) {
            active.getStyleClass().add("active");
        }
    }

    private void loadNowPlayingFromDatabase() {
        allMovies = movieService.getNowPlayingMoviesFromDb()
            .stream()
            .map(UIMovie::fromEntity)
            .toList();

        setupGenresFromDb();
        applyFilters();
    }

    private void updateHeartTopIcon() {
        String iconPath = ThemeManager.isLightMode()
            ? "/icons/heart_filled_black.png"
            : "/icons/heart_filled_white.png";

        var iconStream = getClass().getResourceAsStream(iconPath);
        if (iconStream == null) {
            System.err.println("Warning: Favorites icon not found at " + iconPath);
            return;
        }

        favoritesIcon.setImage(new Image(iconStream));
    }


    private void updateHeartIcon(ImageView heartIcon, UIMovie movie) {
        boolean isFavorite = movieService.isFavorite(movie.getId());

        String iconPath = isFavorite
            ? "/icons/heart_filled_white.png"
            : "/icons/heart_unfilled_white.png";

        heartIcon.setImage(
            new Image(getClass().getResourceAsStream(iconPath))
        );
    }


    private void updateHeartCounter() {
        long count = movieService.getFavoriteCount();
        favoritesCountLabel.setText(String.valueOf(count));
        favoritesCountLabel.setVisible(count > 0);
    }



    @FXML
    private void openSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/SettingsView.fxml")
            );

            loader.setControllerFactory(type -> {
                if (type == SettingsController.class) {
                    return new SettingsController(movieService);
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

            Stage stage = (Stage) movieContainer.getScene().getWindow();
            stage.setScene(scene);

        } catch (IOException e) {
            throw new RuntimeException("Failed to open settings view", e);
        }
    }


}
