package com.nguyen.paul.thanh.walletmovie.pages.moviedetails;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.nguyen.paul.thanh.walletmovie.App;
import com.nguyen.paul.thanh.walletmovie.MainActivity;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.adapters.CastRecyclerViewAdapter;
import com.nguyen.paul.thanh.walletmovie.pages.castdetails.CastDetailsFragment;
import com.nguyen.paul.thanh.walletmovie.model.Cast;
import com.nguyen.paul.thanh.walletmovie.model.Genre;
import com.nguyen.paul.thanh.walletmovie.model.Movie;
import com.nguyen.paul.thanh.walletmovie.ui.RecyclerViewWithEmptyView;
import com.nguyen.paul.thanh.walletmovie.utilities.MovieQueryBuilder;
import com.nguyen.paul.thanh.walletmovie.utilities.NetworkRequest;
import com.nguyen.paul.thanh.walletmovie.utilities.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to display movie details
 */
public class MovieDetailsFragment extends Fragment
        implements YouTubePlayer.OnInitializedListener,
        CastRecyclerViewAdapter.OnMovieCastItemClick,
        MovieDetailsContract.View {

    private static final String TAG = "MovieDetailsFragment";

    private static final String YOUTUBE_API_KEY = "AIzaSyASofF0E6Lmss9m-u1e75MXPxZTcToeF9c";

    private static final String MOVIE_PARCELABLE_KEY = "movie_parcelable_key";

    private static final String NETWORK_REQUEST_TAG = "network_request_tag";
    private static final String DEFAULT_ERROR_MESSAGE = "An error has occured!";
    private static final String FAIL_LOAD_YOUTUBE_VIDEO_ERROR = "Failed to load trailer video!";
    private static final String YOUTUBE_FRAGMENT_TAG = "youtube_fragment_tag";

    private NetworkRequest mNetworkRequest;
    private String trailerVideoKey;

    private Context mContext;
    private MainActivity mActivity;
    private TextView mTitle;
    private TextView mReleaseDateValue;
    private TextView mVoteValue;
    private TextView mGenres;
    private TextView mDescription;
    private ProgressBar mCastListSpinner;
    private CastRecyclerViewAdapter mCastRecyclerViewAdapter;
    private List<Cast> mCastList;
    private Movie mMovie;
    private YouTubePlayerSupportFragment mYouTubePlayerSupportFragment;

    private MovieDetailsContract.Presenter mPresenter;

    private ViewGroup mParentContainer;

    private FirebaseAuth mAuth;

    //display mMovie poster image if there is no trailers available
    private ImageView mMoviePoster;

    //    private ProgressDialog mProgressDialog;
    private List<Genre> mGenreListFromApi;
    private YouTubePlayer mYoutubePlayer;
    private RecyclerViewWithEmptyView mCastRecyclerView;
    private TextView mPlaceholderView;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mPresenter = new MovieDetailsPresenter(this);

        if (getActivity() instanceof MainActivity) {
            mActivity = (MainActivity) getActivity();
        }
        mNetworkRequest = NetworkRequest.getInstance(mContext);
        mCastList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();

        //get genre values from cache
        mGenreListFromApi = ((App) getActivity().getApplication()).getGenreListFromApi();

        if (mGenreListFromApi.size() == 0) {
            //if there is no genre values from cache, send a http request to get them
            String genreListUrl = MovieQueryBuilder.getInstance().getGenreListUrl();
            sendRequestToGetGenreList(genreListUrl);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //enable fragment to append menu items to toolbar
        setHasOptionsMenu(true);

        //retain this fragment state during activity re-creation progress
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mParentContainer = container;
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);

        //setup recycler view list for mMovie casts
        mCastRecyclerView = (RecyclerViewWithEmptyView) view.findViewById(R.id.movie_cast_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayout.HORIZONTAL, false);
        mCastRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mCastRecyclerView.setLayoutManager(layoutManager);
        //set placeholder view when the list is empty
        mPlaceholderView = (TextView) view.findViewById(R.id.placeholder_view);
        mCastRecyclerView.setPlaceholderView(mPlaceholderView);
        //make scrolling smooth when recyclerview inside another scrolling layout
        mCastRecyclerView.setNestedScrollingEnabled(false);
        mCastRecyclerViewAdapter = new CastRecyclerViewAdapter(mContext, mCastList, this);
        //set adapter for recycler view
        mCastRecyclerView.setAdapter(mCastRecyclerViewAdapter);

        mTitle = (TextView) view.findViewById(R.id.movie_title);
        mReleaseDateValue = (TextView) view.findViewById(R.id.movie_release_date_value);
        mVoteValue = (TextView) view.findViewById(R.id.movie_vote_value);
        mGenres = (TextView) view.findViewById(R.id.movie_genres);
        mDescription = (TextView) view.findViewById(R.id.movie_description);
        mMoviePoster = (ImageView) view.findViewById(R.id.movie_poster);

        mCastListSpinner = (ProgressBar) view.findViewById(R.id.spinner);
        mCastListSpinner.setVisibility(View.VISIBLE);
        mPlaceholderView.setVisibility(View.GONE);

        Bundle args = getArguments();
        if (args != null) {
            mMovie = args.getParcelable(MOVIE_PARCELABLE_KEY);

            displayMovieTrailerOrPoster(mMovie);

            displayMovieDetails(mMovie);

            populateCastList(mMovie.getId());

        }

        initializeYoutubePlayer();

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        //set toolbar title
        if (mActivity != null) mActivity.setToolbarTitle(R.string.title_movie_details);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //show the "+" icon on toolbar
        menu.findItem(R.id.action_add).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //add movie to favourites
        if (id == R.id.action_add) {
            showAddMovieConfirmDialog();
            return true;
        }

        return false;
    }

    private void showAddMovieConfirmDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Confirmation");
        builder.setMessage("Add this movie to your favourites?");
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do nothing
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mMovie != null && mGenreListFromApi != null) {
                    //start adding movie to favourites
                    mPresenter.addMovieToFavourites(mMovie);
                } else {
                    showSnackBarWithResult(DEFAULT_ERROR_MESSAGE);
                }
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
        for (int i = 0; i < genres.length(); i++) {
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
        ((App) getActivity().getApplication()).setGenreListFromApi(mGenreListFromApi);
    }

    @Override
    public void onStop() {
        super.onStop();
        mNetworkRequest.getRequestQueue().cancelAll(NETWORK_REQUEST_TAG);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mYoutubePlayer != null) {
            mYoutubePlayer = null;
        }
    }

    private void populateCastList(int movieId) {
        mCastList.clear();
        final int castLimit = 6;
        String movieCastListUrl = MovieQueryBuilder.getInstance()
                .movies()
                .getCasts(movieId)
                .build();

        mPresenter.getCasts(movieCastListUrl);
    }

    private void displayMovieDetails(Movie movie) {
        String unknown = "Unknown";
        mTitle.setText(movie.getTitle());
        mReleaseDateValue.setText(TextUtils.isEmpty(movie.getReleaseDate()) ? unknown : movie.getReleaseDate());
        mVoteValue.setText(TextUtils.isEmpty(String.valueOf(movie.getVoteAverage())) ? unknown : String.valueOf(movie.getVoteAverage()));
        List<Genre> genres = movie.getGenres();
        StringBuilder genreValues = new StringBuilder();
        String prefix = " | ";
        for (int i = 0; i < genres.size(); i++) {
            genreValues.append(genres.get(i).getName());
            genreValues.append(prefix);

        }

        String noDescription = "There is currently no description available for this movie yet. The description will be updated soon in the future. Sorry for any inconveniences.";
        mGenres.setText((genreValues.toString().length() > 0) ? genreValues.delete(genreValues.length() - 2, genreValues.length() - 1) : "Unknown");
        mDescription.setText(TextUtils.isEmpty(movie.getOverview()) ? noDescription : movie.getOverview());
    }

    private void displayMovieTrailerOrPoster(final Movie movie) {

        //get mMovie trailers url
        String movieTrailerUrl = MovieQueryBuilder.getInstance()
                .movies()
                .getVideos(movie.getId())
                .build();
        mPresenter.getTrailers(movieTrailerUrl);

    }

    private void initializeYoutubePlayer() {
        //load youtube trailer video
        mYouTubePlayerSupportFragment = YouTubePlayerSupportFragment.newInstance();
        //initialize youtube player
//        mYouTubePlayerSupportFragment.initialize(YOUTUBE_API_KEY, this);
        //add youtube player fragment to the page
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.youtube_video_frame, mYouTubePlayerSupportFragment, YOUTUBE_FRAGMENT_TAG)
                .commit();
        //hide progress dialog since mMovie details and mMovie trailer have been loaded
        //mProgressDialog.dismiss();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        mYoutubePlayer = youTubePlayer;
//        //successfully load youtube video
        if (!wasRestored) {
            //set style for youtube player
//            youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
            youTubePlayer.cueVideo(trailerVideoKey);
        }

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        //error loading trailer video, display mMovie poster instead
        mMoviePoster.setVisibility(View.VISIBLE);
        //load mMovie thumb from internet
        String imgUrl = MovieQueryBuilder.getInstance().getImageBaseUrl("w342") + mMovie.getPosterPath();

        //test
        Fragment youtubeFragment = getChildFragmentManager().findFragmentByTag(YOUTUBE_FRAGMENT_TAG);
        if (youtubeFragment != null) {
            getChildFragmentManager().beginTransaction()
                    .remove(youtubeFragment)
                    .commit();
        }

        Glide.with(mContext).load(imgUrl)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .error(R.drawable.ic_image_placeholder_white_24dp)
                .into(mMoviePoster);
        if (youTubeInitializationResult.isUserRecoverableError()) {
            //do something
            //mProgressDialog.dismiss();
        } else {
            //mProgressDialog.dismiss();
            showSnackBarWithResult(youTubeInitializationResult.toString());
        }
    }

    @Override
    public void onMovieCastItemClick(Cast cast) {

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, CastDetailsFragment.newInstance(cast))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void updateCastList(List<Cast> castList) {
        if (castList != null) {
            mCastList.clear();
            for (Cast c : castList) {
                mCastList.add(c);
            }
            //update adapter
            mCastRecyclerViewAdapter.notifyDataSetChanged();
            //show cast list
            mCastRecyclerView.setVisibility(View.VISIBLE);
            mCastListSpinner.setVisibility(View.GONE);
            if (mCastList.size() == 0) mPlaceholderView.setVisibility(View.VISIBLE);
        } else {
            mCastRecyclerView.setVisibility(View.VISIBLE);
            mCastListSpinner.setVisibility(View.GONE);
        }
    }

    @Override
    public void showSnackBarWithResult(String message) {
        Utils.createSnackBar(getResources(), mParentContainer, message).show();
    }

    @Override
    public void showSnackBarWithResult(int resStringId) {
        Utils.createSnackBar(getResources(), mParentContainer, getString(resStringId)).show();
    }

    @Override
    public void displayMovieTrailer(List<String> trailerList) {
        trailerVideoKey = trailerList.get(0);

        //initialize youtube player
        if (mYouTubePlayerSupportFragment != null) {
            mYouTubePlayerSupportFragment.initialize(YOUTUBE_API_KEY, MovieDetailsFragment.this);
        }
    }

    @Override
    public void displayMoviePoster() {
        String imgUrl = "";
        if (mMovie != null) {
            imgUrl = MovieQueryBuilder.getInstance().getImageBaseUrl("w342") + mMovie.getPosterPath();
        }

        Fragment youtubeFragment = getChildFragmentManager().findFragmentByTag(YOUTUBE_FRAGMENT_TAG);
        if (youtubeFragment != null) {
            getChildFragmentManager().beginTransaction()
                    .remove(youtubeFragment)
                    .commit();
        }

        //no trailer available, display mMovie poster instead
        mMoviePoster.setVisibility(View.VISIBLE);

        Glide.with(mContext).load(imgUrl)
                .crossFade()
                .placeholder(R.drawable.ic_image_placeholder_white_24dp)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .error(R.drawable.ic_image_placeholder_white_24dp)
                .into(mMoviePoster);
    }
}
