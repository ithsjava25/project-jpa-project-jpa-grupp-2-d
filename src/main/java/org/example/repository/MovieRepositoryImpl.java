package org.example.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.movie.entity.Movie;
import org.example.movie.entity.MovieTag;
import org.example.util.JPAUtil;

import java.util.List;
import java.util.Optional;

public class MovieRepositoryImpl implements MovieRepository {

    @Override
    public Movie save(Movie movie) {
        JPAUtil.inTransaction(em -> {
            if (movie.getId() == null) {
                em.persist(movie);
            } else {
                em.merge(movie);
            }
        });
        return movie;
    }

    @Override
    public Optional<Movie> findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Movie.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Movie> findByTmdbId(int tmdbId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT m FROM Movie m WHERE m.tmdbId = :tmdbId",
                    Movie.class
                )
                .setParameter("tmdbId", tmdbId)
                .getResultStream()
                .findFirst();
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Movie> findByTitle(String title) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Movie> query = em.createQuery(
                "SELECT m FROM Movie m WHERE m.title = :title",
                Movie.class
            );
            query.setParameter("title", title);

            return query.getResultStream().findFirst();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Movie> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT m FROM Movie m",
                Movie.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Movie> findByTitleContaining(String search) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(:search)",
                    Movie.class
                )
                .setParameter("search", "%" + search + "%")
                .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public long count() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT COUNT(m) FROM Movie m",
                    Long.class
                )
                .getSingleResult();
        } finally {
            em.close();
        }
    }

    public Optional<Movie> findByTmdbIdWithRoles(EntityManager em, int tmdbId) {
        return em.createQuery("""
        SELECT m
        FROM Movie m
        LEFT JOIN FETCH m.roles r
        LEFT JOIN FETCH r.person
        WHERE m.tmdbId = :tmdbId
        """, Movie.class)
            .setParameter("tmdbId", tmdbId)
            .getResultStream()
            .findFirst();
    }

    @Override
    public List<Movie> findByTag(MovieTag tag) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT m FROM Movie m WHERE m.tag = :tag",
                    Movie.class
                )
                .setParameter("tag", tag)
                .getResultList();
        } finally {
            em.close();
        }
    }


}
