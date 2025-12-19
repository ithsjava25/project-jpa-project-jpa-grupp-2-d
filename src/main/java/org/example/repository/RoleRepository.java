package org.example.repository;

import org.example.movie.entity.Role;

import java.util.List;

public interface RoleRepository {

    void save(Role role);

    List<Role> findByMovieId(Long movieId);

    List<Role> findByPersonId(Long personId);
}
