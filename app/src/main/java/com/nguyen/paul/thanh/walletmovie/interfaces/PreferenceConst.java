package com.nguyen.paul.thanh.walletmovie.interfaces;

/**
 * This interface holds key constants for SharedPreferences
 */

public interface PreferenceConst {
    public static final String GLOBAL_PREF_KEY = "global_preference_key";

    public static final int MOVIE_NAME_SORT = 1;
    public static final int MOVIE_DATE_SORT = 2;
    public static final int MOVIE_VOTE_SORT = 3;

    public interface Authenticate {
        public static final String GUEST_MODE_PREF_KEY = "guest_mode_preference_key";
        public static final String FIRST_TIME_USER_PREF_KEY = "first_time_user_preference_key";
    }

    public interface Settings {
        public static final String MOVIE_SORT_SETTINGS_KEY = "movie_sort_setttings_key";
    }
}
