package com.ratik.popularmovies.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ratik.popularmovies.R;
import com.ratik.popularmovies.model.Movie;

/**
 * Created by Ratik on 02/02/16.
 */
public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Get passed data
        Intent intent = getIntent();
        Movie movie = intent.getParcelableExtra(MainActivity.MOVIE_DATA);
        boolean isFave = intent.getBooleanExtra(MainActivity.IS_FAVE, false);
        boolean isNetworkAvailable = intent.getBooleanExtra(MainActivity.NETWORK_STATE, true);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putBoolean(MainActivity.NETWORK_STATE, isNetworkAvailable);
            arguments.putBoolean(MainActivity.IS_FAVE, isFave);
            arguments.putParcelable(MainActivity.MOVIE_DATA, movie);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        }
    }
}
