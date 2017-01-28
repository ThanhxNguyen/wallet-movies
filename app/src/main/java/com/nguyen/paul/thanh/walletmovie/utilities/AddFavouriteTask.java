package com.nguyen.paul.thanh.walletmovie.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nguyen.paul.thanh.walletmovie.activities.SigninActivity;
import com.nguyen.paul.thanh.walletmovie.database.MoviesTableOperator;
import com.nguyen.paul.thanh.walletmovie.database.interfaces.DatabaseOperator;
import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;

import java.util.List;

import static com.nguyen.paul.thanh.walletmovie.App.GLOBAL_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GUEST_MODE_PREF_KEY;

/**
 * This class extends AsyncTask class and will handle long operation in background thread
 * add movies to favourites in this case
 */

public class AddFavouriteTask extends AsyncTask<Movie, Void, Void> {

    private static final String TAG = "AddFavouriteTask";

    private Context mContext;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersRef;
    private List<Genre> mGenreListFromApi;
    private ViewGroup mViewGroup;

    private Activity mActivity;

    public AddFavouriteTask(Context context, List<Genre> genreList, Activity activity) {
        mContext = context;
        mGenreListFromApi = genreList;
        mActivity = activity;
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDB = FirebaseDatabase.getInstance();
        mUsersRef = firebaseDB.getReference("users");
    }

    @Override
    protected Void doInBackground(Movie... movies) {
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
                //movie already exists
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        makeSnackBar("Successfully added to your favourites!");
                    }
                });

            } else if(operationResult == 0) {
                //insert movie to DB successfully
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        makeSnackBar("The movie has already been in your favourites!");
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        makeSnackBar("Error! Failed to add the movie to your favourites!");
                    }
                });
            }

        } else {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if(currentUser == null) {
                //user is not signed in
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //redirect to signin page
                        Intent intent = new Intent(mContext, SigninActivity.class);
                        mActivity.startActivity(intent);
                    }
                });

            } else {
                //store movie to cloud db (Firebase)
                writeDataToFirebase(movie, currentUser.getUid());
            }
        }

        return null;

    }

    /**
     * This method will handle data writing operation to Firebase (cloud db)
     */
    private void writeDataToFirebase(final Movie movie, final String uid) {
        mUsersRef.child(uid)
                    .child("favourite_movies")
                    .child(String.valueOf(movie.getId()))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                //there is already a movie with same value
                                makeSnackBar("The movie has already been in your favourites!");

                            } else {
                                //no existing movie, safe to add
                                mUsersRef.child(uid)
                                        .child("favourite_movies")
                                        .child(String.valueOf(movie.getId()))
                                        .setValue(movie, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                if(databaseError != null) {
                                                    //errors occur while writing data
                                                    makeSnackBar("Error! Failed to add the movie to your favourites!");
                                                } else {
                                                    //successfully added new data to Firebase
                                                    makeSnackBar("Successfully added to your favourites!");
                                                }
                                            }
                                        });
                            }//end if-else
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            //handle errors
                        }
                    });

    }

    public void setParentContainerForSnackBar(ViewGroup viewGroup) {
        mViewGroup = viewGroup;
    }

    //helper method to create a toast message
    private void makeSnackBar(String text) {
        Utils.createSnackBar(mActivity.getResources(), mViewGroup, text).show();
    }
}
