package com.nguyen.paul.thanh.walletmovie.database.constants;

/**
 * Holds constants for genres_movies pivot table
 */

public class GenresMoviesPivotTableConst {

    //to avoid class instantiation
    private GenresMoviesPivotTableConst() {}

    public static final String GENRES_MOVIES_TABLE_NAME = "genres_movies";
    public static final String GENRES_MOVIES_COLUMN_GENRE_ID = "genre_id";
    public static final String GENRES_MOVIES_COLUMN_MOVIE_ID = "movie_id";

    public static final String SQL_CREATE_GENRES_MOVIES =
            "CREATE TABLE " + GENRES_MOVIES_TABLE_NAME + " (" +
                    GENRES_MOVIES_COLUMN_GENRE_ID + " INTEGER REFERENCES "+ GenresTableConst.GENRES_TABLE_NAME +"(" +
                        GenresTableConst.GENRES_COLUMN_ID +") ON DELETE CASCADE, " +
                    GENRES_MOVIES_COLUMN_MOVIE_ID + " INTEGER REFERENCES "+ MoviesTableConst.MOVIES_TABLE_NAME +"(" +
                        MoviesTableConst.MOVIES_COLUMN_ID +") ON DELETE CASCADE)";

    public static final String SQL_DELETE_GENRES_MOVIES =
            "DROP TABLE IF EXISTS " + GENRES_MOVIES_TABLE_NAME;

}
