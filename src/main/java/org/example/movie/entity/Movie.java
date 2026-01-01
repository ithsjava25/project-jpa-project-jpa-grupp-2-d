package org.example.movie.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    private String genre;
    @Column(length = 1000)
    private String description;
    @Column(nullable = false, unique = true)
    private Integer tmdbId;
    @Column(name = "image_url")
    private String imageUrl;
    @Column(name = "vote_count")
    private Integer voteCount;
    @Column(length = 500)
    private String homepage;
    @Column(length = 255)
    private String tagline;
    @Column(length = 500)
    private String spokenLanguages;
    private Integer releaseYear;
    private LocalDate releaseDate;
    private Double imdbRating;
    private Integer runtime;
    @Column(length = 50)
    private String status;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieTag tag;


    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getRuntime() {
        return runtime;
    }
    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Role> roles = new ArrayList<>();
    protected Movie() {}
    public Movie(String title, Integer tmdbId) {
        this.title = title;
        this.tmdbId = tmdbId;
    }

    public Movie(String title, Integer tmdbId, MovieTag tag) {
        this.title = title;
        this.tmdbId = tmdbId;
        this.tag = tag;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }
    public Double getImdbRating() { return imdbRating; }
    public void setImdbRating(Double imdbRating) {
        this.imdbRating = imdbRating;
    }
    public Integer getTmdbId() {
        return tmdbId;
    }
    public void setTmdbId(Integer tmdbId) {
        if (tmdbId == null || tmdbId <= 0) {
            throw new IllegalArgumentException("tmdbId must be a positive number");
        }
        this.tmdbId = tmdbId;
    }

    public LocalDate getReleaseDate() { return releaseDate;}
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate;}
    public String getSpokenLanguages() { return spokenLanguages; }
    public void setSpokenLanguages(String spokenLanguages) { this.spokenLanguages = spokenLanguages; }
    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }
    public String getHomepage() { return homepage; }
    public void setHomepage(String homepage) { this.homepage = homepage; }
    public Integer getVoteCount() { return voteCount; }
    public void setVoteCount(Integer voteCount) { this.voteCount = voteCount; }
    public List<Role> getRoles() { return roles; }
    public void addRole(Role role) {
        roles.add(role);
        role.setMovie(this);
    }
    public MovieTag getTag() { return tag; }
    public void setTag(MovieTag tag) { this.tag = tag; }
}
