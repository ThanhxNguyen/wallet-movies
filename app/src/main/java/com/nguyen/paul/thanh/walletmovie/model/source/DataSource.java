package com.nguyen.paul.thanh.walletmovie.model.source;

import com.nguyen.paul.thanh.walletmovie.model.Movie;

/**
 * Created by THANH on 17/02/2017.
 */

public interface DataSource {
    void addMovie(Movie movie);
    int updateMovie(Movie movie);
    void getMovies();
    Movie find(int movieId);
    void deleteMovie(Movie movie);
}
