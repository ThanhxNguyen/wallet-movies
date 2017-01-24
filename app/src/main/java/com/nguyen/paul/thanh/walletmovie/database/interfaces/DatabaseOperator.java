package com.nguyen.paul.thanh.walletmovie.database.interfaces;

import android.database.sqlite.SQLiteDatabase;

import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;

import java.util.List;

/**
 * This interface defines methods for database operations.
 * The Purpose is to provide a uniform way for database operations.
 */

public interface DatabaseOperator {
    void find(int id);
    List<Movie> findAll();
    long insert(Movie movie, List<Genre> genreList);
    long update(int id);
    int delete(int id);
    SQLiteDatabase openDB();
    void closeDB();
}
