package com.nguyen.paul.thanh.walletmovie.utilities;

import android.net.Uri;

import com.nguyen.paul.thanh.walletmovie.interfaces.CustomBuilder;

/**
 * TheMovieDb.org api offers 3 different ways to search for movies including
 * - Discover: most powerful search and very flexible
 * - Search: text based search is the most common way
 * - Find: the last but still very useful way to find data is with existing external IDs
 * Reference: https://www.themoviedb.org/documentation/api
 *
 * This builder class will build a query search for "discover" search function
 */

public class TMDBDiscoverQueryBuilder implements CustomBuilder {

    private static final String BASE_URL = "https://api.themoviedb.org/3/discover/movie";
    private static final int RESULT_LIMIT = 1;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static TMDBDiscoverQueryBuilder mInstance;
    private String apiKey;
    private Uri.Builder urlBuilder;

    private TMDBDiscoverQueryBuilder(String apiKey) {
        this.apiKey = apiKey;
        urlBuilder = Uri.parse(BASE_URL).buildUpon();
    }

    public static TMDBDiscoverQueryBuilder getInstance(String apiKey) {
        if(mInstance == null) {
            mInstance = new TMDBDiscoverQueryBuilder(apiKey);
        }

        return mInstance;
    }

    public TMDBDiscoverQueryBuilder mostPopular() {
        urlBuilder.appendQueryParameter("sort_by", "popularity.desc");
        return this;
    }

    public Uri.Builder showing() {
        return null;
    }

    @Override
    public String build() {
        //append api key to request param for authentication
        urlBuilder.appendQueryParameter("api_key", apiKey)
                    .appendQueryParameter("language", "en-US")
                    .appendQueryParameter("include_video", "false")
                    .appendQueryParameter("include_adult", "false")
                    .appendQueryParameter("page", Integer.toString(RESULT_LIMIT));
        String url = urlBuilder.build().toString();
        //clear url params
        urlBuilder.clearQuery();

        return url;
    }
}
