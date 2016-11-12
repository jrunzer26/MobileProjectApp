package com.mobileproject.game;

import com.google.android.gms.maps.model.Polygon;

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

    Tile(TileID id, String username, Polygon polygon) {
        this.id = id;
        this.username = username;
        this.polygon = polygon;
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
}
