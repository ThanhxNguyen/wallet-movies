package com.nguyen.paul.thanh.walletmovie.utilities;

import android.net.Uri;

import com.nguyen.paul.thanh.walletmovie.interfaces.CustomBuilder;

/**
 * This class provides helper methods to make a http request call to TMDB (themoviedb.org) api.
 * TMDB api provides a range of helper api end points such as get most popular movies,
 * get currently showing movies in cinemas etc...
 */

public class TMDBMoviesQueryBuilder implements CustomBuilder {

    private static TMDBMoviesQueryBuilder mInstance;
    private String apiKey;
    private static final String BASE_URL = "https://api.themoviedb.org";
    private static final int RESULT_LIMIT = 1;
    private Uri.Builder mUrlBuilder;

    private TMDBMoviesQueryBuilder(String apiKey) {
        this.apiKey = apiKey;
        mUrlBuilder = Uri.parse(BASE_URL).buildUpon();
    }

    public static TMDBMoviesQueryBuilder getInstance(String apiKey) {
        if(mInstance == null) {
            mInstance = new TMDBMoviesQueryBuilder(apiKey);
        }

        return mInstance;
    }

    public TMDBMoviesQueryBuilder showing() {
        mUrlBuilder.path("/3/movie/now_playing");
        return this;
    }

    public TMDBMoviesQueryBuilder popular() {
        mUrlBuilder.path("/3/movie/popular");
        return this;
    }

    public TMDBMoviesQueryBuilder upcoming() {
        mUrlBuilder.path("/3/movie/upcoming");
        return this;
    }

    public TMDBMoviesQueryBuilder getVideos(int movieId) {
        mUrlBuilder.path("/3/movie/" + movieId + "/videos");
        return this;
    }

    public TMDBMoviesQueryBuilder getCasts(int movieId) {
        mUrlBuilder.path("/3/movie/" + movieId + "/credits");
        return this;
    }

    public TMDBMoviesQueryBuilder getSingleCast(int castId) {
        mUrlBuilder.path("/3/person/" + castId);
        return this;
    }

    @Override
    public String build() {
        //append api key to request param for authentication
        mUrlBuilder.appendQueryParameter("api_key", apiKey)
                .appendQueryParameter("language", "en-US")
                .appendQueryParameter("page", Integer.toString(RESULT_LIMIT));
        String url = mUrlBuilder.build().toString();
        //clear url params
        mUrlBuilder.clearQuery();

        return url;
    }
}
