package com.nguyen.paul.thanh.walletmovie.pages.castdetails;


import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nguyen.paul.thanh.walletmovie.MainActivity;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.model.Cast;
import com.nguyen.paul.thanh.walletmovie.pages.home.MovieListFragment;
import com.nguyen.paul.thanh.walletmovie.utilities.MovieQueryBuilder;
import com.nguyen.paul.thanh.walletmovie.utilities.NetworkRequest;
import com.nguyen.paul.thanh.walletmovie.utilities.ScreenMeasurer;

/**
 * Fragment for movie cast details
 */
public class CastDetailsFragment extends Fragment implements CastDetailsContract.View {

    private static final String CAST_PARCELABLE_KEY = "cast_parcelable_key";
    private static final String NETWORK_REQUEST_TAG = "network_request_tag";

    private Context mContext;
    private MainActivity mActivity;
    private NetworkRequest mNetworkRequest;
    private TextView mCastName;
    private TextView mCastBirthdayValue;
    private TextView mCastBirthPlaceValue;
    private TextView mCastBiography;
    private ImageView mParallaxImage;
    private ConstraintLayout mLayout;

    private CastDetailsContract.Presenter mPresenter;

    public CastDetailsFragment() {
        // Required empty public constructor
    }

    public static CastDetailsFragment newInstance(Cast cast) {
        CastDetailsFragment fragment = new CastDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(CAST_PARCELABLE_KEY, cast);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mPresenter = new CastDetailsPresenter(this);

        if(getActivity() instanceof MainActivity) {
            mActivity = (MainActivity) getActivity();
        }
        mNetworkRequest = NetworkRequest.getInstance(mContext);
    }

    @Override
    public void onStop() {
        super.onStop();
        mNetworkRequest.getRequestQueue().cancelAll(NETWORK_REQUEST_TAG);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mParallaxImage.setImageDrawable(null);
        mParallaxImage.setVisibility(View.GONE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        //set toolbar title to empty
        if(mActivity != null) mActivity.setToolbarTitle("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //get reference of the parallax image from parent AppbarLayout
        mParallaxImage = (ImageView) getActivity().getWindow().getDecorView().findViewById(R.id.parallax_image);
        mParallaxImage.setVisibility(View.VISIBLE);

        //get CollapsingToolbarLayout reference and set title appropriately
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) getActivity().getWindow().getDecorView().findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(getString(R.string.title_cast_details));
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_cast_details, container, false);

        mLayout = (ConstraintLayout) view.findViewById(R.id.main_layout_constraint);
//        mSpinner = (ProgressBar) view.findViewById(R.id.spinner);
        mCastName = (TextView) view.findViewById(R.id.movie_cast_name);
        mCastBirthdayValue = (TextView) view.findViewById(R.id.movie_cast_birthday_value);
        mCastBirthPlaceValue = (TextView) view.findViewById(R.id.movie_cast_birth_place_value);
        mCastBiography = (TextView) view.findViewById(R.id.movie_cast_biography);
        Button moviesForThisCastBtn = (Button) view.findViewById(R.id.movies_for_this_cast_btn);


        mLayout.setVisibility(View.GONE);

        Bundle args = getArguments();
        if(args != null) {
            final Cast cast = args.getParcelable(CAST_PARCELABLE_KEY);
            assert cast != null;
            final int castId = cast.getId();
            //set click listener
            moviesForThisCastBtn.setOnClickListener(new View.OnClickListener() {
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

            mPresenter.getCastDetails(singleCastDetailsUrl);

        }

        return view;
    }

    @Override
    public void populateCastDetails(Cast cast) {
        if(cast != null) {
            String unknown = "Unknown";
            String castBirthday = ( TextUtils.isEmpty(cast.getBirthday()) || cast.getBirthday().equals("null") ) ? unknown : cast.getBirthday();
            String castBirthPlace = ( TextUtils.isEmpty(cast.getPlaceOfBirth()) || cast.getPlaceOfBirth().equals("null") ) ? unknown : cast.getPlaceOfBirth();
            String castBio = ( TextUtils.isEmpty(cast.getBiography()) || cast.getBiography().equals("null") ) ? unknown : cast.getBiography();

            //update UI
            mCastName.setText(cast.getName());
            mCastBirthdayValue.setText(castBirthday);
            mCastBirthPlaceValue.setText(castBirthPlace);
            mCastBiography.setText(castBio);

            //show cast details
            mLayout.setVisibility(View.VISIBLE);

            String castProfilePhotoSize = "w342";

            if(getActivity() != null) {
                //get screen size and display cast profile photo appropriately.
                ScreenMeasurer screenMeasurer = new ScreenMeasurer(getActivity());
                int screenWidth = screenMeasurer.getDpWidth();

                castProfilePhotoSize = (screenWidth < 840) ? "w342" : "w500";
            }

            //load cast profile image
            String castProfileImageUrl = MovieQueryBuilder.getInstance()
                    .getImageBaseUrl(castProfilePhotoSize) + cast.getProfilePath();
            Glide.with(mContext).load(castProfileImageUrl)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .error(R.drawable.ic_account_circle_white_24dp)
                    .into(mParallaxImage);

        }
    }
}
