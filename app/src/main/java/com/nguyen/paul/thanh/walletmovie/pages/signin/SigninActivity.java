package com.nguyen.paul.thanh.walletmovie.pages.signin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.nguyen.paul.thanh.walletmovie.R;
import com.nguyen.paul.thanh.walletmovie.pages.signup.SignupActivity;
import com.nguyen.paul.thanh.walletmovie.fragments.ResetPasswordDialogFragment;
import com.nguyen.paul.thanh.walletmovie.utilities.FormInputValidator;
import com.nguyen.paul.thanh.walletmovie.utilities.Utils;

import static com.nguyen.paul.thanh.walletmovie.App.FIRST_TIME_USER_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GLOBAL_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GUEST_MODE_PREF_KEY;

public class SigninActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        ResetPasswordDialogFragment.ResetPasswordAcquireListener {

    private static final String TAG = "SigninActivity";

    private static final int RC_SIGN_IN = 100;
    public static final String RESET_PASSWORD_DIALOG_TAG = "reset_password_dialog_tag";

    private TextView mEmailTv;
    private TextView mPasswordTv;
    private Button mSigninBtn;
    private Button mSignupBtn;
    private SignInButton mGoogleSigninButton;
    private LoginButton mFacebookSigninButton;
    private CallbackManager mCallbackManager;
    private TextInputLayout mEmailWrapper;
    private TextInputLayout mPasswordWrapper;
    private TextView mAuthErrorMessage;
    private FormInputValidator mFormValidator;
    private ProgressDialog mProgressDialog;
    private GoogleApiClient mGoogleApiClient;
    private Button mResetPasswordBtn;

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
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.setCancelable(false);

        mAuthErrorMessage = (TextView) findViewById(R.id.auth_error_message);

        mEmailTv = (TextView) findViewById(R.id.email);
        mPasswordTv = (TextView) findViewById(R.id.password);
        mSigninBtn = (Button) findViewById(R.id.signin_btn);
        mSignupBtn = (Button) findViewById(R.id.signup_btn);
        mResetPasswordBtn = (Button) findViewById(R.id.reset_password_btn);
        mGoogleSigninButton = (SignInButton) findViewById(R.id.google_signin_btn);
        //change default text for google signin button
        TextView googleSigninBtnTv = (TextView) mGoogleSigninButton.getChildAt(0);
        googleSigninBtnTv.setText(getString(R.string.signin_with_google));

        //Facebook authentication
        mCallbackManager = CallbackManager.Factory.create();
        mFacebookSigninButton = (LoginButton) findViewById(R.id.facebook_signin_btn);

        mEmailWrapper = (TextInputLayout) findViewById(R.id.email_wrapper);
        mPasswordWrapper = (TextInputLayout) findViewById(R.id.password_wrapper);

        //set text change listeners for email and password inputs
        setTextChangeListenerForEmailInput();
        setTextChangeListenerForPasswordInput();

        //set click listener for signin and signup buttons
        setClickListenerForSigninBtn();
        setClicklistenerforSignupBtn();
        setClickListenerResetPasswordBtn();

        setUpSigninWithGoogle();
        setUpSigninWithFacebook();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mProgressDialog.dismiss();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mProgressDialog.dismiss();
    }

    private void setUpSigninWithFacebook() {
        mFacebookSigninButton.setReadPermissions("email", "public_profile");
        //register callback, these callbacks will be invoke when facebook authentication start
        mFacebookSigninButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                signinFirebaseWithFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });
    }

    private void signinFirebaseWithFacebookToken(AccessToken token) {
        //show progress dialog
        mProgressDialog.show();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a snackbar with failure message to the user. If sign in succeeds
                        // redirect the user back to MainActivity
                        if(task.isSuccessful()) {
                            //disable guest mode
                            disableGuestMode();

                            //successfully signed in with Google account
                            mProgressDialog.dismiss();
                            //redirect back
                            finish();
                        } else {
                            //failed to signed in with Google account
                            mProgressDialog.dismiss();
                            showSnackBar("Authentication failed.");
                        }
                    }
                });
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
        } else {
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //show progress dialog
        mProgressDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        //Firebase sign in with Google email and password
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //successfully signed in with Google account
                        if(task.isSuccessful()) {
                            //disable guest mode
                            disableGuestMode();
                            mProgressDialog.dismiss();
                            //redirect back
                            finish();
                        } else {
                            //failed to signed in with Google account
                            mProgressDialog.dismiss();
                            showSnackBar("Authentication failed.");
                        }

                    }
                });
    }

    /*
     * Helper method to display a snackbar
     */
    private void showSnackBar(String message) {
        Utils.createSnackBar(getResources(), findViewById(R.id.signin_form_activity), message).show();
    }


    private void setTextChangeListenerForEmailInput() {
        mEmailTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                //validating when user typing new input values
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
                //validating when user typing new input values
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
                //hide soft keyboard
                Utils.hideKeyboard(SigninActivity.this, mSigninBtn);
                String emailInput = mEmailTv.getText().toString().trim();
                final String passwordInput = mPasswordTv.getText().toString().trim();

                //validate form inputs
                boolean validEmail = validateEmailInput(emailInput);
                boolean validPassword = validatePasswordInput(passwordInput);

                if(validEmail && validPassword) {
                    //show progress dialog
                    mProgressDialog.show();

                    //sign in user with email and password
                    mAuth.signInWithEmailAndPassword(emailInput, passwordInput)
                            .addOnCompleteListener(SigninActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {

                                        disableGuestMode();

                                        //dimiss the progress dialog
                                        mProgressDialog.dismiss();
                                        //successfully signed in, redirect to MainActivity for now
                                        showSnackBar("Sign in successfully!");
                                        finish();

                                    } else {
                                        //dismiss progress dialog and display errors
                                        mProgressDialog.dismiss();
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
            //email invalid
            mEmailWrapper.setError(getString(R.string.error_email_invalid));
            return false;

        } else {
            //goog to go
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

    private void setClickListenerResetPasswordBtn() {
        mResetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openResetPasswordDialog();
            }
        });

    }

    private void openResetPasswordDialog() {
        ResetPasswordDialogFragment dialog = new ResetPasswordDialogFragment();
        dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
        dialog.setResetPasswordAcquireListener(this);
//        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), RESET_PASSWORD_DIALOG_TAG);
    }

    private void sendEmailResetPassword(final String email) {
        mProgressDialog.show();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mProgressDialog.dismiss();
                        if(task.isSuccessful()) {
                            createAlertDialog("A reset password link has been sent to " + email).show();
                        } else {
                            createAlertDialog("Error! Could not send a reset password email. Please try again later!").show();
                        }
                    }
                });

    }

    private AlertDialog createAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alert");
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        return builder.create();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //called when google sign in fails
    }

    @Override
    public void onResetPasswordAcquire(final String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        builder.setMessage("Are you sure you want to reset your password?");
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                mProgressDialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sendEmailResetPassword(email);
            }
        });

        //create alert dialog
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void disableGuestMode() {
        //since user signed in, disable guest mode
        SharedPreferences prefs = getSharedPreferences(GLOBAL_PREF_KEY, Context.MODE_PRIVATE);

        boolean isFirstTimeUser = prefs.getBoolean(FIRST_TIME_USER_PREF_KEY, true);

        if(isFirstTimeUser) {
            prefs.edit().putBoolean(FIRST_TIME_USER_PREF_KEY, false).apply();
        }

        //since user is signed in, disable guest mode if it's enabled
        boolean isGuest = prefs.getBoolean(GUEST_MODE_PREF_KEY, true);

        if(isGuest) {
            prefs.edit().putBoolean(GUEST_MODE_PREF_KEY, false).apply();
        }
    }
}
