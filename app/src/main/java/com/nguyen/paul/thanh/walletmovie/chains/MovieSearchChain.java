package com.nguyen.paul.thanh.walletmovie.chains;

import com.nguyen.paul.thanh.walletmovie.model.Movie;

import java.util.List;

/**
 *
 */

public interface MovieSearchChain {

    //callback for passing movie list back when complete getting movies from api
    interface MoviesSearchChainListener {
        void onMoviesSearchComplete(List<Movie> movieList);
    }

    void setNextChain(MovieSearchChain nextChain);
    void search(String url);
}
