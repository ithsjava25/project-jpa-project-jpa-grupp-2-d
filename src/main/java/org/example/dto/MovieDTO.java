package org.example.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record MovieDTO(
   int id,
   String title,
   String overview, // overview = description in entity
   @SerializedName("release_date") String releaseDate,
   @SerializedName("vote_average") double voteAverage, // voteAverage = imdbRating in entity
   @SerializedName("poster_path") String posterPath,
   List<Integer> genreId
) {}
