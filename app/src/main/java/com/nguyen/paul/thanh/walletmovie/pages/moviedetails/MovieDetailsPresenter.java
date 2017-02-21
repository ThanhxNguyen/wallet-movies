package com.nguyen.paul.thanh.walletmovie.pages.moviedetails;

import android.content.Context;
import android.content.SharedPreferences;

import com.nguyen.paul.thanh.walletmovie.App;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.model.Cast;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.model.source.MovieStoreManager;
import com.nguyen.paul.thanh.walletmovie.model.source.remote.TMDBSource;

import java.util.List;

import static com.nguyen.paul.thanh.walletmovie.App.GLOBAL_PREF_KEY;

/**
 * Created by THANH on 21/02/2017.
 */

public class MovieDetailsPresenter implements MovieDetailsContract.Presenter,
                                            MovieStoreManager.MovieOperationListener {

    private MovieDetailsContract.View mView;
    private SharedPreferences mPrefs;
    private MovieStoreManager mMovieStoreManager;
    private TMDBSource mTMDBSource;

    public MovieDetailsPresenter(MovieDetailsContract.View view) {
        mView = view;
        mPrefs = App.getAppContext().getSharedPreferences(GLOBAL_PREF_KEY, Context.MODE_PRIVATE);
        mMovieStoreManager = new MovieStoreManager(this);
        mTMDBSource = new TMDBSource();
        //set callback when complete getting movie trailers
        mTMDBSource.setTrailersRequestListener(new TMDBSource.TrailersRequestListener() {
            @Override
            public void onTrailersRequestComplete(List<String> trailerList) {

                if(trailerList != null && trailerList.size() > 0) {
                    //trailers available
                    mView.displayMovieTrailer(trailerList);
                } else {
                    //no trailers available, display movie poster instead
                    mView.displayMoviePoster();
                }
            }
        });

        //set callback when complete getting casts related to movie
        mTMDBSource.setMovieCastsRequestListener(new TMDBSource.MovieCastsRequestListener() {
            @Override
            public void onMovieCastsRequestComplete(List<Cast> castList) {
                mView.updateCastList(castList);
            }
        });
    }

    @Override
    public void addMovieToFavourites(Movie movie) {
        mMovieStoreManager.addMovie(movie);
    }

    @Override
    public void getTrailers(String movieTrailerUrl) {
        mTMDBSource.getTrailers(movieTrailerUrl);
    }

    @Override
    public void getCasts(String movieCastsUrl) {
        mTMDBSource.getMovieCasts(movieCastsUrl);
    }

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
