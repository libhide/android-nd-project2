package com.ratik.popularmovies.model;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Ratik on 31/01/16.
 */
public class Movie implements Parcelable {
    private static final String TAG = Movie.class.getSimpleName();

    private String id;
    private String title;
    private String releaseDate;
    private String poster;
    private String backdrop;
    private String votesAverage;
    private String plot;
    byte[] posterByteArray;
    byte[] backdropByteArray;
    private ArrayList<String> trailerUrls;
    private ArrayList<MovieReview> movieReviews;

    public Movie() {
        // Empty Constructor
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    @SuppressLint("SimpleDateFormat")
    public String getFormattedReleaseDate() {
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

    public String getBackdropUrl() {
        String basePath = "http://image.tmdb.org/t/p";
        String backdropWidth = "/w500";

        return basePath + backdropWidth + backdrop;
    }

    public String getBackdrop() {
        return backdrop;
    }

    public void setBackdrop(String backdrop) {
        this.backdrop = backdrop;
    }

    public String getVotesAverage() {
        return votesAverage;
    }

    public void setVotesAverage(String votesAverage) {
        this.votesAverage = votesAverage;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public ArrayList<String> getTrailerUrls() {
        return trailerUrls;
    }

    public void setTrailerUrls(ArrayList<String> trailerUrls) {
        this.trailerUrls = trailerUrls;
    }

    public ArrayList<MovieReview> getMovieReviews() {
        return movieReviews;
    }

    public void setMovieReviews(ArrayList<MovieReview> movieReviews) {
        this.movieReviews = movieReviews;
    }

    public byte[] getPosterByteArray() {
        return posterByteArray;
    }

    public void setPosterByteArray(byte[] posterByteArray) {
        this.posterByteArray = posterByteArray;
    }

    public byte[] getBackdropByteArray() {
        return backdropByteArray;
    }

    public void setBackdropByteArray(byte[] backdropByteArray) {
        this.backdropByteArray = backdropByteArray;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(releaseDate);
        dest.writeString(poster);
        dest.writeString(backdrop);
        dest.writeString(votesAverage);
        dest.writeString(plot);
        dest.writeList(trailerUrls);

        dest.writeInt(posterByteArray.length);
        dest.writeByteArray(posterByteArray);

        dest.writeInt(backdropByteArray.length);
        dest.writeByteArray(backdropByteArray);
    }

    private Movie(Parcel in) {
        id = in.readString();
        title = in.readString();
        releaseDate = in.readString();
        poster = in.readString();
        backdrop = in.readString();
        votesAverage = in.readString();
        plot = in.readString();

        trailerUrls = new ArrayList<>();
        in.readList(trailerUrls, null);

        posterByteArray = new byte[in.readInt()];
        in.readByteArray(posterByteArray);

        backdropByteArray = new byte[in.readInt()];
        in.readByteArray(backdropByteArray);
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
