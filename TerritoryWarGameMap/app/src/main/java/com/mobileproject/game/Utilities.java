package com.mobileproject.game;

/**
 * Created by jocs on 2016-10-26.
 */

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;
import org.json.JSONObject;
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
        if ((t = tiles.get(tileID)) == null) {
            t = new Tile(
                    tileID,
                    username, soldiers, gold, food, colour);
            tiles.put(tileID, t);
        } else {
            t.setColour(colour);
        }
        t.drawTile(mMap);
        System.out.println("Tiles size: " + tiles.size());
    }


    public static void SoundPlayer(Context context , final MediaPlayer mp ,String mode) {
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
