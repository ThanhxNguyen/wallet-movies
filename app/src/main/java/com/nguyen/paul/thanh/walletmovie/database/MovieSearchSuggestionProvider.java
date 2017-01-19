package com.nguyen.paul.thanh.walletmovie.database;

import android.content.SearchRecentSuggestionsProvider;

/**
 * This class provides search suggestions when searching for movies using search widget or search dialog
 * Reference: https://developer.android.com/guide/topics/search/adding-recent-query-suggestions.html
 */

public class MovieSearchSuggestionProvider extends SearchRecentSuggestionsProvider {

    public static final String AUTHORITY = "com.nguyen.paul.thanh.walletmovie.MovieSearchSuggestionProvider";
    public static final int MODE = DATABASE_MODE_QUERIES;

    public MovieSearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
