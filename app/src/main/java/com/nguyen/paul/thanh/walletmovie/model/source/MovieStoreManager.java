package com.nguyen.paul.thanh.walletmovie.model.source;

import android.content.SharedPreferences;

import com.nguyen.paul.thanh.walletmovie.App;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.model.source.local.LocalDBStore;
import com.nguyen.paul.thanh.walletmovie.model.source.remote.FirebaseDBStore;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.nguyen.paul.thanh.walletmovie.App.GLOBAL_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GUEST_MODE_PREF_KEY;

/**
 * Created by THANH on 17/02/2017.
 */

public class MovieStoreManager extends SimpleDataStore {

    private DataStore mLocalDBStore;
    private MovieOperationListener mListener;
    private DataStore mFirebaseDBStore;
    private SharedPreferences mPrefs;

    public interface MovieOperationListener {
        void onAddMovieComplete(RESULT result);
        void onDeleteMovieComplete(RESULT result);
        void onGetMoviesComplete(List<Movie> movieList);
        void onErrorsOccur(String errorMessage);
    }

    public enum RESULT {
        SUCCESS_ADD_MOVIE, FAIL_ADD_MOVIE, MOVIE_EXIST, SUCCESS_DELETE, FAIL_DELETE
    }

    public MovieStoreManager(MovieOperationListener listener) {
        mListener = listener;
        mLocalDBStore = new LocalDBStore(mListener);
        mFirebaseDBStore = new FirebaseDBStore(mListener);

        mPrefs = App.getAppContext().getSharedPreferences(GLOBAL_PREF_KEY, MODE_PRIVATE);
    }

    @Override
    public void getMovies() {
        boolean isGuest = mPrefs.getBoolean(GUEST_MODE_PREF_KEY, true);

        if(isGuest) {
            //get favourite movies from local DB
            mLocalDBStore.getMovies();
        } else {
            //get favourite movies from Firebase DB
            mFirebaseDBStore.getMovies();
        }
    }

    @Override
    public void addMovie(Movie movie) {
        boolean isGuest = mPrefs.getBoolean(GUEST_MODE_PREF_KEY, true);

        if(isGuest) {
            //store movie locally
            mLocalDBStore.addMovie(movie);
        } else {
            //store movie remotely on cloud
            mFirebaseDBStore.addMovie(movie);
        }
    }

    @Override
    public void deleteMovie(Movie movie) {
        SharedPreferences prefs = App.getAppContext().getSharedPreferences(GLOBAL_PREF_KEY, MODE_PRIVATE);
        boolean isGuest = prefs.getBoolean(GUEST_MODE_PREF_KEY, true);

        if(isGuest) {
            mLocalDBStore.deleteMovie(movie);
        } else {
            mFirebaseDBStore.deleteMovie(movie);
        }
    }

    public void removeFirebaseListener() {
        ( (FirebaseDBStore) mFirebaseDBStore).removeFirebaseListener();
    }
}
