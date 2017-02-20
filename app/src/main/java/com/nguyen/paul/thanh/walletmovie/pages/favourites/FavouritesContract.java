package com.nguyen.paul.thanh.walletmovie.pages.favourites;

import com.nguyen.paul.thanh.walletmovie.BasePresenter;
import com.nguyen.paul.thanh.walletmovie.BaseView;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.model.source.MovieStoreManager;

import java.util.List;

/**
 * Contracts for favourite page view and presenter
 */

public interface FavouritesContract {

    interface View extends BaseView {
        void updateMovieList(List<Movie> movieList);
        void showSnackBarWithResult(MovieStoreManager.RESULT result);
        void notifyListChange();
    }

    interface Presenter extends BasePresenter {
        void getFavouriteMovies();
        void removeFirebaseListener();
        void removeMovieFromFavourites(Movie movie);
    }
}
