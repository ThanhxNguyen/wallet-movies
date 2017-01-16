package com.nguyen.paul.thanh.walletmovie.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.utilities.MovieQueryBuilder;
import com.nguyen.paul.thanh.walletmovie.utilities.NetworkRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailsFragment extends Fragment implements YouTubePlayer.OnInitializedListener {

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
    //use for displaying movie poster if there is no trailer available
    private ImageView mPoster;

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

        mPoster = (ImageView) view.findViewById(R.id.movie_thumbnail);

        mTitle = (TextView) view.findViewById(R.id.movie_title);
        mReleaseDateValue = (TextView) view.findViewById(R.id.movie_release_date_value);
        mVoteValue = (TextView) view.findViewById(R.id.movie_vote_value);
        mGenres = (TextView) view.findViewById(R.id.movie_genres);
        mDescription = (TextView) view.findViewById(R.id.movie_description);

        Bundle args = getArguments();
        if(args != null) {
            Movie movie = args.getParcelable(MOVIE_PARCELABLE_KEY);

            displayMovieTrailerOrPoster(movie);

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

        return view;
    }

    private void displayMovieTrailerOrPoster(final Movie movie) {
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
                                //show imageview
//                                mPoster.setVisibility(View.VISIBLE);
//                                //there is no trailer available for this movie
//                                //use poster image instead
//                                String imgUrl = MovieQueryBuilder.getInstance().getImageBaseUrl("w300") + movie.getPosterPath();
//                                Glide.with(mContext).load(imgUrl)
//                                        .crossFade()
//                                        .fitCenter()
//                                        .placeholder(R.drawable.ic_image_placeholder_white)
//                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                                        .error(R.drawable.ic_image_placeholder_white)
//                                        .into(mPoster);
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
}
