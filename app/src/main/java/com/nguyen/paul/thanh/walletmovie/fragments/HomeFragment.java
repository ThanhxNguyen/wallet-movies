package com.nguyen.paul.thanh.walletmovie.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.adapters.MoviePagerAdapter;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    public static final String FRAGMENT_TAG = HomeFragment.class.getSimpleName();

    private ViewPager mPager;
    private TabLayout mTabLayout;
    private Context mContext;
    private MoviePagerAdapter mPagerAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }


    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Log.d(TAG, "newInstance:");
//        Bundle args = new Bundle();
//
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        Log.e(TAG, "onAttach: ");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //retain this fragment state during activity re-creation progress
        setRetainInstance(true);

        Log.d(TAG, "onCreate: " + TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //test
        Log.d(TAG, "onCreateView:");

        // Inflate the layout for this fragment
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);

        //get viewpager ref and set adapter for it
        mPager = (ViewPager) view.findViewById(R.id.view_pager);
        mPagerAdapter = new MoviePagerAdapter(getChildFragmentManager(), mContext);
        mPager.setAdapter(mPagerAdapter);

        //get tab layout ref and incorporate viewpager with it
        mTabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        mTabLayout.setupWithViewPager(mPager);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "onSaveInstanceState: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e(TAG, "onDestroyView: ");
    }

}
