package com.nguyen.paul.thanh.walletmovie.pages.home;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.MainActivity;
import com.nguyen.paul.thanh.walletmovie.adapters.MoviePagerAdapter;

/**
 * Fragment for home page
 */
public class HomeFragment extends Fragment {

    public static final String FRAGMENT_TAG = HomeFragment.class.getSimpleName();
    private MainActivity mActivity;

    public HomeFragment() {
        // Required empty public constructor
    }


    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(getActivity() instanceof MainActivity) {
            mActivity = (MainActivity) getActivity();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //retain this fragment state during activity re-creation progress
//        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);

        //get viewpager ref and set adapter for it
        ViewPager pager = (ViewPager) view.findViewById(R.id.view_pager);
        MoviePagerAdapter pagerAdapter = new MoviePagerAdapter(getChildFragmentManager());
        pager.setAdapter(pagerAdapter);

        //get tab layout ref and incorporate viewpager with it
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(pager);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //set toolbar title
        if(mActivity != null) mActivity.setToolbarTitle(R.string.title_home);
    }
}
