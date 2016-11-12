package com.mobileproject.game;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
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
    private LatLng newLoc;
    private Context context;
    private ColorSet colors;
    private GoogleMap mMap;

    public UpdateTilesAsync(HashMap<TileID, Tile> tiles, LatLng newLoc, Context context, GoogleMap mMap, ColorSet colors) {
        this.tiles = tiles;
        this.newLoc = newLoc;
        this.context = context;
        this.colors = colors;
        this.mMap = mMap;
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
        System.out.println("Result from server: " + result);
        try {
            JSONObject jsonObject = new JSONObject(result);
            String tileLatID = jsonObject.getString("tileLatID");
            String tileLngID = jsonObject.getString("tileLngID");
            String tileUsername = jsonObject.getString("username");
            LocationID latLng = new LocationID(Integer.parseInt(tileLatID), Integer.parseInt(tileLngID));
            System.out.println("tile username: " + tileUsername + " username " + LoginActivity.username);
            Tile t;
            if(tileUsername.equals("null")) {
                if (GameMapUI.currentLngID == Integer.parseInt(tileLngID) && GameMapUI.currentLatID == Integer.parseInt(tileLatID))
                    //updateTile(colors.gray, tileLatID, tileLngID, tileUsername);
                    TileWebserviceUtility.captureTile(Integer.parseInt(tileLatID), Integer.parseInt(tileLngID), LoginActivity.username, LoginActivity.password, this, context);
                Utilities.updateTile(colors.gray, tileLatID, tileLngID, null,mMap, tiles);
            }
            else if (tileUsername.equalsIgnoreCase(LoginActivity.username)) {
                Utilities.updateTile(colors.green, tileLatID, tileLngID, tileUsername, mMap, tiles);
            } else {
                Utilities.updateTile(colors.red, tileLatID, tileLngID, tileUsername, mMap, tiles);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}