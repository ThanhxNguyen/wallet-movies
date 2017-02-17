package com.nguyen.paul.thanh.walletmovie.model.source;

import com.nguyen.paul.thanh.walletmovie.model.Movie;

import java.util.List;

/**
 * Created by THANH on 17/02/2017.
 */

public class SimpleDataSource implements DataSourceContract {

    @Override
    public void addMovie(Movie movie) {
    }

    @Override
    public int updateMovie(Movie movie) {
        return 0;
    }

    @Override
    public List<Movie> getMovies() {
        return null;
    }

    @Override
    public Movie find(int movieId) {
        return null;
    }
}
