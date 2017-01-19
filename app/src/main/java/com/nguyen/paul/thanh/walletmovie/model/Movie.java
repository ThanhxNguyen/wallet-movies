package com.nguyen.paul.thanh.walletmovie.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Plain Old Java Object (POJO) holds information related a movie
 */

public class Movie implements Parcelable {

    private int id;
    private String title;
    private String overview;
    private String releaseDate;
    private int runtime;
    private String country;
    private String status;
    private double voteAverage;
    private String posterPath;
    private List<Genre> genres;

    public Movie() {
        genres = new ArrayList<>();
    }

    public Movie(int id, String title, String overview, String releaseDate,
                 int runtime, String country, String status, double voteAverage, String posterPath) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.runtime = runtime;
        this.country = country;
        this.status = status;
        this.voteAverage = voteAverage;
        this.posterPath = posterPath;
        genres = new ArrayList<>();
    }

    public Movie(int id, String title, String overview, String releaseDate,
                 int runtime, String country, String status, double voteAverage, String posterPath, List<Genre> genres) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.runtime = runtime;
        this.country = country;
        this.status = status;
        this.voteAverage = voteAverage;
        this.posterPath = posterPath;
        this.genres = genres;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", overview='" + overview + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", runtime=" + runtime +
                ", country='" + country + '\'' +
                ", status='" + status + '\'' +
                ", voteAverage=" + voteAverage +
                ", posterPath='" + posterPath + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.title);
        dest.writeString(this.overview);
        dest.writeString(this.releaseDate);
        dest.writeInt(this.runtime);
        dest.writeString(this.country);
        dest.writeString(this.status);
        dest.writeDouble(this.voteAverage);
        dest.writeString(this.posterPath);
        dest.writeList(this.genres);
    }

    protected Movie(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.overview = in.readString();
        this.releaseDate = in.readString();
        this.runtime = in.readInt();
        this.country = in.readString();
        this.status = in.readString();
        this.voteAverage = in.readDouble();
        this.posterPath = in.readString();
        this.genres = new ArrayList<Genre>();
        in.readList(this.genres, Genre.class.getClassLoader());
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    //use for sorting arraylist of movies by movie name
    public static Comparator<Movie> MovieNameSort = new Comparator<Movie>() {
        @Override
        public int compare(Movie movie1, Movie movie2) {
            //get movie names
            String movieName1 = movie1.getTitle().toUpperCase();
            String movieName2 = movie2.getTitle().toUpperCase();

            //sort in ascending order
            return movieName1.compareTo(movieName2);
        }
    };

    //use for sorting arraylist of movies by movie vote average
    public static Comparator<Movie> MovieVoteSort = new Comparator<Movie>() {
        @Override
        public int compare(Movie movie1, Movie movie2) {
            Double movie1Vote = movie1.getVoteAverage();
            Double movie2Vote = movie2.getVoteAverage();

            //sort in descending order
            return movie2Vote.compareTo(movie1Vote);
        }
    };

    //use for sorting arraylist of movies by movie release date
    public static Comparator<Movie> MovieReleaseDateSort = new Comparator<Movie>() {
        @Override
        public int compare(Movie movie1, Movie movie2) {
            String movie1ReleaseDate = movie1.getReleaseDate();
            String movie2ReleaseDate = movie2.getReleaseDate();

            //sort in descending order
            return movie2ReleaseDate.compareTo(movie1ReleaseDate);
        }
    };
}
