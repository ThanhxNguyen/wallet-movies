package com.nguyen.paul.thanh.walletmovie.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * POJO for movie casts
 */

public class Cast implements Parcelable {
    private String name;
    private String biography;
    private String birthday;
    private String placeOfBirth;
    private String profilePath;
    private String character;

    public Cast() {
    }

    public Cast(String name, String biography, String birthday, String placeOfBirth, String profilePath) {
        this.name = name;
        this.biography = biography;
        this.birthday = birthday;
        this.placeOfBirth = placeOfBirth;
        this.profilePath = profilePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    @Override
    public String toString() {
        return "Cast{" +
                "name='" + name + '\'' +
                ", biography='" + biography + '\'' +
                ", birthday='" + birthday + '\'' +
                ", placeOfBirth='" + placeOfBirth + '\'' +
                ", profilePath='" + profilePath + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.biography);
        dest.writeString(this.birthday);
        dest.writeString(this.placeOfBirth);
        dest.writeString(this.profilePath);
    }

    protected Cast(Parcel in) {
        this.name = in.readString();
        this.biography = in.readString();
        this.birthday = in.readString();
        this.placeOfBirth = in.readString();
        this.profilePath = in.readString();
    }

    public static final Parcelable.Creator<Cast> CREATOR = new Parcelable.Creator<Cast>() {
        @Override
        public Cast createFromParcel(Parcel source) {
            return new Cast(source);
        }

        @Override
        public Cast[] newArray(int size) {
            return new Cast[size];
        }
    };
}
