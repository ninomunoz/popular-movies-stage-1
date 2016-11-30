package com.example.popular_movies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by i57198 on 11/23/16.
 */

public class MoviePosterAdapter extends ArrayAdapter<Movie> {

    public MoviePosterAdapter(Context context, ArrayList<Movie> movies) {
        super(context, R.layout.list_item_movie_poster, movies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Movie movie = getItem(position);
        ViewHolder viewHolder;

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_movie_poster, parent, false);
            viewHolder.moviePoster = (ImageView) convertView.findViewById(R.id.iv_movie_poster);
            convertView.setTag(viewHolder); // attach viewHolder object to View
        }
        else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Picasso.with(getContext()).load(movie.posterPath).into(viewHolder.moviePoster);

        return convertView;
    }

    // ViewHolder speeds up population of GridView by caching the imageView
    private static class ViewHolder {
        ImageView moviePoster;
    }
}
