package org.example.repository;

import jakarta.persistence.EntityManager;
import org.example.movie.entity.Role;
import org.example.util.JPAUtil;

import java.util.List;

public class RoleRepositoryImpl implements RoleRepository {

    @Override
    public void save(Role role) {
        JPAUtil.inTransaction(em -> em.persist(role));
    }

    @Override
    public List<Role> findByMovieId(Long movieId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT r FROM Role r WHERE r.movie.id = :movieId",
                    Role.class
                )
                .setParameter("movieId", movieId)
                .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Role> findByPersonId(Long personId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT r FROM Role r WHERE r.person.id = :personId",
                    Role.class
                )
                .setParameter("personId", personId)
                .getResultList();
        } finally {
            em.close();
        }
    }
}

