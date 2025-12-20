package org.example.dto;

import com.google.gson.annotations.SerializedName;

public record MovieDTO(
   int id,
   String title,
   String overview, // overview = description in entity
   @SerializedName("release_date") String releaseDate,
   @SerializedName("vote_average") double voteAverage, // voteAverage = imdbRating in entity
   @SerializedName("poster_path") String posterPath
) {}
