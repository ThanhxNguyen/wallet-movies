package com.nguyen.paul.thanh.walletmovie;

import android.app.Application;

import com.nguyen.paul.thanh.walletmovie.model.Genre;

import java.util.ArrayList;
import java.util.List;

/**
 * The purpose of this class is to store the search query string from MainActivity
 */

public class WalletMovieApp extends Application {

    private String searchQuery = "";

    private List<Genre> mGenreListFromApi = new ArrayList<>();

    public List<Genre> getGenreListFromApi() {
        return mGenreListFromApi;
    }

    public void setGenreListFromApi(List<Genre> genreListFromApi) {
        mGenreListFromApi = genreListFromApi;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
}
