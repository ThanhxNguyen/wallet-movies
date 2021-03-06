package com.nguyen.paul.thanh.walletmovie.pages.home;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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

import com.nguyen.paul.thanh.walletmovie.MainActivity;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.adapters.MovieRecyclerViewAdapter;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.pages.moviedetails.MovieDetailsFragment;
import com.nguyen.paul.thanh.walletmovie.ui.RecyclerViewWithEmptyView;
import com.nguyen.paul.thanh.walletmovie.utilities.MovieQueryBuilder;
import com.nguyen.paul.thanh.walletmovie.utilities.ScreenMeasurer;
import com.nguyen.paul.thanh.walletmovie.utilities.Utils;

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
                    MovieListContract.View {

    public static final String FRAGMENT_TAG = MovieListFragment.class.getSimpleName();

    public static final String TAB_POSITION_KEY = "tab_position_key";
    public static final String SEARCH_QUERY_KEY = "search_query_key";
    public static final String CAST_ID_KEY = "cast_id_key";

    //these constants will be used to identify what kind of movies it should display
    //such as movies for viewpager from home page, movies for user search query or
    //movies related to a cast.
    public static final String INIT_KEY = "initiating_key";
    public static final int DISPLAY_MOVIES_FOR_VIEWPAGER = 1;
    public static final int DISPLAY_MOVIES_FOR_SEARCH_RESULT = 2;
    public static final int DISPLAY_MOVIES_RELATED_TO_CAST = 3;

    private Context mContext;
    private MainActivity mActivity;
    private List<Movie> mMoviesList;
    private MovieRecyclerViewAdapter mAdapter;
    private RecyclerViewWithEmptyView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    //flag to indicate the display type of list view
    private boolean displayInGrid;
    private boolean isViewPagerItem;

    //indicate the total of items in the list after the last request
    private int previousTotalItemCount = 0;
    //indicate if the list is getting more data
    private boolean loading = true;
    //define the threshold when to load more items (e.g. load more items when the list has 5 items left when scroll down)
    private int visibleThreshold = 5;
    //page number for api request
    private int currentPage = 1;
    private int firstVisibleItem = 0;
    private int visibleItemCount, totalItemCount;

    private SharedPreferences mPrefs;

    private ViewGroup mParentContainer;

    private int mTabPosition;
    private MovieQueryBuilder mMovieQueryBuilder;
    private MovieListPresenter mPresenter;

    //flag to indicate when this fragment is visible to users
    private boolean isViewVisible = false;

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
        if(getActivity() instanceof MainActivity) {
            mActivity = (MainActivity) getActivity();
        }

        //link this view to its presenter
        mPresenter = new MovieListPresenter(this);

        mMoviesList = new ArrayList<>();

        mMovieQueryBuilder = MovieQueryBuilder.getInstance();

        //initialize shared preference
        mPrefs = mContext.getSharedPreferences(GLOBAL_PREF_KEY, Context.MODE_PRIVATE);

        //set the list view display in grid by default
        displayInGrid = mPrefs.getBoolean(DISPLAY_LIST_IN_GRID_KEY, true);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRetainInstance(true);
        //enable fragment to append menu items to toolbar
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mParentContainer = container;
        // Inflate the layout for this fragment
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_movie_list, container, false);

        mRecyclerView = (RecyclerViewWithEmptyView) view.findViewById(R.id.movie_list);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //get placeholder view and set it to display when the list is empty
        TextView placeholderView = (TextView) view.findViewById(R.id.placeholder_view);
        mRecyclerView.setPlaceholderView(placeholderView);
        //setup adapter
        mAdapter = new MovieRecyclerViewAdapter(mContext, mMoviesList, this, R.menu.home_movie_list_item_popup_menu);

        //to avoid populating the list twice because setUserVisibleHint is called outside of fragment lifecycle
        if(!isViewVisible) {
            populateMovieList();
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isViewPagerItem) {
                    //reset page number
                    currentPage = 1;
                    mMoviesList.clear();
                    if(mAdapter != null) mAdapter.notifyDataSetChanged();
                    getMoviesForViewPager();
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            //this method will be invoked when a scroll event occurs, need to implement with care here
            //because it can slow down the performance
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                //check if the user scroll down and this fragment is inside the view pager
                if(isViewPagerItem && dy > 0) {
                    if(displayInGrid) {
                        //grid layout manager
                        firstVisibleItem = ( (GridLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                    } else {
                        //linear layout manager
                        firstVisibleItem = ( (LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                    }
                    //get the visible items (items are seen on the mobile screen)
                    visibleItemCount = mRecyclerView.getChildCount();
                    //the total list items in the adapter
                    totalItemCount = mRecyclerView.getLayoutManager().getItemCount();


                    if(totalItemCount < previousTotalItemCount) {
                        previousTotalItemCount = totalItemCount;
                        currentPage = 1;
                        if(totalItemCount == 0) {
                            loading = true;
                        }
                    }
                    //finish loading
                    if (loading && (totalItemCount > previousTotalItemCount)) {
                        loading = false;
                        previousTotalItemCount = totalItemCount;
                    }
                    //if the user is about to reach the end of the current list view, get more movies from api and append
                    //to the list
                    if (!loading && (firstVisibleItem + visibleItemCount + visibleThreshold) >= totalItemCount) {
                        currentPage++;
                        getMoviesForViewPager();
                    }

                }

            }
        });

        Bundle args = getArguments();
        if(args != null) {
            int displayType = args.getInt(INIT_KEY);

            switch (displayType) {
                case DISPLAY_MOVIES_FOR_VIEWPAGER:
                    int tabPosition = args.getInt(TAB_POSITION_KEY, 0);
                    isViewPagerItem = true;
                    //set toolbar title
                    if(mActivity != null) mActivity.setToolbarTitle(R.string.title_home);
                    mTabPosition = tabPosition;
                    getMoviesForViewPager();
                    break;
                case DISPLAY_MOVIES_FOR_SEARCH_RESULT:
                    isViewPagerItem = false;
                    //set toolbar title
                    if(mActivity != null) mActivity.setToolbarTitle(R.string.title_search_result);
                    String searchQuery = args.getString(SEARCH_QUERY_KEY);
                    getMoviesForSearchResult(searchQuery);
                    break;
                case DISPLAY_MOVIES_RELATED_TO_CAST:
                    isViewPagerItem = false;
                    //set toolbar title
                    if(mActivity != null) mActivity.setToolbarTitle(R.string.title_movie_list);
                    int castId = args.getInt(CAST_ID_KEY);
                    getMoviesRelatedToCast(castId);
            }

        }//end if

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getView() != null) {
            isViewVisible = true;
            populateMovieList();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        resetLoadMore();
    }

    private void resetLoadMore() {
        currentPage = 1;
        loading = true;
        previousTotalItemCount = 0;
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
                getActivity().invalidateOptionsMenu();
                populateMovieList();
                break;

            case R.id.action_list_display_type:
                mPrefs.edit().putBoolean(DISPLAY_LIST_IN_GRID_KEY, false).apply();
                getActivity().invalidateOptionsMenu();
                populateMovieList();
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
        if(displayInGrid && mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
            int numRows = getNumRowsForMovieList();
            //update grid layout based on new screen size
            layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
            layoutManager.setSpanCount(numRows);
            mRecyclerView.setLayoutManager(layoutManager);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void getMoviesForViewPager() {
        String url;
                /* grab movies according tab position
                * 0: top movies list
                * 1: now showing movies list
                * 3: upcoming movies list
                */
        switch (mTabPosition) {
            case 0:
                url = mMovieQueryBuilder.discover().mostPopular().page(currentPage).build();
                break;
            case 1:
                url = mMovieQueryBuilder.discover().showing().page(currentPage).build();
                break;
            case 2:
                url = mMovieQueryBuilder.discover().upcoming().page(currentPage).build();
                break;
            default:
                url = mMovieQueryBuilder.discover().mostPopular().page(currentPage).build();
                break;
        }
        makeMovieRequest(url);
    }

    private void getMoviesForSearchResult(String searchQuery) {
        mMoviesList.clear();
        String url = MovieQueryBuilder.getInstance().search().query(searchQuery).build();
        makeMovieRequest(url);
    }

    private void getMoviesRelatedToCast(int castId) {
        mMoviesList.clear();
        String url = MovieQueryBuilder.getInstance().discover().moviesRelatedTo(castId).build();
        makeMovieRequest(url);
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
        mPresenter.cancelRequests();
    }

    private void makeMovieRequest(String url) {
        mSwipeRefreshLayout.setRefreshing(true);
        //start getting movies
        mPresenter.getMovies(url);
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
                //add the movie to favourite
                mPresenter.addMovieToFavourite(movie);
                break;
            default:
                break;
        }
    }

    @Override
    public void updateMovieList(List<Movie> movieList) {
        //update movie list
        if(movieList != null) {
            if(movieList.size() > 0) {
                for(Movie m : movieList) {
                    if(m != null) {
                        boolean exist = false;
                        if(mMoviesList.size() > 0) {
                            for(Movie temp : mMoviesList) {
                                if(temp.getId() == m.getId()) {
                                    exist = true;
                                    break;
                                }
                            }
                        }
                        if(!exist) mMoviesList.add(m);
                    }
                }
            }
        }

        mAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showSnackBarWithResult(int resStringId) {
        Utils.createSnackBar(getResources(), mParentContainer, getString(resStringId)).show();
    }

    @Override
    public void showSnackBarWithResult(String message) {
        Utils.createSnackBar(getResources(), mParentContainer, message).show();
    }
}
