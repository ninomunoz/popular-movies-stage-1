package com.example.popular_movies;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {

    Movie currentMovie;
    ImageView thumbnail;
    TextView tvTitle;
    TextView tvReleaseDate;
    TextView tvRating;
    TextView tvSynopsis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        currentMovie = (Movie) getIntent().getParcelableExtra(MoviePosterActivity.INTENT_EXTRA_MOVIE);

        thumbnail = (ImageView)findViewById(R.id.iv_movie_detail_thumbnail);
        tvTitle = (TextView)findViewById(R.id.tv_movie_detail_title);
        tvReleaseDate = (TextView)findViewById(R.id.tv_movie_detail_release_date);
        tvRating = (TextView)findViewById(R.id.tv_movie_detail_rating);
        tvSynopsis = (TextView)findViewById(R.id.tv_movie_detail_synopsis);

        loadData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadData() {
        if (currentMovie != null) {
            Picasso.with(this).load(currentMovie.posterPath).into(thumbnail);
            tvTitle.setText(currentMovie.title);
            tvReleaseDate.setText(formatReleaseDate(currentMovie.releaseDate));
            tvRating.setText(formatRating(currentMovie.rating));
            tvSynopsis.setText(currentMovie.description);
        }
    }

    private String formatReleaseDate(String date) {
        String prefix = getString(R.string.release_date);
        return prefix + date;
    }

    private String formatRating(double rating) {
        String strRating = Double.toString(rating);
        String prefix = getString(R.string.rating);
        String suffix = getString(R.string.rating_suffix);
        return prefix + strRating + suffix;
    }
}
