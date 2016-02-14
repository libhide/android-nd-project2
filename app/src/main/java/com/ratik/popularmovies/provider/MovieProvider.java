package com.ratik.popularmovies.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.ratik.popularmovies.data.MovieContract;
import com.ratik.popularmovies.data.MovieDbHelper;

/**
 * Created by Ratik on 14/02/16.
 */
public class MovieProvider extends ContentProvider {

    private SQLiteDatabase writeableDb;
    private SQLiteDatabase readableDb;

    @Override
    public boolean onCreate() {
        MovieDbHelper dbHelper = new MovieDbHelper(getContext());
        writeableDb = dbHelper.getWritableDatabase();
        readableDb = dbHelper.getReadableDatabase();
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return readableDb.query(MovieContract.MovieEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Uri returnUri;
        long _id = writeableDb.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
        if (_id > 0) {
            returnUri = MovieContract.MovieEntry.buildUri(_id);
        } else
            throw new android.database.SQLException("Failed to insert row into " + uri);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return writeableDb.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return writeableDb.update(MovieContract.MovieEntry.TABLE_NAME, values, selection,
                selectionArgs);
    }
}
