package com.nguyen.paul.thanh.walletmovie.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.model.Movie;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieDetailsFragment extends Fragment {

    private static final String MOVIE_PARCELABLE_KEY = "movie_parcelable_key";
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);

        title = (TextView) view.findViewById(R.id.movie_title);

        Bundle args = getArguments();
        if(args != null) {
            Movie movie = args.getParcelable(MOVIE_PARCELABLE_KEY);
            title.setText(movie.getTitle());
        }

        return view;
    }

}
