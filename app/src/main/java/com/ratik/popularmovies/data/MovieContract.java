package com.ratik.popularmovies.data;

import android.provider.BaseColumns;

/**
 * Created by Ratik on 14/02/16.
 */
public class MovieContract {
    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_BACKDROP = "backdrop";
        public static final String COLUMN_VOTES_AVG = "votes_average";
        public static final String COLUMN_PLOT = "plot";
    }
}




