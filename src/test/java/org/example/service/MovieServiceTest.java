package org.example.service;

import org.example.api.TmdbClient;
import org.example.movie.entity.Movie;
import org.example.movie.entity.MovieTag;
import org.example.repository.MovieRepository;
import org.example.repository.PersonRepository;
import org.example.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MovieServiceTest {

    private MovieRepository movieRepository;
    private PersonRepository personRepository;
    private RoleRepository roleRepository;
    private TmdbClient tmdbClient;

    private MovieService movieService;

    @BeforeEach
    public void setUp() {
        movieRepository = mock(MovieRepository.class);
        personRepository = mock(PersonRepository.class);
        roleRepository = mock(RoleRepository.class);
        tmdbClient = mock(TmdbClient.class);

        movieService = new MovieService(
            movieRepository,
            personRepository,
            roleRepository,
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
        //arrange förbered testet
        when(movieRepository.count()).thenReturn(0L);
        when(movieRepository.findAll()).thenReturn(List.of());

        // Stub: så fort TMDB anropas vet vi att import startat, med meddelande stub, bevis att importen startar. Om den här metoden aldrig anropas → inget exception → testet failar
        when(tmdbClient.getTopRatedMovies(anyInt()))
            .thenThrow(new RuntimeException("stub"));

        // Act -kör metoden
        //✅ assertThrows fångar exceptionet .  ❌ Om inget exception kastas → testet misslyckas
        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> movieService.getAllMovies()
        );

        // Assert- kontrollera resultatet, det är vårt stub-exeption inte något annat fel!
        assertEquals("stub", ex.getMessage());
        verify(tmdbClient).getTopRatedMovies(anyInt()); // ➡️ Verifierar att: TMDB verkligen anropades och Importen triggades
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
        // arrange, förbered
        when(movieRepository.findByTmdbId(999))
            .thenReturn(Optional.empty());

        // Act & assert, kör och verifiera
        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> movieService.getMovieByTmdbId(999)
        );

        assertTrue(ex.getMessage().contains("Movie not found"));

    }





}
