package com.nguyen.paul.thanh.walletmovie.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by THANH on 17/02/2017.
 */

public class User implements Parcelable {

    private String uid;
    private String displayName;
    private String email;
    private Uri profileUrl;

    public User() {}

    public User(String uid, String displayName, String email, Uri profileUrl) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
        this.profileUrl = profileUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Uri getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(Uri profileUrl) {
        this.profileUrl = profileUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    protected User(Parcel in) {
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
