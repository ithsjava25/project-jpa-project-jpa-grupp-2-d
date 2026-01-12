package org.example.repository;

import org.example.movie.entity.Favorite;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository {
    Favorite save(Favorite favorite);

    void delete(Favorite favorite);

    Optional<Favorite> findByMovieId(Long movieId);

    Optional<Favorite> findByTmdbId(int tmdbId);


    List<Favorite> findAll();

    long count();
}
