package com.nguyen.paul.thanh.walletmovie.database.constants;

/**
 * This interface holds constants related to favourites table
 */

public class MoviesTableConst {

    //to avoid class instantiation
    private MoviesTableConst() {}

    public static final String MOVIES_TABLE_NAME = "movies";
    public static final String MOVIES_COLUMN_ID = "id";
    public static final String MOVIES_COLUMN_TITLE = "title";
    public static final String MOVIES_COLUMN_OVERVIEW = "overview";
    public static final String MOVIES_COLUMN_RELEASE_DATE = "release_date";
    public static final String MOVIES_COLUMN_RUNTIME = "runtime";
    public static final String MOVIES_COLUMN_COUNTRY = "country";
    public static final String MOVIES_COLUMN_STATUS = "status";
    public static final String MOVIES_COLUMN_VOTE_AVERAGE = "vote_average";
    public static final String MOVIES_COLUMN_POSTER_PATH = "poster_path";
    public static final String MOVIES_COLUMN_CREATED_AT = "created_at";

    public static final String SQL_CREATE_MOVIES =
            "CREATE TABLE " + MOVIES_TABLE_NAME + " (" +
                    MOVIES_COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY, " +
                    MOVIES_COLUMN_TITLE + " TEXT, " +
                    MOVIES_COLUMN_OVERVIEW + " TEXT, " +
                    MOVIES_COLUMN_RELEASE_DATE + " TEXT, " +
                    MOVIES_COLUMN_RUNTIME + " INTEGER, " +
                    MOVIES_COLUMN_COUNTRY + " TEXT, " +
                    MOVIES_COLUMN_STATUS + " TEXT, " +
                    MOVIES_COLUMN_VOTE_AVERAGE + " REAL, " +
                    MOVIES_COLUMN_POSTER_PATH + " TEXT, " +
                    MOVIES_COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP)";

    public static final String SQL_DELETE_MOVIES =
            "DROP TABLE IF EXISTS " + MOVIES_TABLE_NAME;
}
