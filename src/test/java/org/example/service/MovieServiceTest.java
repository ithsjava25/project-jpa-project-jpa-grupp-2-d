package org.example.service;

import org.example.api.TmdbClient;
import org.example.movie.entity.Movie;
import org.example.movie.entity.MovieTag;
import org.example.repository.FavoriteRepository;
import org.example.repository.MovieRepository;
import org.example.repository.PersonRepository;
import org.example.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MovieServiceTest {

    private MovieRepository movieRepository;
    private PersonRepository personRepository;
    private RoleRepository roleRepository;
    private FavoriteRepository favoriteRepository;
    private TmdbClient tmdbClient;

    private MovieService movieService;

    @BeforeEach
    public void setUp() {
        movieRepository = mock(MovieRepository.class);
        personRepository = mock(PersonRepository.class);
        roleRepository = mock(RoleRepository.class);
        favoriteRepository = mock(FavoriteRepository.class);
        tmdbClient = mock(TmdbClient.class);

        movieService = new MovieService(
            movieRepository,
            personRepository,
            roleRepository,
            favoriteRepository,
            tmdbClient
        );
    }

    @Test
    public void getAllMovies_whenDatabaseHasData_returnsMovies(){
        Movie movie = new Movie("Test Movie", 1, MovieTag.TOP_RATED);
        when(movieRepository.count()).thenReturn(1L);
        when(movieRepository.findAll()).thenReturn(Arrays.asList(movie));

        List<Movie> result = movieService.getAllMovies();

        assertEquals(1, result.size());
        assertEquals("Test Movie", result.get(0).getTitle());

        verifyNoInteractions(tmdbClient);
    }

    @Test
    public void getAllMovies_whenDatabaseIsEmpty_triggersImport(){
        when(movieRepository.count()).thenReturn(0L);
        when(movieRepository.findAll()).thenReturn(List.of());

        when(tmdbClient.getTopRatedMovies(anyInt()))
            .thenThrow(new RuntimeException("stub"));

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> movieService.getAllMovies()
        );

        assertEquals("stub", ex.getMessage());
        verify(tmdbClient).getTopRatedMovies(anyInt());
    }

    @Test
   public void getMovieByTmdbId_existingMovie_returnsMovie(){
        //arrange, förbered testet
        Movie movie = new Movie("Inception", 123, MovieTag.TOP_RATED);
        when(movieRepository.findByTmdbId(123))
            .thenReturn(Optional.of(movie));

        //Act
        Movie result = movieService.getMovieByTmdbId(123);

        //Assert
        assertEquals("Inception", result.getTitle());
    }

    @Test
    public void getMovieByTmdbId_missingMovie_throwsException(){
        when(movieRepository.findByTmdbId(999))
            .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> movieService.getMovieByTmdbId(999)
        );

        assertTrue(ex.getMessage().contains("Movie not found"));
    }

    @Test
    public void getTopRatedMoviesFromDb_returnsOnlyTopRated(){
        when(movieRepository.findByTag(MovieTag.TOP_RATED))
            .thenReturn(List.of(new Movie("Top", 1, MovieTag.TOP_RATED)));

        List<Movie> result = movieService.getTopRatedMoviesFromDb();

        assertEquals(1, result.size());
        assertEquals(MovieTag.TOP_RATED, result.get(0).getTag());
    }

    @Test
    public void getNowPlayingMoviesFromDb_returnsNowPlaying(){
        when(movieRepository.findByTag(MovieTag.NOW_PLAYING))
            .thenReturn(List.of(new Movie("Now", 2, MovieTag.NOW_PLAYING)));

        List<Movie> result = movieService.getNowPlayingMoviesFromDb();

        assertEquals(1, result.size());
        assertEquals(MovieTag.NOW_PLAYING, result.get(0).getTag());
    }


}
