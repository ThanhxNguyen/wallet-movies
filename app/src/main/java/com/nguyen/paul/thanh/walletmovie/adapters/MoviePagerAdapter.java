package com.nguyen.paul.thanh.walletmovie.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nguyen.paul.thanh.walletmovie.fragments.MovieListFragment;

/**
 * Created by THANH on 5/01/2017.
 */

public class MoviePagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "MovieFragmentPagerAdapt";

    final int PAGER_COUNT = 3;
    private String tabTitles[] = new String[] {"Popular", "Showing", "Upcoming"};
    private Context mContext;

    public MoviePagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.mContext = context;
    }

    @Override
    public Fragment getItem(int position) {

        return MovieListFragment.newInstance(MovieListFragment.DISPLAY_MOVIES_FOR_VIEWPAGER, position);
    }

    @Override
    public int getCount() {
        return PAGER_COUNT;
    }

    /**
     * set tab title appropriately
     * @param position
     * @return
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
