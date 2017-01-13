package com.nguyen.paul.thanh.walletmovie;

import android.app.Application;

/**
 * The purpose of this class is to store the search query string from MainActivity
 */

public class WalletMovieApp extends Application {

    private String searchQuery = "";

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
}
