package com.example.popular_movies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MoviePosterActivity extends AppCompatActivity {

    final String INTENT_TAG = "IntentTag";

    GridView mGridView;
    MoviePosterAdapter mAdapter;
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_poster);

        mGridView = (GridView) findViewById(R.id.gridview_movie_posters);
        mAdapter = new MoviePosterAdapter(this, new ArrayList<Movie>());
        mGridView.setAdapter(mAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = mAdapter.getItem(position);
                Intent intent = new Intent(MoviePosterActivity.this, MovieDetailActivity.class);
                intent.putExtra(INTENT_TAG, movie);
                startActivity(intent);
            }
        });
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
    protected void onStart() {
        super.onStart();

        // Get shared preferences
        if (mSharedPreferences == null) {
            mSharedPreferences = this.getSharedPreferences(getString(R.string.shared_preference_file_key), Context.MODE_PRIVATE);
        }

        // Retrieve sort preference
        String sortBy = mSharedPreferences.getString(
                getString(R.string.sort_pref_key),
                getString(R.string.sort_by_popularity));

        getMovies(sortBy);
    }

    void getMovies(String sortBy) {
        if (!isOnline()) {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
        }
        else {
            FetchMovieTask fetchMovieTask = new FetchMovieTask();
            fetchMovieTask.execute(sortBy);
        }
    }

    boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
            // If there's no sort method provided, there's nothing to look up. Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {
                // Construct the URL for themoviedb query
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(params[0])
                        .appendQueryParameter("api_key", BuildConfig.THEMOVIEDB_API_KEY)
                        .appendQueryParameter("language", "en-US");

                String urlString = builder.build().toString();
                URL url = new URL(urlString);

                // Create the request to themoviedb, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the movie data.
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movieData) {
            if (movieData != null)
            {
                mAdapter.clear();
                mAdapter.addAll(movieData);
            }
        }

        private ArrayList<Movie> getMovieDataFromJson(String movieJsonStr) throws JSONException
        {
            // These are the names of the JSON objects that need to be extracted.
            final String MOVIE_LIST = "results";
            final String MOVIE_TITLE = "title";
            final String MOVIE_DESCRIPTION = "overview";
            final String MOVIE_POSTER = "poster_path";
            final String MOVIE_RELEASE_DATE = "release_date";
            final String MOVIE_RATING = "vote_average";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieList = movieJson.getJSONArray(MOVIE_LIST);

            ArrayList<Movie> movies =  new ArrayList<Movie>();

            for (int i = 0; i < movieList.length(); i++)
            {
                String title, description, posterPath, releaseDate;
                double rating;

                // Get the JSON object representing the movie
                JSONObject movieObject = movieList.getJSONObject(i);

                // Extract movie data
                title = movieObject.getString(MOVIE_TITLE);
                description = movieObject.getString(MOVIE_DESCRIPTION);
                posterPath = movieObject.getString(MOVIE_POSTER);
                releaseDate = movieObject.getString(MOVIE_RELEASE_DATE);
                rating = movieObject.getDouble(MOVIE_RATING);

                // Create Movie object and add to movies
                Movie movie = new Movie(title, description, posterPath, releaseDate, rating);
                movies.add(movie);
            }

            return movies;
        }
    }

}