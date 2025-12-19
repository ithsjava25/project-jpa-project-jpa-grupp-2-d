package org.example.ui;

import java.util.List;
import org.example.dto.MovieDTO;

public interface MovieDataSource {
    List<MovieDTO> getTopRatedMovies();


}
