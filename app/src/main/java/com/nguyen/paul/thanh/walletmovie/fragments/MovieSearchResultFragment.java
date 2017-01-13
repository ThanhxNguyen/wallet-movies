package com.nguyen.paul.thanh.walletmovie.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nguyen.paul.thanh.walletmovie.R;

public class MovieSearchResultFragment extends Fragment {

    public MovieSearchResultFragment() {
        // Required empty public constructor
    }

    public static MovieSearchResultFragment newInstance(String param1, String param2) {
        MovieSearchResultFragment fragment = new MovieSearchResultFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movie_search_result, container, false);
    }

}
