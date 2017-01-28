package com.nguyen.paul.thanh.walletmovie.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.utilities.FormInputValidator;
import com.nguyen.paul.thanh.walletmovie.utilities.Utils;

import static com.nguyen.paul.thanh.walletmovie.App.FIRST_TIME_USER_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GLOBAL_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GUEST_MODE_PREF_KEY;

public class SignupActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;

    private TextView mFirstNameTv;
    private TextView mLastNameTv;
    private TextView mEmailTv;
    private TextView mPasswordTv;
    private TextView mConfirmPasswordTv;
    private Button mSignupBtn;

    private TextInputLayout mFirstNameWrapper;
    private TextInputLayout mLastNameWrapper;
    private TextInputLayout mEmailWrapper;
    private TextInputLayout mPasswordWrapper;
    private TextInputLayout mConfirmPasswordWrapper;


    private FormInputValidator mFormValidator;

    //firebase auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //initialize Firebase auth
        mAuth = FirebaseAuth.getInstance();
        mFormValidator = FormInputValidator.getInstance();

        mFirstNameTv = (TextView) findViewById(R.id.first_name);
        mLastNameTv = (TextView) findViewById(R.id.last_name);
        mEmailTv = (TextView) findViewById(R.id.email);
        mPasswordTv = (TextView) findViewById(R.id.password);
        mConfirmPasswordTv = (TextView) findViewById(R.id.confirm_password);
        mSignupBtn = (Button) findViewById(R.id.signup_btn);

        mFirstNameWrapper = (TextInputLayout) findViewById(R.id.first_name_wrapper);
        mLastNameWrapper = (TextInputLayout) findViewById(R.id.last_name_wrapper);
        mEmailWrapper = (TextInputLayout) findViewById(R.id.email_wrapper);
        mPasswordWrapper = (TextInputLayout) findViewById(R.id.password_wrapper);
        mConfirmPasswordWrapper = (TextInputLayout) findViewById(R.id.confirm_password_wrapper);

        setListenerForSignupBtn();

        //set text change listener for TextView
        setTextChangeListenerForFirstNameTv();
        setTextChangeListenerForLastNameTv();
        setTextChangeListenerForEmailTv();
        setTextChangeListenerForPasswordTv();
    }

    private void setTextChangeListenerForFirstNameTv() {
        mFirstNameTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                validateFirstNameInput(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void setTextChangeListenerForLastNameTv() {
        mLastNameTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                validateLastNameInput(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

    }

    private void setTextChangeListenerForEmailTv() {
        mEmailTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                validateEmailInput(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

    }

    private void setTextChangeListenerForPasswordTv() {
        mPasswordTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                validatePasswordInput(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

    }

    private void setListenerForSignupBtn() {

        mProgressDialog = new ProgressDialog(SignupActivity.this, ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("User Registration");
        mProgressDialog.setMessage("Registering user...");


        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String firstName = mFirstNameTv.getText().toString().trim();
                final String lastName = mLastNameTv.getText().toString().trim();
                final String email = mEmailTv.getText().toString().trim();
                final String password = mPasswordTv.getText().toString().trim();
                final String confirmPassword = mConfirmPasswordTv.getText().toString().trim();

                boolean validFirstName = validateFirstNameInput(firstName);
                boolean validLastName = validateLastNameInput(lastName);
                boolean validEmail = validateEmailInput(email);
                boolean validPassword = validatePasswordInput(password);
                boolean passwordMatch = ConfirmPassword(password, confirmPassword);

                if(validFirstName && validLastName && validEmail && validPassword && passwordMatch) {
                    //show progress dialog
                    mProgressDialog.show();

                    //sign up user with email and password
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        //since user signed in, disable guest mode
                                        SharedPreferences prefs = getSharedPreferences(GLOBAL_PREF_KEY, Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefs.edit();

                                        boolean isFirstTimeUser = prefs.getBoolean(FIRST_TIME_USER_PREF_KEY, true);
                                        boolean isGuest = prefs.getBoolean(GUEST_MODE_PREF_KEY, true);

                                        if(isFirstTimeUser) {
                                            editor.putBoolean(FIRST_TIME_USER_PREF_KEY, false);
                                            editor.apply();
                                        }
                                        if(isGuest) {
                                            editor.putBoolean(GUEST_MODE_PREF_KEY, false);
                                            editor.apply();
                                        }

                                        //update profile info
                                        setUserDisplayNameAfterSignup(firstName, lastName);
                                    } else {
                                        //errors occur while registering new user
                                        AlertDialog alertDialog = createAlertDialogForRegistrationFail();
                                        //show alertDialog
                                        alertDialog.show();
                                    }
                                }
                            });

                }

            }
        });
    }

    private AlertDialog createAlertDialogForRegistrationFail() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SignupActivity.this);

        dialogBuilder.setTitle(R.string.dialog_title_registration_fail)
                .setMessage(R.string.dialog_message_registration_fail);

        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //create alert dialog and return it
        return dialogBuilder.create();
    }

    private boolean validateFirstNameInput(String firstName) {
        if(mFormValidator.isEmpty(firstName)) {
            //input empty
            mFirstNameWrapper.setError(getString(R.string.error_field_required));
            return false;

        } else if(!mFormValidator.isValidName(firstName)) {
            //not a valid name
            mFirstNameWrapper.setError(getString(R.string.error_name_invalid));
            return false;

        } else {
            mFirstNameWrapper.setError(null);
            return true;
        }
    }

    private boolean validateLastNameInput(String lastName) {
        if(mFormValidator.isEmpty(lastName)) {
            //input empty
            mLastNameWrapper.setError(getString(R.string.error_field_required));
            return false;

        } else if(!mFormValidator.isValidName(lastName)) {
            //not a valid name
            mLastNameWrapper.setError(getString(R.string.error_name_invalid));
            return false;

        } else {
            mLastNameWrapper.setError(null);
            return true;
        }
    }

    private boolean validateEmailInput(String emailInput) {
        if(mFormValidator.isEmpty(emailInput)) {
            //email field is empty
            mEmailWrapper.setError(getString(R.string.error_field_required));
            return false;

        } else if(!mFormValidator.isValidEmail(emailInput)) {
            mEmailWrapper.setError(getString(R.string.error_email_invalid));
            return false;

        } else {
            mEmailWrapper.setError(null);
            return true;
        }
    }

    private boolean ConfirmPassword(String password, String confirmPassword) {
        if(mFormValidator.isEmpty(confirmPassword)) {
            mConfirmPasswordWrapper.setError(getString(R.string.error_field_required));
            return false;
        } else if(!confirmPassword.equals(password)) {
            //passwords are not matching
            mConfirmPasswordWrapper.setError(getString(R.string.error_password_not_match));
            return false;
        } else {
            mConfirmPasswordWrapper.setError(null);
            return true;
        }
    }

    private boolean validatePasswordInput(String passwordInput) {
        if(mFormValidator.isEmpty(passwordInput)) {
            mPasswordWrapper.setError(getString(R.string.error_field_required));
            return false;
        } else {
            int passwordValidationResult = mFormValidator.isValidPassword(passwordInput);
            switch (passwordValidationResult) {

                case FormInputValidator.MIN_LENGTH_ERROR:
                    mPasswordWrapper.setError(getString(R.string.error_password_too_short));
                    return false;

                case FormInputValidator.MAX_LENGTH_ERROR:
                    mPasswordWrapper.setError(getString(R.string.error_password_too_long));
                    return false;

                case FormInputValidator.PASSWORD_OK:
                    mPasswordWrapper.setError(null);
                    return true;

                default:
                    mPasswordWrapper.setError(getString(R.string.error_password_fail_validate));
                    return false;
            }
        }
    }

    private void setUserDisplayNameAfterSignup(String firstName, String lastName) {
        FirebaseUser user = mAuth.getCurrentUser();

        //convert the first letter to uppercase
        String fname = firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase();
        String lname = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase();

        //make sure user is currently signed in
        if(user != null) {
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(fname + " " + lname)
                                                    .build();

            //start updating user profile
            user.updateProfile(profile)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                mProgressDialog.dismiss();
                                Utils.createSnackBar(getResources(), findViewById(R.id.signup_form_activity), "Sign up Successfully").show();
                                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                startActivity(intent);
                            }    
                        }
                    });
        }
    }
}
