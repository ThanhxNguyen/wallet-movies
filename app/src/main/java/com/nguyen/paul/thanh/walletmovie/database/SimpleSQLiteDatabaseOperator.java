package com.nguyen.paul.thanh.walletmovie.database;

import android.database.sqlite.SQLiteDatabase;

import com.nguyen.paul.thanh.walletmovie.database.interfaces.DatabaseOperator;
import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;

import java.util.List;

/**
 * The purpose of this class is to provide a convenient way to implement DatabaseOperator.
 * Extend this class and override methods when needed. Therefore, no need to implement all
 * methods when only need some certain methods.
 */

public class SimpleSQLiteDatabaseOperator implements DatabaseOperator {
    @Override
    public void find(int id) {

    }

    @Override
    public List<Movie> findAll() {
        return null;
    }

    @Override
    public long insert(Movie movie, List<Genre> genreList) {
        return 0;
    }

    @Override
    public long update(int id) {
        return 0;
    }

    @Override
    public int delete(int id) {
        return 0;
    }

    @Override
    public SQLiteDatabase openDB() {
        return null;
    }

    @Override
    public void closeDB() {

    }
}
