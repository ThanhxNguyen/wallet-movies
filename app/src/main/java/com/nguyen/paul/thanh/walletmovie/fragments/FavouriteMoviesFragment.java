package com.nguyen.paul.thanh.walletmovie.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.activities.SigninActivity;
import com.nguyen.paul.thanh.walletmovie.adapters.MovieRecyclerViewAdapter;
import com.nguyen.paul.thanh.walletmovie.database.MoviesTableOperator;
import com.nguyen.paul.thanh.walletmovie.database.interfaces.DatabaseOperator;
import com.nguyen.paul.thanh.walletmovie.interfaces.PreferenceConst;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.ui.RecyclerViewWithEmptyView;
import com.nguyen.paul.thanh.walletmovie.utilities.ScreenMeasurer;
import com.nguyen.paul.thanh.walletmovie.utilities.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fragment for favourites movies
 */
public class FavouriteMoviesFragment extends Fragment
                                implements MovieRecyclerViewAdapter.OnRecyclerViewClickListener,
                                            DatabaseReference.CompletionListener{

    public static final String FRAGMENT_TAG = FavouriteMoviesFragment.class.getSimpleName();

    private Context mContext;
    private List<Movie> mMoviesList;
    private MovieRecyclerViewAdapter mAdapter;
    private RecyclerViewWithEmptyView mRecyclerView;
    private ViewGroup mViewContainer;

    private ProgressDialog mProgressDialog;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    //listener to listen for data changes
    private ValueEventListener mValueEventListener;

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;

    //flag to indicate if the user is in guest mode or register mode
    private boolean isGuest;

    public FavouriteMoviesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FavouriteMoviesFragment.
     */

    public static FavouriteMoviesFragment newInstance() {
        FavouriteMoviesFragment fragment = new FavouriteMoviesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        //get shared preference
        SharedPreferences prefs = getActivity().getSharedPreferences(PreferenceConst.GLOBAL_PREF_KEY, Context.MODE_PRIVATE);
        //determine if the user is in guest mode or registered mode
        isGuest = prefs.getBoolean(PreferenceConst.Authenticate.GUEST_MODE_PREF_KEY, true);

        mContext = context;
        mMoviesList = new ArrayList<>();

        //initiate ProgressDialog
        mProgressDialog = new ProgressDialog(mContext, ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Loading favourite movies");

        //initialize shared preference
        mPrefs = mContext.getSharedPreferences(PreferenceConst.GLOBAL_PREF_KEY, Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();

        //initialize Firebase stuffs
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        mUserRef = firebaseDatabase.getReference("users");

        //initialize ValueEventListener
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMoviesList = parseMovieResultsFromFirebase(dataSnapshot);
                //setup recyclerview adapter here
                mAdapter = new MovieRecyclerViewAdapter(mContext, mMoviesList, FavouriteMoviesFragment.this, R.menu.favourite_movie_list_item_popup_menu);

                //hide progress dialog when complete getting movies
                mProgressDialog.dismiss();

                mRecyclerView.setAdapter(mAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //errors occur
                Utils.createSnackBar(getResources(), mViewContainer, databaseError.toString()).show();
            }
        };

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //enable fragment to append menu items to toolbar
        setHasOptionsMenu(true);
        //retain this fragment state during activity re-creation progress
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.title_favourite_movies);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mViewContainer = container;
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favourite_movies, container, false);

        mRecyclerView = (RecyclerViewWithEmptyView) view.findViewById(R.id.favourite_movie_list);
        //layout manager
        int numRows = getNumRowsForMovieList();
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, numRows);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(layoutManager);

        //placeholder view when the list is empty
        TextView placeholderView = (TextView) view.findViewById(R.id.placeholder_view);
        mRecyclerView.setPlaceholderView(placeholderView);

        //setup recyclerview adapter here
        mAdapter = new MovieRecyclerViewAdapter(mContext, mMoviesList, FavouriteMoviesFragment.this, R.menu.favourite_movie_list_item_popup_menu);

        initMovieList();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_action_movie, menu);

        MenuItem item;
        int sortOption = mPrefs.getInt(PreferenceConst.Settings.MOVIE_SORT_SETTINGS_KEY, 0);
        //get user preference regarding sorting options for movie list and set sorting option appropriately
        switch (sortOption) {
            case PreferenceConst.MOVIE_DATE_SORT:
                item = menu.findItem(R.id.action_sort_by_date);
                item.setChecked(true);
                onOptionsItemSelected(item);
                break;
            case PreferenceConst.MOVIE_NAME_SORT:
                item = menu.findItem(R.id.action_sort_by_name);
                item.setChecked(true);
                onOptionsItemSelected(item);
                break;
            case PreferenceConst.MOVIE_VOTE_SORT:
                item = menu.findItem(R.id.action_sort_by_vote);
                item.setChecked(true);
                onOptionsItemSelected(item);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        item.setChecked(true);

        switch (id) {
            case R.id.action_sort_by_name:
                Collections.sort(mMoviesList, Movie.MovieNameSort);
                mAdapter.notifyDataSetChanged();
                mEditor.putInt(PreferenceConst.Settings.MOVIE_SORT_SETTINGS_KEY, PreferenceConst.MOVIE_NAME_SORT).apply();
                break;

            case R.id.action_sort_by_date:
                Collections.sort(mMoviesList, Movie.MovieReleaseDateSort);
                mAdapter.notifyDataSetChanged();
                mEditor.putInt(PreferenceConst.Settings.MOVIE_SORT_SETTINGS_KEY, PreferenceConst.MOVIE_DATE_SORT).apply();
                break;

            case R.id.action_sort_by_vote:
                Collections.sort(mMoviesList, Movie.MovieVoteSort);
                mAdapter.notifyDataSetChanged();
                mEditor.putInt(PreferenceConst.Settings.MOVIE_SORT_SETTINGS_KEY, PreferenceConst.MOVIE_VOTE_SORT).apply();
                break;
        }

        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int numRows = getNumRowsForMovieList();

        GridLayoutManager gridLayoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        gridLayoutManager.setSpanCount(numRows);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mAdapter.notifyDataSetChanged();
    }

    private int getNumRowsForMovieList() {
        ScreenMeasurer screenMeasurer = new ScreenMeasurer(getActivity());
        //get screen size and display movie list appropriately
        int numRows = 1;
        int screenWidth = screenMeasurer.getDpWidth();
        if(screenWidth < 480) {//phone portrait
            numRows = 1;
        } else if(screenWidth > 480 && screenWidth < 840) {//phone landscape or small tablet portrait
            numRows = 2;
        } else if(screenWidth > 840) {
            numRows = 3;
        }

        return numRows;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mProgressDialog.dismiss();
    }

    @Override
    public void onDestroy() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            mUserRef.child(currentUser.getUid()).removeEventListener(mValueEventListener);
        } else {
            mUserRef.removeEventListener(mValueEventListener);
        }

        super.onDestroy();
    }

    private void initMovieList() {
        mProgressDialog.show();

        if(isGuest) {
            //get favourite movies from local db (Sqlite)
            getFavouriteMoviesFromLocalDB();
        } else {
            //get favourite movies from cloud db (Firebase)
            getFavouriteMoviesFromFirebase();
        }
    }

    private void getFavouriteMoviesFromFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            //user is currently signed in
            mUserRef.child(currentUser.getUid())
                    .child("favourite_movies")
                        .addValueEventListener(mValueEventListener);

        } else {
            Intent intent = new Intent(mContext, SigninActivity.class);
            getActivity().startActivity(intent);
        }
    }

    private void getFavouriteMoviesFromLocalDB() {
        GetFavouriteMoviesTask getFavouriteMoviesTask = new GetFavouriteMoviesTask();
        getFavouriteMoviesTask.execute();
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

    @Override
    public void onRecyclerViewClick(Movie movie) {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, MovieDetailsFragment.newInstance(movie))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onPopupMenuClick(PopupMenu popupMenu, Movie movie, int action) {
        switch (action) {
            case MovieRecyclerViewAdapter.OnRecyclerViewClickListener.REMOVE_MOVIE_TRIGGERED:
                //remove the movie from favourites list
                showConfirmPopupBeforeDelete(movie);
                break;
            default:
                break;
        }
    }

    private void showConfirmPopupBeforeDelete(final Movie movie) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to remove this movie from your favourites?");
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do something
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                removeMovieFromFavouritesList(movie);
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void removeMovieFromFavouritesList(Movie movie) {
        if(isGuest) {
            DeleteMoveFromFavouritesTask deleteMoveFromFavouritesTask = new DeleteMoveFromFavouritesTask();
            deleteMoveFromFavouritesTask.execute(movie);
        } else {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if(currentUser != null) {
                mUserRef.child(currentUser.getUid())
                        .child("favourite_movies")
                        .child(String.valueOf(movie.getId()))
                        .removeValue(this);
            } else {
                Utils.createSnackBar(getResources(), mViewContainer, "Please sign in to proceed").show();
            }
        }
    }

    @Override
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        //get called when complete Firebase write operation. If success DatabaseError object is empty, fail otherwise
        if(databaseError != null) {
            //errors occur
            Utils.createSnackBar(getResources(), mViewContainer, "Error! Sorry failed to remove this movie").show();
        } else {
            //success
            Utils.createSnackBar(getResources(), mViewContainer, "Successfully removed from favourite").show();
        }
    }

    //handle data operation in background thread
    private class DeleteMoveFromFavouritesTask extends AsyncTask<Movie, Void, Movie> {

        @Override
        protected Movie doInBackground(Movie... movies) {
            Movie movie = movies[0];
            DatabaseOperator databaseOperator = MoviesTableOperator.getInstance(mContext);
            int result = databaseOperator.delete(movie.getId());

            if(result > 0) {
                //successfully deleted
                return movie;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Movie movie) {
            if(movie != null) {
                //successfully removed movie
                Utils.createSnackBar(getResources(), mViewContainer, "Successfully removed from favourite").show();
                //update movie list and adapter
                mMoviesList.remove(movie);
                mAdapter.notifyDataSetChanged();
            } else {
                //failed to remove movie from favourites
                Utils.createSnackBar(getResources(), mViewContainer, "Error! Sorry failed to remove this movie").show();
            }
        }
    }

    //handle database operation in background thread
    private class GetFavouriteMoviesTask extends AsyncTask<Void, Void, List<Movie>> {

        @Override
        protected List<Movie> doInBackground(Void... voids) {
            List<Movie> movieList;
            //get movie from local database and return to onPostExecute (UI thread) to handle data
            DatabaseOperator databaseOperator = MoviesTableOperator.getInstance(mContext);
            movieList = databaseOperator.findAll();

            return movieList;
        }

        @Override
        protected void onPostExecute(List<Movie> movieList) {
            
            mMoviesList = movieList;
            //update adapter to refresh the list
            mAdapter = new MovieRecyclerViewAdapter(mContext, mMoviesList, FavouriteMoviesFragment.this, R.menu.favourite_movie_list_item_popup_menu);
            mAdapter.notifyDataSetChanged();

            //hide ProgressDialog
            mProgressDialog.dismiss();

            mRecyclerView.setAdapter(mAdapter);

        }
    }
}
