package com.nguyen.paul.thanh.walletmovie.utilities;

import android.net.Uri;

import com.nguyen.paul.thanh.walletmovie.interfaces.CustomBuilder;

/**
 * Created by THANH on 14/01/2017.
 */

public class TMDBSearchQueryBuilder implements CustomBuilder {
    private static final String BASE_URL = "https://api.themoviedb.org/3/search/movie";
    private static final int RESULT_LIMIT = 1;
    private String apiKey;
    private Uri.Builder mUrlBuilder;

    private static TMDBSearchQueryBuilder mInstance;

    private TMDBSearchQueryBuilder(String apiKey) {
        this.apiKey = apiKey;
        mUrlBuilder = Uri.parse(BASE_URL).buildUpon();
    }

    public static TMDBSearchQueryBuilder getInstance(String apiKey) {
        if(mInstance == null) {
            mInstance = new TMDBSearchQueryBuilder(apiKey);
        }

        return mInstance;
    }

    public TMDBSearchQueryBuilder query(String query) {
        mUrlBuilder.appendQueryParameter("query", query);

        return mInstance;
    }


    @Override
    public String build() {
        //append api key to request param for authentication
        mUrlBuilder.appendQueryParameter("api_key", apiKey)
                .appendQueryParameter("language", "en-US")
                .appendQueryParameter("include_adult", "false")
                .appendQueryParameter("page", Integer.toString(RESULT_LIMIT));

        String url = mUrlBuilder.build().toString();
        //clear url params
        mUrlBuilder.clearQuery();

        return url;
    }
}
