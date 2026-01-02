package org.example.repository;

import org.example.movie.entity.Person;

import java.util.List;
import java.util.Optional;

public interface PersonRepository {

    Person save(Person person);

    Optional<Person> findById(Long id);

    Optional<Person> findByName(String name);

    List<Person> findAll();

    void deleteAll();

}
