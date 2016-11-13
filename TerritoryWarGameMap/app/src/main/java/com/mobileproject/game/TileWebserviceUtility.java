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
        System.out.println("collecting resources");
        ServerControl sc = new ServerControl(callback);
        sc.execute(context.getString(R.string.server) + "users/collect",
                ServerControl.POST, jsonObject.toString(), auth.toString(), "2");
    }

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
        System.out.println("collecting resources");
        ServerControl sc = new ServerControl(callback);
        sc.execute(context.getString(R.string.server) + "users/",
                ServerControl.POST, jsonObject.toString(), auth.toString(), "3");
    }
}
