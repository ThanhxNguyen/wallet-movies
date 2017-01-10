package com.nguyen.paul.thanh.walletmovie.database.interfaces;

/**
 * Created by THANH on 10/01/2017.
 */

public interface GenresMoviesPivotTableConst {

    public static final String TABLE_NAME = "genres_movies";
    public static final String COLUMN_GENRE_ID = "genre_id";
    public static final String COLUMN_MOVIE_ID = "movie_id";

    public static final String SQL_CREATE_GENRES_MOVIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_GENRE_ID + " INTEGER REFERENCES "+ GenresTableConst.TABLE_NAME +"(" +
                        GenresTableConst.COLUMN_ID +") ON DELETE CASCADE, " +
                    COLUMN_MOVIE_ID + " INTEGER REFERENCES "+ MoviesTableConst.TABLE_NAME +"(" +
                        MoviesTableConst.COLUMN_ID +") ON DELETE CASCADE)";

    public static final String SQL_DELETE_GENRES_MOVIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

}
