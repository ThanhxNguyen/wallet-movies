package com.nguyen.paul.thanh.walletmovie.model.source;

import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nguyen.paul.thanh.walletmovie.App;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.model.source.local.LocalDBSource;
import com.nguyen.paul.thanh.walletmovie.model.source.remote.FirebaseDBSource;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.nguyen.paul.thanh.walletmovie.App.GLOBAL_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GUEST_MODE_PREF_KEY;

/**
 * Created by THANH on 17/02/2017.
 */

public class MovieSourceManager extends SimpleDataSource {

    private DataSource mLocalDBSource;
    private MovieOperationListener mListener;
    private DataSource mFirebaseDBSource;

    public interface MovieOperationListener {
        void onAddMovieComplete(RESULT result);
        void onDeleteMovieComplete(RESULT result);
        void onGetMoviesComplete(List<Movie> movieList);
    }

    public enum RESULT {
        SUCCESS_ADD_MOVIE, FAIL_ADD_MOVIE, MOVIE_EXIST, SUCCESS_DELETE, FAIL_DELETE
    }

    public MovieSourceManager(MovieOperationListener listener) {
        mListener = listener;
        mLocalDBSource = new LocalDBSource(mListener);
        mFirebaseDBSource = new FirebaseDBSource(mListener);
    }

    @Override
    public void getMovies() {
        SharedPreferences prefs = App.getAppContext().getSharedPreferences(GLOBAL_PREF_KEY, MODE_PRIVATE);
        boolean isGuest = prefs.getBoolean(GUEST_MODE_PREF_KEY, true);

        if(isGuest) {
            //get favourite movies from local DB
            mLocalDBSource.getMovies();
        } else {
            //get favourite movies from Firebase DB
            mFirebaseDBSource.getMovies();
        }
    }

    @Override
    public void addMovie(Movie movie) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if(user != null) {
            //user is signed in, store the movie on Firebase
            mFirebaseDBSource.addMovie(movie);
        } else {
            //do some checking here wether add locally or remotely
            mLocalDBSource.addMovie(movie);
        }
    }

    @Override
    public void deleteMovie(Movie movie) {
        SharedPreferences prefs = App.getAppContext().getSharedPreferences(GLOBAL_PREF_KEY, MODE_PRIVATE);
        boolean isGuest = prefs.getBoolean(GUEST_MODE_PREF_KEY, true);

        if(isGuest) {
            mLocalDBSource.deleteMovie(movie);
        } else {
            mFirebaseDBSource.deleteMovie(movie);
        }
    }

    public void removeFirebaseListener() {
        ( (FirebaseDBSource) mFirebaseDBSource).removeFirebaseListener();
    }
}
