package com.nguyen.paul.thanh.walletmovie.pages.signin;

import com.facebook.AccessToken;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.nguyen.paul.thanh.walletmovie.BasePresenter;
import com.nguyen.paul.thanh.walletmovie.BaseView;

/**
 * Contracts for sign in page View and Presenter, it provides ways for communication
 * between view and presenter
 */

public interface SignInContract {

    interface View extends BaseView {
        void showSnackBarWithResult(String message);
        void showDialogResult(String message);
        void redirect(Class<?> redirectTo);
        void goBack();
        void setAuthErrorMessage(String message);
    }

    interface Presenter extends BasePresenter {
        void signInUser(String email, String password);
        void firebaseAuthWithGoogle(GoogleSignInAccount acct);
        void firebaseAuthWithFacebookToken(AccessToken token);
        void resetPassword(String email);
    }
}
