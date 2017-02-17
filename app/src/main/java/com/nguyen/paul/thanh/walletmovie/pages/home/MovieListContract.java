package com.nguyen.paul.thanh.walletmovie.pages.home;

import com.nguyen.paul.thanh.walletmovie.BasePresenter;
import com.nguyen.paul.thanh.walletmovie.BaseView;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.model.source.MovieSourceManager;

import java.util.List;

/**
 * Created by THANH on 17/02/2017.
 */

public interface MovieListContract {

    interface View extends BaseView {
        void updateMovieList(List<Movie> movieList);
        void showSnackBarWithResult(MovieSourceManager.RESULT result);
    }

    interface Presenter extends BasePresenter {
        void getMovies(String url);
        void cancelRequests();
        void addMovieToFavourite(Movie movie);
    }
}
