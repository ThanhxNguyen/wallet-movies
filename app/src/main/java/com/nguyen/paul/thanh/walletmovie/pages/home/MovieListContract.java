package com.nguyen.paul.thanh.walletmovie.pages.home;

import com.nguyen.paul.thanh.walletmovie.BasePresenter;
import com.nguyen.paul.thanh.walletmovie.BaseView;
import com.nguyen.paul.thanh.walletmovie.model.Movie;

import java.util.List;

/**
 * Contracts for movie list View and Presenter
 */

public interface MovieListContract {

    interface View extends BaseView {
        void updateMovieList(List<Movie> movieList);
        void showSnackBarWithResult(int resStringId);
        void showSnackBarWithResult(String message);
    }

    interface Presenter extends BasePresenter {
        void getMovies(String url);
        void cancelRequests();
        void addMovieToFavourite(Movie movie);
    }
}
