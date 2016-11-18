package com.mobileproject.game;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.mobileproject.game.GameMapUI.bdUnit;

/**
 * Created by 100520993 on 11/10/2016.
 */

class UpdateTilesAsync extends AsyncTask<Void, Void, String> implements AsyncResponse {
    private HashMap<Tile.TileID, Tile> tiles;
    private LatLng newLoc;
    private Context context;
    private ColorSet colors;
    private GoogleMap mMap;

    public UpdateTilesAsync(HashMap<Tile.TileID, Tile> tiles, LatLng newLoc, Context context, GoogleMap mMap, ColorSet colors) {
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

        Tile.TileID id = new Tile.TileID(Utilities.shifter(newLoc, 0, 0));
        TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, context);
        // draw tiles in cross from the user
        for (int i = 1; i < bdUnit; i++) {
            id = new Tile.TileID(Utilities.shifter(newLoc, i * GameMapUI.latTileUnit, 0));
            TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, context);
            id = new Tile.TileID(Utilities.shifter(newLoc, (-i) * GameMapUI.latTileUnit, 0));
            TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, context);
            id = new Tile.TileID(Utilities.shifter(newLoc, 0, i * GameMapUI.lngTileUnit));
            TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, context);
            id = new Tile.TileID(Utilities.shifter(newLoc, 0, (-i) * GameMapUI.lngTileUnit));
            TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, context);
        }
        // draw polygons in corners
        for (int i = 1; i < bdUnit; i++) {
            for (int j = 1; j < bdUnit; j++) {
                id = new Tile.TileID(Utilities.shifter(newLoc, i * GameMapUI.latTileUnit, j * GameMapUI.lngTileUnit));
                TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, context);
                id = new Tile.TileID(Utilities.shifter(newLoc, i * (-GameMapUI.latTileUnit), j * GameMapUI.lngTileUnit));
                TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, context);
                id = new Tile.TileID(Utilities.shifter(newLoc, i * (-GameMapUI.latTileUnit), j * (-GameMapUI.lngTileUnit)));
                TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, context);
                id = new Tile.TileID(Utilities.shifter(newLoc, i * (GameMapUI.latTileUnit), j * (-GameMapUI.lngTileUnit)));
                TileWebserviceUtility.getResources(id.getLatID(), id.getLngID(), LoginActivity.username, LoginActivity.password, this, context);
            }
        }
        return null;
    }

    @Override
    public void processResult(String result) {
        System.out.println("Result from server: " + result);
        System.out.println("Result from server: " + result);
        String[] serverResult = result.split(";");
        try {
            JSONObject jsonObject = new JSONObject(serverResult[1]);
            int tileLatID = jsonObject.getInt("tileLatID");
            int tileLngID = jsonObject.getInt("tileLngID");
            String tileUsername = jsonObject.getString("username");
            int soldiers = jsonObject.getInt("soldiers");
            int gold = jsonObject.getInt("gold");
            int food = jsonObject.getInt("food");
            System.out.println("tile username: " + tileUsername + " username " + LoginActivity.username);
            Tile t;
            if(tileUsername.equals("null")) {
                if (GameMapUI.currentLngID == tileLngID && GameMapUI.currentLatID == tileLatID) {
                    Utilities.updateTile(colors.green, tileLatID, tileLngID, LoginActivity.username ,mMap, tiles, soldiers, gold, food);
                    TileWebserviceUtility.captureTile(tileLatID, tileLngID, LoginActivity.username, LoginActivity.password, this, context);
                } else {
                    Utilities.updateTile(colors.gray, tileLatID, tileLngID, null, mMap, tiles, soldiers, gold, food);
                }
            }
            else if (tileUsername.equalsIgnoreCase(LoginActivity.username)) {
                Utilities.updateTile(colors.green, tileLatID, tileLngID, tileUsername, mMap, tiles, soldiers, gold, food);
            } else {
                Utilities.updateTile(colors.red, tileLatID, tileLngID, tileUsername, mMap, tiles, soldiers, gold, food);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}