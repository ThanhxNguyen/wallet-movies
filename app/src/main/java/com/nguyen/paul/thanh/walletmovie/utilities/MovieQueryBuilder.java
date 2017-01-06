package com.nguyen.paul.thanh.walletmovie.utilities;

/**
 * Helper class to build query url for TMDB api.
 * This class won't work well with concurrency
 */

public class MovieQueryBuilder {

    private String apiKey = "1bd3f3a91c22eef0c9d9c15212f43593";
    private static MovieQueryBuilder mInstance;

    private MovieQueryBuilder() {

    }

    public static MovieQueryBuilder getInstance() {
        if(mInstance == null) {
            mInstance = new MovieQueryBuilder();
        }

        return mInstance;
    }

    //initialize discover search function from TMDB (themoviedb.org)
    public TMDBDiscoverQueryBuilder discover() {
        return TMDBDiscoverQueryBuilder.getInstance(apiKey);
    }

    public static String getImageBaseUrl(String sizeConfig) {
        String baseImageUrl = "http://image.tmdb.org/t/p/" + sizeConfig;

        return baseImageUrl;
    }
}
