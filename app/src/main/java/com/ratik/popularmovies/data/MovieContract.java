package com.ratik.popularmovies.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import com.ratik.popularmovies.helpers.Constants;

/**
 * Created by Ratik on 14/02/16.
 */
public class MovieContract {

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" +
            Constants.CONTENT_AUTHORITY);

    public static final class MovieEntry implements BaseColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" +
                Constants.CONTENT_AUTHORITY;

        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_BACKDROP = "backdrop";
        public static final String COLUMN_VOTES_AVG = "votes_average";
        public static final String COLUMN_PLOT = "plot";

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(BASE_CONTENT_URI, id);
        }
    }
}




