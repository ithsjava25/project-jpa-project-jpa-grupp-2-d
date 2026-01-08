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
            .thenReturn(Optional.empty()); // du stubbar repository så att när findbytmdbid(999) anropas returneras ingen film, optionl.empty() betyder att filmen finns inte i db

        // Act & assert, kör och verifiera. Eftersom Optional är tom, runtimeExeption kastas, assertThrows fångar exceptionet och sparar det i variabeln ex. Om inget exeption kastas testet misslyckas
        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> movieService.getMovieByTmdbId(999)
        );

        assertTrue(ex.getMessage().contains("Movie not found"));
    }

    @Test       //vilken metod testas, vad vi förväntar oss
    public void getTopRatedMoviesFromDb_returnsOnlyTopRated(){
        //Arrange,förbered testet. Du stubar repository och när findByTag(TOP-RATED) anropas, returnera en lista med en top rated film(ingen db, ingen jpa, du kontrollerar exact datan)
        when(movieRepository.findByTag(MovieTag.TOP_RATED))
            .thenReturn(List.of(new Movie("Top", 1, MovieTag.TOP_RATED)));

        //Act- kör metoden som testas. Den anropar repositroryt och skickar tillbaka resultatet
        List<Movie> result = movieService.getTopRatedMoviesFromDb();

        //Assert- verifiera bekräfta resultatet, endast 1 film returneras
        assertEquals(1, result.size());
        assertEquals(MovieTag.TOP_RATED, result.get(0).getTag());// bekräftar att filmen har rätt tag och att inga andra filmer blandas in

        //🧠 Vad testet faktiskt bevisar
        //✔️ Service-metoden anropar rätt repository-metod
        //✔️ Rätt parameter (TOP_RATED) används
        //✔️ Resultatet returneras oförändrat
        //📌 Notera:
        //Filtreringen sker i repositoryt – inte i servicen
        //🎓 Hur du förklarar detta för läraren
        //Testet verifierar att getTopRatedMoviesFromDb hämtar filmer med korrekt tag från repositoryt och returnerar dem utan extra logik. Repositoryt mockas för att säkerställa isolerad testning av service-lagret.
        //Sammanfattning (1 mening)
        //Testet säkerställer att service-metoden returnerar endast filmer med taggen TOP_RATED genom att anropa rätt repository-metod.
    }

    @Test
    public void getNowPlayingMoviesFromDb_returnsNowPlaying(){
        // Arrange, förbered testet
        when(movieRepository.findByTag(MovieTag.NOW_PLAYING))
            .thenReturn(List.of(new Movie("Now", 2, MovieTag.NOW_PLAYING)));

        //Act
        List<Movie> result = movieService.getNowPlayingMoviesFromDb();

        //Assert
        assertEquals(1, result.size());
        assertEquals(MovieTag.NOW_PLAYING, result.get(0).getTag());
    }

//🧠 Hur du förklarar detta för läraren (kort)
//“Vi har skrivit enhetstester för service-lagret där affärslogiken finns.
//Repository och TMDB är mockade så testerna är snabba och isolerade.
//UI och externa API:er har medvetet uteslutits eftersom de hör till integrationstester.”

//🎯 Mål med testerna för MovieService
//Vi ska:
//testa affärslogik
//mocka bort databas & TMDB (inga riktiga API-anrop)
//använda JUnit 5 + Mockito
//inte röra JavaFX eller JPA-transaktioner i detalj





}
