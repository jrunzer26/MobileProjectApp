package com.example.android.mobileproject;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by 100520993 on 10/13/2016.
 */


/**
 * A Utility class.
 */
public class Util {

    /**
     * Hides the soft keyboard.
     * @param view the current view
     * @param activity the current activity
     */
    public static void hideKeyboard(View view, Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
