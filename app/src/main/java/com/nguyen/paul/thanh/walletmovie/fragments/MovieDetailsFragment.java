package com.nguyen.paul.thanh.walletmovie.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailsFragment extends Fragment implements YouTubePlayer.OnInitializedListener {

    private static final String TAG = "MovieDetailsFragment";

    public static final String FRAGMENT_TAG = MovieDetailsFragment.class.getSimpleName();

    private static final String YOUTUBE_API_KEY = "AIzaSyASofF0E6Lmss9m-u1e75MXPxZTcToeF9c";

    private static final String MOVIE_PARCELABLE_KEY = "movie_parcelable_key";

    private Context mContext;
    private TextView title;

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

        Log.d(TAG, "onCreate: " + TAG);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);

        title = (TextView) view.findViewById(R.id.movie_title);

        Bundle args = getArguments();
        if(args != null) {
            Movie movie = args.getParcelable(MOVIE_PARCELABLE_KEY);

            //load youtube trailer video
            YouTubePlayerSupportFragment youTubePlayerSupportFragment = YouTubePlayerSupportFragment.newInstance();
            //initialize youtube player
            youTubePlayerSupportFragment.initialize(YOUTUBE_API_KEY, this);

            //add youtube player fragment to the page
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.youtube_video_frame, youTubePlayerSupportFragment)
                    .commit();

            title.setText(movie.getTitle());

            //will replace soon to populate genre list value
            for(Genre g : movie.getGenres()) {
//                Log.d(TAG, "onCreateView: genre: " + g.getName());
            }
        }

        return view;
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        //successfully load youtube video
        if(!wasRestored) {
//            youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
            //hard video id here, will replace soon
            youTubePlayer.cueVideo("frdj1zb9sMY");
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
