package org.example.service;

import org.example.api.TmdbClient;
import org.example.dto.*;
import org.example.movie.entity.*;
import org.example.repository.MovieRepository;
import org.example.repository.PersonRepository;
import org.example.repository.RoleRepository;

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

        // kan kräva transaction beroende på hur ni kör JPA (LAZY)
        return movie.getRoles();
    }

    // imports ALL data from Tmdb
    private void importAllDataFromTmdb() {
        // first import list of top-rated movies
        TopRatedResponseDTO response = tmdbClient.getTopRatedMovies();

        for (MovieDTO movieDTO : response.results()) {

            // 1. Create/Get movie
            Movie movie = createMovieIfNotExists(movieDTO);

            // 2. update current movie and add movie details
            importMovieDetails(movie);

            // 3. import credits for the movie
            importCredits(movie);
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

        if (details.releaseDate() != null && !details.releaseDate().isBlank()) {
            movie.setReleaseYear(Integer.parseInt(details.releaseDate().substring(0, 4)));
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


        // Takes max 9 actors
        credits.cast().stream()
            // TMDB cast är redan sorterad, men vi säkrar på order
            .sorted(Comparator.comparingInt(CastDTO::order))
            .limit(9)
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

        // Takes max 1 directos
        credits.crew().stream()
            // Filter only crew members with the job title "Director"
            .filter(crew -> "Director".equalsIgnoreCase(crew.job()))
            .limit(1)
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
}
