package org.example.service;

import jakarta.persistence.EntityManager;
import org.example.api.TmdbClient;
import org.example.dto.*;
import org.example.movie.entity.*;
import org.example.repository.MovieRepository;
import org.example.repository.PersonRepository;
import org.example.repository.RoleRepository;
import org.example.ui.MovieDetailsUI;
import org.example.util.JPAUtil;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MovieService {

    private final MovieRepository movieRepository;
    private final PersonRepository personRepository;
    private final RoleRepository roleRepository;
    private final TmdbClient tmdbClient;

    public MovieService(
        MovieRepository movieRepository,
        PersonRepository personRepository,
        RoleRepository roleRepository,
        TmdbClient tmdbClient
    ) {
        this.movieRepository = movieRepository;
        this.personRepository = personRepository;
        this.roleRepository = roleRepository;
        this.tmdbClient = tmdbClient;
    }

    // When application starts, this method should run first!
    public List<Movie> getAllMovies() {
        // check if db has data.
        if (movieRepository.count() > 0) {
            return movieRepository.findAll(); // if data exists, get all Movies.
        }
        // if db is empty, import from Tmdb endpoints
        importAllDataFromTmdb();
        importNowPlaying();

        // after import get all movies from db
        return movieRepository.findAll();
    }

    // get a movie with tmdbId
    public Movie getMovieByTmdbId(int tmdbId) {
        return movieRepository.findByTmdbId(tmdbId)
            .orElseThrow(() -> new RuntimeException("Movie not found: tmdbId=" + tmdbId));
    }

    public List<Role> getCreditsForMovie(int tmdbId) {
        Movie movie = getMovieByTmdbId(tmdbId);

        return movie.getRoles();
    }

    // imports ALL data from Tmdb
    private void importAllDataFromTmdb() {

        int pagesToFetch = 5; // 5 * 20 = 100 movies

        for (int page = 1; page <= pagesToFetch; page++) {

            TopRatedResponseDTO response =
                tmdbClient.getTopRatedMovies(page);

            for (MovieDTO movieDTO : response.results()) {

                Movie movie = createMovieIfNotExists(movieDTO, MovieTag.TOP_RATED);

                importMovieDetails(movie);
                importCredits(movie);
            }
        }
    }

    public void importNowPlaying() {

        int pagesToFetch = 1; // 1 * 20 = 20 movies

        for (int page = 1; page <= pagesToFetch; page++) {

            NowPlayingDTO response =
                tmdbClient.getNowPlayingMovies(page);
            System.out.println("Now playing response.page = " + response.page());

            for (MovieDTO dto : response.results()) {

                Movie movie =
                    createMovieIfNotExists(dto, MovieTag.NOW_PLAYING);

                importMovieDetails(movie);
                importCredits(movie);
            }
        }
    }


    private Movie createMovieIfNotExists(MovieDTO dto, MovieTag tag) {
        return movieRepository
            .findByTmdbId(dto.id())
            .map(existing -> {

                if (existing.getTag() != tag) {
                    existing.setTag(tag);
                    movieRepository.save(existing);
                }
                return existing;
            })
            .orElseGet(() -> {
                Movie movie = new Movie(dto.title(), dto.id(), tag);


                movie.setDescription(dto.overview());
                movie.setImdbRating(dto.voteAverage());
                movie.setImageUrl(dto.posterPath());

                return movieRepository.save(movie);
            });
    }



    private void importMovieDetails(Movie movie) {
        // Fetch detailed movie information from TMDB using the movie's tmdbId
        MovieDetailsDTO details = tmdbClient.getMovieDetails(movie.getTmdbId());

        // Map data from tmdb response to movie entity
        movie.setDescription(details.overview());
        movie.setImdbRating(details.voteAverage());
        movie.setRuntime(details.runtime());
        movie.setImageUrl(details.posterPath());
        movie.setTagline(details.tagline());
        movie.setHomepage("https://www.imdb.com/title/" + details.imdb_id());
        movie.setVoteCount(details.voteCount());
        movie.setStatus(details.status());


        if (details.spokenLanguages() != null && !details.spokenLanguages().isEmpty()) {
            String languages = details.spokenLanguages().stream()
                .map(SpokenLanguageDTO::englishName)
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);

            movie.setSpokenLanguages(languages);
        }

        if (details.releaseDate() != null && !details.releaseDate().isBlank()) {
            try {
                LocalDate releaseDate = LocalDate.parse(details.releaseDate());

                movie.setReleaseDate(releaseDate);
                movie.setReleaseYear(releaseDate.getYear());

            } catch (DateTimeParseException e) {
                System.err.println(
                    "Invalid release date from TMDB for movie " + movie.getTmdbId()
                        + ": " + details.releaseDate()
                );
            }
        }


        // TMDB returns genres as objects with id and name
        // We store them as a comma-separated string ("Drama, Crime")
        if (details.genres() != null && !details.genres().isEmpty()) {
            String genreString = details.genres().stream()
                .map(GenreDTO::name)
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);

            movie.setGenre(genreString);
        }

        // Persist the updated Movie entity to the database
        // JPA will perform an UPDATE since the entity already exists
        movieRepository.save(movie);
    }

    private void importCredits(Movie movie) {

        JPAUtil.inTransaction(em -> {

            Movie managedMovie = em.merge(movie);

            CreditsDTO credits = tmdbClient.getMovieCredits(managedMovie.getTmdbId());

            credits.cast().stream()
                .limit(15)
                .forEach(cast -> {
                    Person person = getOrCreatePerson(em, cast.name());
                    Role role = new Role(RoleType.ACTOR, null, person);
                    role.setCreditOrder(cast.order());
                    managedMovie.addRole(role);
                });

            credits.crew().stream()
                .filter(c -> "Director".equalsIgnoreCase(c.job()))
                .limit(5)
                .forEach(crew -> {
                    Person person = getOrCreatePerson(em, crew.name());
                    Role role = new Role(RoleType.DIRECTOR, null, person);
                    managedMovie.addRole(role);
                });
        });
    }


    private Person getOrCreatePerson(EntityManager em, String name) {
        return personRepository
            .findByName(em, name)
            .orElseGet(() -> personRepository.save(em, new Person(name)));
    }


    public MovieDetailsUI getMovieDetails(int tmdbId) {
        return JPAUtil.inTransactionResult(em -> {

            Movie movie = movieRepository
                .findByTmdbIdWithRoles(em, tmdbId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));

            List<String> directors = movie.getRoles().stream()
                .filter(r -> r.getRoleType() == RoleType.DIRECTOR)
                .map(r -> r.getPerson().getName())
                .toList();

            List<String> actors = movie.getRoles().stream()
                .filter(r -> r.getRoleType() == RoleType.ACTOR)
                .map(r -> r.getPerson().getName())
                .limit(15)
                .toList();

            return new MovieDetailsUI(
                movie.getTitle(),
                movie.getDescription(),
                movie.getImdbRating(),
                movie.getReleaseYear(),
                movie.getReleaseDate(),
                movie.getRuntime(),
                movie.getImageUrl(),
                movie.getGenre(),
                directors,
                actors,
                movie.getTagline(),
                movie.getVoteCount(),
                movie.getStatus(),
                movie.getSpokenLanguages(),
                movie.getHomepage()
            );

        });
    }

    public List<Movie> getTopRatedMoviesFromDb() {
        return movieRepository.findByTag(MovieTag.TOP_RATED);
    }

    public List<Movie> getNowPlayingMoviesFromDb() {
        return movieRepository.findByTag(MovieTag.NOW_PLAYING);
    }


    // NOTE: Deletes are executed here to ensure single-transaction atomicity.
    // Repository deleteAll() methods are intentionally not used.
    public void resetDatabaseAndImport() {
        JPAUtil.inTransaction(em -> {
            em.createQuery("DELETE FROM Role").executeUpdate();
            em.createQuery("DELETE FROM Person").executeUpdate();
            em.createQuery("DELETE FROM Movie").executeUpdate();
        });

        importAllDataFromTmdb();
        importNowPlaying();
    }







}
