package com.nguyen.paul.thanh.walletmovie.model.source.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nguyen.paul.thanh.walletmovie.App;
import com.nguyen.paul.thanh.walletmovie.database.MoviesTableOperator;
import com.nguyen.paul.thanh.walletmovie.database.interfaces.DatabaseOperator;
import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.model.source.MovieSourceManager;
import com.nguyen.paul.thanh.walletmovie.model.source.SimpleDataSource;

import java.util.List;

import static com.nguyen.paul.thanh.walletmovie.App.GLOBAL_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GUEST_MODE_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.model.source.MovieSourceManager.RESULT.FAIL_ADD_MOVIE;
import static com.nguyen.paul.thanh.walletmovie.model.source.MovieSourceManager.RESULT.MOVIE_EXIST;
import static com.nguyen.paul.thanh.walletmovie.model.source.MovieSourceManager.RESULT.SUCCESS_ADD_MOVIE;

/**
 * Created by THANH on 17/02/2017.
 */

public class LocalDBSource extends SimpleDataSource {

    private MovieSourceManager.MovieOperationListener mListener;

    public LocalDBSource(MovieSourceManager.MovieOperationListener listener) {
        mListener = listener;
    }

    @Override
    public void addMovie(Movie movie) {
        List<Genre> genreList = ( (App) App.getAppContext()).getGenreListFromApi();
        new AddFavouriteTask(App.getAppContext(), mListener, genreList).execute(movie);
    }

    //AsyncTask to handle adding movies locally or remotely
    public static class AddFavouriteTask extends AsyncTask<Movie, Void, MovieSourceManager.RESULT> {

        private Context mContext;
        private FirebaseAuth mAuth;
        private DatabaseReference mUsersRef;
        private List<Genre> mGenreListFromApi;
        private MovieSourceManager.MovieOperationListener mListener;

        public AddFavouriteTask(Context context, MovieSourceManager.MovieOperationListener listener, List<Genre> genreList) {
            mContext = context;
            mGenreListFromApi = genreList;
            mListener = listener;
            mAuth = FirebaseAuth.getInstance();
            FirebaseDatabase firebaseDB = FirebaseDatabase.getInstance();
            mUsersRef = firebaseDB.getReference("users");
        }

        @Override
        protected MovieSourceManager.RESULT doInBackground(Movie... movies) {
            Handler handler = new Handler(mContext.getMainLooper());
            Movie movie = movies[0];

            //get shared preference and check if user is in guest mode
            SharedPreferences prefs = mContext.getSharedPreferences(GLOBAL_PREF_KEY, Context.MODE_PRIVATE);
            boolean isGuest = prefs.getBoolean(GUEST_MODE_PREF_KEY, false);

            if(isGuest) {
                //user is in guest mode
                //store movie in local db (SQLite)
                DatabaseOperator movieDBOperator = MoviesTableOperator.getInstance(mContext);
                long operationResult = movieDBOperator.insert(movie, mGenreListFromApi);
                //close database to avoid memory leaks
                movieDBOperator.closeDB();

                if(operationResult == movie.getId()) {
                    //successfully added movie, return result back to presenter
                    return SUCCESS_ADD_MOVIE;

                } else if(operationResult == 0) {
                    //movie already existed, return result back to presenter
                    return MOVIE_EXIST;

                } else {
                    //Failed to add movie, return result back to presenter
                    return FAIL_ADD_MOVIE;

                }

            } else {
//                FirebaseUser currentUser = mAuth.getCurrentUser();
//                if(currentUser == null) {
//                    //user is not signed in
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            //redirect to signin page
//                            Intent intent = new Intent(mContext, SigninActivity.class);
//                            mActivity.startActivity(intent);
//                        }
//                    });
//
//                } else {
//                    //store movie to cloud db (Firebase)
//                    writeDataToFirebase(movie, currentUser.getUid());
//                }
            }

            return null;

        }

        @Override
        protected void onPostExecute(MovieSourceManager.RESULT result) {
            super.onPostExecute(result);
            mListener.onAddMovieComplete(result);
        }

        /**
         * This method will handle data writing operation to Firebase (cloud db)
         */
//        private void writeDataToFirebase(final Movie movie, final String uid) {
//            mUsersRef.child(uid)
//                    .child("favourite_movies")
//                    .child(String.valueOf(movie.getId()))
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            if(dataSnapshot.exists()) {
//                                //there is already a movie with same value
//                                makeSnackBar("The movie has already been in your favourites!");
//
//                            } else {
//                                //no existing movie, safe to add
//                                mUsersRef.child(uid)
//                                        .child("favourite_movies")
//                                        .child(String.valueOf(movie.getId()))
//                                        .setValue(movie, new DatabaseReference.CompletionListener() {
//                                            @Override
//                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                                if(databaseError != null) {
//                                                    //errors occur while writing data
//                                                    makeSnackBar("Error! Failed to add the movie to your favourites!");
//                                                } else {
//                                                    //successfully added new data to Firebase
//                                                    makeSnackBar("Successfully added to your favourites!");
//                                                }
//                                            }
//                                        });
//                            }//end if-else
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//                            //handle errors
//                        }
//                    });
//
//        }

    }
}
