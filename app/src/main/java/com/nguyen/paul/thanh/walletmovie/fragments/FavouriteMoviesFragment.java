package com.nguyen.paul.thanh.walletmovie.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

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
import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.ui.RecyclerViewWithEmptyView;
import com.nguyen.paul.thanh.walletmovie.utilities.NetworkRequest;
import com.nguyen.paul.thanh.walletmovie.utilities.ScreenMeasurer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FavouriteMoviesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavouriteMoviesFragment extends Fragment
                                implements MovieRecyclerViewAdapter.OnRecyclerViewClickListener {

    private static final String TAG = "FavouriteMoviesFragment";

    public static final String FRAGMENT_TAG = FavouriteMoviesFragment.class.getSimpleName();

    private Context mContext;
    private NetworkRequest mNetworkRequest;
    private List<Movie> mMoviesList;
    private List<Genre> mGenreListFromApi;
    private MovieRecyclerViewAdapter mAdapter;
    private RecyclerViewWithEmptyView mRecyclerView;

    private ProgressDialog mProgressDialog;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserRef;
    //listener to listen for data changes
    private ValueEventListener mValueEventListener;

    //flag to indicate if the user is in guest mode or register mode
    private boolean isGuest;
    private ScreenMeasurer mScreenMeasurer;

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

        SharedPreferences prefs = getActivity().getSharedPreferences(PreferenceConst.GLOBAL_PREF_KEY, Context.MODE_PRIVATE);
        isGuest = prefs.getBoolean(PreferenceConst.Authenticate.GUEST_MODE_PREF_KEY, true);

        mContext = context;
        mNetworkRequest = NetworkRequest.getInstance(mContext);
        mMoviesList = new ArrayList<>();

        //initiate ProgressDialog
        mProgressDialog = new ProgressDialog(mContext, ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Loading favourite movies");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserRef = mFirebaseDatabase.getReference("users");

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
                //do something
            }
        };

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favourite_movies, container, false);

        //enable fragment to append menu items to toolbar
        setHasOptionsMenu(true);

        mRecyclerView = (RecyclerViewWithEmptyView) view.findViewById(R.id.favourite_movie_list);
        //layout manager
        int numRows = getNumRowsForMovieList();
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, numRows);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(layoutManager);

        //placeholder view when the list is empty
        TextView placeholderView = (TextView) view.findViewById(R.id.placeholder_view);
        mRecyclerView.setPlaceholderView(placeholderView);

        initMovieList();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_action_movie, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_sort_by_name:
                Collections.sort(mMoviesList, Movie.MovieNameSort);
                mAdapter.notifyDataSetChanged();
                break;

            case R.id.action_sort_by_date:
                Collections.sort(mMoviesList, Movie.MovieReleaseDateSort);
                mAdapter.notifyDataSetChanged();
                break;

            case R.id.action_sort_by_vote:
                Collections.sort(mMoviesList, Movie.MovieVoteSort);
                mAdapter.notifyDataSetChanged();
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
        mScreenMeasurer = new ScreenMeasurer(getActivity());
        //get screen size and display movie list appropriately
        int numRows = 1;
        int screenWidth = mScreenMeasurer.getDpWidth();
        if(screenWidth < 480) {//phone portrait
            numRows = 1;
        } else if(screenWidth > 480 && screenWidth < 600) {//phone landscape or small tablet portrait
            numRows = 2;
        } else if(screenWidth > 600) {
            numRows = 3;
        }

        return numRows;
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
                removeMovieFromFavouritesList(movie);
                break;
            default:
                break;
        }
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
                        .removeValue();
            } else {
                Toast.makeText(mContext, "Please sign in", Toast.LENGTH_LONG).show();
            }
        }
    }

    //handle data operation in background thread
    private class DeleteMoveFromFavouritesTask extends AsyncTask<Movie, Void, Movie> {

        @Override
        protected Movie doInBackground(Movie... movies) {
            Movie movie = movies[0];
            DatabaseOperator databaseOperator = MoviesTableOperator.getInstance(mContext);
            databaseOperator.delete(movie.getId());

            return movie;
        }

        @Override
        protected void onPostExecute(Movie movie) {
            if(movie != null) {
                Toast.makeText(mContext, "Successfully removed from favourites!", Toast.LENGTH_SHORT).show();
                //update movie list and adapter
                mMoviesList.remove(movie);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    //handle database operation in background thread
    private class GetFavouriteMoviesTask extends AsyncTask<Void, Void, List<Movie>> {

        private static final String TAG = "GetFavouriteMoviesTask";

        @Override
        protected List<Movie> doInBackground(Void... voids) {
            List<Movie> movieList = new ArrayList<>();
            //get movie from local database and return to onPostExecute (UI thread) to handle data
            DatabaseOperator databaseOperator = MoviesTableOperator.getInstance(mContext);
            movieList = databaseOperator.findAll();

            return movieList;
        }

        @Override
        protected void onPostExecute(List<Movie> movieList) {
            
            mMoviesList = movieList;
            //setup recyclerview adapter here
            mAdapter = new MovieRecyclerViewAdapter(mContext, mMoviesList, FavouriteMoviesFragment.this, R.menu.favourite_movie_list_item_popup_menu);

            //hide ProgressDialog
            mProgressDialog.dismiss();

            mRecyclerView.setAdapter(mAdapter);

        }
    }
}
