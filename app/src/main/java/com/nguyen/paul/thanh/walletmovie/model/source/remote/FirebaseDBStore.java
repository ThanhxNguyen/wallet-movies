package com.nguyen.paul.thanh.walletmovie.model.source.remote;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.model.source.MovieStoreManager;
import com.nguyen.paul.thanh.walletmovie.model.source.SimpleDataStore;

import java.util.ArrayList;
import java.util.List;

import static com.nguyen.paul.thanh.walletmovie.model.source.MovieStoreManager.RESULT.FAIL_ADD_MOVIE;
import static com.nguyen.paul.thanh.walletmovie.model.source.MovieStoreManager.RESULT.FAIL_DELETE;
import static com.nguyen.paul.thanh.walletmovie.model.source.MovieStoreManager.RESULT.MOVIE_EXIST;
import static com.nguyen.paul.thanh.walletmovie.model.source.MovieStoreManager.RESULT.SUCCESS_ADD_MOVIE;
import static com.nguyen.paul.thanh.walletmovie.model.source.MovieStoreManager.RESULT.SUCCESS_DELETE;

/**
 * This class handles reading/writing data to Firebase database
 */

public class FirebaseDBStore extends SimpleDataStore {

    //constants for Firebase database nodes
    public static final String NODE_USERS = "users";
    public static final String NODE_FAVOURITE_MOVIES = "favourite_movies";
    private static final String SIGN_IN_REQUIRED_ERROR_MESSAGE = "Please sign in to proceed";
    private static final String DEFAULT_ERROR_MESSAGE = "An error has occured!";

    private final ValueEventListener mValueEventListener;
    private DatabaseReference mUsersRef;
    private FirebaseAuth mAuth;
    private MovieStoreManager.MovieOperationListener mListener;

    public FirebaseDBStore(MovieStoreManager.MovieOperationListener listener) {
        mListener = listener;
        mAuth = FirebaseAuth.getInstance();
        mUsersRef = FirebaseDatabase.getInstance().getReference(NODE_USERS);

        //initialize ValueEventListener
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Movie> movieList = parseMovieResultsFromFirebase(dataSnapshot);
                mListener.onGetMoviesComplete(movieList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mListener.onErrorsOccur(DEFAULT_ERROR_MESSAGE);
            }
        };
    }

    public void removeFirebaseListener() {
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            if(mValueEventListener != null) {
                mUsersRef.child(user.getUid())
                        .child(NODE_FAVOURITE_MOVIES)
                        .removeEventListener(mValueEventListener);
            }
        } else {
            //user not signed in, return back error message
            mListener.onErrorsOccur(SIGN_IN_REQUIRED_ERROR_MESSAGE);
        }
    }

    @Override
    public void getMovies() {
        //get favourite movies from Firebase
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            //user is currently signed in
            mUsersRef.child(user.getUid())
                    .child(NODE_FAVOURITE_MOVIES)
                    .addValueEventListener(mValueEventListener);

        } else {
            //user not signed in, return back error message
            mListener.onErrorsOccur(SIGN_IN_REQUIRED_ERROR_MESSAGE);
        }
    }

    /**
     * This method will handle data writing operation to Firebase (cloud db)
     */
    @Override
    public void addMovie(final Movie movie) {
        final FirebaseUser user = mAuth.getCurrentUser();

        if(user != null) {
            final String uid = user.getUid();
            mUsersRef.child(uid)
                    .child(NODE_FAVOURITE_MOVIES)
                    .child(String.valueOf(movie.getId()))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                //there is already a movie with same value
//                                makeSnackBar("The movie has already been in your favourites!");
                                mListener.onAddMovieComplete(MOVIE_EXIST);

                            } else {
                                //no existing movie, safe to add
                                mUsersRef.child(uid)
                                        .child(NODE_FAVOURITE_MOVIES)
                                        .child(String.valueOf(movie.getId()))
                                        .setValue(movie, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                if(databaseError != null) {
                                                    //errors occur while writing data
//                                                    makeSnackBar("Error! Failed to add the movie to your favourites!");
                                                    mListener.onAddMovieComplete(FAIL_ADD_MOVIE);
                                                } else {
                                                    //successfully added new data to Firebase
//                                                    makeSnackBar("Successfully added to your favourites!");
                                                    mListener.onAddMovieComplete(SUCCESS_ADD_MOVIE);
                                                }
                                            }
                                        });
                            }//end if-else
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //return an error message back to user
                            mListener.onErrorsOccur(DEFAULT_ERROR_MESSAGE);
                        }
                    });

        } else {
            //user is not signed in, show message to ask user to sign in
            mListener.onErrorsOccur(SIGN_IN_REQUIRED_ERROR_MESSAGE);
        }

    }

    @Override
    public void deleteMovie(Movie movie) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            mUsersRef.child(currentUser.getUid())
                    .child(NODE_FAVOURITE_MOVIES)
                    .child(String.valueOf(movie.getId()))
                    .removeValue(new DatabaseReference.CompletionListener() {

                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null) {
                                //errors occur
                                mListener.onDeleteMovieComplete(FAIL_DELETE);
                            } else {
                                //success
                                mListener.onDeleteMovieComplete(SUCCESS_DELETE);
                            }
                        }
                    });
        } else {
            mListener.onDeleteMovieComplete(FAIL_DELETE);
        }
    }

    private List<Movie> parseMovieResultsFromFirebase(DataSnapshot snapshot) {
        List<Movie> movieList = new ArrayList<>();
        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
            //each movie object has a key to identify each movie
            String childKey = childSnapshot.getKey();
            Movie movie = snapshot.child(childKey).getValue(Movie.class);
            movieList.add(movie);
        }

        return movieList;
    }

}
