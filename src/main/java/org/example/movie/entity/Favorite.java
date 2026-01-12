package org.example.movie.entity;

import jakarta.persistence.*;
@Entity
@Table(
    name = "favorites",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "tmdb_id")
    }
)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tmdb_id", nullable = false)
    private int tmdbId;

    protected Favorite() { }

    public Favorite(int tmdbId) {
        this.tmdbId = tmdbId;
    }

    public Long getId() {
        return id;
    }

    public int getTmdbId() {
        return tmdbId;
    }
}
