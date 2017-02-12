package com.nguyen.paul.thanh.walletmovie.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.activities.MainActivity;
import com.nguyen.paul.thanh.walletmovie.activities.SigninActivity;
import com.nguyen.paul.thanh.walletmovie.adapters.MovieRecyclerViewAdapter;
import com.nguyen.paul.thanh.walletmovie.database.MoviesTableOperator;
import com.nguyen.paul.thanh.walletmovie.database.interfaces.DatabaseOperator;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.ui.RecyclerViewWithEmptyView;
import com.nguyen.paul.thanh.walletmovie.utilities.ScreenMeasurer;
import com.nguyen.paul.thanh.walletmovie.utilities.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.nguyen.paul.thanh.walletmovie.App.DISPLAY_LIST_IN_GRID_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GLOBAL_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GUEST_MODE_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.MOVIE_DATE_SORT;
import static com.nguyen.paul.thanh.walletmovie.App.MOVIE_NAME_SORT;
import static com.nguyen.paul.thanh.walletmovie.App.MOVIE_SORT_SETTINGS_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.MOVIE_VOTE_SORT;

/**
 * Fragment for favourites movies
 */
public class FavouriteMoviesFragment extends Fragment
                                implements MovieRecyclerViewAdapter.OnRecyclerViewClickListener,
                                            DatabaseReference.CompletionListener{

    public static final String FRAGMENT_TAG = FavouriteMoviesFragment.class.getSimpleName();

    private Context mContext;
    private MainActivity mActivity;
    private SwipeRefreshLayout mRefreshLayout;
    private List<Movie> mMoviesList;
    private MovieRecyclerViewAdapter mAdapter;
    private RecyclerViewWithEmptyView mRecyclerView;
    private ViewGroup mViewContainer;
    private ProgressBar mSpinner;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    //listener to listen for data changes
    private ValueEventListener mValueEventListener;

    private SharedPreferences mPrefs;

    //flag to indicate if the user is in guest mode or register mode
    private boolean isGuest;
    private boolean displayInGrid;

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
        SharedPreferences prefs = getActivity().getSharedPreferences(GLOBAL_PREF_KEY, Context.MODE_PRIVATE);
        //determine if the user is in guest mode or registered mode
        isGuest = prefs.getBoolean(GUEST_MODE_PREF_KEY, true);

        mContext = context;
        if(getActivity() instanceof MainActivity) {
            mActivity = (MainActivity) getActivity();
        }

        mMoviesList = new ArrayList<>();

        //initialize shared preference
        mPrefs = mContext.getSharedPreferences(GLOBAL_PREF_KEY, Context.MODE_PRIVATE);

        //set the list view display in grid by default
        displayInGrid = mPrefs.getBoolean(DISPLAY_LIST_IN_GRID_KEY, true);

        //initialize Firebase stuffs
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        mUserRef = firebaseDatabase.getReference("users");

        //initialize ValueEventListener
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMoviesList = parseMovieResultsFromFirebase(dataSnapshot);
                mSpinner.setVisibility(View.GONE);
                populateMovieList();
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
//        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mViewContainer = container;
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favourite_movies, container, false);

        mSpinner = (ProgressBar) view.findViewById(R.id.spinner);
        if(mMoviesList.size() == 0) mSpinner.setVisibility(View.VISIBLE);

        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //refresh the movie list
                initMovieList();
            }
        });

        mRecyclerView = (RecyclerViewWithEmptyView) view.findViewById(R.id.favourite_movie_list);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //placeholder view when the list is empty
        TextView placeholderView = (TextView) view.findViewById(R.id.placeholder_view);
        mRecyclerView.setPlaceholderView(placeholderView);

        initMovieList();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //set toolbar title
        if(mActivity != null) mActivity.setToolbarTitle(R.string.title_favourite_movies);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        isGuest = mPrefs.getBoolean(GUEST_MODE_PREF_KEY, true);
        if(!isGuest && currentUser == null) {
            //remove from back stack to avoid users navigate back to this when not signed in
            getFragmentManager().popBackStack();
        }
    }

    private void populateMovieList() {
        displayInGrid = mPrefs.getBoolean(DISPLAY_LIST_IN_GRID_KEY, true);
        RecyclerView.LayoutManager layoutManager;
        if(displayInGrid) {
            //layout manager
            int numRows = getNumRowsForMovieList();
            layoutManager = new GridLayoutManager(mContext, numRows);
        } else {
            layoutManager = new LinearLayoutManager(mContext);
        }
        mRecyclerView.setLayoutManager(layoutManager);

        //setup recycler view adapter here
        mAdapter = new MovieRecyclerViewAdapter(mContext, mMoviesList, this, R.menu.favourite_movie_list_item_popup_menu);
        //check if the display type of list view and set layout appropriately
        if(displayInGrid) {
            mAdapter.setGridListViewLayout();
        } else {
            mAdapter.setListViewLayout();
        }
        mRecyclerView.setAdapter(mAdapter);

        //hide refresh spinner
        mRefreshLayout.setRefreshing(false);
    }

    private void updateListDisplayTypeMenu(Menu menu) {
        displayInGrid = mPrefs.getBoolean(DISPLAY_LIST_IN_GRID_KEY, true);
        //update list view display type icon based on user preference
        if(displayInGrid) {
            menu.findItem(R.id.action_grid_list_display_type).setVisible(false);
            menu.findItem(R.id.action_list_display_type).setVisible(true);
        } else {
            menu.findItem(R.id.action_grid_list_display_type).setVisible(true);
            menu.findItem(R.id.action_list_display_type).setVisible(false);
        }

        populateMovieList();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        updateListDisplayTypeMenu(menu);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_action_movie, menu);

        updateListDisplayTypeMenu(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        item.setChecked(true);

        switch (id) {
            case R.id.action_sort_by_name:
                Collections.sort(mMoviesList, Movie.MovieNameSort);
                mAdapter.notifyDataSetChanged();
                mPrefs.edit().putInt(MOVIE_SORT_SETTINGS_KEY, MOVIE_NAME_SORT).apply();
                break;

            case R.id.action_sort_by_date:
                Collections.sort(mMoviesList, Movie.MovieReleaseDateSort);
                mAdapter.notifyDataSetChanged();
                mPrefs.edit().putInt(MOVIE_SORT_SETTINGS_KEY, MOVIE_DATE_SORT).apply();
                break;

            case R.id.action_sort_by_vote:
                Collections.sort(mMoviesList, Movie.MovieVoteSort);
                mAdapter.notifyDataSetChanged();
                mPrefs.edit().putInt(MOVIE_SORT_SETTINGS_KEY, MOVIE_VOTE_SORT).apply();
                break;

            case R.id.action_grid_list_display_type:
                mPrefs.edit().putBoolean(DISPLAY_LIST_IN_GRID_KEY, true).apply();
                //refresh toolbar
                getActivity().invalidateOptionsMenu();
                break;

            case R.id.action_list_display_type:
                mPrefs.edit().putBoolean(DISPLAY_LIST_IN_GRID_KEY, false).apply();
                //refresh toolbar
                getActivity().invalidateOptionsMenu();
                break;

            default:
                break;
        }

        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        displayInGrid = mPrefs.getBoolean(DISPLAY_LIST_IN_GRID_KEY, true);

        GridLayoutManager layoutManager;
        if(displayInGrid) {
            int numRows = getNumRowsForMovieList();
            //update grid layout based on new screen size
            layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
            layoutManager.setSpanCount(numRows);
            mRecyclerView.setLayoutManager(layoutManager);
        }
        mAdapter.notifyDataSetChanged();

//        int numRows = getNumRowsForMovieList();
//
//        GridLayoutManager gridLayoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
//        gridLayoutManager.setSpanCount(numRows);
//        mRecyclerView.setLayoutManager(gridLayoutManager);
//        mAdapter.notifyDataSetChanged();
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

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            mUserRef.child(currentUser.getUid()).removeEventListener(mValueEventListener);
        }
    }

    private void initMovieList() {
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
                Intent intent = new Intent(mContext, SigninActivity.class);
                getActivity().startActivity(intent);
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
            populateMovieList();
            //hide spinner
            mSpinner.setVisibility(View.GONE);

        }
    }
}
