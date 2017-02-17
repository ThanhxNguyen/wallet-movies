package com.nguyen.paul.thanh.walletmovie.Auth;

import com.nguyen.paul.thanh.walletmovie.model.User;

/**
 * Created by THANH on 17/02/2017.
 */

public interface AuthManager {
    AuthManager getAuth();
    void signOut();
    boolean isAuthenticated();
    User getCurrentUser();
    void on();
    void off();
}
