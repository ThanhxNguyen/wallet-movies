package com.nguyen.paul.thanh.walletmovie.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
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
import com.nguyen.paul.thanh.walletmovie.model.Movie;
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
public class MoviePagerFragment extends Fragment implements MovieRecyclerViewAdapter.OnRecyclerViewClickListener {
    private static final String TAG = "MoviePagerFragment";

    private static final String TAB_POSITION = "tab_position";
    public static final String NETWORK_REQUEST_TAG = "network_request_tag";
    private int mTabPosition;
    private Context mContext;
    private NetworkRequest mNetworkRequest;
    private List<Movie> mMoviesList;
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
                    url = MovieQueryBuilder.getInstance().discover().build().toString();
                    break;
                case 2:
                    url = MovieQueryBuilder.getInstance().discover().build().toString();
                    break;
                default:
                    url = MovieQueryBuilder.getInstance().discover().build().toString();
                    break;
            }

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Top pager");
        // Inflate the layout for this fragment
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_movie_pager, container, false);

        mRecylerView = (RecyclerView) view.findViewById(R.id.movie_list);
        //layout manager
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 2);
//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecylerView.setItemAnimator(new DefaultItemAnimator());
        mRecylerView.setLayoutManager(layoutManager);
        //setup recyclerview adapter here
        mAdapter = new MovieRecyclerViewAdapter(mContext, mMoviesList, this);
        mRecylerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        mNetworkRequest.cancelPendingRequests(NETWORK_REQUEST_TAG);
    }

    private Movie parseMovieJsonObject(JSONObject obj) {
        Movie movie = new Movie();
        try {
            movie.setId(obj.getInt("id"));
            movie.setTitle(obj.getString("title"));
            movie.setOverview(obj.getString("overview"));
            movie.setReleaseDate(obj.getString("release_date"));
            movie.setRuntime(0);
            movie.setCountry(null);
            movie.setStatus(null);
            movie.setVoteAverage(obj.getDouble("vote_average"));
            movie.setPosterPath(obj.getString("poster_path"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return movie;
    }

    @Override
    public void onRecyclerViewClick(Movie movie) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, MovieDetailsFragment.newInstance(movie))
                .addToBackStack(null)
                .commit();
    }
}
