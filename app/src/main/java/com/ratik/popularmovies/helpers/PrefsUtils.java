package com.ratik.popularmovies.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ratik.popularmovies.ui.MainActivity;

/**
 * Created by Ratik on 08/02/16.
 */
public class PrefsUtils {
    public static final String PREF_SORT_TYPE = "sort_type";

    public static void setSortType(Context context, String sortType) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREF_SORT_TYPE, sortType);
        editor.apply();
    }

    public static String getSortType(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_SORT_TYPE, MainActivity.SORT_BY_POPULARITY);
    }
}
