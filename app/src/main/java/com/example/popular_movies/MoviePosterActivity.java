package com.example.popular_movies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

public class MoviePosterActivity extends AppCompatActivity implements FetchMovieTask.IAsyncTaskCompleteListener {

    public static final String INTENT_EXTRA_MOVIE = "IntentExtraMovie";
    private final String BUNDLE_MOVIES_KEY = "BundleMoviesKey";

    @BindView(R.id.gridview_movie_posters) GridView mGridView;

    MoviePosterAdapter mAdapter;
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_poster);
        ButterKnife.bind(this);

        mSharedPreferences = getSharedPreferences(getString(R.string.shared_preference_file_key), Context.MODE_PRIVATE);

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_MOVIES_KEY)) {
            ArrayList<Movie> movies = savedInstanceState.getParcelableArrayList(BUNDLE_MOVIES_KEY);
            mAdapter = new MoviePosterAdapter(this, movies);
        }
        else {
            mAdapter = new MoviePosterAdapter(this, new ArrayList<Movie>());

            // Retrieve sort preference
            String sortBy = mSharedPreferences.getString(
                    getString(R.string.sort_pref_key),
                    getString(R.string.sort_by_popularity));

            // Fetch movies according to sort pref
            getMovies(sortBy);
        }

        mGridView.setAdapter(mAdapter);
    }

    @OnItemClick(R.id.gridview_movie_posters)
    void onItemClick(int position) {
        Movie movie = mAdapter.getItem(position);
        Intent intent = new Intent(MoviePosterActivity.this, MovieDetailActivity.class);
        intent.putExtra(INTENT_EXTRA_MOVIE, movie);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get shared preferences
        if (mSharedPreferences == null) {
            mSharedPreferences = this.getSharedPreferences(getString(R.string.shared_preference_file_key), Context.MODE_PRIVATE);
        }

        switch (item.getItemId()) {
            case R.id.sort_by_popularity:
                String sortByPopularity = getString(R.string.sort_by_popularity);
                mSharedPreferences.edit().putString(
                        getString(R.string.sort_pref_key),
                        sortByPopularity).apply();
                getMovies(sortByPopularity);
                return true;
            case R.id.sort_by_rating:
                String sortByRating = getString(R.string.sort_by_rating);
                mSharedPreferences.edit().putString(
                        getString(R.string.sort_pref_key),
                       sortByRating).apply();
                getMovies(sortByRating);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(BUNDLE_MOVIES_KEY, mAdapter.getMovies());
        super.onSaveInstanceState(outState);
    }

    void getMovies(String sortBy) {
        if (!isOnline()) {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
        }
        else {
            FetchMovieTask fetchMovieTask = new FetchMovieTask(this);
            fetchMovieTask.execute(sortBy);
        }
    }

    boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onTaskComplete(ArrayList<Movie> movies) {
        if (movies != null) {
            mAdapter.clear();
            mAdapter.addAll(movies);
        }
    }
}