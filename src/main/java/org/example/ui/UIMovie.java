package org.example.ui;

import org.example.movie.entity.Movie;
import java.util.Arrays;
import java.util.List;


public class UIMovie {

    private final int id;
    private final String title;
    private final String posterPath;
    private final List<String> genres;
    private final String overview;
    private final double rating;
    private final int releaseYear;

    public UIMovie(int id, String title, String posterPath, String overview, List<String> genres, double rating, int releaseYear) {
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
        this.overview = overview;
        this.genres = genres;
        this.rating = rating;
        this.releaseYear = releaseYear;
    }


    public static UIMovie fromEntity(Movie movie) {
        int releaseYear = movie.getReleaseYear() != null
            ? movie.getReleaseYear()
            : 0;

        List<String> genres =
            movie.getGenre() == null || movie.getGenre().isBlank()
                ? List.of()
                : Arrays.stream(movie.getGenre().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        return new UIMovie(
            movie.getTmdbId(),
            movie.getTitle(),
            movie.getImageUrl(),
            movie.getDescription(),
            genres,
            movie.getImdbRating(),
            releaseYear
        );
    }


    // getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getPosterPath() { return posterPath; }
    public List<String> getGenre() { return genres; }
    public String getOverview() { return overview; }
    public double getRating() { return rating; }
    public int getReleaseYear() { return releaseYear; }
}
