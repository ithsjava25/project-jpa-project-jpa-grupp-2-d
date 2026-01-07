package org.example.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record MovieDetailsDTO(
    int id,
    String title,
    String overview, // overview = description in entity
    @SerializedName("release_date") String releaseDate,
    int runtime,
    @SerializedName("vote_average") double voteAverage, // voteAverage = imdbRating in entity
    @SerializedName("poster_path") String posterPath,
    List<GenreDTO> genres,
    @SerializedName("spoken_languages") List<SpokenLanguageDTO> spokenLanguages,
    String tagline,
    String homepage,
    String imdb_id,
    @SerializedName("vote_count") Integer voteCount,
    String status


) {}
