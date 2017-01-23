package com.nguyen.paul.thanh.walletmovie.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.adapters.MoviePagerAdapter;

/**
 * Fragment for home page
 */
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
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //retain this fragment state during activity re-creation progress
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);

        //get viewpager ref and set adapter for it
        mPager = (ViewPager) view.findViewById(R.id.view_pager);
        mPagerAdapter = new MoviePagerAdapter(getChildFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        //get tab layout ref and incorporate viewpager with it
        mTabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        mTabLayout.setupWithViewPager(mPager);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //set toolbar title
        getActivity().setTitle(R.string.title_home);
    }
}
