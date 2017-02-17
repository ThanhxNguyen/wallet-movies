package com.nguyen.paul.thanh.walletmovie.model.source.local;

import android.content.Context;
import android.os.AsyncTask;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nguyen.paul.thanh.walletmovie.App;
import com.nguyen.paul.thanh.walletmovie.database.MoviesTableOperator;
import com.nguyen.paul.thanh.walletmovie.database.interfaces.DatabaseOperator;
import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.model.source.MovieSourceManager;
import com.nguyen.paul.thanh.walletmovie.model.source.SimpleDataSource;

import java.util.List;

import static com.nguyen.paul.thanh.walletmovie.model.source.MovieSourceManager.RESULT.FAIL_ADD_MOVIE;
import static com.nguyen.paul.thanh.walletmovie.model.source.MovieSourceManager.RESULT.FAIL_DELETE;
import static com.nguyen.paul.thanh.walletmovie.model.source.MovieSourceManager.RESULT.MOVIE_EXIST;
import static com.nguyen.paul.thanh.walletmovie.model.source.MovieSourceManager.RESULT.SUCCESS_ADD_MOVIE;
import static com.nguyen.paul.thanh.walletmovie.model.source.MovieSourceManager.RESULT.SUCCESS_DELETE;

/**
 * Created by THANH on 17/02/2017.
 */

public class LocalDBSource extends SimpleDataSource {

    private MovieSourceManager.MovieOperationListener mListener;

    public LocalDBSource(MovieSourceManager.MovieOperationListener listener) {
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
        private MovieSourceManager.MovieOperationListener mListener;

        public DeleteMovieFromFavouritesTask(Context context, MovieSourceManager.MovieOperationListener listener) {
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
//                Utils.createSnackBar(getResources(), mViewContainer, "Error! Sorry failed to remove this movie").show();
            }
        }
    }


    public static class GetFavouriteMoviesTask extends AsyncTask<Void, Void, List<Movie>> {
        private Context mContext;
        private MovieSourceManager.MovieOperationListener mListener;

        public GetFavouriteMoviesTask(Context context, MovieSourceManager.MovieOperationListener listener) {
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

    //AsyncTask to handle adding movies locally or remotely
    public static class AddFavouriteTask extends AsyncTask<Movie, Void, MovieSourceManager.RESULT> {

        private Context mContext;
        private FirebaseAuth mAuth;
        private DatabaseReference mUsersRef;
        private List<Genre> mGenreListFromApi;
        private MovieSourceManager.MovieOperationListener mListener;

        public AddFavouriteTask(Context context, MovieSourceManager.MovieOperationListener listener, List<Genre> genreList) {
            mContext = context;
            mGenreListFromApi = genreList;
            mListener = listener;
            mAuth = FirebaseAuth.getInstance();
            FirebaseDatabase firebaseDB = FirebaseDatabase.getInstance();
            mUsersRef = firebaseDB.getReference("users");
        }

        @Override
        protected MovieSourceManager.RESULT doInBackground(Movie... movies) {
            Movie movie = movies[0];

            //user is in guest mode
            //store movie in local db (SQLite)
            DatabaseOperator movieDBOperator = MoviesTableOperator.getInstance(mContext);
            long operationResult = movieDBOperator.insert(movie, mGenreListFromApi);
            //close database to avoid memory leaks
            movieDBOperator.closeDB();

            if(operationResult == movie.getId()) {
                //successfully added movie, return result back to presenter
                return SUCCESS_ADD_MOVIE;

            } else if(operationResult == 0) {
                //movie already existed, return result back to presenter
                return MOVIE_EXIST;

            } else {
                //Failed to add movie, return result back to presenter
                return FAIL_ADD_MOVIE;

            }

        }

        @Override
        protected void onPostExecute(MovieSourceManager.RESULT result) {
            super.onPostExecute(result);
            mListener.onAddMovieComplete(result);
        }

    }
}
