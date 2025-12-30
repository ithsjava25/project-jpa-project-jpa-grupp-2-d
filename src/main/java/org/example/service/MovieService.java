package org.example.service;

import org.example.api.TmdbClient;
import org.example.dto.*;
import org.example.movie.entity.*;
import org.example.repository.MovieRepository;
import org.example.repository.PersonRepository;
import org.example.repository.RoleRepository;
import org.example.ui.MovieDetailsUI;
import org.example.util.JPAUtil;
import java.time.LocalDate;
import java.util.Comparator;
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

        // kan kräva transaction(LAZY)
        return movie.getRoles();
    }

    // imports ALL data from Tmdb
    private void importAllDataFromTmdb() {

        int pagesToFetch = 5; // 5 * 20 = 100 filmer

        for (int page = 1; page <= pagesToFetch; page++) {

            TopRatedResponseDTO response =
                tmdbClient.getTopRatedMovies(page);

            for (MovieDTO movieDTO : response.results()) {

                Movie movie = createMovieIfNotExists(movieDTO);

                importMovieDetails(movie);
                importCredits(movie);
            }
        }
    }


    private Movie createMovieIfNotExists(MovieDTO dto) {
        // try to find if movie already exist with tmdbId
        // if movie already exists, return it directly
        return movieRepository
            .findByTmdbId(dto.id())
            .orElseGet(() -> {
                // if not exist, create a new movie entity using title and tmdbId
                Movie movie = new Movie(dto.title(), dto.id());

                // Map data that is available from the TopRatedMovies endpoint
                // TMDB 'overview' =  Movie 'description'
                movie.setDescription(dto.overview());

                // Map TMDB rating to Movie rating field
                movie.setImdbRating(dto.voteAverage());

                movie.setImageUrl(dto.posterPath());

                // Extract release year from full release date string (YYYY-MM-DD)
                if (dto.releaseDate() != null && !dto.releaseDate().isBlank()) {
                    movie.setReleaseYear(Integer.parseInt(dto.releaseDate().substring(0, 4)));
                }

                // Persist the newly created Movie entity to the database
                // After saving, the Movie will have a generated database id
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
        movie.setHomepage(details.homepage());
        movie.setVoteCount(details.voteCount());
        movie.setStatus(details.status());


        if (details.spokenLanguages() != null && !details.spokenLanguages().isEmpty()) {
            String languages = details.spokenLanguages().stream()
                .map(SpokenLanguageDTO::englishName) // eller ::name om ni vill
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);

            movie.setSpokenLanguages(languages);
        }

        if (details.releaseDate() != null && !details.releaseDate().isBlank()) {
            LocalDate releaseDate = LocalDate.parse(details.releaseDate());

            movie.setReleaseDate(releaseDate);          // 2024-02-24
            movie.setReleaseYear(releaseDate.getYear()); // 2024
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
        // Fetch credits (cast and crew) from TMDB using the movie's tmdbId
        CreditsDTO credits = tmdbClient.getMovieCredits(movie.getTmdbId());


        // Takes max 15 actors
        credits.cast().stream()
            // TMDB cast är redan sorterad, men vi säkrar på order
            .sorted(Comparator.comparingInt(CastDTO::order))
            .limit(15)
            .forEach(cast -> {
                // Find existing Person by name or create a new one if it does not exist
                Person person = getOrCreatePerson(cast.name());

                // Create a new Role linking the Movie and the Person as an ACTOR
                Role role = new Role(RoleType.ACTOR, movie, person);

                // Store the credit order (lower number = more prominent actor)
                role.setCreditOrder(cast.order());

                // Persist the Role entity (links Movie ↔ Person)
                roleRepository.save(role);
            });

        // Takes max 5 directors
        credits.crew().stream()
            // Filter only crew members with the job title "Director"
            .filter(crew -> "Director".equalsIgnoreCase(crew.job()))
            .limit(5)
            .forEach(crew -> {
                // Find existing Person by name or create a new one if it does not exist
                Person person = getOrCreatePerson(crew.name());

                // Create a new Role linking the Movie and the Person as a DIRECTOR
                Role role = new Role(RoleType.DIRECTOR, movie, person);

                // Persist the Role entity
                roleRepository.save(role);
            });
    }


    private Person getOrCreatePerson(String name) {
        // Attempt to find an existing Person with the given name

        return personRepository
            // If no Person is found, create and persist a new one
            .findByName(name)
            .orElseGet(() -> personRepository.save(new Person(name)));
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
}
