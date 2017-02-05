package com.nguyen.paul.thanh.walletmovie.utilities;

import android.app.Activity;

import com.nguyen.paul.thanh.walletmovie.chains.RequestChain;
import com.nguyen.paul.thanh.walletmovie.chains.SearchMoviesByCast;
import com.nguyen.paul.thanh.walletmovie.chains.SearchMoviesByName;

/**
 * Created by THANH on 28/01/2017.
 */

public class MoviesMultiSearch {

    private RequestChain mSearchMoviesByName;

    public MoviesMultiSearch(Activity activity, RequestChain.RequestChainComplete listener, NetworkRequest networkRequest, String requestTag) {
        mSearchMoviesByName = new SearchMoviesByName(activity, listener, networkRequest, requestTag);
        RequestChain searchMoviesByCast = new SearchMoviesByCast(activity, listener, networkRequest, requestTag);

        //setting chain of responsibility for searching movies
        mSearchMoviesByName.setNextChain(searchMoviesByCast);
    }

    public void search(String url) {
        mSearchMoviesByName.search(url);
    }
}
