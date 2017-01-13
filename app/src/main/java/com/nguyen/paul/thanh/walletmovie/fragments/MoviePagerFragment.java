package com.nguyen.paul.thanh.walletmovie.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.adapters.MovieRecyclerViewAdapter;
import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.utilities.AddFavouriteTask;
import com.nguyen.paul.thanh.walletmovie.utilities.MovieQueryBuilder;
import com.nguyen.paul.thanh.walletmovie.utilities.NetworkRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MoviePagerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MoviePagerFragment extends Fragment 
        implements MovieRecyclerViewAdapter.OnRecyclerViewClickListener {
    
    private static final String TAG = "MoviePagerFragment";

    public static final String FRAGMENT_TAG = MoviePagerFragment.class.getSimpleName();

    private static final String TAB_POSITION = "tab_position";
    private static final String NETWORK_REQUEST_TAG = "network_request_tag";
    private int mTabPosition;
    private Context mContext;
    private NetworkRequest mNetworkRequest;
    private List<Movie> mMoviesList;
    private List<Genre> mGenreListFromApi;
    private MovieRecyclerViewAdapter mAdapter;
    private RecyclerView mRecylerView;

    public MoviePagerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MoviePagerFragment.
     */
    public static MoviePagerFragment newInstance(int tabPosition) {
        MoviePagerFragment fragment = new MoviePagerFragment();
        Bundle args = new Bundle();
        args.putInt(TAB_POSITION, tabPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mNetworkRequest = NetworkRequest.getInstance(mContext);
        mMoviesList = new ArrayList<>();
        mGenreListFromApi = new ArrayList<>();

        String genreListUrl = MovieQueryBuilder.getInstance().getGenreListUrl();
        sendRequestToGetGenreList(genreListUrl);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args != null) {
            String url;
            mTabPosition = args.getInt(TAB_POSITION, 0);

            /* grab movies according tab position
            * 0: top movies list
            * 1: now showing movies list
            * 3: upcoming movies list
            */
            switch (mTabPosition) {
                case 0:
                    url = MovieQueryBuilder.getInstance().discover().build().toString();
                    break;
                case 1:
                    url = "https://api.themoviedb.org/3/discover/movie?api_key=1bd3f3a91c22eef0c9d9c15212f43593&primary_release_date.gte=2014-09-15&primary_release_date.lte=2014-10-22";
                    break;
                case 2:
                    url = "https://api.themoviedb.org/3/discover/movie?api_key=1bd3f3a91c22eef0c9d9c15212f43593&certification_country=US&certification=R&sort_by=vote_average.desc";
                    break;
                default:
                    url = MovieQueryBuilder.getInstance().discover().build().toString();
                    break;
            }

            sendRequestToGetMovieList(url);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_movie_pager_item, container, false);

        mRecylerView = (RecyclerView) view.findViewById(R.id.movie_list);
        //layout manager
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 2);
//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecylerView.setItemAnimator(new DefaultItemAnimator());
        mRecylerView.setLayoutManager(layoutManager);
        //setup recyclerview adapter here
        mAdapter = new MovieRecyclerViewAdapter(mContext, mMoviesList, this, R.menu.home_movie_list_item_popup_menu);
        mRecylerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        mNetworkRequest.cancelPendingRequests(NETWORK_REQUEST_TAG);
    }

    private void sendRequestToGetMovieList(String url) {
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
                                mMoviesList.add(movie);
                            }

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
                        //errors occur, handle here
                    }
                });
        //making network request to get json object from themoviedb.org
        mNetworkRequest.addToRequestQueue(moviesListJsonObject, NETWORK_REQUEST_TAG);
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
            movie.setPosterPath(obj.getString("poster_path"));
            //get genre id from movie json object and use it to get genre name from genre list
            JSONArray genreIds = obj.getJSONArray("genre_ids");
            if(mGenreListFromApi.size() == 0) {
                //list empty

            } else {
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
        AddFavouriteTask task = new AddFavouriteTask(mContext, genreList);
        task.execute(movie);
    }

}
