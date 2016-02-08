package com.ratik.popularmovies.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ratik.popularmovies.Keys;
import com.ratik.popularmovies.R;
import com.ratik.popularmovies.data.Movie;
import com.ratik.popularmovies.helpers.Constants;
import com.ratik.popularmovies.helpers.ErrorUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

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
    private ArrayList<String> trailerUrls;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        // Get data
        Intent intent = getIntent();
        movie = intent.getParcelableExtra(MainActivity.MOVIE_DATA);

        fetchVideoData();

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
        TextView overviewTextView = (TextView) findViewById(R.id.overviewTextView);
        TextView releaseDateTextView = (TextView) findViewById(R.id.releaseDateTextView);
        TextView voteAverageTextView = (TextView) findViewById(R.id.voteAverageTextView);
        ImageView movieBackdrop = (ImageView) findViewById(R.id.movieImage);

        // Fill in data
        Picasso.with(this).load(movie.getBackdropUrl()).into(movieBackdrop, new Callback() {
            @Override
            public void onSuccess() {
                playImage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError() {

            }
        });

        titleTextView.setText(movie.getTitle());
        Picasso.with(this).load(movie.getPosterUrl()).into(posterImageView);
        overviewTextView.setText(movie.getPlot());
        releaseDateTextView.setText(movie.getReleaseDate());
        voteAverageTextView.setText(String.format(getString(R.string.vote_average_placeholder),
                movie.getVoteAverage()));

        // Click listeners
        playImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int numberOfTrailer = trailerUrls.size();
                if (numberOfTrailer == 0) {
                    Toast.makeText(DetailActivity.this, "No trailer available. Sorry!",
                            Toast.LENGTH_SHORT).show();
                } else if (movie.getTrailerUrls().size() > 1) {
                    // Dialog
                    showTrailerList();
                } else {
                    String url = movie.getTrailerUrls().get(0);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            }
        });
    }

    private void showTrailerList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a trailer");

        String[] trailerTitles = new String[trailerUrls.size()];
        for (int i = 0; i < trailerTitles.length; i++) {
            trailerTitles[i] = "Trailer " + (i + 1);
        }
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, trailerTitles);

        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrls.get(which))));
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    private void fetchVideoData() {
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
                // Unimplemented
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    if (response.isSuccessful()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.hide();
                            }
                        });
                        JSONObject movieObject = new JSONObject(jsonData);
                        JSONArray moviesArray = movieObject.getJSONArray("results");
                        ArrayList<String> urls = new ArrayList<>();
                        for (int i = 0; i < moviesArray.length(); i++) {
                            JSONObject movieTrailer = moviesArray.getJSONObject(i);
                            String trailerURL = Constants.YT_BASE_URL + movieTrailer.getString("key");
                            urls.add(trailerURL);
                        }
                        trailerUrls = urls;
                        movie.setTrailerUrls(urls);
                    } else {
                        ErrorUtils.showGenericError(DetailActivity.this);
                    }
                } catch (IOException | JSONException e) {
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
