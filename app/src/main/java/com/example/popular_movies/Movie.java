package com.example.popular_movies;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by i57198 on 11/24/16.
 */

public class Movie implements Parcelable {

    private static final String LOG_TAG = Movie.class.getSimpleName();
    private static final String MOVIE_POSTER_BASE_URL = "http://image.tmdb.org/t/p/w342/";

    String title;
    String description;
    String posterPath;
    String releaseDate;
    double rating;

    public Movie(String title, String description, String posterPath, String releaseDate, double rating) {
        this.title = title;
        this.description = description;
        this.posterPath = MOVIE_POSTER_BASE_URL + posterPath;
        this.releaseDate = formatReleaseDate(releaseDate);
        this.rating = rating;
    }

    private String formatReleaseDate(String date) {
        SimpleDateFormat origFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat newFormat = new SimpleDateFormat("MM/dd/yyyy");

        try {
            date = newFormat.format(origFormat.parse(date));
        }
        catch (ParseException e){
            Log.e(LOG_TAG, "Error parsing release date.");
        }

        return date;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(posterPath);
        parcel.writeString(releaseDate);
        parcel.writeDouble(rating);
    }

    // Using the 'in' variable, retrieve the values written into the parcel. This constructor is private
    // so that only the 'CREATOR' field can access it. Values are read in the order they were written.
    private Movie(Parcel in) {
        title = in.readString();
        description = in.readString();
        posterPath = in.readString();
        releaseDate = in.readString();
        rating = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // After implementing the `Parcelable` interface, we need to create the `Parcelable.Creator<MyParcelable> CREATOR` constant.
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
