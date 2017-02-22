package com.nguyen.paul.thanh.walletmovie.pages.signin;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.nguyen.paul.thanh.walletmovie.App;

import static com.nguyen.paul.thanh.walletmovie.App.FIRST_TIME_USER_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GLOBAL_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GUEST_MODE_PREF_KEY;

/**
 * Presenter for sign in page
 */

public class SignInPresenter implements SignInContract.Presenter {

    private static final String SIGN_IN_SUCCESS_MESSAGE = "Sign in successfully!";
    private static final String AUTH_ERROR_MESSAGE = "Authentication failed! Incorrect email or password!";
    private static final String GOOGLE_SIGNIN_FAIL_ERROR_MESSAGE = "Failed to sign in with Google account!";
    private static final String FACEBOOK_SIGNIN_FAIL_ERROR_MESSAGE = "Failed to sign in with Facebook account!";
    private FirebaseAuth mAuth;
    private SignInContract.View mView;
    private SharedPreferences mPrefs;

    public SignInPresenter(SignInContract.View view) {
        mView = view;
        mAuth = FirebaseAuth.getInstance();
        mPrefs = App.getAppContext().getSharedPreferences(GLOBAL_PREF_KEY, Context.MODE_PRIVATE);
    }

    @Override
    public void resetPassword(final String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            mView.showDialogResult("A reset password link has been sent to " + email);
                        } else {
                            mView.showDialogResult("Error! Could not send a reset password email. Please try again later!");
                        }
                    }
                });

    }

    @Override
    public void signInUser(String emailInput, String passwordInput) {
        //sign in user with email and password
        mAuth.signInWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {

                            disableGuestMode();
                            //successfully signed in, redirect to MainActivity for now
                            mView.showSnackBarWithResult(SIGN_IN_SUCCESS_MESSAGE);
                            mView.goBack();

                        } else {
                            mView.setAuthErrorMessage(AUTH_ERROR_MESSAGE);
                        }
                    }
                });
    }

    @Override
    public void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        //Firebase sign in with Google email and password
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //successfully signed in with Google account
                        if(task.isSuccessful()) {
                            //disable guest mode
                            disableGuestMode();
                            //redirect back
                            mView.goBack();
                        } else {
                            //failed to signed in with Google account
                            mView.showSnackBarWithResult(GOOGLE_SIGNIN_FAIL_ERROR_MESSAGE);
                        }

                    }
                });
    }

    @Override
    public void firebaseAuthWithFacebookToken(AccessToken token) {
        
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a snackbar with failure message to the user. If sign in succeeds
                        // redirect the user back to MainActivity
                        if(task.isSuccessful()) {
                            //disable guest mode
                            disableGuestMode();
                            
                            //redirect back
                            mView.goBack();
                        } else {
                            //failed to signed in with Facebook account
                            mView.showSnackBarWithResult(FACEBOOK_SIGNIN_FAIL_ERROR_MESSAGE);
                        }
                    }
                });
    }

    private void disableGuestMode() {
        //since user signed in, disable guest mode
        boolean isFirstTimeUser = mPrefs.getBoolean(FIRST_TIME_USER_PREF_KEY, true);

        if(isFirstTimeUser) {
            mPrefs.edit().putBoolean(FIRST_TIME_USER_PREF_KEY, false).apply();
        }

        //since user is signed in, disable guest mode if it's enabled
        boolean isGuest = mPrefs.getBoolean(GUEST_MODE_PREF_KEY, true);

        if(isGuest) {
            mPrefs.edit().putBoolean(GUEST_MODE_PREF_KEY, false).apply();
        }
    }
}
