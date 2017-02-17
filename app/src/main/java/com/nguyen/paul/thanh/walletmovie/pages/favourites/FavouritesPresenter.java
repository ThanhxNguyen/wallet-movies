package com.nguyen.paul.thanh.walletmovie.pages.favourites;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.model.source.MovieSourceManager;

import java.util.List;

/**
 * Created by THANH on 17/02/2017.
 */

public class FavouritesPresenter implements FavouritesContract.Presenter,
        MovieSourceManager.MovieOperationListener {

//    private ValueEventListener mValueEventListener;
    private FavouritesContract.View mView;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;
    private MovieSourceManager mMovieSourceManager;

    public FavouritesPresenter(FavouritesContract.View view) {

        mView = view;
        mAuth = FirebaseAuth.getInstance();
        mUserRef = FirebaseDatabase.getInstance().getReference("users");

        mMovieSourceManager = new MovieSourceManager(this);

    }

    @Override
    public void getFavouriteMovies() {
        mMovieSourceManager.getMovies();
    }

    @Override
    public void removeFirebaseListener() {
        mMovieSourceManager.removeFirebaseListener();
    }

    @Override
    public void removeMovieFromFavourites(Movie movie) {
        mMovieSourceManager.deleteMovie(movie);
    }

    //callbacks when complete movie operations such as add, delete, get etc...
    //it will pass the result back to view
    @Override
    public void onAddMovieComplete(MovieSourceManager.RESULT result) {

    }

    @Override
    public void onDeleteMovieComplete(MovieSourceManager.RESULT result) {
        mView.notifyListChange();
        mView.showSnackBarWithResult(result);
    }

    @Override
    public void onGetMoviesComplete(List<Movie> movieList) {
        mView.updateMovieList(movieList);
    }
}
