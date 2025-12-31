package org.example.ui;

import java.time.LocalDate;
import java.util.List;

public class MovieDetailsUI {

    private final String title;
    private final String overview;
    private final double rating;
    private final int releaseYear;
    private final LocalDate releaseDate;
    private final String posterPath;
    private final List<String> directors;
    private final List<String> actors;
    private final Integer runtime;
    private final String genre;
    private final String tagline;
    private final Integer voteCount;
    private final String statusLabel;
    private final String spokenLanguages;
    private final String homepageLink;


    public MovieDetailsUI(
        String title,
        String overview,
        double rating,
        int releaseYear,
        LocalDate releaseDate,
        Integer runtime,
        String posterPath,
        String genre,
        List<String> directors,
        List<String> actors,
        String tagline,
        Integer voteCount,
        String statusLabel,
        String spokenLanguages,
        String homepageLink
    ) {
        this.title = title;
        this.overview = overview;
        this.rating = rating;
        this.releaseYear = releaseYear;
        this.releaseDate = releaseDate;
        this.runtime = runtime;
        this.posterPath = posterPath;
        this.genre = genre;
        this.directors = directors;
        this.actors = actors;
        this.tagline = tagline;
        this.voteCount = voteCount;
        this.statusLabel = statusLabel;
        this.spokenLanguages = spokenLanguages;
        this.homepageLink = homepageLink;
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
    public LocalDate getReleaseDate() { return releaseDate; }
    public String getPosterPath() {
        return posterPath;
    }
    public String getGenre() { return genre; }
    public List<String> getDirectors() {
        return directors;
    }
    public List<String> getActors() { return actors; }
    public Integer getRuntime() { return runtime; }
    public String getTagline() { return tagline; }
    public Integer getVoteCount() { return voteCount; }
    public String getStatus() { return statusLabel; }
    public String getSpokenLanguages() { return spokenLanguages; }
    public String getHomepage() { return homepageLink; }

}
