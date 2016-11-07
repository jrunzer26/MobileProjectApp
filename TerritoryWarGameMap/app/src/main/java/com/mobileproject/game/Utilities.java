package com.mobileproject.game;

/**
 * Created by jocs on 2016-10-26.
 */


import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A Utility class.
 */

public class Utilities {

    public void Utilities(){}

    /**
     * Hides the soft keyboard.
     * @param view the current view
     * @param activity the current activity
     */
    public static void hideKeyboard(View view, Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String getErr(String error) {
        try {
            JSONObject resultJSON = new JSONObject(error);
            return resultJSON.get("err").toString();
        } catch (JSONException err) {
            err.printStackTrace();
            return "Error finding err";
        }
    }

    public double KMs2Lat(double KMs){
        return (KMs/111.325);
    }

    public double KMs2Lng(double KMs, double Lat){
        return KMs/111.325*Math.cos(Lat);
    }




}
