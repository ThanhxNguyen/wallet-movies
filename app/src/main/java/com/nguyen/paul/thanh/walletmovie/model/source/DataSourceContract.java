package com.nguyen.paul.thanh.walletmovie.model.source;

import com.nguyen.paul.thanh.walletmovie.model.Movie;

import java.util.List;

/**
 * Created by THANH on 17/02/2017.
 */

public interface DataSourceContract {
    void addMovie(Movie movie);
    int updateMovie(Movie movie);
    List<Movie> getMovies();
    Movie find(int movieId);
}
