package com.nguyen.paul.thanh.walletmovie.database.interfaces;

/**
 * Holds constants for genres_movies pivot table
 */

public interface GenresMoviesPivotTableConst {

    static final String TABLE_NAME = "genres_movies";
    static final String COLUMN_GENRE_ID = "genre_id";
    static final String COLUMN_MOVIE_ID = "movie_id";

    static final String SQL_CREATE_GENRES_MOVIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_GENRE_ID + " INTEGER REFERENCES "+ GenresTableConst.TABLE_NAME +"(" +
                        GenresTableConst.COLUMN_ID +") ON DELETE CASCADE, " +
                    COLUMN_MOVIE_ID + " INTEGER REFERENCES "+ MoviesTableConst.TABLE_NAME +"(" +
                        MoviesTableConst.COLUMN_ID +") ON DELETE CASCADE)";

    static final String SQL_DELETE_GENRES_MOVIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

}
