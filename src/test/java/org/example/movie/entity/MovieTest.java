package org.example.movie.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MovieTest {

    @Test
    public void constructor_shouldSetRequiredFields() {
        Movie movie = new Movie("Inception", 123, MovieTag.TOP_RATED);

        assertEquals("Inception", movie.getTitle());
        assertEquals(123, movie.getTmdbId());
        assertEquals(MovieTag.TOP_RATED, movie.getTag());
    }

    @Test
    public void setTmdbId_withInvalidValue_shouldThrowException() {
        Movie movie = new Movie("Test", 1, MovieTag.TOP_RATED);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
                () -> movie.setTmdbId(-1)
        );

        assertTrue(ex.getMessage().contains("tmdbId must be a positive number"));
    }

    @Test
    public void addRole_shouldAddRoleAndSetMovieOnRole(){
        Movie movie = new Movie("Test Movie", 1, MovieTag.TOP_RATED);
        Role role = new Role();

        movie.addRole(role);

        assertEquals(1, movie.getRoles().size());
        assertEquals(movie, role.getMovie());
    }
}
