package com.nguyen.paul.thanh.walletmovie.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;

import java.util.ArrayList;
import java.util.List;

import static com.nguyen.paul.thanh.walletmovie.database.constants.GenresMoviesPivotTableConst.GENRES_MOVIES_COLUMN_GENRE_ID;
import static com.nguyen.paul.thanh.walletmovie.database.constants.GenresMoviesPivotTableConst.GENRES_MOVIES_COLUMN_MOVIE_ID;
import static com.nguyen.paul.thanh.walletmovie.database.constants.GenresMoviesPivotTableConst.GENRES_MOVIES_TABLE_NAME;
import static com.nguyen.paul.thanh.walletmovie.database.constants.GenresTableConst.GENRES_COLUMN_ID;
import static com.nguyen.paul.thanh.walletmovie.database.constants.GenresTableConst.GENRES_COLUMN_NAME;
import static com.nguyen.paul.thanh.walletmovie.database.constants.GenresTableConst.GENRES_TABLE_NAME;
import static com.nguyen.paul.thanh.walletmovie.database.constants.MoviesTableConst.MOVIES_COLUMN_COUNTRY;
import static com.nguyen.paul.thanh.walletmovie.database.constants.MoviesTableConst.MOVIES_COLUMN_ID;
import static com.nguyen.paul.thanh.walletmovie.database.constants.MoviesTableConst.MOVIES_COLUMN_OVERVIEW;
import static com.nguyen.paul.thanh.walletmovie.database.constants.MoviesTableConst.MOVIES_COLUMN_POSTER_PATH;
import static com.nguyen.paul.thanh.walletmovie.database.constants.MoviesTableConst.MOVIES_COLUMN_RELEASE_DATE;
import static com.nguyen.paul.thanh.walletmovie.database.constants.MoviesTableConst.MOVIES_COLUMN_RUNTIME;
import static com.nguyen.paul.thanh.walletmovie.database.constants.MoviesTableConst.MOVIES_COLUMN_STATUS;
import static com.nguyen.paul.thanh.walletmovie.database.constants.MoviesTableConst.MOVIES_COLUMN_TITLE;
import static com.nguyen.paul.thanh.walletmovie.database.constants.MoviesTableConst.MOVIES_COLUMN_VOTE_AVERAGE;
import static com.nguyen.paul.thanh.walletmovie.database.constants.MoviesTableConst.MOVIES_TABLE_NAME;

/**
 * This class follows singlton pattern and provides some helper methods to interact with sqlite database
 */

public class MoviesTableOperator extends SimpleSQLiteDatabaseOperator {

    private static MoviesTableOperator mInstance;
    private SQLiteOpenHelper mDatabase;

    private MoviesTableOperator(Context context) {
        mDatabase = new DatabaseHelper(context);
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
        if(mDatabase != null) {
            mDatabase.close();
        }
    }

    @Override
    public long insert(Movie movie, List<Genre> genreListFromApi) {
        SQLiteDatabase db = openDB();

        insertGenreValues(db, genreListFromApi);

        return insertMovieValues(db, movie);

    }

    public boolean emptyTables() {
        SQLiteDatabase db = openDB();
        //remove all entries. Because pivot table is cascade delete
        //no need to remove pivot entries
        int deletedMovieRows = db.delete(MOVIES_TABLE_NAME, "1", null);
        int deletedGenreRows = db.delete(GENRES_TABLE_NAME, "1", null);

        closeDB();

        return deletedMovieRows > 0 && deletedGenreRows > 0;
    }

    @Override
    public List<Movie> findAll() {
        List<Movie> movieList = new ArrayList<>();

        SQLiteDatabase db = openDB();
        String sql = "SELECT * FROM " + MOVIES_TABLE_NAME;
        Cursor cursor = db.rawQuery(sql, null);

        //loop through results and add each to movie list
        if(cursor.moveToFirst()) {//if the result is not empty
            //keep looping until no more row
            do {
                Movie movie = new Movie();
                movie.setId(cursor.getInt(cursor.getColumnIndex(MOVIES_COLUMN_ID)));
                movie.setTitle(cursor.getString(cursor.getColumnIndex(MOVIES_COLUMN_TITLE)));
                movie.setOverview(cursor.getString(cursor.getColumnIndex(MOVIES_COLUMN_OVERVIEW)));
                movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(MOVIES_COLUMN_RELEASE_DATE)));
                movie.setRuntime(cursor.getInt(cursor.getColumnIndex(MOVIES_COLUMN_RUNTIME)));
                movie.setCountry(cursor.getString(cursor.getColumnIndex(MOVIES_COLUMN_COUNTRY)));
                movie.setStatus(cursor.getString(cursor.getColumnIndex(MOVIES_COLUMN_STATUS)));
                movie.setVoteAverage(cursor.getDouble(cursor.getColumnIndex(MOVIES_COLUMN_VOTE_AVERAGE)));
                movie.setPosterPath(cursor.getString(cursor.getColumnIndex(MOVIES_COLUMN_POSTER_PATH)));

                //get genre values related to this movie
                String sqlForMovieGenres = "SELECT * FROM " + GENRES_MOVIES_TABLE_NAME + " gm inner join " +
                                            GENRES_TABLE_NAME + " g ON gm." + GENRES_MOVIES_COLUMN_GENRE_ID + " = g." +
                                            GENRES_COLUMN_ID + " WHERE gm." + GENRES_MOVIES_COLUMN_MOVIE_ID + " = " + movie.getId();
                Cursor pivotCursor = db.rawQuery(sqlForMovieGenres, null);
                if(pivotCursor.moveToFirst()) {
                    List<Genre> genres = new ArrayList<>();
                    do {
                        Genre genre = new Genre();
                        genre.setId(pivotCursor.getInt(pivotCursor.getColumnIndex(GENRES_COLUMN_ID)));
                        genre.setName(pivotCursor.getString(pivotCursor.getColumnIndex(GENRES_COLUMN_NAME)));
                        //add to genres list
                        genres.add(genre);

                    } while(pivotCursor.moveToNext());

                    //add to this movie
                    movie.setGenres(genres);
                    //close cursor
                    pivotCursor.close();
                }

                //add to movie list
                movieList.add(movie);

            } while(cursor.moveToNext());

        }
        cursor.close();
        //close database
        closeDB();

        return movieList;
    }

    @Override
    public int delete(int id) {
        SQLiteDatabase db = openDB();
        //remove genre ids associated with this movie id in pivot table genres_movies first
        //to avoid left-over when the movie get deleted
        db.delete(GENRES_MOVIES_TABLE_NAME, GENRES_MOVIES_COLUMN_MOVIE_ID +"="+id, null);
        //remove the movie
        return db.delete(MOVIES_TABLE_NAME, MOVIES_COLUMN_ID +"="+id, null);
    }

    private void insertGenreValues(SQLiteDatabase db, List<Genre> genreListFromApi) {
        //need to do some syncing operation here, for example sync every week/day to check if the local
        //genre values are still matching with genre values from the TMDB api


        //get number of rows in genres table. If it's empty, insert genre value from genre list from TMDB api
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + GENRES_TABLE_NAME, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);//get number of rows of first column
        if(count == 0) {
            //table is empty, insert genre list into local db
            if(genreListFromApi.size() > 0) {
                for(Genre g : genreListFromApi) {
                    ContentValues genreValues = new ContentValues();
                    genreValues.put(GENRES_COLUMN_ID, g.getId());
                    genreValues.put(GENRES_COLUMN_NAME, g.getName());

                    db.insert(GENRES_TABLE_NAME, null, genreValues);
                }
            }
        }
        cursor.close();
    }

    private long insertMovieValues(SQLiteDatabase db, Movie movie) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + MOVIES_TABLE_NAME + " WHERE " + MOVIES_COLUMN_ID + "=" + movie.getId(), null);
        if(cursor.getCount() > 0) {
            //close cursor
            cursor.close();
            //movie is already existed
            return Long.valueOf(0);
        } else {

            //Create content values
            ContentValues movieValues = new ContentValues();
            movieValues.put(MOVIES_COLUMN_ID, movie.getId());
            movieValues.put(MOVIES_COLUMN_TITLE, movie.getTitle());
            movieValues.put(MOVIES_COLUMN_OVERVIEW, movie.getOverview());
            movieValues.put(MOVIES_COLUMN_RELEASE_DATE, movie.getReleaseDate());
            movieValues.put(MOVIES_COLUMN_RUNTIME, movie.getRuntime());
            movieValues.put(MOVIES_COLUMN_COUNTRY, movie.getCountry());
            movieValues.put(MOVIES_COLUMN_STATUS, movie.getStatus());
            movieValues.put(MOVIES_COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
            movieValues.put(MOVIES_COLUMN_POSTER_PATH, movie.getPosterPath());

            long lastInsertedMovieId = db.insert(MOVIES_TABLE_NAME, null, movieValues);

            //update pivot table genres_movies
            for(Genre g : movie.getGenres()) {
                ContentValues genreMovieValues = new ContentValues();
                genreMovieValues.put(GENRES_MOVIES_COLUMN_GENRE_ID, g.getId());
                genreMovieValues.put(GENRES_MOVIES_COLUMN_MOVIE_ID, movie.getId());

                db.insert(GENRES_MOVIES_TABLE_NAME, null, genreMovieValues);
            }

            //close cursor
            cursor.close();

            return lastInsertedMovieId;
        }

    }
}
