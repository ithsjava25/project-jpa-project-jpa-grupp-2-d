package org.example.search;

import org.example.api.TmdbClient;
import org.example.dto.*;
import org.example.movie.entity.Movie;
import org.example.repository.*;
import org.example.service.MovieService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SearchTest {
    @Test
    void searchStoresMoviesAndMakesThemSearchable() {

        TmdbClient tmdb = mock(TmdbClient.class);
        MovieRepository movieRepo = new MovieRepositoryImpl();
        PersonRepository personRepo = new PersonRepositoryImpl();
        RoleRepository roleRepo = new RoleRepositoryImpl();
        FavoriteRepository favoriteRepo = new FavoriteRepositoryImpl();

        MovieService service =
            new MovieService(movieRepo, personRepo, roleRepo, favoriteRepo, tmdb);

        MovieDTO dto = new MovieDTO(
            123,
            "Pokemon: The Movie",
            "overview",
            "1983-11-28",
            7.5,
            "/img.jpg",
            List.of(16, 12)
        );

        SearchResponseDTO response =
            new SearchResponseDTO(1, List.of(dto), 1, 1);

        when(tmdb.searchMovies("Pokemon")).thenReturn(response);

        MovieDetailsDTO details = new MovieDetailsDTO(
            123,
            "Pokemon: The Movie",
            "A pokemon adventure",
            "1983-11-28",
            240,
            8.3,
            "/pokemon.png",
            List.of(new GenreDTO(12, "Comedy")),
            List.of(new SpokenLanguageDTO("en", "English", "English")),
            "Gotta catch 'em all",
            "https://pokemon.com",
            "tt0123456",
            1000,
            "Released"
        );

        when(tmdb.getMovieDetails(123)).thenReturn(details);
        when(tmdb.getMovieCredits(123))
            .thenReturn(new CreditsDTO(1, List.of(), List.of()));

        service.searchAndStoreMovies("Pokemon");
        List<Movie> results = service.searchMoviesFromDb("Pokemon");

        assertEquals(1, results.size());
        assertEquals("Pokemon: The Movie", results.get(0).getTitle());

        service.searchAndStoreMovies("Pokemon"); // again

        assertEquals(1, movieRepo.count());
    }
}
