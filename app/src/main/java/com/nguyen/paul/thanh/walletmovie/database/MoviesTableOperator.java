package com.nguyen.paul.thanh.walletmovie.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nguyen.paul.thanh.walletmovie.database.interfaces.MoviesTableConst;
import com.nguyen.paul.thanh.walletmovie.database.interfaces.GenresMoviesPivotTableConst;
import com.nguyen.paul.thanh.walletmovie.database.interfaces.GenresTableConst;
import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;

import java.util.ArrayList;
import java.util.List;

/**
 * This class follows singlton pattern
 */

public class MoviesTableOperator extends SimpleSQLiteDatabaseOperator {

    private static MoviesTableOperator mInstance;
    private SQLiteOpenHelper mDatabase;
    private Context mContext;

    private MoviesTableOperator(Context context) {
        mContext = context;
        mDatabase = new DatabaseHelper(mContext);
    }

    public static synchronized MoviesTableOperator getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new MoviesTableOperator(context);
        }

        return mInstance;
    }

    @Override
    public SQLiteDatabase openDB() {
        return mDatabase.getWritableDatabase();
    }

    @Override
    public void closeDB() {
        mDatabase.close();
    }

    @Override
    public long insert(Movie movie, List<Genre> genreListFromApi) {
        SQLiteDatabase db = openDB();

        insertGenreValues(db, genreListFromApi);

        long lastInsertedMovieId = insertMovieValues(db, movie);

        return lastInsertedMovieId;

    }

    @Override
    public List<Movie> findAll() {
        List<Movie> movieList = new ArrayList<>();

        SQLiteDatabase db = openDB();
        String sql = "SELECT * FROM " + MoviesTableConst.TABLE_NAME;
        Cursor cursor = db.rawQuery(sql, null);

        //loop through results and add each to movie list
        if(cursor.moveToFirst()) {//if the result is not empty
            //keep looping until no more row
            do {
                Movie movie = new Movie();
                movie.setId(cursor.getInt(cursor.getColumnIndex(MoviesTableConst.COLUMN_ID)));
                movie.setTitle(cursor.getString(cursor.getColumnIndex(MoviesTableConst.COLUMN_TITLE)));
                movie.setOverview(cursor.getString(cursor.getColumnIndex(MoviesTableConst.COLUMN_OVERVIEW)));
                movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(MoviesTableConst.COLUMN_RELEASE_DATE)));
                movie.setRuntime(cursor.getInt(cursor.getColumnIndex(MoviesTableConst.COLUMN_RUNTIME)));
                movie.setCountry(cursor.getString(cursor.getColumnIndex(MoviesTableConst.COLUMN_COUNTRY)));
                movie.setStatus(cursor.getString(cursor.getColumnIndex(MoviesTableConst.COLUMN_STATUS)));
                movie.setVoteAverage(cursor.getDouble(cursor.getColumnIndex(MoviesTableConst.COLUMN_VOTE_AVERAGE)));
                movie.setPosterPath(cursor.getString(cursor.getColumnIndex(MoviesTableConst.COLUMN_POSTER_PATH)));

                //get genre values related to this movie
                String sqlForMovieGenres = "SELECT * FROM " + GenresMoviesPivotTableConst.TABLE_NAME + " gm inner join " +
                                            GenresTableConst.TABLE_NAME + " g ON gm." + GenresMoviesPivotTableConst.COLUMN_GENRE_ID + " = g." +
                                            GenresTableConst.COLUMN_ID + " WHERE gm." + GenresMoviesPivotTableConst.COLUMN_MOVIE_ID + " = " + movie.getId();
                Cursor pivotCursor = db.rawQuery(sqlForMovieGenres, null);
                if(pivotCursor.moveToFirst()) {
                    List<Genre> genres = new ArrayList<>();
                    do {
                        Genre genre = new Genre();
                        genre.setId(pivotCursor.getInt(pivotCursor.getColumnIndex(GenresTableConst.COLUMN_ID)));
                        genre.setName(pivotCursor.getString(pivotCursor.getColumnIndex(GenresTableConst.COLUMN_NAME)));
                        //add to genres list
                        genres.add(genre);

                    } while(pivotCursor.moveToNext());

                    //add to this movie
                    movie.setGenres(genres);
                }

                //add to movie list
                movieList.add(movie);

            } while(cursor.moveToNext());

        }

        return movieList;
    }

    @Override
    public int delete(int id) {
        SQLiteDatabase db = openDB();
        //remove genre ids associated with this movie id in pivot table genres_movies first
        //to avoid left-over when the movie get deleted
        db.delete(GenresMoviesPivotTableConst.TABLE_NAME, GenresMoviesPivotTableConst.COLUMN_MOVIE_ID+"="+id, null);
        //remove the movie
        int result = db.delete(MoviesTableConst.TABLE_NAME, MoviesTableConst.COLUMN_ID+"="+id, null);

        return result;
    }

    private void insertGenreValues(SQLiteDatabase db, List<Genre> genreListFromApi) {
        //need to do some syncing operation here, for example sync every week/day to check if the local
        //genre values are still matching with genre values from the TMDB api


        //get number of rows in genres table. If it's empty, insert genre value from genre list from TMDB api
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + GenresTableConst.TABLE_NAME, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);//get number of rows of first column
        if(count == 0) {
            //table is empty, insert genre list into local db
            if(genreListFromApi.size() > 0) {
                for(Genre g : genreListFromApi) {
                    ContentValues genreValues = new ContentValues();
                    genreValues.put(GenresTableConst.COLUMN_ID, g.getId());
                    genreValues.put(GenresTableConst.COLUMN_NAME, g.getName());

                    db.insert(GenresTableConst.TABLE_NAME, null, genreValues);
                }
            }
        }
    }

    private long insertMovieValues(SQLiteDatabase db, Movie movie) {
        //Create content values
        ContentValues movieValues = new ContentValues();
        movieValues.put(MoviesTableConst.COLUMN_ID, movie.getId());
        movieValues.put(MoviesTableConst.COLUMN_TITLE, movie.getTitle());
        movieValues.put(MoviesTableConst.COLUMN_OVERVIEW, movie.getOverview());
        movieValues.put(MoviesTableConst.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        movieValues.put(MoviesTableConst.COLUMN_RUNTIME, movie.getRuntime());
        movieValues.put(MoviesTableConst.COLUMN_COUNTRY, movie.getCountry());
        movieValues.put(MoviesTableConst.COLUMN_STATUS, movie.getStatus());
        movieValues.put(MoviesTableConst.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
        movieValues.put(MoviesTableConst.COLUMN_POSTER_PATH, movie.getPosterPath());

        long lastInsertedMovieId = db.insert(MoviesTableConst.TABLE_NAME, null, movieValues);

        //update pivot table genres_movies
        for(Genre g : movie.getGenres()) {
            ContentValues genreMovieValues = new ContentValues();
            genreMovieValues.put(GenresMoviesPivotTableConst.COLUMN_GENRE_ID, g.getId());
            genreMovieValues.put(GenresMoviesPivotTableConst.COLUMN_MOVIE_ID, movie.getId());

            db.insert(GenresMoviesPivotTableConst.TABLE_NAME, null, genreMovieValues);
        }

        return lastInsertedMovieId;
    }
}
