package org.example.ui;

import org.example.dto.GenreDTO;
import org.example.dto.MovieDTO;
import org.example.dto.MovieDetailsDTO;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    // ✅ THIS is what Movie::fromDto refers to
    public static UIMovie fromDto(MovieDTO dto, Map<Integer, String> genreMap) {
        List<String> genres = dto.genreId() == null ? List.of() : dto.genreId().stream()
            .map(genreMap::get)
            .filter(Objects::nonNull)
            .toList();

        int releaseYear = 0;
        if (dto.releaseDate() != null && !dto.releaseDate().isBlank()) {
            releaseYear = Integer.parseInt(dto.releaseDate().substring(0, 4));
        }

        return new UIMovie(
            dto.id(),
            dto.title(),
            dto.posterPath(),
            dto.overview(),
            genres,
            dto.voteAverage(),
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
