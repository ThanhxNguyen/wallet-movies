package com.nguyen.paul.thanh.walletmovie.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;

import com.nguyen.paul.thanh.walletmovie.App;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.adapters.MovieRecyclerViewAdapter;
import com.nguyen.paul.thanh.walletmovie.chains.RequestChain;
import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.ui.RecyclerViewWithEmptyView;
import com.nguyen.paul.thanh.walletmovie.utilities.AddFavouriteTask;
import com.nguyen.paul.thanh.walletmovie.utilities.MovieQueryBuilder;
import com.nguyen.paul.thanh.walletmovie.utilities.MoviesMultiSearch;
import com.nguyen.paul.thanh.walletmovie.utilities.NetworkRequest;
import com.nguyen.paul.thanh.walletmovie.utilities.ScreenMeasurer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.nguyen.paul.thanh.walletmovie.App.DISPLAY_LIST_IN_GRID_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GLOBAL_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.MOVIE_DATE_SORT;
import static com.nguyen.paul.thanh.walletmovie.App.MOVIE_NAME_SORT;
import static com.nguyen.paul.thanh.walletmovie.App.MOVIE_SORT_SETTINGS_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.MOVIE_VOTE_SORT;

/**
 * Fragment to display movies list
 */
public class MovieListFragment extends Fragment
        implements MovieRecyclerViewAdapter.OnRecyclerViewClickListener,
        RequestChain.OnChainComplete {

    public static final String FRAGMENT_TAG = MovieListFragment.class.getSimpleName();

    private static final String TAB_POSITION_KEY = "tab_position_key";
    private static final String SEARCH_QUERY_KEY = "search_query_key";
    public static final String CAST_ID_KEY = "cast_id_key";

    //these constants will be used to identify what kind of movies it should display
    //such as movies for viewpager from home page, movies for user search query or
    //movies related to a cast.
    private static final String INIT_KEY = "initiating_key";
    public static final int DISPLAY_MOVIES_FOR_VIEWPAGER = 1;
    public static final int DISPLAY_MOVIES_FOR_SEARCH_RESULT = 2;
    public static final int DISPLAY_MOVIES_RELATED_TO_CAST = 3;

    private static final String NETWORK_REQUEST_TAG = "network_request_tag";
    private Context mContext;
    private NetworkRequest mNetworkRequest;
    private List<Movie> mMoviesList;
    private List<Genre> mGenreListFromApi;
    private MovieRecyclerViewAdapter mAdapter;
    private RecyclerViewWithEmptyView mRecyclerView;
    private MoviesMultiSearch mMoviesMultiSearch;
    //flag to indicate the display type of list view
    private boolean displayInGrid;

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;

    private ViewGroup mParentContainer;

    private ProgressDialog mProgressDialog;

    public MovieListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param displayType
     *          page type to display movies such as viewpager in home page, movies list for a cast etc...
     * @param param
     *          view pager display type, param will be an integer for tab position
     *          movie related to a cast, param will be cast ID
     *
     */
    public static MovieListFragment newInstance(int displayType, int param) {
        MovieListFragment fragment = new MovieListFragment();
        Bundle args = new Bundle();
        args.putInt(INIT_KEY, displayType);
        if(displayType == DISPLAY_MOVIES_FOR_VIEWPAGER) {
            args.putInt(TAB_POSITION_KEY, param);
        } else if(displayType == DISPLAY_MOVIES_RELATED_TO_CAST) {
            args.putInt(CAST_ID_KEY, param);
        }
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Overload newInstance(...)
     * This will be used when displaying movies from search with search query
     */
    public static MovieListFragment newInstance(int displayType, String searchQuery) {
        MovieListFragment fragment = new MovieListFragment();
        Bundle args = new Bundle();
        args.putInt(INIT_KEY, displayType);
        args.putString(SEARCH_QUERY_KEY, searchQuery);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mNetworkRequest = NetworkRequest.getInstance(mContext);
        mMoviesList = new ArrayList<>();
        //initiate ProgressDialog
        mProgressDialog = new ProgressDialog(mContext, ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Loading movies...");
        mProgressDialog.setCancelable(false);

        //initialize movie search chain
        mMoviesMultiSearch = new MoviesMultiSearch(getActivity(), this, mNetworkRequest, NETWORK_REQUEST_TAG);

        //initialize shared preference
        mPrefs = mContext.getSharedPreferences(GLOBAL_PREF_KEY, Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();

        //set the list view display in grid by default
        displayInGrid = mPrefs.getBoolean(DISPLAY_LIST_IN_GRID_KEY, true);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null) {
            mProgressDialog.show();
        }
        setRetainInstance(true);
        //get genres value list from cache
        mGenreListFromApi = ( (App) getActivity().getApplicationContext()).getGenreListFromApi();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mProgressDialog.dismiss();
    }

    private void updateListDisplayTypeMenu(Menu menu) {
        displayInGrid = mPrefs.getBoolean(DISPLAY_LIST_IN_GRID_KEY, true);
        //update list view display type icon based on user preference
        if(displayInGrid) {
            menu.findItem(R.id.action_grid_list_display_type).setVisible(true);
            menu.findItem(R.id.action_list_display_type).setVisible(false);
        } else {
            menu.findItem(R.id.action_grid_list_display_type).setVisible(false);
            menu.findItem(R.id.action_list_display_type).setVisible(true);
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

        MenuItem item;
        int sortOption = mPrefs.getInt(MOVIE_SORT_SETTINGS_KEY, 0);
        //get user preference regarding sorting options for movie list and set sorting option appropriately
        switch (sortOption) {
            case MOVIE_DATE_SORT:
                item = menu.findItem(R.id.action_sort_by_date);
                item.setChecked(true);
                onOptionsItemSelected(item);
                break;

            case MOVIE_NAME_SORT:
                item = menu.findItem(R.id.action_sort_by_name);
                item.setChecked(true);
                onOptionsItemSelected(item);
                break;

            case MOVIE_VOTE_SORT:
                item = menu.findItem(R.id.action_sort_by_vote);
                item.setChecked(true);
                onOptionsItemSelected(item);
                break;

            default:
                break;
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        displayInGrid = mPrefs.getBoolean(DISPLAY_LIST_IN_GRID_KEY, true);
        int id = item.getItemId();
        item.setChecked(true);

        switch (id) {
            case R.id.action_sort_by_name:
                Collections.sort(mMoviesList, Movie.MovieNameSort);
                mAdapter.notifyDataSetChanged();
                mEditor.putInt(MOVIE_SORT_SETTINGS_KEY, MOVIE_NAME_SORT).apply();
                break;

            case R.id.action_sort_by_date:
                Collections.sort(mMoviesList, Movie.MovieReleaseDateSort);
                mAdapter.notifyDataSetChanged();
                mEditor.putInt(MOVIE_SORT_SETTINGS_KEY, MOVIE_DATE_SORT).apply();
                break;

            case R.id.action_sort_by_vote:
                Collections.sort(mMoviesList, Movie.MovieVoteSort);
                mAdapter.notifyDataSetChanged();
                mEditor.putInt(MOVIE_SORT_SETTINGS_KEY, MOVIE_VOTE_SORT).apply();
                break;

            case R.id.action_grid_list_display_type:
                mEditor.putBoolean(DISPLAY_LIST_IN_GRID_KEY, false).apply();
                getActivity().invalidateOptionsMenu();
//                updateListDisplayTypeMenu(mMenu);
                break;

            case R.id.action_list_display_type:
                mEditor.putBoolean(DISPLAY_LIST_IN_GRID_KEY, true).apply();
                getActivity().invalidateOptionsMenu();
//                updateListDisplayTypeMenu(mMenu);
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
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
    }

    private void displayMoviesForViewPager(int tabPosition) {
        String url;
        MovieQueryBuilder movieQueryBuilder = MovieQueryBuilder.getInstance();
                /* grab movies according tab position
                * 0: top movies list
                * 1: now showing movies list
                * 3: upcoming movies list
                */
        switch (tabPosition) {
            case 0:
                url = movieQueryBuilder.discover().mostPopular().build();
                break;
            case 1:
                url = movieQueryBuilder.discover().showing().build();
                break;
            case 2:
                url = movieQueryBuilder.discover().upcoming().build();
                break;
            default:
                url = movieQueryBuilder.discover().mostPopular().build();
                break;
        }
        sendRequestToGetMovieList(url);
    }

    private void displayMoviesForSearchResult(String searchQuery) {
        String url = MovieQueryBuilder.getInstance().search().query(searchQuery).build();
        sendRequestToGetMovieList(url);
    }

    private void displayMoviesRelatedToCast(int castId) {
        String url = MovieQueryBuilder.getInstance().discover().moviesRelatedTo(castId).build();
        sendRequestToGetMovieList(url);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mParentContainer = container;
        // Inflate the layout for this fragment
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_movie_pager_item, container, false);

        //enable fragment to append menu items to toolbar
        setHasOptionsMenu(true);

        mRecyclerView = (RecyclerViewWithEmptyView) view.findViewById(R.id.movie_list);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //get placeholder view and set it to display when the list is empty
        TextView placeholderView = (TextView) view.findViewById(R.id.placeholder_view);
        mRecyclerView.setPlaceholderView(placeholderView);
        populateMovieList();

        Bundle args = getArguments();
        if(args != null) {
            int displayType = args.getInt(INIT_KEY);

            switch (displayType) {
                case DISPLAY_MOVIES_FOR_VIEWPAGER:
                    int tabPosition = args.getInt(TAB_POSITION_KEY, 0);
                    displayMoviesForViewPager(tabPosition);
                    break;
                case DISPLAY_MOVIES_FOR_SEARCH_RESULT:
                    getActivity().setTitle(R.string.title_search_result);
                    String searchQuery = args.getString(SEARCH_QUERY_KEY);
                    displayMoviesForSearchResult(searchQuery);
                    break;
                case DISPLAY_MOVIES_RELATED_TO_CAST:
                    int castId = args.getInt(CAST_ID_KEY);
                    getActivity().setTitle(R.string.title_movie_list);
                    displayMoviesRelatedToCast(castId);
            }

        }//end if

        return view;
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
        mAdapter = new MovieRecyclerViewAdapter(mContext, mMoviesList, this, R.menu.home_movie_list_item_popup_menu);
        //check if the display type of list view and set layout appropriately
        if(displayInGrid) {
            mAdapter.setGridListViewLayout();
        } else {
            mAdapter.setListViewLayout();
        }
        mRecyclerView.setAdapter(mAdapter);
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
    public void onStop() {
        super.onStop();
        //having trouble when add to request queue using singleton class methods
//        mNetworkRequest.cancelPendingRequests(NETWORK_REQUEST_TAG);
        mNetworkRequest.getRequestQueue().cancelAll(NETWORK_REQUEST_TAG);
    }

    private void sendRequestToGetMovieList(String url) {
        //empty movie list if there is any
        mMoviesList.clear();
        //start getting movies
        mMoviesMultiSearch.search(url);
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
            case MovieRecyclerViewAdapter.OnRecyclerViewClickListener.ADD_TO_FAVOURITE_TRIGGERED:
                addMovieToFavourites(movie, mGenreListFromApi);
                break;
            default:
                break;
        }
    }

    private void addMovieToFavourites(Movie movie, List<Genre> genreList) {
        AddFavouriteTask task = new AddFavouriteTask(mContext, genreList, getActivity());
        task.setParentContainerForSnackBar(mParentContainer);
        task.execute(movie);
    }

    @Override
    public void onChainComplete(List<Movie> movieList) {

        for(Movie m : movieList) {
            if(m != null) {
                mMoviesList.add(m);
            }
        }

        mAdapter.notifyDataSetChanged();
        
        mProgressDialog.dismiss();
    }
}
