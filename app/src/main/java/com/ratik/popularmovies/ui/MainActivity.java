package com.ratik.popularmovies.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ratik.popularmovies.Keys;
import com.ratik.popularmovies.R;
import com.ratik.popularmovies.adapters.MovieAdapter;
import com.ratik.popularmovies.data.MovieContract;
import com.ratik.popularmovies.helpers.BitmapUtils;
import com.ratik.popularmovies.helpers.Constants;
import com.ratik.popularmovies.helpers.ErrorUtils;
import com.ratik.popularmovies.helpers.NetworkUtils;
import com.ratik.popularmovies.helpers.PrefsUtils;
import com.ratik.popularmovies.listeners.RecyclerItemClickListener;
import com.ratik.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String MOVIE_DATA = "movie_data";
    public static final String MOVIES_DATA = "movies_data";
    public static final String IS_FAVE = "is_fave";
    public static final String NETWORK_STATE = "network_state";

    private static final String RV_SCROLL_POS = "scroll_position";

    public static final String SORT_BY_POPULARITY = "popularity";
    public static final String SORT_BY_RATING = "rating";
    public static final String SORT_BY_FAVE = "fave";

    // Views
    private RecyclerView moviesView;
    private ProgressBar progressBar;

    // Data
    private ArrayList<Movie> movies = new ArrayList<>();
    private MovieAdapter adapter;

    // Misc
    private RecyclerView.LayoutManager layoutManager;
    private Parcelable rvState;
    private String currentSortType = SORT_BY_POPULARITY;
    private boolean isNetworkPresent;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    public static boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar setup
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Views init
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        moviesView = (RecyclerView) findViewById(R.id.moviesView);

        isNetworkPresent = NetworkUtils.isNetworkAvailable(this);

        // State check
        currentSortType = PrefsUtils.getSortType(this);
        setToolbarTitle();
        if (savedInstanceState != null) {
            movies = (ArrayList<Movie>) savedInstanceState.getSerializable(MOVIES_DATA);
            progressBar.setVisibility(View.INVISIBLE);
        } else {
            if (isNetworkPresent) {
                // YES, do the network call!
                if (currentSortType.equals(SORT_BY_POPULARITY)) {
                    fetchMovies(Constants.ORDER_BY_POPULARITY);
                } else {
                    fetchMovies(Constants.ORDER_BY_VOTES);
                }
            } else {
                // NO, show toast
                Toast.makeText(this, getString(R.string.network_unavailable_message),
                        Toast.LENGTH_LONG).show();
            }
        }

        // Tablet check
        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-sw600dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            getFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, new DefaultDetailFragment()).commit();
        }

        setupRecyclerView();

        // Get fave movies if network isn't present
        if (!isNetworkPresent) {
            fetchFaves();
        }
    }

    private void setToolbarTitle() {
        if (getSupportActionBar() != null) {
            switch (currentSortType) {
                case SORT_BY_POPULARITY:
                    getSupportActionBar().setTitle(getString(R.string.app_name));
                    break;
                case SORT_BY_RATING:
                    getSupportActionBar().setTitle(R.string.top_rated_title);
                    break;
                case SORT_BY_FAVE:
                    getSupportActionBar().setTitle(R.string.favorite_title);
                    break;
            }
        }
    }

    private void setupRecyclerView() {
        moviesView.setHasFixedSize(true);
        if (mTwoPane) {
            layoutManager = new GridLayoutManager(this, 3);
        } else {
            if (getResources().getConfiguration().orientation
                    == Configuration.ORIENTATION_PORTRAIT) {
                layoutManager = new GridLayoutManager(this, 2);
            } else {
                layoutManager = new GridLayoutManager(this, 4);
            }
        }
        moviesView.setLayoutManager(layoutManager);
        adapter = new MovieAdapter(MainActivity.this, movies);
        moviesView.setAdapter(adapter);
        // Click listener
        moviesView.addOnItemTouchListener(
                new RecyclerItemClickListener(MainActivity.this,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Movie movie = movies.get(position);
                                if (mTwoPane) {
                                    DetailFragment detailFragment = new DetailFragment();
                                    Bundle arguments = new Bundle();
                                    arguments.putBoolean(NETWORK_STATE, isNetworkPresent);
                                    if (!currentSortType.equals(SORT_BY_FAVE)) {
                                        if (!movies.get(position).getPoster().isEmpty()) {
                                            arguments.putParcelable(MOVIE_DATA, movie);
                                            arguments.putBoolean(IS_FAVE, false);
                                        } else {
                                            Toast.makeText(MainActivity.this, R.string.corrupt_movie_data_error,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        arguments.putParcelable(MOVIE_DATA, movie);
                                        arguments.putBoolean(IS_FAVE, true);
                                    }
                                    detailFragment.setArguments(arguments);
                                    getFragmentManager().beginTransaction()
                                            .replace(R.id.movie_detail_container, detailFragment)
                                            .commit();
                                } else {
                                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                                    intent.putExtra(NETWORK_STATE, isNetworkPresent);
                                    if (!currentSortType.equals(SORT_BY_FAVE)) {
                                        if (!movies.get(position).getPoster().isEmpty()) {
                                            intent.putExtra(MOVIE_DATA, movie);
                                            intent.putExtra(IS_FAVE, false);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(MainActivity.this, R.string.corrupt_movie_data_error,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        intent.putExtra(MOVIE_DATA, movie);
                                        intent.putExtra(IS_FAVE, true);
                                        startActivity(intent);
                                    }
                                }
                            }
                        })
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentSortType.equals(SORT_BY_FAVE)) {
            fetchFaves();
        } else {
            if (rvState != null) {
                layoutManager.onRestoreInstanceState(rvState);
            }
        }
    }

    private void fetchMovies(String orderBy) {
        progressBar.setVisibility(View.VISIBLE);
        String url = Constants.BASE_URL;
        url += "?sort_by=" + orderBy;
        url += "&";
        url += "api_key=" + Keys.API_KEY;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
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
                        getMoviesData(jsonData);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    } else {
                        ErrorUtils.showGenericError(MainActivity.this);
                    }
                } catch (IOException | JSONException e) {
                    Log.e(TAG, "Exception caught:", e);
                }
            }
        });
    }

    private void fetchFaves() {
        currentSortType = SORT_BY_FAVE;

        setToolbarTitle();

        movies.clear();

        Cursor cursor = getContentResolver().query(MovieContract.BASE_CONTENT_URI, null,
                null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Movie movie = new Movie();

                movie.setId(cursor.getString(cursor.getColumnIndex(
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID)));
                movie.setTitle(cursor.getString(cursor.getColumnIndex(
                        MovieContract.MovieEntry.COLUMN_TITLE)));
                movie.setReleaseDate(cursor.getString(cursor.getColumnIndex(
                        MovieContract.MovieEntry.COLUMN_RELEASE_DATE)));
                movie.setVotesAverage(cursor.getString(cursor.getColumnIndex(
                        MovieContract.MovieEntry.COLUMN_VOTES_AVG)));
                movie.setPlot(cursor.getString(cursor.getColumnIndex(
                        MovieContract.MovieEntry.COLUMN_PLOT)));

                movie.setTrailerUrls(null);
                movie.setMovieReviews(null);
                movie.setPoster("");
                movie.setPosterByteArray(cursor.getBlob(cursor.getColumnIndex(
                        MovieContract.MovieEntry.COLUMN_POSTER)));
                movie.setBackdrop("");
                movie.setBackdropByteArray(cursor.getBlob(cursor.getColumnIndex(
                        MovieContract.MovieEntry.COLUMN_BACKDROP)));

                movies.add(movie);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Toast.makeText(this, R.string.no_faves_message, Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.INVISIBLE);
    }

    // Populates movies data object
    private void getMoviesData(String jsonData) throws JSONException {
        movies.clear();
        JSONObject moviesObject = new JSONObject(jsonData);
        JSONArray moviesArray = moviesObject.getJSONArray("results");
        for (int i = 0; i < moviesArray.length(); i++) {
            JSONObject movieObject = moviesArray.getJSONObject(i);

            Movie movie = new Movie();
            movie.setId(movieObject.getString(Constants.MOVIE_ID));
            movie.setTitle(movieObject.getString(Constants.MOVIE_TITLE));
            movie.setReleaseDate(movieObject.getString(Constants.MOVIE_RELEASE_DATE));
            movie.setPoster(movieObject.getString(Constants.MOVIE_POSTER));
            movie.setBackdrop(movieObject.getString(Constants.MOVIE_BACKDROP));
            movie.setVotesAverage(movieObject.getString(Constants.MOVIE_VOTE_AVERAGE));
            movie.setPlot(movieObject.getString(Constants.MOVIE_PLOT));

            movie.setPosterByteArray(BitmapUtils.getBitmapInBytes(BitmapFactory.decodeResource(
                    getResources(), R.drawable.dummy_backdrop)));
            movie.setBackdropByteArray(BitmapUtils.getBitmapInBytes(BitmapFactory.decodeResource(
                    getResources(), R.drawable.error_poster)));

            movies.add(movie);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            movies = (ArrayList<Movie>) savedInstanceState.getSerializable(MOVIES_DATA);
            rvState = savedInstanceState.getParcelable(RV_SCROLL_POS);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(RV_SCROLL_POS, layoutManager.onSaveInstanceState());
        outState.putSerializable(MOVIES_DATA, movies);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        isNetworkPresent = NetworkUtils.isNetworkAvailable(this);
        switch (item.getItemId()) {
            case R.id.action_popular:
                currentSortType = SORT_BY_POPULARITY;
                if (isNetworkPresent) {
                    // Save sort type
                    PrefsUtils.setSortType(this, currentSortType);
                    // Fetch movies
                    fetchMovies(Constants.ORDER_BY_POPULARITY);
                } else {
                    Toast.makeText(MainActivity.this, R.string.general_network_unavailable_message,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_top_rated:
                currentSortType = SORT_BY_RATING;
                if (isNetworkPresent) {
                    // Save sort type
                    PrefsUtils.setSortType(this, currentSortType);
                    // Fetch movies
                    fetchMovies(Constants.ORDER_BY_VOTES);
                } else {
                    Toast.makeText(MainActivity.this, R.string.general_network_unavailable_message,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_fave:
                currentSortType = SORT_BY_FAVE;
                // Save sort type
                PrefsUtils.setSortType(this, currentSortType);
                // Fetch movies
                fetchFaves();
                break;
        }
        setToolbarTitle();
        return super.onOptionsItemSelected(item);
    }
}
