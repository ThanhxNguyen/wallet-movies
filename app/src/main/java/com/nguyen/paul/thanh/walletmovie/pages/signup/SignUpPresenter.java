package com.nguyen.paul.thanh.walletmovie.pages.signup;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.nguyen.paul.thanh.walletmovie.App;
import com.nguyen.paul.thanh.walletmovie.MainActivity;

import static com.nguyen.paul.thanh.walletmovie.App.FIRST_TIME_USER_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GLOBAL_PREF_KEY;
import static com.nguyen.paul.thanh.walletmovie.App.GUEST_MODE_PREF_KEY;

/**
 * Presenter for sign up page
 */

public class SignUpPresenter implements SignUpContract.Presenter {

    private static final String EMAIL_EXIST_ERROR_MESSAGE = "Email has already been registered!";
    private static final String REGISTRATION_FAIL_ERROR_MESSAGE = "Opps! Something went wrong! Failed to register new user.";
    private static final String SUCCESS_REGISTER_MESSAGE = "Sign up Successfully";
    private static final String ERROR_MESSAGE = "Error! Something went wrong.";
    private FirebaseAuth mAuth;
    private SignUpContract.View mView;

    public SignUpPresenter(SignUpContract.View view) {
        mView = view;
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void registerUser(final String firstName, final String lastName, final String email, final String password) {

        //sign up user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            //since user signed in, disable guest mode
                            SharedPreferences prefs = App.getAppContext().getSharedPreferences(GLOBAL_PREF_KEY, Context.MODE_PRIVATE);

                            boolean isFirstTimeUser = prefs.getBoolean(FIRST_TIME_USER_PREF_KEY, true);

                            if(isFirstTimeUser) {
                                prefs.edit().putBoolean(FIRST_TIME_USER_PREF_KEY, false).apply();
                            }

                            //since user is signed in, disable guest mode if it's enabled
                            boolean isGuest = prefs.getBoolean(GUEST_MODE_PREF_KEY, true);

                            if(isGuest) {
                                prefs.edit().putBoolean(GUEST_MODE_PREF_KEY, false).apply();
                            }

                            //update profile info
                            setUserDisplayNameAfterSignup(firstName, lastName, email, password);
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                mView.showDialogResult(EMAIL_EXIST_ERROR_MESSAGE);
                            } catch (Exception e) {
                                mView.showDialogResult(REGISTRATION_FAIL_ERROR_MESSAGE);
                            }
                        }
                    }
                });


    }

    private void setUserDisplayNameAfterSignup(String firstName, String lastName, final String email, final String password) {
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

                                //Firebase currently has a bug that it doesn't show display name when the user signed up for
                                //the first time. A workaround is sign in the user manually after registration.
                                mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if(task.isSuccessful()) {
                                                    mView.showSnackBarWithResult(SUCCESS_REGISTER_MESSAGE);
                                                    //redirect
                                                    mView.redirect(MainActivity.class);

                                                } else {
                                                    mView.showDialogResult(ERROR_MESSAGE);
                                                }
                                            }
                                        });

                            } else {
                                mView.showDialogResult(ERROR_MESSAGE);
                            }
                        }
                    });
        }
    }
}
