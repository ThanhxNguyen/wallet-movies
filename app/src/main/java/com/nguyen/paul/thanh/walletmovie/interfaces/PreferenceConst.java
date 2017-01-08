package com.nguyen.paul.thanh.walletmovie.interfaces;

/**
 * This interface holds key constants for SharedPreferences
 */

public interface PreferenceConst {
    public static final String GLOBAL_PREF_KEY = "global_preference_key";

    public interface Auth {
        public static final String GUEST_MODE_PREF_KEY = "guest_mode_preference_key";
        public static final String FIRST_TIME_USER_PREF_KEY = "first_time_user_preference_key";
    }
}
