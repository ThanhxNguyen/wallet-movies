package com.nguyen.paul.thanh.walletmovie.utilities;

/**
 * /**
 * TheMovieDb.org api offers 3 different ways to search for movies including
 * - Discover: most powerful search and very flexible
 * - Search: text based search is the most common way
 * - Find: the last but still very useful way to find data is with existing external IDs
 * Reference: https://www.themoviedb.org/documentation/api
 *
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

    //initialize search (query search) funciton from TMDB (themoviedb.org)
    public TMDBSearchQueryBuilder search() {
        return TMDBSearchQueryBuilder.getInstance(apiKey);
    }

    public TMDBMoviesQueryBuilder movies() {
        return TMDBMoviesQueryBuilder.getInstance(apiKey);
    }

    //static method to get poster image for a movie with different sizes
    public String getImageBaseUrl(String sizeConfig) {

        return "http://image.tmdb.org/t/p/" + sizeConfig;
    }

    public String getGenreListUrl() {

        return "https://api.themoviedb.org/3/genre/movie/list?api_key=" + apiKey;
    }

}
