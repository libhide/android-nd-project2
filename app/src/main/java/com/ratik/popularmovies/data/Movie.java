package com.ratik.popularmovies.data;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ratik on 31/01/16.
 */
public class Movie implements Parcelable {
    private static final String TAG = Movie.class.getSimpleName();

    private String title;
    private String releaseDate;
    private String poster;
    private String voteAverage;
    private String plot;

    public Movie() {
        // Empty Constructor
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @SuppressLint("SimpleDateFormat")
    public String getReleaseDate() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = fmt.parse(releaseDate);
        } catch (ParseException e) {
            Log.e(TAG, "Exception caught: ", e);
        }
        SimpleDateFormat fmtOut = new SimpleDateFormat("dd MMM yyyy");
        return fmtOut.format(date);
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPosterUrl() {
        String basePath = "http://image.tmdb.org/t/p";
        String posterWidth = "/w342";

        return basePath + posterWidth + poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }
    public String getPoster() {
        return poster;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(String voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(releaseDate);
        dest.writeString(poster);
        dest.writeString(voteAverage);
        dest.writeString(plot);
    }

    private Movie(Parcel in) {
        title = in.readString();
        releaseDate = in.readString();
        poster = in.readString();
        voteAverage = in.readString();
        plot = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
