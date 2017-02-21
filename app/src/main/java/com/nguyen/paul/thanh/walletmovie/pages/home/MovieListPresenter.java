package com.nguyen.paul.thanh.walletmovie.pages.home;

import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.model.source.MovieStoreManager;
import com.nguyen.paul.thanh.walletmovie.model.source.remote.TMDBSource;

import java.util.List;

/**
 * Presenter for movie list. Will handle communicate between movie list and data source
 */

public class MovieListPresenter implements MovieListContract.Presenter,
        TMDBSource.MovieRequestListener,
        MovieStoreManager.MovieOperationListener {

    private TMDBSource mTMDBSource;
    private MovieStoreManager mMovieStoreManager;
    private MovieListContract.View mView;

    MovieListPresenter(MovieListContract.View view) {
        mTMDBSource = new TMDBSource();
        mTMDBSource.setMovieRequestListener(this);
        mMovieStoreManager = new MovieStoreManager(this);
        mView = view;
    }

    @Override
    public void getMovies(String url) {
        mTMDBSource.getMovies(url);
    }

    @Override
    public void cancelRequests() {
        mTMDBSource.cancelRequests();
    }

    @Override
    public void addMovieToFavourite(Movie movie) {
        mMovieStoreManager.addMovie(movie);
    }

    @Override
    public void onMovieRequestComplete(List<Movie> movieList) {
        mView.updateMovieList(movieList);
    }

    //callbacks when complete movie operations such as add, delete, get etc...
    //it will pass the result back to view
    @Override
    public void onAddMovieComplete(MovieStoreManager.RESULT result) {
        switch (result) {
            case SUCCESS_ADD_MOVIE:
                mView.showSnackBarWithResult(R.string.success_add_movie);
                break;
            case FAIL_ADD_MOVIE:
                mView.showSnackBarWithResult(R.string.fail_add_movie);
                break;
            case MOVIE_EXIST:
                mView.showSnackBarWithResult(R.string.movie_exist);
                break;
            default:
                mView.showSnackBarWithResult(R.string.default_snackbar_error_message);
                break;
        }
    }

    @Override
    public void onDeleteMovieComplete(MovieStoreManager.RESULT result) {

    }

    @Override
    public void onGetMoviesComplete(List<Movie> movieList) {

    }

    @Override
    public void onErrorsOccur(String errorMessage) {
        mView.showSnackBarWithResult(errorMessage);
    }
}
