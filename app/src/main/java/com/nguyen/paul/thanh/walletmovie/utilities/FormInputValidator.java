package com.nguyen.paul.thanh.walletmovie.utilities;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Pattern;

/**
 * Helper class for form inputs validation
 */

public class FormInputValidator {
    public static final int PASSWORD_OK = 0;
    public static final int MIN_LENGTH_ERROR = 1;
    public static final int MAX_LENGTH_ERROR = 2;

    private String peopleNamePattern = "^[a-zA-Z-'\\. ]{3,}$";
    private Pattern PEOPLE_NAME = Pattern.compile(peopleNamePattern);

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
        return Patterns.EMAIL_ADDRESS.matcher(input).matches();

    }

    public boolean isEmpty(String input) {
        return TextUtils.isEmpty(input);
    }

    public int isValidPassword(String input) {
        int PASSWORD_MIN_LENGTH = 6;
        int PASSWORD_MAX_LENGTH = 100;
        if(input.length() < PASSWORD_MIN_LENGTH) {
            return MIN_LENGTH_ERROR;
        } else if(input.length() > PASSWORD_MAX_LENGTH) {
            return MAX_LENGTH_ERROR;
        }

        return PASSWORD_OK;
    }

    public boolean isValidName(String input) {
        return PEOPLE_NAME.matcher(input).matches();

    }
}
