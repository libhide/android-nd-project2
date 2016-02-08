package com.ratik.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ratik on 08/02/16.
 */
public class MovieReview implements Parcelable {
    private String author;
    private String review;

    public MovieReview() {

    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    @Override
    public String toString() {
        return review + " by " + author;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.author);
        dest.writeString(this.review);
    }

    private MovieReview(Parcel in) {
        this.author = in.readString();
        this.review = in.readString();
    }

    public static final Parcelable.Creator<MovieReview> CREATOR = new Parcelable.Creator<MovieReview>() {
        public MovieReview createFromParcel(Parcel source) {
            return new MovieReview(source);
        }

        public MovieReview[] newArray(int size) {
            return new MovieReview[size];
        }
    };
}
