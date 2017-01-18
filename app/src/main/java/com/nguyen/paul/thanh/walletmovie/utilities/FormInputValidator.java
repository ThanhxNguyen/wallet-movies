package com.nguyen.paul.thanh.walletmovie.utilities;

import android.text.TextUtils;
import android.util.Patterns;

/**
 * Helper class for form inputs validation
 */

public class FormInputValidator {
    public static final int PASSWORD_OK = 0;
    public static final int MIN_LENGTH_ERROR = 1;
    public static final int MAX_LENGTH_ERROR = 2;

    private final int PASSWORD_MIN_LENGTH = 6;
    private final int PASSWORD_MAX_LENGTH = 100;

    private static FormInputValidator mInstance;

    private FormInputValidator() {
        //required for singleton pattern
    }

    public static FormInputValidator getInstance() {
        if(mInstance == null) {
            mInstance = new FormInputValidator();
        }

        return mInstance;
    }

    public boolean isValidEmail(String input) {
        if(!Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            //not a valid email address
            return false;
        }

        return true;
    }

    public boolean isEmpty(String input) {
        return TextUtils.isEmpty(input);
    }

    public int isValidPassword(String input) {
        if(input.length() < PASSWORD_MIN_LENGTH) {
            return MIN_LENGTH_ERROR;
        } else if(input.length() > PASSWORD_MAX_LENGTH) {
            return MAX_LENGTH_ERROR;
        }

        return PASSWORD_OK;
    }
}
