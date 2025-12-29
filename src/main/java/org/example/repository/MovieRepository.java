package org.example.repository;

import jakarta.persistence.EntityManager;
import org.example.movie.entity.Movie;

import java.util.List;
import java.util.Optional;

public interface MovieRepository {

        Movie save(Movie movie);

        Optional<Movie> findById(Long id);

        Optional<Movie> findByTmdbId(int tmdbId);

        Optional<Movie> findByTitle(String title);

        List<Movie> findAll();

        List<Movie> findByTitleContaining(String title);

        long count();

        Optional<Movie> findByTmdbIdWithRoles(EntityManager em, int tmdbId);

}

