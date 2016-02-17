package com.ratik.popularmovies.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ratik.popularmovies.Keys;
import com.ratik.popularmovies.R;
import com.ratik.popularmovies.data.MovieContract;
import com.ratik.popularmovies.helpers.BitmapUtils;
import com.ratik.popularmovies.helpers.Constants;
import com.ratik.popularmovies.helpers.ErrorUtils;
import com.ratik.popularmovies.model.Movie;
import com.ratik.popularmovies.model.MovieReview;
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
 * Created by Ratik on 17/02/16.
 */
public class DetailFragment extends Fragment {

    private static final String TAG = DetailFragment.class.getSimpleName();
    // Data
    private Movie movie;
    private ArrayList<String> trailerUrls;
    private ArrayList<MovieReview> reviews;

    private LinearLayout contentContainer;
    private ImageView posterImageView;
    private ImageView movieBackdrop;
    private ImageView playImage;
    private ProgressDialog progressDialog;
    private FloatingActionButton fab;

    private ContentResolver contentResolver;

    private boolean isFave;
    private boolean inDb;
    private boolean isNetworkAvailable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get arguments
        isFave = getArguments().getBoolean(MainActivity.IS_FAVE);
        isNetworkAvailable = getArguments().getBoolean(MainActivity.NETWORK_STATE);
        movie = getArguments().getParcelable(MainActivity.MOVIE_DATA);

        // get content resolver
        contentResolver = getActivity().getContentResolver();

        // show progress dialog
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Please wait...");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        if (!MainActivity.mTwoPane) {
            AppCompatActivity activity = ((AppCompatActivity) getActivity());
            // Set the toolbar
            Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout)
                rootView.findViewById(R.id.collapsingToolbar);
        collapsingToolbar.setTitle(movie.getTitle());

        // fab setup
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        // Views init
        playImage = (ImageView) rootView.findViewById(R.id.playImage);
        contentContainer = (LinearLayout) rootView.findViewById(R.id.detailContentContainer);
        movieBackdrop = (ImageView) rootView.findViewById(R.id.movieImage);
        posterImageView = (ImageView) rootView.findViewById(R.id.posterImageView);

        posterImageView.setDrawingCacheEnabled(true);
        movieBackdrop.setDrawingCacheEnabled(true);

        TextView titleTextView = (TextView) rootView.findViewById(R.id.titleTextView);
        TextView overviewTextView = (TextView) rootView.findViewById(R.id.overviewTextView);
        TextView releaseDateTextView = (TextView) rootView.findViewById(R.id.releaseDateTextView);
        TextView voteAverageTextView = (TextView) rootView.findViewById(R.id.voteAverageTextView);
        TextView reviewsHeaderTextView = (TextView) rootView.findViewById(R.id.reviewsHeader);

        // Fetch meta data
        if (isNetworkAvailable && !isFave) {
            fetchVideoData();
            fetchReviews();
        } else {
            playImage.setVisibility(View.GONE);
            reviewsHeaderTextView.setVisibility(View.GONE);
            progressDialog.hide();
        }

        if (!isFave) {
            // Fill in data
            Picasso.with(getActivity()).load(movie.getBackdropUrl()).into(movieBackdrop, new Callback() {
                @Override
                public void onSuccess() {
                    playImage.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError() {

                }
            });

            titleTextView.setText(movie.getTitle());
            Picasso.with(getActivity()).load(movie.getPosterUrl()).into(posterImageView);
            overviewTextView.setText(movie.getPlot());
            releaseDateTextView.setText(movie.getFormattedReleaseDate());
            voteAverageTextView.setText(String.format(getString(R.string.vote_average_placeholder),
                    movie.getVotesAverage()));

            // Click listeners
            playImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int numberOfTrailer = trailerUrls.size();
                    if (numberOfTrailer == 0) {
                        Toast.makeText(getActivity(), "No trailer available. Sorry!",
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
        } else {
            titleTextView.setText(movie.getTitle());
            Picasso.with(getActivity()).load(movie.getPosterUrl()).into(posterImageView);
            overviewTextView.setText(movie.getPlot());
            releaseDateTextView.setText(movie.getFormattedReleaseDate());
            voteAverageTextView.setText(String.format(getString(R.string.vote_average_placeholder),
                    movie.getVotesAverage()));

            posterImageView.setImageBitmap(BitmapUtils.getBitmapFromBytes(movie.getPosterByteArray()));
            movieBackdrop.setImageBitmap(BitmapUtils.getBitmapFromBytes(movie.getBackdropByteArray()));
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        inDb = checkIifMovieIsInDatabase();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inDb) {
                    // Remove record from faves
                    contentResolver.delete(
                            MovieContract.BASE_CONTENT_URI,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID + getString(R.string.selection),
                            new String[]{movie.getId()}
                    );
                    Toast.makeText(getActivity(), "Removed!", Toast.LENGTH_SHORT).show();
                } else {
                    // Add record to faves
                    ContentValues values = new ContentValues();
                    values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.getId());
                    values.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
                    values.put(MovieContract.MovieEntry.COLUMN_PLOT, movie.getPlot());
                    values.put(MovieContract.MovieEntry.COLUMN_VOTES_AVG, movie.getVotesAverage());
                    values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
                    values.put(MovieContract.MovieEntry.COLUMN_POSTER, BitmapUtils
                            .getBitmapInBytes(posterImageView.getDrawingCache()));
                    values.put(MovieContract.MovieEntry.COLUMN_BACKDROP, BitmapUtils
                            .getBitmapInBytes(movieBackdrop.getDrawingCache()));

                    contentResolver.insert(MovieContract.BASE_CONTENT_URI, values);
                    Toast.makeText(getActivity(), "Movie added to favorites!", Toast.LENGTH_SHORT).show();
                }
                inDb = checkIifMovieIsInDatabase();
            }
        });
    }

    private void showTrailerList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select a trailer");

        String[] trailerTitles = new String[trailerUrls.size()];
        for (int i = 0; i < trailerTitles.length; i++) {
            trailerTitles[i] = "Trailer " + (i + 1);
        }
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),
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
        progressDialog.show();
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
                        getActivity().runOnUiThread(new Runnable() {
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
                        ErrorUtils.showGenericError(getActivity());
                    }
                } catch (IOException | JSONException e) {
                    Log.e(TAG, "Exception caught:", e);
                }
            }
        });
    }

    private void fetchReviews() {
        progressDialog.show();
        String url = Constants.MOVIE_BASE_URL;
        url += "/" + movie.getId();
        url += "/reviews";
        url += "?api_key=" + Keys.API_KEY;

        OkHttpClient client = new OkHttpClient();
        Request videosRequest = new Request.Builder()
                .url(url)
                .build();

        Call reviewsCall = client.newCall(videosRequest);
        reviewsCall.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Unimplemented
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    if (response.isSuccessful()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.hide();
                            }
                        });
                        JSONObject movieObject = new JSONObject(jsonData);
                        JSONArray reviewsArray = movieObject.getJSONArray("results");

                        reviews = new ArrayList<>();
                        for (int i = 0; i < reviewsArray.length(); i++) {
                            JSONObject reviewObject = reviewsArray.getJSONObject(i);

                            MovieReview rs = new MovieReview();
                            rs.setAuthor(reviewObject.getString("author"));
                            rs.setReview(reviewObject.getString("content"));

                            reviews.add(rs);
                        }
                        movie.setMovieReviews(reviews);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (reviews.size() > 0) {
                                    addReviewsToLayout(true);
                                } else {
                                    addReviewsToLayout(false);
                                }
                            }
                        });
                    } else {
                        ErrorUtils.showGenericError(getActivity());
                    }
                } catch (IOException | JSONException e) {
                    Log.e(TAG, "Exception caught:", e);
                }
            }
        });
    }

    private void addReviewsToLayout(boolean reviewsPresent) {
        if (reviewsPresent) {
            for (MovieReview review : reviews) {
                View view = getActivity().getLayoutInflater().inflate(R.layout.cardview_template,
                        contentContainer, false);

                CardView cv = (CardView) view.findViewById(R.id.card);
                TextView reviewTextView = (TextView) view.findViewById(R.id.reviewContent);
                TextView authorTextView = (TextView) view.findViewById(R.id.reviewAuthor);
                reviewTextView.setText(review.getReview());
                authorTextView.setText("– " + review.getAuthor());

                contentContainer.addView(cv);
            }
        } else {
            View noReviewsView = getActivity().getLayoutInflater().inflate(R.layout.no_reviews_textview,
                    contentContainer, false);
            contentContainer.addView(noReviewsView);
        }
    }

    private boolean checkIifMovieIsInDatabase() {
        Cursor c = contentResolver.query(
                MovieContract.BASE_CONTENT_URI,
                new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID},
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + getString(R.string.selection),
                new String[]{movie.getId()},
                null
        );
        if (c != null && c.getCount() > 0) {
            c.close();
            fab.setImageResource(R.drawable.ic_star);
            return true;
        } else {
            fab.setImageResource(R.drawable.ic_star_outline);
            return false;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_detail, menu);
        if (!isNetworkAvailable || isFave) {
            MenuItem item = menu.findItem(R.id.action_share);
            item.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
            case R.id.action_share:
                if (trailerUrls != null) {
                    if (trailerUrls.size() != 0) {
                        showShareDialog();
                    }
                }
                break;
        }
        return true;
    }

    private void showShareDialog() {
        String url = movie.getTrailerUrls().get(0);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        String message = String.format("Check this %s trailer out! " +
                "It's awesome!\n\n%s", movie.getTitle(), url);
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        shareIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareIntent,
                getString(com.ratik.popularmovies.R.string.share)));
    }
}
