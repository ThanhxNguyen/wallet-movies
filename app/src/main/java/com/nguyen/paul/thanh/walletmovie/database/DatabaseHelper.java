package com.nguyen.paul.thanh.walletmovie.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nguyen.paul.thanh.walletmovie.database.interfaces.MoviesTableConst;
import com.nguyen.paul.thanh.walletmovie.database.interfaces.GenresMoviesPivotTableConst;
import com.nguyen.paul.thanh.walletmovie.database.interfaces.GenresTableConst;

/**
 * This class will be used to create/upgrade sqlite database
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "favourite_movies.db";

    private Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(MoviesTableConst.SQL_CREATE_MOVIES);
        sqLiteDatabase.execSQL(GenresTableConst.SQL_CREATE_GENRES);
        sqLiteDatabase.execSQL(GenresMoviesPivotTableConst.SQL_CREATE_GENRES_MOVIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(MoviesTableConst.SQL_DELETE_MOVIES);
        sqLiteDatabase.execSQL(GenresTableConst.SQL_DELETE_GENRES);
        sqLiteDatabase.execSQL(GenresMoviesPivotTableConst.SQL_DELETE_GENRES_MOVIES);

        onCreate(sqLiteDatabase);
    }
}
