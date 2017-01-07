package com.nguyen.paul.thanh.walletmovie.fragments;

import android.content.Context;
import android.net.Uri;
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

    private OnFragmentInteractionListener mListener;
    private ViewPager mPager;
    private TabLayout mTabLayout;
    private Context mContext;
    private MoviePagerAdapter mPagerAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }


    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
//        Bundle args = new Bundle();
//
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);

        //get viewpager ref and set adapter for it
        mPager = (ViewPager) view.findViewById(R.id.view_pager);
//            mPager.setOffscreenPageLimit(3);
        mPagerAdapter = new MoviePagerAdapter(getChildFragmentManager(), mContext);
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPageChangeListener());

        //get tab layout ref and incorporate viewpager with it
        mTabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        mTabLayout.setupWithViewPager(mPager);

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private static class ViewPageChangeListener extends ViewPager.SimpleOnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            Log.d(TAG, "onPageSelected: position - " + position);
        }
    }

}
