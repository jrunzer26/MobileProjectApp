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
    private int soldiers;
    private int gold;
    private int food;
    private boolean selected;

    Tile(TileID id, String username, Polygon polygon, int soldiers, int gold, int food) {
        this.id = id;
        this.username = username;
        this.polygon = polygon;
        this.soldiers = soldiers;
        this.gold = gold;
        this.food = food;
        this.setSelected(false);
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

    public boolean isSelected(){
        return selected;
    }

}