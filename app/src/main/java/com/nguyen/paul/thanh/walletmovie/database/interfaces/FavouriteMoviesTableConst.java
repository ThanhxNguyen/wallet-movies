package com.nguyen.paul.thanh.walletmovie.database.interfaces;

/**
 * This interface holds constants related to favourites table
 */

public interface FavouriteMoviesTableConst {

    public static final String TABLE_NAME = "movies";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_OVERVIEW = "overview";
    public static final String COLUMN_RELEASE_DATE = "release_date";
    public static final String COLUMN_RUNTIME = "runtime";
    public static final String COLUMN_COUNTRY = "country";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_VOTE_AVERAGE = "vote_average";
    public static final String COLUMN_POSTER_PATH = "poster_path";

    public static final String SQL_CREATE_MOVIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY," +
                    COLUMN_TITLE + " TEXT," +
                    COLUMN_OVERVIEW + " TEXT)" +
                    COLUMN_RELEASE_DATE + " TEXT)" +
                    COLUMN_RUNTIME + " INTEGER)" +
                    COLUMN_COUNTRY + " TEXT)" +
                    COLUMN_STATUS + " TEXT)" +
                    COLUMN_VOTE_AVERAGE + " REAL)" +
                    COLUMN_POSTER_PATH + " TEXT)";

    public static final String SQL_DELETE_MOVIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
