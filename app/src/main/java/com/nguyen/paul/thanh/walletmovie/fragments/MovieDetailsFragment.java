package com.nguyen.paul.thanh.walletmovie.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.adapters.CastRecyclerViewAdapter;
import com.nguyen.paul.thanh.walletmovie.model.Cast;
import com.nguyen.paul.thanh.walletmovie.model.Genre;
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
 */
public class MovieDetailsFragment extends Fragment
                                implements YouTubePlayer.OnInitializedListener,
                                            CastRecyclerViewAdapter.OnMovieCastItemClick{

    private static final String TAG = "MovieDetailsFragment";

    public static final String FRAGMENT_TAG = MovieDetailsFragment.class.getSimpleName();

    private static final String YOUTUBE_API_KEY = "AIzaSyASofF0E6Lmss9m-u1e75MXPxZTcToeF9c";

    private static final String MOVIE_PARCELABLE_KEY = "movie_parcelable_key";

    private static final String NETWORK_REQUEST_TAG = "network_request_tag";

    private NetworkRequest mNetworkRequest;
    private String trailerVideoKey;

    private Context mContext;
    private TextView mTitle;
    private TextView mReleaseDateValue;
    private TextView mVoteValue;
    private TextView mGenres;
    private TextView mDescription;
    private RecyclerView mCastRecyclerView;
    private CastRecyclerViewAdapter mCastRecyclerViewAdapter;
    private List<Cast> mCastList;

    private ProgressDialog mProgressDialog;

    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    public static MovieDetailsFragment newInstance(Movie movie) {
        
        Bundle args = new Bundle();
        args.putParcelable(MOVIE_PARCELABLE_KEY, movie);
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //retain this fragment state during activity re-creation progress
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mNetworkRequest = NetworkRequest.getInstance(mContext);
        mCastList = new ArrayList<>();

        mProgressDialog = new ProgressDialog(mContext, ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Loading data...");
    }

    @Override
    public void onStop() {
        super.onStop();
        mNetworkRequest.getRequestQueue().cancelAll(NETWORK_REQUEST_TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);

        //setup recycler view list for movie casts
        mCastRecyclerView = (RecyclerView) view.findViewById(R.id.movie_cast_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayout.HORIZONTAL, false);
//        mCastRecyclerView.addItemDecoration(new RecyclerViewGridSpaceItemDecorator(1, dpToPx(20), true));
        mCastRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mCastRecyclerView.setLayoutManager(layoutManager);
        mCastRecyclerViewAdapter = new CastRecyclerViewAdapter(mContext, mCastList, this);
        //set adapter for recycler view
        mCastRecyclerView.setAdapter(mCastRecyclerViewAdapter);

        mTitle = (TextView) view.findViewById(R.id.movie_title);
        mReleaseDateValue = (TextView) view.findViewById(R.id.movie_release_date_value);
        mVoteValue = (TextView) view.findViewById(R.id.movie_vote_value);
        mGenres = (TextView) view.findViewById(R.id.movie_genres);
        mDescription = (TextView) view.findViewById(R.id.movie_description);

        Bundle args = getArguments();
        if(args != null) {
            Movie movie = args.getParcelable(MOVIE_PARCELABLE_KEY);

            displayMovieTrailerOrPoster(movie);

            displayMovieDetails(movie);

            populateCastList(movie.getId());

        }

        return view;
    }

    private void populateCastList(int movieId) {
        mCastList.clear();
        final int castLimit = 6;
        String movieCastListUrl = MovieQueryBuilder.getInstance()
                                                    .movies()
                                                    .getCasts(movieId)
                                                    .build();
        JsonObjectRequest castJsonObjectRequest = new JsonObjectRequest(Request.Method.GET, movieCastListUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray casts = response.getJSONArray("cast");
                            //get the first 6 casts for now
                            for(int i=0; i<castLimit; i++) {
                                JSONObject castJsonObj = casts.getJSONObject(i);
                                Cast cast = new Cast();
                                cast.setId(castJsonObj.getInt("id"));
                                cast.setName(castJsonObj.getString("name"));
                                cast.setCharacter(castJsonObj.getString("character"));
                                cast.setProfilePath(castJsonObj.getString("profile_path"));

                                //add new cast to the list
                                mCastList.add(cast);
                            }

                            //update adapter
                            mCastRecyclerViewAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //handle error
                    }
                });

        //set tag for this request
        castJsonObjectRequest.setTag(NETWORK_REQUEST_TAG);
        //add to request queue
        mNetworkRequest.getRequestQueue().add(castJsonObjectRequest);
    }

    private void displayMovieDetails(Movie movie) {
        mTitle.setText(movie.getTitle());
        mReleaseDateValue.setText(movie.getReleaseDate());
        mVoteValue.setText(String.valueOf(movie.getVoteAverage()));
        List<Genre> genres = movie.getGenres();
        StringBuilder genreValues = new StringBuilder();
        String prefix = " | ";
        for(int i=0; i<genres.size(); i++) {
            genreValues.append(genres.get(i).getName());
            genreValues.append(prefix);

        }
        mGenres.setText( (genreValues.toString().length()>0) ? genreValues.delete(genreValues.length()-2, genreValues.length()-1) : "Unknown");
        mDescription.setText(movie.getOverview());
    }

    private void displayMovieTrailerOrPoster(final Movie movie) {
        //show progress dialog since loading youtube video might take sometimes
        mProgressDialog.show();

        //get movie trailers url
        String movieTrailerUrl = MovieQueryBuilder.getInstance()
                                                    .movies()
                                                    .getVideos(movie.getId())
                                                    .build();

        JsonObjectRequest movieTrailersJsonRequest = new JsonObjectRequest(Request.Method.GET, movieTrailerUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray trailerList = response.getJSONArray("results");
                            if(trailerList.length() > 0) {
                                //get the first trailer videos for this movie (normally the official one)
                                JSONObject trailerObj = trailerList.getJSONObject(0);
                                String trailerKey = trailerObj.getString("key");
                                trailerVideoKey = trailerKey;
                                loadVideo(trailerVideoKey);

                            } else {
                                //TMDB api provides a range of trailer videos link for each movie, there is a very low
                                //chance that there is no movie trailer available. In this case, display movie poster instead for better UX
                                //problem: having trouble rendering image correctly
                                //clue: something to do with image size when append to framelayout or need to re-draw UI
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //handle error
                    }
                });

        movieTrailersJsonRequest.setTag(NETWORK_REQUEST_TAG);
        mNetworkRequest.getRequestQueue().add(movieTrailersJsonRequest);

    }

    private void loadVideo(String videoKey) {
        //load youtube trailer video
        YouTubePlayerSupportFragment youTubePlayerSupportFragment = YouTubePlayerSupportFragment.newInstance();
        //initialize youtube player
        youTubePlayerSupportFragment.initialize(YOUTUBE_API_KEY, this);

        //add youtube player fragment to the page
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.youtube_video_frame, youTubePlayerSupportFragment)
                .commit();

        //hide progress dialog since movie details and movie trailer have been loaded
        mProgressDialog.dismiss();
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        //successfully load youtube video
        if(!wasRestored) {
//            youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
            //hard video id here, will replace soon
            youTubePlayer.cueVideo(trailerVideoKey);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if (youTubeInitializationResult.isUserRecoverableError()) {
            //do something
        } else {
            Toast.makeText(mContext, youTubeInitializationResult.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMovieCastItemClick(Cast cast) {

        getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content_frame, MovieCastDetailsFragment.newInstance(cast))
                        .addToBackStack(null)
                        .commit();
    }
}
