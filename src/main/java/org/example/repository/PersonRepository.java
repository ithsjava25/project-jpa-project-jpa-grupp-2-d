package org.example.repository;

import jakarta.persistence.EntityManager;
import org.example.movie.entity.Person;

import java.util.List;
import java.util.Optional;

public interface PersonRepository {

    Person save(Person person);

    // use when we already are in a transaction and have EntityManager.
    Person save(EntityManager em, Person person);

    Optional<Person> findById(Long id);

    Optional<Person> findByName(String name);

    Optional<Person> findByName(EntityManager em, String name);

    List<Person> findAll();

    void deleteAll();

}
