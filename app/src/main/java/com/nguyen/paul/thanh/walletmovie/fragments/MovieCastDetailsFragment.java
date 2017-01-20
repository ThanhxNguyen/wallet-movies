package com.nguyen.paul.thanh.walletmovie.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.model.Cast;
import com.nguyen.paul.thanh.walletmovie.utilities.MovieQueryBuilder;
import com.nguyen.paul.thanh.walletmovie.utilities.NetworkRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MovieCastDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MovieCastDetailsFragment extends Fragment {

    private static final String CAST_PARCELABLE_KEY = "cast_parcelable_key";
    private static final String NETWORK_REQUEST_TAG = "network_request_tag";

    private Context mContext;
    private NetworkRequest mNetworkRequest;
    private ImageView mCastProfile;
    private TextView mCastName;
    private TextView mCastBirthdayValue;
    private TextView mCastBirthPlaceValue;
    private TextView mCastBiography;
    private Button mMoviesForThisCastBtn;

    public MovieCastDetailsFragment() {
        // Required empty public constructor
    }

    public static MovieCastDetailsFragment newInstance(Cast cast) {
        MovieCastDetailsFragment fragment = new MovieCastDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(CAST_PARCELABLE_KEY, cast);
        fragment.setArguments(args);
        return fragment;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //retain this fragment state during activity re-creation progress
        setRetainInstance(true);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(R.string.title_cast_details);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_cast_details, container, false);

        mCastProfile = (ImageView) view.findViewById(R.id.movie_cast_profile);
        mCastName = (TextView) view.findViewById(R.id.movie_cast_name);
        mCastBirthdayValue = (TextView) view.findViewById(R.id.movie_cast_birthday_value);
        mCastBirthPlaceValue = (TextView) view.findViewById(R.id.movie_cast_birth_place_value);
        mCastBiography = (TextView) view.findViewById(R.id.movie_cast_biography);
        mMoviesForThisCastBtn = (Button) view.findViewById(R.id.movies_for_this_cast_btn);

        Bundle args = getArguments();
        if(args != null) {
            final Cast cast = args.getParcelable(CAST_PARCELABLE_KEY);
            final int castId = cast.getId();
            //set click listener
            mMoviesForThisCastBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content_frame,
                                    MovieListFragment.newInstance(MovieListFragment.DISPLAY_MOVIES_RELATED_TO_CAST, castId))
                            .addToBackStack(null)
                            .commit();
                }
            });

            String singleCastDetailsUrl = MovieQueryBuilder.getInstance()
                                                    .movies()
                                                    .getSingleCast(castId)
                                                    .build();

            JsonObjectRequest castDetailsJsonRequest = new JsonObjectRequest(Request.Method.GET, singleCastDetailsUrl, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            String unknown = "Unknown";
                            //there is no need to set id, name, character, profile image path
                            //because they have been set from MovieDetailsFragment
                            try {
                                cast.setBirthday(response.getString("birthday"));
                                cast.setPlaceOfBirth(response.getString("place_of_birth"));
                                cast.setBiography(response.getString("biography"));

                                String castBirthday = TextUtils.isEmpty(cast.getBirthday()) ? unknown : cast.getBirthday();
                                String castBirthPlace = TextUtils.isEmpty(cast.getPlaceOfBirth()) ? unknown : cast.getPlaceOfBirth();
                                String castBio = TextUtils.isEmpty(cast.getBiography()) ? unknown : cast.getBiography();

                                //update UI
                                mCastName.setText(cast.getName());
                                mCastBirthdayValue.setText(castBirthday);
                                mCastBirthPlaceValue.setText(castBirthPlace);
                                mCastBiography.setText(castBio);
                                //load cast profile image
                                String castProfileImageUrl = MovieQueryBuilder.getInstance()
                                                                .getImageBaseUrl("w500") + cast.getProfilePath();
                                Glide.with(mContext).load(castProfileImageUrl)
                                        .crossFade()
                                        .fitCenter()
                                        .placeholder(R.drawable.ic_account_circle_white_24dp)
                                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                        .error(R.drawable.ic_account_circle_white_24dp)
                                        .into(mCastProfile);

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

            castDetailsJsonRequest.setTag(NETWORK_REQUEST_TAG);
            mNetworkRequest.getRequestQueue().add(castDetailsJsonRequest);

        }

        return view;
    }

}
