package com.nguyen.paul.thanh.walletmovie.model.source;

import com.nguyen.paul.thanh.walletmovie.model.Movie;

/**
 * Created by THANH on 17/02/2017.
 */

public class SimpleDataSource implements DataSource {

    @Override
    public void addMovie(Movie movie) {
    }

    @Override
    public int updateMovie(Movie movie) {
        return 0;
    }

    @Override
    public void getMovies() {
    }

    @Override
    public Movie find(int movieId) {
        return null;
    }

    @Override
    public void deleteMovie(Movie movie) {

    }
}
