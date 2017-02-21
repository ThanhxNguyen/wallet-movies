package com.nguyen.paul.thanh.walletmovie.pages.favourites;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.model.source.MovieStoreManager;

import java.util.List;

/**
 * Presenter in MVP for favourite page
 */

public class FavouritesPresenter implements FavouritesContract.Presenter,
        MovieStoreManager.MovieOperationListener {

//    private ValueEventListener mValueEventListener;
    private FavouritesContract.View mView;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;
    private MovieStoreManager mMovieStoreManager;

    public FavouritesPresenter(FavouritesContract.View view) {

        mView = view;
        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance().getReference("users");

        mMovieStoreManager = new MovieStoreManager(this);

    }

    @Override
    public void getFavouriteMovies() {
        mMovieStoreManager.getMovies();
    }

    @Override
    public void removeFirebaseListener() {
        mMovieStoreManager.removeFirebaseListener();
    }

    @Override
    public void removeMovieFromFavourites(Movie movie) {
        mMovieStoreManager.deleteMovie(movie);
    }

    //callbacks when complete movie operations such as add, delete, get etc...
    //it will pass the result back to view
    @Override
    public void onAddMovieComplete(MovieStoreManager.RESULT result) {

    }

    @Override
    public void onDeleteMovieComplete(MovieStoreManager.RESULT result) {
        mView.notifyListChange();

        switch (result) {
            case SUCCESS_DELETE:
                mView.showSnackBarWithResult(R.string.success_delete_movie);
                break;
            case FAIL_DELETE:
                mView.showSnackBarWithResult(R.string.fail_delete_movie);
                break;
            default:
                mView.showSnackBarWithResult(R.string.default_snackbar_error_message);
                break;
        }
    }

    @Override
    public void onGetMoviesComplete(List<Movie> movieList) {
        mView.updateMovieList(movieList);
    }

    @Override
    public void onErrorsOccur(String errorMessage) {
        mView.showSnackBarWithResult(errorMessage);
    }
}
