package org.example.DTO;

import java.util.List;

public class TopRatedResponseDTO {
    // This is what we get in response from the API. API response is a wrapper-object.
    public int page;
    public List<MovieDTO> results;
    public int total_pages;
    public int total_results;
}
