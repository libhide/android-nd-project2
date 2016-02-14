package com.ratik.popularmovies.helpers;

/**
 * Created by Ratik on 31/01/16.
 */
public class Constants {
    // App constants
    public static final String CONTENT_AUTHORITY = "com.ratik.popularmovies.provider";

    // TMDB GET constants
    public static final String BASE_URL = "http://api.themoviedb.org/3/discover/movie";
    public static final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie";
    public static final String ORDER_BY_POPULARITY = "popularity.desc";
    public static final String ORDER_BY_VOTES = "vote_average.desc";

    // Other GET constants
    public static final String YT_BASE_URL = "http://youtube.com/watch?v=";

    // Movie features constants
    public static final String MOVIE_ID = "id";
    public static final String MOVIE_TITLE = "title";
    public static final String MOVIE_RELEASE_DATE = "release_date";
    public static final String MOVIE_POSTER = "poster_path";
    public static final String MOVIE_BACKDROP = "backdrop_path";
    public static final String MOVIE_VOTE_AVERAGE = "vote_average";
    public static final String MOVIE_PLOT = "overview";
}