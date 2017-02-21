package com.nguyen.paul.thanh.walletmovie.Auth;

import com.nguyen.paul.thanh.walletmovie.model.User;

public interface AuthManager {
    AuthManager getAuth();
    void signOut();
    boolean isAuthenticated();
    User getCurrentUser();
    void on();
    void off();
}
