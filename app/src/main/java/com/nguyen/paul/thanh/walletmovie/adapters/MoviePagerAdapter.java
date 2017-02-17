package com.nguyen.paul.thanh.walletmovie.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nguyen.paul.thanh.walletmovie.pages.home.MovieListFragment;

/**
 * Adapter for movie pager on home page
 */

public class MoviePagerAdapter extends FragmentStatePagerAdapter {

    private static final int PAGER_COUNT = 3;

    public MoviePagerAdapter(FragmentManager fm) {
        super(fm);
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
     */
    @Override
    public CharSequence getPageTitle(int position) {
        //display tab title appropriately
        String tabTitles[] = new String[] {"Popular", "New Release", "Upcoming"};
        return tabTitles[position];
    }
}
