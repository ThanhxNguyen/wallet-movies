package com.nguyen.paul.thanh.walletmovie.pages.account;

import com.nguyen.paul.thanh.walletmovie.BasePresenter;
import com.nguyen.paul.thanh.walletmovie.BaseView;

/**
 * Contracts for account page View and Presenter
 */

public interface AccountContract {

    interface View extends BaseView {
        void showDialogResult(String message);
        void showSnackBarWithResult(String message);
        void setDisplayNameText(String displayName);
    }

    interface Presenter extends BasePresenter {
        void resetPassword();
        void updateEmail(String newEmail, String password);
        void updatePassword(String oldPass, String newPass);
        void updateDisplayName(String currentName, String newName);
    }
}
