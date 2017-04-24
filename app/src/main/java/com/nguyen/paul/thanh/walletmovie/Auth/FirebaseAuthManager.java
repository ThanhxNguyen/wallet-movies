package com.nguyen.paul.thanh.walletmovie.Auth;

import android.support.annotation.NonNull;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nguyen.paul.thanh.walletmovie.model.User;


public class FirebaseAuthManager implements AuthManager {

    private AuthenticateListener mListener;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    public interface AuthenticateListener {

        void onSignIn();
        void onSignOut();
    }

    public FirebaseAuthManager() {}

    public FirebaseAuthManager(AuthenticateListener listener) {
        mListener = listener;
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null) {
                    //user is signed in
                    if(mListener != null) mListener.onSignIn();
                } else {
                    if(mListener != null) mListener.onSignOut();
                }
            }
        };
    }

    @Override
    public AuthManager getAuth() {
        return this;
    }

    @Override
    public void signOut() {
        //check if the user is currently signed in with Facebook
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken != null && !accessToken.isExpired()) {
            //signout Facebook
            LoginManager.getInstance().logOut();
        }
        mAuth.signOut();
    }

    @Override
    public boolean isAuthenticated() {
        return mAuth.getCurrentUser() == null;
    }

    @Override
    public User getCurrentUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null) {
            return null;
        } else {
            return new User(user.getUid(), user.getDisplayName(), user.getEmail(), user.getPhotoUrl());
        }

    }

    @Override
    public void on() {
        if(mAuthStateListener != null) mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void off() {
        if(mAuthStateListener != null) mAuth.removeAuthStateListener(mAuthStateListener);
    }


}
