package com.mobileproject.game;

/**
 * Created by jocs on 2016-10-26.
 */


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.mobileproject.game.GameMapUI.latTileUnit;
import static com.mobileproject.game.GameMapUI.lngTileUnit;

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
     * Gets a LocationID object from a latlng
     * @param loc the location
     * @return the LocationID
     */
    public static LocationID LocationToID (LatLng loc){
        int LatID = (int)(loc.latitude/ latTileUnit) + 1;
        int LngID = (int)(loc.longitude/ lngTileUnit) ;
        return new LocationID(LatID,LngID);
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
    public static void updateTile(int colour, String tileLatID, String tileLngID, String username, GoogleMap mMap, HashMap<TileID, Tile> tiles) {
        Tile t;
        TileID tileID = new TileID(Integer.parseInt(tileLatID), Integer.parseInt(tileLngID));
        LocationID latLng = new LocationID(Integer.parseInt(tileLatID), Integer.parseInt(tileLngID));
        if ((t = tiles.get(tileID)) != null) {
            //remove the previous tile from the map
            t.remove();
            t.setPolygon(drawPolygon(mMap, IdTOLocation(latLng), latTileUnit, lngTileUnit, colour));
        } else {
            t = new Tile(
                    tileID,
                    username,
                    drawPolygon(mMap, IdTOLocation(latLng), latTileUnit, lngTileUnit, colour));
            tiles.put(tileID, t);
        }
        System.out.println("Tiles size: " + tiles.size());
    }


    /**
     * Draws a polygon on the map
     * @param map the google map
     * @param loc the starting location to draw
     * @param width the width of the polygon
     * @param height the height of the polygon
     * @param color the ColorSet colour of the polygon
     * @return the created polygon
     */
    public static Polygon drawPolygon(GoogleMap map, LatLng loc, double width, double height, int color) {
        Polygon polygon = map.addPolygon(new PolygonOptions()
                .addAll(createRectangle(loc, width / 2, height / 2))
                .fillColor(color)
                .strokeWidth(0)
                .clickable(true)
                .strokeColor(Color.argb(0, 255, 255, 255)));
        return polygon;
    }

    /**
     * Creates a List of LatLngs that form a rectangle with the given dimensions.
     */
    public static List<LatLng> createRectangle(LatLng center, double halfWidth, double halfHeight) {
        return Arrays.asList(new LatLng(center.latitude - halfHeight, center.longitude - halfWidth),
                new LatLng(center.latitude - halfHeight, center.longitude + halfWidth),
                new LatLng(center.latitude + halfHeight, center.longitude + halfWidth),
                new LatLng(center.latitude + halfHeight, center.longitude - halfWidth),
                new LatLng(center.latitude - halfHeight, center.longitude - halfWidth));
    }


    /**
     * Gets the latitude longitude of the location ID
     * @param newID the location ID
     * @return the location of that ID
     */
    public static LatLng IdTOLocation(LocationID newID){
        double lat = (newID.getLatID() - 1) * latTileUnit + latTileUnit/ 2.0;
        double lng = (newID.getLngID()) * lngTileUnit + lngTileUnit / 2.0 -lngTileUnit;
        System.out.println("converted lat: " + lat + " converted lng: " + lng);
        LatLng loc = new LatLng(lat,lng);
        return loc;
    }

}
