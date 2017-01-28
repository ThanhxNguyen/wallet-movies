package com.nguyen.paul.thanh.walletmovie;

import android.app.Application;

import com.nguyen.paul.thanh.walletmovie.model.Genre;

import java.util.ArrayList;
import java.util.List;

/**
 * The purpose of this class is to store the search query string from MainActivity
 */

public class App extends Application {

    //global constants
    public static final String GLOBAL_PREF_KEY = "global_preference_key";

    public static final int MOVIE_NAME_SORT = 1;
    public static final int MOVIE_DATE_SORT = 2;
    public static final int MOVIE_VOTE_SORT = 3;

    public static final String GUEST_MODE_PREF_KEY = "guest_mode_preference_key";
    public static final String FIRST_TIME_USER_PREF_KEY = "first_time_user_preference_key";
    public static final String DISPLAY_LIST_IN_GRID_KEY = "display_list_in_grid_key";

    public static final String MOVIE_SORT_SETTINGS_KEY = "movie_sort_setttings_key";


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
