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
    public void find(int id);
    public List<Movie> findAll();
    public long insert(Movie movie, List<Genre> genreList);
    public long update(int id);
    public int delete(int id);
    public SQLiteDatabase openDB();
    public void closeDB();
}
