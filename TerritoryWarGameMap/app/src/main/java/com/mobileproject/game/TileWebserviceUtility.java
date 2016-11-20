package com.mobileproject.game;

import android.content.Context;
import android.content.res.Resources;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 100520993 on 11/8/2016.
 */

/**
 * Used to capture a tile or get its resources/create it.
 */
public class TileWebserviceUtility {

    /**
     * Captures a tile on the server.
     */
    public static void captureTile(int tileLatID, int tileLngID, String username, String password, AsyncResponse callback, Context context) {
        JSONObject jsonObject = new JSONObject();
        JSONObject auth = new JSONObject();
        try {
            auth.put("username", username);
            auth.put("password", password);
            jsonObject.put("username", username);
            jsonObject.put("tileLatID", tileLatID);
            jsonObject.put("tileLngID", tileLngID);
            System.out.println(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ServerControl capture = new ServerControl(callback);
        capture.execute(context.getString(R.string.server) + "tiles/capture",
                ServerControl.POST, jsonObject.toString(), auth.toString(), "1");
    }

    /**
     * Gets the Resources of the tile.
     */
    public static void getResources(int tileLatID, int tileLngID, String username, String password, AsyncResponse callback, Context context) {
        JSONObject jsonObject = new JSONObject();
        JSONObject auth = new JSONObject();
        try {
            auth.put("username", username);
            auth.put("password", password);
            jsonObject.put("tileLatID", tileLatID);
            jsonObject.put("tileLngID", tileLngID);
            System.out.println(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ServerControl sc = new ServerControl(callback);
        sc.execute(context.getString(R.string.server) + "tiles/resources",
                ServerControl.POST, jsonObject.toString(), auth.toString(), "1");
    }

    /** Collects the resources for the user **/
    public static void collectResources(String username, String password, AsyncResponse callback, Context context) {
        JSONObject jsonObject = new JSONObject();
        JSONObject auth = new JSONObject();
        try {
            auth.put("username", username);
            auth.put("password", password);
            jsonObject.put("username", username);
            System.out.println(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ServerControl sc = new ServerControl(callback);
        sc.execute(context.getString(R.string.server) + "users/collect",
                ServerControl.POST, jsonObject.toString(), auth.toString(), "2");
    }

    /** Gets the user's resources from the server **/
    public static void getUser(String username, String password, AsyncResponse callback, Context context) {
        JSONObject jsonObject = new JSONObject();
        JSONObject auth = new JSONObject();
        try {
            auth.put("username", username);
            auth.put("password", password);
            jsonObject.put("username", username);
            System.out.println(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ServerControl sc = new ServerControl(callback);
        sc.execute(context.getString(R.string.server) + "users/",
                ServerControl.POST, jsonObject.toString(), auth.toString(), "3");
    }

    public static void buySoldiers(String username, String password, int tileLatID, int tileLngID, int soldiers, AsyncResponse callback, Context context) {
        JSONObject jsonObject = new JSONObject();
        JSONObject auth = new JSONObject();
        try {
            auth.put("username", username);
            auth.put("password", password);
            jsonObject.put("username", username);
            jsonObject.put("tileLatID", tileLatID);
            jsonObject.put("tileLngID", tileLngID);
            jsonObject.put("soldiers", soldiers);
            System.out.println(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ServerControl sc = new ServerControl(callback);
        // fix spelling on server
        sc.execute(context.getString(R.string.server) + "tiles/purchace-soldiers",
                ServerControl.POST, jsonObject.toString(), auth.toString(), "4");
    }

    /**
     * Battles two tiles on the server.
     * @param username the username
     * @param password the password
     * @param tile1 the tile that the user owns
     * @param tile2 the tile that the user would like to attack
     */
    public static void battle(String username, String password, Tile tile1, Tile tile2, AsyncResponse callback, Context context) {
        JSONObject jsonObject = new JSONObject();
        JSONObject auth = new JSONObject();
        try {
            auth.put("username", username);
            auth.put("password", password);
            jsonObject.put("username", username);
            Tile.TileID tile1ID = tile1.getTileID();
            Tile.TileID tile2ID = tile2.getTileID();
            jsonObject.put("tileLatID1",tile1ID.getLatID());
            jsonObject.put("tileLngID1", tile1ID.getLngID());
            jsonObject.put("tileLatID2",tile2ID.getLatID());
            jsonObject.put("tileLngID2", tile2ID.getLngID());
            System.out.println(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ServerControl sc = new ServerControl(callback);
        // fix spelling on server
        sc.execute(context.getString(R.string.server) + "tiles/battle",
                ServerControl.POST, jsonObject.toString(), auth.toString(), "5");
        GameMapUI.mapLock = true;
    }
}
