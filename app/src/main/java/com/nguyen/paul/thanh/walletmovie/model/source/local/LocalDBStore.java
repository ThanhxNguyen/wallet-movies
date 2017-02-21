package com.nguyen.paul.thanh.walletmovie.model.source.local;

import android.content.Context;
import android.os.AsyncTask;

import com.nguyen.paul.thanh.walletmovie.App;
import com.nguyen.paul.thanh.walletmovie.database.MoviesTableOperator;
import com.nguyen.paul.thanh.walletmovie.database.interfaces.DatabaseOperator;
import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.model.source.MovieStoreManager;
import com.nguyen.paul.thanh.walletmovie.model.source.SimpleDataStore;

import java.util.List;

import static com.nguyen.paul.thanh.walletmovie.model.source.MovieStoreManager.RESULT.FAIL_ADD_MOVIE;
import static com.nguyen.paul.thanh.walletmovie.model.source.MovieStoreManager.RESULT.FAIL_DELETE;
import static com.nguyen.paul.thanh.walletmovie.model.source.MovieStoreManager.RESULT.MOVIE_EXIST;
import static com.nguyen.paul.thanh.walletmovie.model.source.MovieStoreManager.RESULT.SUCCESS_ADD_MOVIE;
import static com.nguyen.paul.thanh.walletmovie.model.source.MovieStoreManager.RESULT.SUCCESS_DELETE;

/**
 * This class handles local data storage mechanism
 */

public class LocalDBStore extends SimpleDataStore {

    private MovieStoreManager.MovieOperationListener mListener;

    public LocalDBStore(MovieStoreManager.MovieOperationListener listener) {
        mListener = listener;
    }

    @Override
    public void addMovie(Movie movie) {
        List<Genre> genreList = ( (App) App.getAppContext()).getGenreListFromApi();
        new AddFavouriteTask(App.getAppContext(), mListener, genreList).execute(movie);
    }

    @Override
    public void getMovies() {
        new GetFavouriteMoviesTask(App.getAppContext(), mListener).execute();
    }

    @Override
    public void deleteMovie(Movie movie) {
        new DeleteMovieFromFavouritesTask(App.getAppContext(), mListener).execute(movie);
    }

    //handle data operation in background thread
    public static class DeleteMovieFromFavouritesTask extends AsyncTask<Movie, Void, Movie> {

        private Context mContext;
        private MovieStoreManager.MovieOperationListener mListener;

        public DeleteMovieFromFavouritesTask(Context context, MovieStoreManager.MovieOperationListener listener) {
            mContext = context;
            mListener = listener;
        }

        @Override
        protected Movie doInBackground(Movie... movies) {
            Movie movie = movies[0];
            DatabaseOperator databaseOperator = MoviesTableOperator.getInstance(mContext);
            int result = databaseOperator.delete(movie.getId());

            if(result > 0) {
                //successfully deleted
                return movie;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Movie movie) {
            if(movie != null) {
                //successfully deleted the movie
                mListener.onDeleteMovieComplete(SUCCESS_DELETE);
            } else {
                //failed to remove movie from favourites
                mListener.onDeleteMovieComplete(FAIL_DELETE);
            }
        }
    }

    //handle data operation in background thread
    public static class GetFavouriteMoviesTask extends AsyncTask<Void, Void, List<Movie>> {
        private Context mContext;
        private MovieStoreManager.MovieOperationListener mListener;

        public GetFavouriteMoviesTask(Context context, MovieStoreManager.MovieOperationListener listener) {
            mContext = context;
            mListener = listener;
        }

        @Override
        protected List<Movie> doInBackground(Void... voids) {
            List<Movie> movieList;
            //get movie from local database and return to onPostExecute (UI thread) to handle data
            DatabaseOperator databaseOperator = MoviesTableOperator.getInstance(mContext);
            movieList = databaseOperator.findAll();

            return movieList;
        }

        @Override
        protected void onPostExecute(List<Movie> movieList) {
            mListener.onGetMoviesComplete(movieList);
        }
    }

    //handle data operation in background thread
    public static class AddFavouriteTask extends AsyncTask<Movie, Void, MovieStoreManager.RESULT> {

        private Context mContext;
        private List<Genre> mGenreListFromApi;
        private MovieStoreManager.MovieOperationListener mListener;

        public AddFavouriteTask(Context context, MovieStoreManager.MovieOperationListener listener, List<Genre> genreList) {
            mContext = context;
            mGenreListFromApi = genreList;
            mListener = listener;
        }

        @Override
        protected MovieStoreManager.RESULT doInBackground(Movie... movies) {
            Movie movie = movies[0];

            //store movie in local db (SQLite)
            DatabaseOperator movieDBOperator = MoviesTableOperator.getInstance(mContext);
            long operationResult = movieDBOperator.insert(movie, mGenreListFromApi);
            //close database to avoid memory leaks
            movieDBOperator.closeDB();

            if(operationResult == movie.getId()) {
                return SUCCESS_ADD_MOVIE;

            } else if(operationResult == 0) {
                return MOVIE_EXIST;

            } else {
                return FAIL_ADD_MOVIE;

            }

        }

        @Override
        protected void onPostExecute(MovieStoreManager.RESULT result) {
            super.onPostExecute(result);
            //return result back by invoking callback
            mListener.onAddMovieComplete(result);
        }

    }
}
