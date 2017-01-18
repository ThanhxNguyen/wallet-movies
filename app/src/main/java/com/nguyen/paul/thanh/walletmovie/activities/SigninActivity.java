package com.nguyen.paul.thanh.walletmovie.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.interfaces.PreferenceConst;
import com.nguyen.paul.thanh.walletmovie.utilities.FormInputValidator;

public class SigninActivity extends AppCompatActivity {

    private static final String TAG = "SigninActivity";

    private TextView mEmailTv;
    private TextView mPasswordTv;
    private Button mSigninBtn;
    private Button mSignupBtn;
    private TextInputLayout mEmailWrapper;
    private TextInputLayout mPasswordWrapper;
    private TextView mAuthErrorMessage;
    private ProgressBar mProgressBar;
    private ConstraintLayout mSigninForm;

    private FormInputValidator mFormValidator;

    //Firebase auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        //initialize Firebase auth
        mAuth = FirebaseAuth.getInstance();
        mFormValidator = FormInputValidator.getInstance();

        mAuthErrorMessage = (TextView) findViewById(R.id.auth_error_message);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mSigninForm = (ConstraintLayout) findViewById(R.id.signin_form);

        mEmailTv = (TextView) findViewById(R.id.email);
        mPasswordTv = (TextView) findViewById(R.id.password);
        mSigninBtn = (Button) findViewById(R.id.signin_btn);
        mSignupBtn = (Button) findViewById(R.id.signup_btn);

        mEmailWrapper = (TextInputLayout) findViewById(R.id.email_wrapper);
        mPasswordWrapper = (TextInputLayout) findViewById(R.id.password_wrapper);

        //set text change listeners for email and password inputs
        setTextChangeListenerForEmailInput();
        setTextChangeListenerForPasswordInput();

        //set click listener for signin and signup buttons
        setClickListenerForSigninBtn();
        setClicklistenerforSignupBtn();

    }

    private void setTextChangeListenerForEmailInput() {
        mEmailTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                mAuthErrorMessage.setVisibility(View.GONE);
                validateEmailInput(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void setTextChangeListenerForPasswordInput() {
        mPasswordTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                mAuthErrorMessage.setVisibility(View.GONE);
                validatePasswordInput(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void setClickListenerForSigninBtn() {

        mSigninBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailInput = mEmailTv.getText().toString().trim();
                String passwordInput = mPasswordTv.getText().toString().trim();

                //validate form inputs
                boolean validEmail = validateEmailInput(emailInput);
                boolean validPassword = validatePasswordInput(passwordInput);

                if(validEmail && validPassword) {
                    //hide signin form and show progress bar and progress authentication
                    mSigninForm.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    //sign in user with email and password
                    mAuth.signInWithEmailAndPassword(emailInput, passwordInput)
                            .addOnCompleteListener(SigninActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {

                                        //since user signed in, disable guest mode
                                        SharedPreferences prefs = getSharedPreferences(PreferenceConst.GLOBAL_PREF_KEY, Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefs.edit();

                                        boolean isFirstTimeUser = prefs.getBoolean(PreferenceConst.Auth.FIRST_TIME_USER_PREF_KEY, true);
                                        boolean isGuest = prefs.getBoolean(PreferenceConst.Auth.GUEST_MODE_PREF_KEY, true);

                                        if(isFirstTimeUser) {
                                            editor.putBoolean(PreferenceConst.Auth.FIRST_TIME_USER_PREF_KEY, false);
                                            editor.apply();
                                        }
                                        if(isGuest) {
                                            editor.putBoolean(PreferenceConst.Auth.GUEST_MODE_PREF_KEY, false);
                                            editor.apply();
                                        }

                                        //successfully signed in, redirect to MainActivity for now
                                        Toast.makeText(SigninActivity.this, "Sign in successfully!", Toast.LENGTH_SHORT).show();
//                                        Intent intent = new Intent(SigninActivity.this, MainActivity.class);
//                                        startActivity(intent);
                                        finish();

                                    } else {
                                        mProgressBar.setVisibility(View.GONE);
                                        mSigninForm.setVisibility(View.VISIBLE);

                                        mAuthErrorMessage.setVisibility(View.VISIBLE);
                                        mAuthErrorMessage.setText(getString(R.string.error_auth_fail));
                                    }
                                }
                            });

                }
            }
        });

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

    private void setClicklistenerforSignupBtn() {
        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //redirect to signup activity
                Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}
