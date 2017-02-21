package com.nguyen.paul.thanh.walletmovie.model.source;

import com.nguyen.paul.thanh.walletmovie.model.Movie;

/*
 * interface for data storage mechanism
 */
public interface DataStore {
    void addMovie(Movie movie);
    int updateMovie(Movie movie);
    void getMovies();
    Movie find(int movieId);
    void deleteMovie(Movie movie);
}
