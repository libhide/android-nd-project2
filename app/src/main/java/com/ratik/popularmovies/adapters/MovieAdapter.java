package com.ratik.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ratik.popularmovies.R;
import com.ratik.popularmovies.helpers.BitmapUtils;
import com.ratik.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Ratik on 07/02/16.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {
    private static final String TAG = MovieAdapter.class.getSimpleName();
    private Context context;
    private ArrayList<Movie> movies;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView posterImageView;
        public ViewHolder(View v) {
            super(v);
            posterImageView = (ImageView) v.findViewById(R.id.posterImageView);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MovieAdapter(Context context, ArrayList<Movie> myDataset) {
        this.context = context;
        movies = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MovieAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movies_grid_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        Movie movie = movies.get(position);
        String posterPath = movie.getPoster();
        if (!posterPath.isEmpty()) {
            Picasso.with(context).load(movies.get(position).getPosterUrl())
                    .into(holder.posterImageView);
            holder.posterImageView.setContentDescription(movies.get(position).getTitle() +
                    "poster");
        } else if (posterPath.isEmpty() && movie.getPosterByteArray() != null){
            holder.posterImageView.setImageBitmap(BitmapUtils
                    .getBitmapFromBytes(movie.getPosterByteArray()));
        } else {
            holder.posterImageView.setImageResource(R.drawable.error_poster);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return movies.size();
    }
}
