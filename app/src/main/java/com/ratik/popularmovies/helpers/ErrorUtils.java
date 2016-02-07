package com.ratik.popularmovies.helpers;

import android.app.AlertDialog;
import android.content.Context;

import com.ratik.popularmovies.R;

/**
 * Created by Ratik on 31/01/16.
 */
public class ErrorUtils {

    public static void showGenericError(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.generic_error_title))
                .setMessage(context.getString(R.string.generic_error_message))
                .setPositiveButton(android.R.string.ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
