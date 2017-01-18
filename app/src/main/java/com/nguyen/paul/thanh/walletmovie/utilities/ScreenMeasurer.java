package com.nguyen.paul.thanh.walletmovie.utilities;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Display;

/**
 * Utility class to get screen sizes in dp
 * Reference: https://www.lynda.com/Android-tutorials/Measure-screen-Java/487934/531385-4.html
 */

public class ScreenMeasurer {

    private int dpWidth;
    private int dpHeight;

    public ScreenMeasurer(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = activity.getResources().getDisplayMetrics().density;

        dpWidth = Math.round(outMetrics.widthPixels / density);
        dpHeight = Math.round(outMetrics.heightPixels / density);

    }

    public int getDpWidth() {
        return dpWidth;
    }

    public int getDpHeight() {
        return dpHeight;
    }

}
