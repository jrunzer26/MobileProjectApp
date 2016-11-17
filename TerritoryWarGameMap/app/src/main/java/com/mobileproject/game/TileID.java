package com.mobileproject.game;

/**
 * Created by 100520993 on 11/10/2016.
 */

/**
 * The unique ID for a tile.
 */
public class TileID {

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


}