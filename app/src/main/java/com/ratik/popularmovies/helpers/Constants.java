package com.ratik.popularmovies.helpers;

/**
 * Created by Ratik on 31/01/16.
 */
public class Constants {

    // TMDB GET constants
    public static final String BASE_URL = "http://api.themoviedb.org/3/discover/movie";
    public static final String ORDER_BY_POPULARITY = "popularity.desc";
    public static final String ORDER_BY_VOTES = "vote_average.desc";

    // Movie features constants
    public static final String MOVIE_TITLE = "title";
    public static final String MOVIE_RELEASE_DATE = "release_date";
    public static final String MOVIE_POSTER = "poster_path";
    public static final String MOVIE_VOTE_AVERAGE = "vote_average";
    public static final String MOVIE_PLOT = "overview";

}