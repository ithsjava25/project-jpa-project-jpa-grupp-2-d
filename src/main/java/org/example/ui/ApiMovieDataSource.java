package org.example.ui;

import java.util.List;
import org.example.api.TmdbClient;
import org.example.dto.MovieDTO;

public class ApiMovieDataSource implements MovieDataSource {

    private final TmdbClient tmdbClient;

    public ApiMovieDataSource(TmdbClient tmdbClient) {
        this.tmdbClient = tmdbClient;
    }

    @Override
    public List<MovieDTO> getTopRatedMovies() {
        return tmdbClient.getTopRatedMovies().results(); // or searchMovies(), etc.
    }
}
