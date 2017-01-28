package com.nguyen.paul.thanh.walletmovie.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.nguyen.paul.thanh.walletmovie.database.constants.GenresMoviesPivotTableConst.SQL_CREATE_GENRES_MOVIES;
import static com.nguyen.paul.thanh.walletmovie.database.constants.GenresMoviesPivotTableConst.SQL_DELETE_GENRES_MOVIES;
import static com.nguyen.paul.thanh.walletmovie.database.constants.GenresTableConst.SQL_CREATE_GENRES;
import static com.nguyen.paul.thanh.walletmovie.database.constants.GenresTableConst.SQL_DELETE_GENRES;
import static com.nguyen.paul.thanh.walletmovie.database.constants.MoviesTableConst.SQL_CREATE_MOVIES;
import static com.nguyen.paul.thanh.walletmovie.database.constants.MoviesTableConst.SQL_DELETE_MOVIES;

/**
 * This class will be used to create/upgrade sqlite database
 */

@SuppressWarnings("WeakerAccess")
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "favourite_movies.db";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES);
        sqLiteDatabase.execSQL(SQL_CREATE_GENRES);
        sqLiteDatabase.execSQL(SQL_CREATE_GENRES_MOVIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(SQL_DELETE_MOVIES);
        sqLiteDatabase.execSQL(SQL_DELETE_GENRES);
        sqLiteDatabase.execSQL(SQL_DELETE_GENRES_MOVIES);

        onCreate(sqLiteDatabase);
    }
}
