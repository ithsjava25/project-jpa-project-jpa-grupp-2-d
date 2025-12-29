package org.example.ui;

import java.util.List;

public class MovieDetailsUI {

    private final String title;
    private final String overview;
    private final double rating;
    private final int releaseYear;
    private final String posterPath;
    private final List<String> directors;
    private final List<String> actors;

    public MovieDetailsUI(String title, String overview, double rating, int releaseYear, String posterPath, List<String> directors, List<String> actors) {
        this.title = title;
        this.overview = overview;
        this.rating = rating;
        this.releaseYear = releaseYear;
        this.posterPath = posterPath;
        this.directors = directors;
        this.actors = actors;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public double getRating() {
        return rating;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public List<String> getDirectors() {
        return directors;
    }

    public List<String> getActors() {
        return actors;
    }
}
