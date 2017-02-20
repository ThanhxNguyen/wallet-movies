package com.nguyen.paul.thanh.walletmovie.pages.home;

import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.model.source.MovieStoreManager;
import com.nguyen.paul.thanh.walletmovie.model.source.remote.TMDBSource;

import java.util.List;

/**
 * Created by THANH on 17/02/2017.
 */

public class MovieListPresenter implements MovieListContract.Presenter,
        TMDBSource.MovieRequestListener,
        MovieStoreManager.MovieOperationListener {

    private TMDBSource mTMDBSource;
    private MovieStoreManager mMovieSourceManager;
    private MovieListContract.View mView;

    MovieListPresenter(MovieListContract.View view) {
        mTMDBSource = new TMDBSource(this);
        mMovieSourceManager = new MovieStoreManager(this);
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
        mMovieSourceManager.addMovie(movie);
    }

    @Override
    public void onMovieRequestComplete(List<Movie> movieList) {
        mView.updateMovieList(movieList);
    }

    //callbacks when complete movie operations such as add, delete, get etc...
    //it will pass the result back to view
    @Override
    public void onAddMovieComplete(MovieStoreManager.RESULT result) {
        mView.showSnackBarWithResult(result);
    }

    @Override
    public void onDeleteMovieComplete(MovieStoreManager.RESULT result) {

    }

    @Override
    public void onGetMoviesComplete(List<Movie> movieList) {

    }

    @Override
    public void onErrorsOccur(String errorMessage) {

    }
}
