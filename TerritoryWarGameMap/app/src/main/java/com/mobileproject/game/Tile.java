package com.mobileproject.game;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.Arrays;
import java.util.List;

import static com.mobileproject.game.GameMapUI.latTileUnit;
import static com.mobileproject.game.GameMapUI.lngTileUnit;

/**
 * Created by 100520993 on 11/10/2016.
 */

/**
 * A Tile Class. Holds relavant information for a tile drawn on the screen.
 */
public class Tile {
    private TileID id;
    private String username;
    private Polygon polygon;
    private int soldiers;
    private int gold;
    private int food;
    private boolean selected;
    private int colour;
    private int selectedColour;
    private LatLng loc;


    Tile(TileID id, String username, int soldiers, int gold, int food, int colour) {
        this.id = id;
        this.username = username;
        this.soldiers = soldiers;
        this.gold = gold;
        this.food = food;
        this.setSelected(false);
        setColour(colour);
    }

    public void setColour(int colour) {
        this.colour = colour;
        selectedColour = Color.argb(200, Color.red(colour), Color.green(colour), Color.blue(colour));
    }
    public TileID getTileID() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public void setPolygon(Polygon p) {
        this.polygon = p;
    }

    public void remove() {
        polygon.remove();
    }

    public TileID getId() {
        return id;
    }

    public int getSoldiers() {
        return soldiers;
    }

    public int getGold() {
        return gold;
    }

    public int getFood() {
        return food;
    }

    public void setSelected(boolean bool){
        selected = bool;
    }

    public boolean select() {
        selected = !selected;
        return selected;
    }

    public boolean isSelected(){
        return selected;
    }

    public void drawTile(GoogleMap mMap) {
        int tempColour;
        if (selected) {
            tempColour = selectedColour;
        } else {
            tempColour = colour;
        }
        if (polygon != null)
            polygon.remove();
        polygon = drawPolygon(mMap, id.getLatLng(), latTileUnit, lngTileUnit, tempColour);
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
    private Polygon drawPolygon(GoogleMap map, LatLng loc, double width, double height, int color) {

        List<LatLng> rectangle = createRectangle(loc, width / 2, height / 2);
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
    private List<LatLng> createRectangle(LatLng center, double halfWidth, double halfHeight) {
        return Arrays.asList(new LatLng(center.latitude - halfHeight, center.longitude - halfWidth),
                new LatLng(center.latitude - halfHeight, center.longitude + halfWidth),
                new LatLng(center.latitude + halfHeight, center.longitude + halfWidth),
                new LatLng(center.latitude + halfHeight, center.longitude - halfWidth),
                new LatLng(center.latitude - halfHeight, center.longitude - halfWidth));
    }

    /**
     * The unique ID for a tile.
     */
    public static class TileID {

        private int latID, lngID;
        public TileID(int latID, int lngID) {
            this.latID = latID;
            this.lngID = lngID;
        }

        @Override
        public int hashCode() {
            String hash = Integer.toString(latID) + Integer.toString(lngID);
            return hash.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TileID)) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            TileID compareObject = (TileID) obj;
            return (compareObject.lngID == this.lngID && compareObject.latID == this.latID);
        }

        public int getLatID() {
            return latID;
        }

        public int getLngID() {
            return lngID;
        }

        /**
         * Gets a LocationID object from a latlng
         * @param loc the location
         * @return the LocationID
         */
        public TileID(LatLng loc){
            latID = (int)(loc.latitude/ latTileUnit) + 1;
            lngID = (int)(loc.longitude/ lngTileUnit) ;
        }


        /**
         * Gets the latitude longitude of the location ID
         * @return the location of that ID
         */
        public LatLng getLatLng(){
            double lat = (latID - 1) * latTileUnit + latTileUnit/ 2.0;
            double lng = (lngID) * lngTileUnit + lngTileUnit / 2.0 -lngTileUnit;
            LatLng loc = new LatLng(lat,lng);
            return loc;
        }
    }
}