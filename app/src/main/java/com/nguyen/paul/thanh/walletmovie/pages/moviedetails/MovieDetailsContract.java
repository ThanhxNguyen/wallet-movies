package com.nguyen.paul.thanh.walletmovie.pages.moviedetails;

import com.nguyen.paul.thanh.walletmovie.BasePresenter;
import com.nguyen.paul.thanh.walletmovie.BaseView;
import com.nguyen.paul.thanh.walletmovie.model.Cast;
import com.nguyen.paul.thanh.walletmovie.model.Movie;

import java.util.List;

/**
 * Created by THANH on 21/02/2017.
 */

public interface MovieDetailsContract {

    interface View extends BaseView {
        void showSnackBarWithResult(String message);
        void showSnackBarWithResult(int resStringId);
        void displayMovieTrailer(List<String> trailerList);
        void displayMoviePoster();
        void updateCastList(List<Cast> castList);
    }

    interface Presenter extends BasePresenter {
        void addMovieToFavourites(Movie movie);
        void getTrailers(String movieTrailerUrl);
        void getCasts(String movieCastsUrl);
    }
}
