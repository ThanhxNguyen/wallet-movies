package com.nguyen.paul.thanh.walletmovie.utilities;

import com.nguyen.paul.thanh.walletmovie.chains.MovieSearchChain;
import com.nguyen.paul.thanh.walletmovie.chains.SearchMoviesByCast;
import com.nguyen.paul.thanh.walletmovie.chains.SearchMoviesByName;

/**
 * Created by THANH on 28/01/2017.
 */

public class MoviesMultiSearch {

    private MovieSearchChain mSearchMoviesByName;

    public MoviesMultiSearch(MovieSearchChain.MoviesSearchChainListener listener, String requestTag) {
        mSearchMoviesByName = new SearchMoviesByName(listener, requestTag);
        MovieSearchChain searchMoviesByCast = new SearchMoviesByCast(listener, requestTag);

        //setting chain of responsibility for searching movies
        mSearchMoviesByName.setNextChain(searchMoviesByCast);
    }

    public void search(String url) {
        mSearchMoviesByName.search(url);
    }
}
