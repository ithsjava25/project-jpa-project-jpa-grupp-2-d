package org.example.repository;

import jakarta.persistence.EntityManager;
import org.example.movie.entity.Favorite;
import org.example.util.JPAUtil;

import java.util.List;
import java.util.Optional;

public class FavoriteRepositoryImpl implements FavoriteRepository{
    @Override
    public Favorite save(Favorite favorite) {
        JPAUtil.inTransaction(em -> em.persist(favorite));
        return favorite;
    }

    @Override
    public void delete(Favorite favorite) {
        JPAUtil.inTransaction(em -> {
            Favorite managed = em.merge(favorite);
            em.remove(managed);
        });
    }

    @Override
    public List<Favorite> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT f FROM Favorite f",
                Favorite.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    @Override
    public long count() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT COUNT(f) FROM Favorite f",
                Long.class
            ).getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Favorite> findByTmdbId(int tmdbId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT f FROM Favorite f WHERE f.tmdbId = :tmdbId",
                    Favorite.class
                )
                .setParameter("tmdbId", tmdbId)
                .getResultStream()
                .findFirst();
        } finally {
            em.close();
        }
    }
}
