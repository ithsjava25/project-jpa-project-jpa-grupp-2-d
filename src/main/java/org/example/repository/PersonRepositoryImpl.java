package org.example.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.movie.entity.Person;
import org.example.util.JPAUtil;

import java.util.List;
import java.util.Optional;

public class PersonRepositoryImpl implements PersonRepository {

    @Override
    public Person save(Person person) {
        JPAUtil.inTransaction(em -> em.persist(person));
        return person;
    }

    @Override
    public Optional<Person> findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Person.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Person> findByName(String name) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Person> query = em.createQuery(
                "SELECT p FROM Person p WHERE p.name = :name",
                Person.class
            );
            query.setParameter("name", name);
            return query.getResultStream().findFirst();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Person> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Person p",
                Person.class
            ).getResultList();
        } finally {
            em.close();
        }
    }
}
