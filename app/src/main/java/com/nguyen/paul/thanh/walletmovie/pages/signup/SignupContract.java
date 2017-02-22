package com.nguyen.paul.thanh.walletmovie.pages.signup;

import com.nguyen.paul.thanh.walletmovie.BasePresenter;
import com.nguyen.paul.thanh.walletmovie.BaseView;

/**
 * Contracts for signup page View and Presenter
 */

public interface SignUpContract {

    interface View extends BaseView {
        void showSnackBarWithResult(String message);
        void showDialogResult(String message);
        void redirect(Class<?> redirectTo);
    }

    interface Presenter extends BasePresenter {
        void registerUser(String firstName, String lastName, String email, String password);
    }
}
