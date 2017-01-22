package com.nguyen.paul.thanh.walletmovie.utilities;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.TextView;

import com.nguyen.paul.thanh.walletmovie.R;

/**
 * Utility class provide a range of helper classes
 */

public class Utils {

    public static Snackbar createSnackBar(Resources resources, View view, String message) {
        int snackBarMessageColor = ResourcesCompat.getColor(resources, R.color.colorPrimaryText, null);
        int snackBarActionBtnColor = ResourcesCompat.getColor(resources, R.color.colorAccent, null);

        Snackbar snackbar = createSnackBar(resources, view, message, snackBarMessageColor, snackBarActionBtnColor);
        return snackbar;
    }

    public static Snackbar createSnackBar(Resources resources, View view, String message, int messageColor, int actionBtnColor) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        //set snackbar message color
        snackbar.setActionTextColor(messageColor);

        //set snackbar action button color
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(actionBtnColor);
        textView.setTypeface(null, Typeface.BOLD);
        return snackbar;
    }

}
