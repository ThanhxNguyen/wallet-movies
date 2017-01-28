package com.nguyen.paul.thanh.walletmovie.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.App;
import com.nguyen.paul.thanh.walletmovie.adapters.MovieRecyclerViewAdapter;
import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.ui.RecyclerViewWithEmptyView;
import com.nguyen.paul.thanh.walletmovie.utilities.AddFavouriteTask;
import com.nguyen.paul.thanh.walletmovie.utilities.MovieQueryBuilder;
import com.nguyen.paul.thanh.walletmovie.utilities.NetworkRequest;
import com.nguyen.paul.thanh.walletmovie.utilities.ScreenMeasurer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.nguyen.paul.thanh.walletmovie.App.GLOBAL_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.MOVIE_DATE_SORT;
import static com.nguyen.paul.thanh.walletmovie.App.MOVIE_NAME_SORT;
import static com.nguyen.paul.thanh.walletmovie.App.MOVIE_SORT_SETTINGS_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.MOVIE_VOTE_SORT;

/**
 * Fragment to display movies list
 */
public class MovieListFragment extends Fragment
        implements MovieRecyclerViewAdapter.OnRecyclerViewClickListener {
    
    private static final String TAG = "MovieListFragment";

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
    private ArrayList<Movie> mMoviesList;
    private List<Genre> mGenreListFromApi;
    private MovieRecyclerViewAdapter mAdapter;
    private RecyclerViewWithEmptyView mRecyclerView;

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
     * @return A new instance of fragment MovieListFragment.
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

        //initialize shared preference
        mPrefs = mContext.getSharedPreferences(GLOBAL_PREF_KEY, Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();

        mGenreListFromApi = ((App) getActivity().getApplication()).getGenreListFromApi();

        if(mGenreListFromApi.size() == 0) {
            String genreListUrl = MovieQueryBuilder.getInstance().getGenreListUrl();
            sendRequestToGetGenreList(genreListUrl);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null) {
            mProgressDialog.show();
        }
        setRetainInstance(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mProgressDialog.dismiss();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_action_movie, menu);

        MenuItem item;
        int sortOption = mPrefs.getInt(MOVIE_SORT_SETTINGS_KEY, 0);
        //get user preference regarding sorting options for movie list and set sorting option appropriately
        switch (sortOption) {
            case MOVIE_DATE_SORT:
                item = menu.findItem(R.id.action_sort_by_date);
                item.setChecked(true);
                onOptionsItemSelected(item);
//                Collections.sort(mMoviesList, Movie.MovieReleaseDateSort);
//                mAdapter.notifyDataSetChanged();
                break;
            case MOVIE_NAME_SORT:
                item = menu.findItem(R.id.action_sort_by_name);
                item.setChecked(true);
                onOptionsItemSelected(item);
//                Collections.sort(mMoviesList, Movie.MovieNameSort);
//                mAdapter.notifyDataSetChanged();
                break;
            case MOVIE_VOTE_SORT:
                item = menu.findItem(R.id.action_sort_by_vote);
                item.setChecked(true);
                onOptionsItemSelected(item);
//                Collections.sort(mMoviesList, Movie.MovieVoteSort);
//                mAdapter.notifyDataSetChanged();
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
        }

        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int numRows = getNumRowsForMovieList();
        //update grid layout based on new screen size
        GridLayoutManager gridLayoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        gridLayoutManager.setSpanCount(numRows);
        mRecyclerView.setLayoutManager(gridLayoutManager);
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
        //layout manager
        int numRows = getNumRowsForMovieList();
        RecyclerView.LayoutManager gridLayoutManager = new GridLayoutManager(mContext, numRows);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(gridLayoutManager);

        //get placeholder view and set it to display when the list is empty
        TextView placeholderView = (TextView) view.findViewById(R.id.placeholder_view);
        mRecyclerView.setPlaceholderView(placeholderView);

        //setup recycler view adapter here
        mAdapter = new MovieRecyclerViewAdapter(mContext, mMoviesList, this, R.menu.home_movie_list_item_popup_menu);
        mRecyclerView.setAdapter(mAdapter);

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
        //create JsonObjectRequest and pass it to Volley
        JsonObjectRequest moviesListJsonObject = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //successfully
                        try {
                            JSONArray results = response.getJSONArray("results");
                            for(int i=0; i<results.length(); i++) {
                                JSONObject tempMovieJsonObj = results.getJSONObject(i);
                                Movie movie = parseMovieJsonObject(tempMovieJsonObj);

                                if(movie != null) {
                                    mMoviesList.add(movie);
                                }
                            }
                            //hide progress dialog when complete loading movies
                            mProgressDialog.dismiss();
                            //notify adapter about changes
                            mAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //hide progress dialog
                        mProgressDialog.dismiss();
                        //errors occur, handle here
                        Log.d(TAG, "onErrorResponse: error: " + error.toString());
                    }
                });
        //making network request to get json object from themoviedb.org
        //having trouble when add to request queue using singleton class methods
//        mNetworkRequest.addToRequestQueue(moviesListJsonObject, NETWORK_REQUEST_TAG);
        moviesListJsonObject.setTag(NETWORK_REQUEST_TAG);
        mNetworkRequest.getRequestQueue().add(moviesListJsonObject);
    }

    private Movie parseMovieJsonObject(JSONObject obj) {
        Movie movie = new Movie();
        try {

            movie.setId(obj.getInt("id"));
            movie.setTitle(obj.getString("title"));
            movie.setOverview(obj.getString("overview"));
            movie.setReleaseDate(obj.getString("release_date"));
            movie.setRuntime(0);//put ternary condition here maybe
            movie.setCountry("Unknown");
            movie.setStatus("Unknown");
            movie.setVoteAverage(obj.getDouble("vote_average"));
            movie.setPosterPath( (obj.isNull("poster_path"))
                                    ? ""
                                    : obj.getString("poster_path"));
            //get genre id from movie json object and use it to get genre name from genre list
            JSONArray genreIds = obj.getJSONArray("genre_ids");
            if(mGenreListFromApi.size() > 0) {
                List<Genre> movieGenreList = new ArrayList<>();
                for(int i=0; i<genreIds.length(); i++) {
                    for(Genre g : mGenreListFromApi) {
                        if(genreIds.getInt(i) == g.getId()) {
                            //found matching id
                            movieGenreList.add(g);
                            break;
                        }
                    }
                }

                movie.setGenres(movieGenreList);

            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return movie;
    }

    private void sendRequestToGetGenreList(String url) {
        JsonObjectRequest genreJsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //successfully get data
                        try {
                            JSONArray genres = response.getJSONArray("genres");
                            parseGenreList(genres);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: Error getting genre list " + error.toString());
                    }
                });

        mNetworkRequest.addToRequestQueue(genreJsonRequest, NETWORK_REQUEST_TAG);
    }

    private void parseGenreList(JSONArray genres) {
        for(int i=0; i<genres.length(); i++) {
            try {
                JSONObject genreJsonObj = genres.getJSONObject(i);
                int genreId = genreJsonObj.getInt("id");
                String genreName = genreJsonObj.getString("name");

                Genre genre = new Genre(genreId, genreName);
                mGenreListFromApi.add(genre);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //cache genres list value to app
        ( (App) getActivity().getApplication()).setGenreListFromApi(mGenreListFromApi);
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

}
