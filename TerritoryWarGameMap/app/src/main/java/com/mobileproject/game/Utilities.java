package com.mobileproject.game;

/**
 * Created by jocs on 2016-10-26.
 */

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;


/**
 * A Utility class.
 */

public class Utilities {

    /**
     * Hides the soft keyboard.
     * @param view the current view
     * @param activity the current activity
     */
    public static void hideKeyboard(View view, Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Gets the error from the JSON string
     * @param error the JSON from the server
     * @return the error message
     */
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



    /**
     * Shifts the location by one tile.
     * @param point the location
     * @param lat the latitude
     * @param lng the longitude
     * @return the displaced latlng
     */
    public static LatLng shifter(LatLng point, double lat, double lng) {
        return new LatLng(point.latitude + lat, point.longitude + lng);
    }


    /**
     * Updates the tile in the Hash Map based on its ID
     * @param colour the colour of the new tile
     * @param tileLatID the latID of the tile
     * @param tileLngID the LngID of the tile
     * @param username the user of the tile - null if empty
     */
    public static void updateTile(int colour, int tileLatID, int tileLngID, String username, GoogleMap mMap, HashMap<Tile.TileID, Tile> tiles, int soldiers, int gold, int food) {
        Tile t;
        Tile.TileID tileID = new Tile.TileID(tileLatID, tileLngID);
        if (tiles != null) {
            t = tiles.get(tileID);
            if (t == null) {
                t = new Tile(
                        tileID,
                        username, soldiers, gold, food, colour);
                tiles.put(tileID, t);
                System.out.println("new tile");
            } else {
                t.setColour(colour);
                t.setFood(food);
                t.setGold(gold);
                t.setSoldiers(soldiers);
                t.setUsername(username);
            }
            t.drawTile(mMap);
            System.out.println("Tiles size: " + tiles.size());
        }
    }

    /**
     * Plays or stops the sound player.
     * @param context the current context
     * @param mp the current media player
     * @param mode "play" to start playing, "stop" to stop playing
     */
    public static void SoundPlayer(Context context, final MediaPlayer mp, String mode) {
        switch (mode){
            case "play":
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mp.start();
                    }
                });
                break;
            case "stop":
                mp.stop();
            default:
                break;
        }
    }

}

/**
 * A Typewriter effect textview type class, achieve a typing animation.
 */

class Typewriter extends TextView {

    private CharSequence mText;
    private int mIndex;
    private long mDelay = 500; //Default 500ms delay

    public Typewriter(Context context) {
        super(context);
    }

    public Typewriter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Handler mHandler = new Handler();
    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            setText(mText.subSequence(0, mIndex++));
            if(mIndex <= mText.length()) {
                mHandler.postDelayed(characterAdder, mDelay);
            }
        }
    };

    public void animateText(CharSequence text) {
        mText = text;
        mIndex = 0;

        setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }
}






