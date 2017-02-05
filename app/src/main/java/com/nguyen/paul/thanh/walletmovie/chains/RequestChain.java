package com.nguyen.paul.thanh.walletmovie.chains;

import com.nguyen.paul.thanh.walletmovie.model.Movie;

import java.util.List;

/**
 *
 */

public interface RequestChain {

    interface RequestChainComplete {
        void onSearchChainComplete(List<Movie> movieList);
    }

    void setNextChain(RequestChain nextChain);
    void search(String url);
}
