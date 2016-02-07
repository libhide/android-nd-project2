package com.ratik.popularmovies.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ratik.popularmovies.Keys;
import com.ratik.popularmovies.R;
import com.ratik.popularmovies.data.Movie;
import com.ratik.popularmovies.helpers.Constants;
import com.ratik.popularmovies.helpers.ErrorUtils;
import com.ratik.popularmovies.helpers.NetworkUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Ratik on 02/02/16.
 */
public class DetailActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = DetailActivity.class.getSimpleName();

    // Data
    private Movie movie;

    // Views
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);



        // Get data
        Intent intent = getIntent();
        movie = intent.getParcelableExtra(MainActivity.MOVIE_DATA);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (NetworkUtils.isNetworkAvailable(this)) {
            // YES, do the network call!
            fetchVideoData();
        } else {
            // NO, show toast
            Toast.makeText(this, getString(R.string.network_unavailable_message),
                    Toast.LENGTH_LONG).show();
        }

        // Set the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout)
                findViewById(R.id.collapsingToolbar);
        collapsingToolbar.setTitle(movie.getTitle());

        // Set up views
        final ImageView playImage = (ImageView) findViewById(R.id.playImage);
        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        ImageView posterImageView = (ImageView) findViewById(R.id.posterImageView);
        final TextView overviewTextView = (TextView) findViewById(R.id.overviewTextView);
        TextView releaseDateTextView = (TextView) findViewById(R.id.releaseDateTextView);
        TextView voteAverageTextView = (TextView) findViewById(R.id.voteAverageTextView);

        ImageView movieBackdrop = (ImageView) findViewById(R.id.movieImage);
        Picasso.with(this).load(movie.getBackdropUrl()).into(movieBackdrop, new Callback() {
            @Override
            public void onSuccess() {
                playImage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError() {

            }
        });

        // Fill in data
        titleTextView.setText(movie.getTitle());
        Picasso.with(this).load(movie.getPosterUrl()).into(posterImageView);
        overviewTextView.setText(movie.getPlot());
        releaseDateTextView.setText(movie.getReleaseDate());
        voteAverageTextView.setText(String.format(getString(R.string.vote_average_placeholder),
                movie.getVoteAverage()));

        playImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Play trailer
                if (movie.getTrailerURL() != null) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(movie.getTrailerURL())));
                } else {
                    Toast.makeText(DetailActivity.this, "No trailer available. Sorry!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

//        // Some palette stuff
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), d);
//        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
//            @Override
//            public void onGenerated(Palette palette) {
//                mutedColor = palette.getMutedColor(R.attr.colorPrimary);
//                collapsingToolbar.setContentScrimColor(mutedColor);
//            }
//        });
    }

    private void fetchVideoData() {
        progressBar.setVisibility(View.VISIBLE);
        String url = Constants.MOVIE_BASE_URL;
        url += "/" + movie.getId();
        url += "/videos";
        url += "?api_key=" + Keys.API_KEY;

        OkHttpClient client = new OkHttpClient();
        Request videosRequest = new Request.Builder()
                .url(url)
                .build();

        Call videosCall = client.newCall(videosRequest);
        videosCall.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Preventing failure by taking steps prior to the
                // network call
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    if (response.isSuccessful()) {
                        JSONObject movieObject = new JSONObject(jsonData);
                        JSONArray moviesArray = movieObject.getJSONArray("results");
                        JSONObject movieTrailer = moviesArray.getJSONObject(0);
                        String trailerURL = Constants.YT_BASE_URL + movieTrailer.getString("key");
                        movie.setTrailerURL(trailerURL);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    } else {
                        ErrorUtils.showGenericError(DetailActivity.this);
                    }
                } catch (IOException | JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                    Log.e(TAG, "Exception caught:", e);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
