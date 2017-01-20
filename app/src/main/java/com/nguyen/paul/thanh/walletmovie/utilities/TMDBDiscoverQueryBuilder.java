package com.nguyen.paul.thanh.walletmovie.utilities;

import android.net.Uri;

import com.nguyen.paul.thanh.walletmovie.interfaces.CustomBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * TheMovieDb.org api offers 3 different ways to search for movies including
 * - Discover: most powerful search and very flexible
 * - Search: text based search is the most common way
 * - Find: the last but still very useful way to find data is with existing external IDs
 * Reference: https://www.themoviedb.org/documentation/api
 *
 * This builder class will build a query search for "discover" search function.
 */

public class TMDBDiscoverQueryBuilder implements CustomBuilder {

    private static final String BASE_URL = "https://api.themoviedb.org/3/discover/movie";
    private static final int RESULT_LIMIT = 1;
    //constant indicates how many more days will be added to start date for "showing movies" url
    private static final int MOVIE_SHOWING_TIME_PERIOD = 7;
    //constant indicates start date for "showing movies" url
    private static final int MOVIE_SHOWING_TIME_START_FROM = -7;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static TMDBDiscoverQueryBuilder mInstance;
    private String apiKey;
    private Uri.Builder mUrlBuilder;

    TMDBDiscoverQueryBuilder(String apiKey) {
        this.apiKey = apiKey;
        mUrlBuilder = Uri.parse(BASE_URL).buildUpon();
    }

    public static TMDBDiscoverQueryBuilder getInstance(String apiKey) {
        if(mInstance == null) {
            mInstance = new TMDBDiscoverQueryBuilder(apiKey);
        }

        return mInstance;
    }

    public TMDBDiscoverQueryBuilder mostPopular() {
        //get date back in one year
        String startDate = addOrMinusDaysToCurrentDate(null, -365);
        mUrlBuilder.appendQueryParameter("primary_release_date.gte", startDate);
        mUrlBuilder.appendQueryParameter("sort_by", "popularity.desc");
        return this;
    }

    public TMDBDiscoverQueryBuilder showing() {
        String endDate = addOrMinusDaysToCurrentDate(null, MOVIE_SHOWING_TIME_PERIOD);
        String startDate = addOrMinusDaysToCurrentDate(null, MOVIE_SHOWING_TIME_START_FROM);
        mUrlBuilder.appendQueryParameter("primary_release_date.gte", startDate);
        mUrlBuilder.appendQueryParameter("primary_release_date.lte", endDate);

        return this;
    }

    public TMDBDiscoverQueryBuilder upcoming() {
        String startDate = addOrMinusDaysToCurrentDate(null, MOVIE_SHOWING_TIME_PERIOD + 1);
        String endDate = addOrMinusDaysToCurrentDate(startDate, 30);
        mUrlBuilder.appendQueryParameter("primary_release_date.gte", startDate);
        mUrlBuilder.appendQueryParameter("primary_release_date.lte", endDate);

        return this;
    }

    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Calendar calendar = Calendar.getInstance();
        Date currentTime = new Date(calendar.getTimeInMillis());

        String currentDate = dateFormat.format(calendar.getTime());

        return currentDate;
    }

    private String addOrMinusDaysToCurrentDate(String startDate, int numDays) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Calendar calendar = Calendar.getInstance();
        Date currentTime = null;

        if(startDate == null) {
            currentTime = new Date(calendar.getTimeInMillis());
        } else {
            try {
                currentTime = dateFormat.parse(startDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        calendar.setTime(currentTime);
        calendar.add(Calendar.DAY_OF_MONTH, numDays);

        String newDate = dateFormat.format(calendar.getTime());

        return newDate;
    }

    public TMDBDiscoverQueryBuilder moviesRelatedTo(int castId) {
        mUrlBuilder.appendQueryParameter("with_cast", String.valueOf(castId));
        return this;
    }

    @Override
    public String build() {
        //append api key to request param for authentication
        mUrlBuilder.appendQueryParameter("api_key", apiKey)
                    .appendQueryParameter("language", "en-US")
                    .appendQueryParameter("include_video", "false")
                    .appendQueryParameter("include_adult", "false")
                    .appendQueryParameter("page", Integer.toString(RESULT_LIMIT));
        String url = mUrlBuilder.build().toString();
        //clear url params
        mUrlBuilder.clearQuery();

        return url;
    }
}
