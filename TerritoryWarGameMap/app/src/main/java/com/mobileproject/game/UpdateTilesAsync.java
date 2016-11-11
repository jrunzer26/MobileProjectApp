package com.mobileproject.game;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.mobileproject.game.GameMapUI.bdUnit;

/**
 * Created by 100520993 on 11/10/2016.
 */

class UpdateTilesAsync extends AsyncTask<Void, Void, String> implements AsyncResponse {
    private HashMap<TileID, Tile> tiles;
    private AsyncResponse callback;
    private LatLng newLoc;
    private Context context;

    public UpdateTilesAsync(AsyncResponse callback, HashMap<TileID, Tile> tiles, LatLng newLoc, Context context) {
        this.callback = callback;
        this.tiles = tiles;
        this.newLoc = newLoc;
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params) {
        //DrawPolygonDemo(mMap, new LatLng(currentLatID * latTileUnit - latTileUnit / 2, currentLngID * lngTileUnit - lngTileUnit / 2));
        //draw tile user is in
        LocationID id = Utilities.LocationToID(Utilities.shifter(newLoc, 0, 0));
        TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, context);
        // draw tiles in cross from the user
        for (int i = 1; i < bdUnit; i++) {
            id = Utilities.LocationToID(Utilities.shifter(newLoc, i * GameMapUI.latTileUnit, 0));
            TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, context);
            id = Utilities.LocationToID(Utilities.shifter(newLoc, (-i) * GameMapUI.latTileUnit, 0));
            TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, context);
            id = Utilities.LocationToID(Utilities.shifter(newLoc, 0, i * GameMapUI.lngTileUnit));
            TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, context);
            id = Utilities.LocationToID(Utilities.shifter(newLoc, 0, (-i) * GameMapUI.lngTileUnit));
            TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, context);
        }
        // draw polygons in corners
        for (int i = 1; i < bdUnit; i++) {
            for (int j = 1; j < bdUnit; j++) {
                id = Utilities.LocationToID(Utilities.shifter(newLoc, i * GameMapUI.latTileUnit, j * GameMapUI.lngTileUnit));
                TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, context);
                id = Utilities.LocationToID(Utilities.shifter(newLoc, i * (-GameMapUI.latTileUnit), j * GameMapUI.lngTileUnit));
                TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, context);
                id = Utilities.LocationToID(Utilities.shifter(newLoc, i * (-GameMapUI.latTileUnit), j * (-GameMapUI.lngTileUnit)));
                TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, context);
                id = Utilities.LocationToID(Utilities.shifter(newLoc, i * (GameMapUI.latTileUnit), j * (-GameMapUI.lngTileUnit)));
                TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, context);
            }
        }
        //capture in the users location
        //TileWebserviceUtility.captureTile(currentLatID, currentLngID,LoginActivity.username, LoginActivity.password, this, context);
        return null;
    }

    @Override
    public void processResult(String result) {

    }
}