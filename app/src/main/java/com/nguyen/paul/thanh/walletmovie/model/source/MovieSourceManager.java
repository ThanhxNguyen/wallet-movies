package com.nguyen.paul.thanh.walletmovie.model.source;

import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.model.source.local.LocalDBSource;

/**
 * Created by THANH on 17/02/2017.
 */

public class MovieSourceManager extends SimpleDataSource {

    private LocalDBSource mLocalDBSource;
    private MovieOperationListener mListener;

    public interface MovieOperationListener {
        void onAddMovieComplete(RESULT result);
        void onDeleteMovieComplete();
        void onGetMoviesComplete();
    }

    public enum RESULT {
        SUCCESS_ADD_MOVIE, FAIL_ADD_MOVIE, MOVIE_EXIST
    }

    public MovieSourceManager(MovieOperationListener listener) {
        mListener = listener;
        mLocalDBSource = new LocalDBSource(mListener);
    }

    @Override
    public void addMovie(Movie movie) {
        //do some checking here wether add locally or remotely
        mLocalDBSource.addMovie(movie);
    }
}
