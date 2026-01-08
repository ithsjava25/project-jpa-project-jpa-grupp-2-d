package org.example.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record SearchResponseDTO(
    int page,
    List<MovieDTO> results,
    int total_pages,
    int total_results
) {}
