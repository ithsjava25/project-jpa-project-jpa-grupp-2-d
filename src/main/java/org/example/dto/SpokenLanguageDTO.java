package org.example.dto;

import com.google.gson.annotations.SerializedName;

public record SpokenLanguageDTO(
    @SerializedName("english_name") String englishName,
    @SerializedName("iso_639_1") String iso6391,
    String name
) {}
