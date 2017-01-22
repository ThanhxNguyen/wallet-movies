package com.nguyen.paul.thanh.walletmovie.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.interfaces.PreferenceConst;
import com.nguyen.paul.thanh.walletmovie.utilities.FormInputValidator;
import com.nguyen.paul.thanh.walletmovie.utilities.Utils;

public class SigninActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SigninActivity";
    private static final int RC_SIGN_IN = 100;

    private TextView mEmailTv;
    private TextView mPasswordTv;
    private Button mSigninBtn;
    private Button mSignupBtn;
    private SignInButton mGoogleSigninButton;
    private TextInputLayout mEmailWrapper;
    private TextInputLayout mPasswordWrapper;
    private TextView mAuthErrorMessage;
    private FormInputValidator mFormValidator;
    private ProgressDialog mProgressDialog;
    private GoogleApiClient mGoogleApiClient;

    //Firebase auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        //initialize Firebase auth
        mAuth = FirebaseAuth.getInstance();
        mFormValidator = FormInputValidator.getInstance();

        //initiate ProgressDialog
        mProgressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Authenticating...");

        mAuthErrorMessage = (TextView) findViewById(R.id.auth_error_message);

        mEmailTv = (TextView) findViewById(R.id.email);
        mPasswordTv = (TextView) findViewById(R.id.password);
        mSigninBtn = (Button) findViewById(R.id.signin_btn);
        mSignupBtn = (Button) findViewById(R.id.signup_btn);
        mGoogleSigninButton = (SignInButton) findViewById(R.id.google_signin_btn);
        //change default text for google signin button
        TextView googleSigninBtnTv = (TextView) mGoogleSigninButton.getChildAt(0);
        googleSigninBtnTv.setText(getString(R.string.signin_with_google));
//        mGoogleSigninButton.setSize(SignInButton.SIZE_WIDE);

        mEmailWrapper = (TextInputLayout) findViewById(R.id.email_wrapper);
        mPasswordWrapper = (TextInputLayout) findViewById(R.id.password_wrapper);

        //set text change listeners for email and password inputs
        setTextChangeListenerForEmailInput();
        setTextChangeListenerForPasswordInput();

        //set click listener for signin and signup buttons
        setClickListenerForSigninBtn();
        setClicklistenerforSignupBtn();

        setUpSigninWithGoogle();
    }

    private void setUpSigninWithGoogle() {
        //configure google sign in options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* SigninActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show progress dialog
                mProgressDialog.show();

                //sign in with google
                signinWithGoogle();
            }
        });

    }

    private void signinWithGoogle() {
        Intent googleSignInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(googleSignInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                //hide progress dialog
                mProgressDialog.dismiss();
                // Google Sign In failed
                showSnackBar("Failed to sign in with google");
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            mProgressDialog.dismiss();
                            Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            mProgressDialog.dismiss();
                            showSnackBar("Authentication failed.");
                        }

                    }
                });
    }

    private void showSnackBar(String message) {
        Utils.createSnackBar(getResources(), findViewById(R.id.signin_form_activity), message).show();
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
        final ProgressDialog progressDialog = new ProgressDialog(SigninActivity.this, ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("User Authentication");
        progressDialog.setMessage("Authenticating...");

        mSigninBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailInput = mEmailTv.getText().toString().trim();
                final String passwordInput = mPasswordTv.getText().toString().trim();

                //validate form inputs
                boolean validEmail = validateEmailInput(emailInput);
                boolean validPassword = validatePasswordInput(passwordInput);

                if(validEmail && validPassword) {
                    //show progress dialog
                    progressDialog.show();

                    //sign in user with email and password
                    mAuth.signInWithEmailAndPassword(emailInput, passwordInput)
                            .addOnCompleteListener(SigninActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {

                                        //since user signed in, disable guest mode
                                        SharedPreferences prefs = getSharedPreferences(PreferenceConst.GLOBAL_PREF_KEY, Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefs.edit();

                                        boolean isFirstTimeUser = prefs.getBoolean(PreferenceConst.Authenticate.FIRST_TIME_USER_PREF_KEY, true);
                                        boolean isGuest = prefs.getBoolean(PreferenceConst.Authenticate.GUEST_MODE_PREF_KEY, true);

                                        if(isFirstTimeUser) {
                                            editor.putBoolean(PreferenceConst.Authenticate.FIRST_TIME_USER_PREF_KEY, false);
                                            editor.apply();
                                        }
                                        if(isGuest) {
                                            editor.putBoolean(PreferenceConst.Authenticate.GUEST_MODE_PREF_KEY, false);
                                            editor.apply();
                                        }

                                        //dimiss the progress dialog
                                        progressDialog.dismiss();
                                        //successfully signed in, redirect to MainActivity for now
                                        showSnackBar("Sign in successfully!");
//                                        Intent intent = new Intent(SigninActivity.this, MainActivity.class);
//                                        startActivity(intent);
                                        finish();

                                    } else {
                                        //dismiss progress dialog and display errors
                                        progressDialog.dismiss();
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //called when google sign in fails
    }
}
