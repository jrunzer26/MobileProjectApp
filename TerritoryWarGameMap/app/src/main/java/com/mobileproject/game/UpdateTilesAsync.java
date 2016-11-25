package com.mobileproject.game;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import static com.mobileproject.game.GameMapUI.bdUnit;
/**
 * Created by 100520993 on 11/10/2016.
 */

/**
 * A more responsive solution to getting the tiles in the background.
 */
class UpdateTilesAsync extends Thread implements AsyncResponse {
    private HashMap<Tile.TileID, Tile> tiles;
    private LatLng newLoc;
    private Context context;
    private ColorSet colors;
    private GoogleMap mMap;

    UpdateTilesAsync(HashMap<Tile.TileID, Tile> tiles, LatLng newLoc, Context context, GoogleMap mMap, ColorSet colors) {
        this.tiles = tiles;
        this.newLoc = newLoc;
        this.context = context;
        this.colors = colors;
        this.mMap = mMap;
    }

    /**
     * Queries the Webservice for the tiles on the map.
     */
    public void run() {
        //draw tile user is in
        Tile.TileID id = new Tile.TileID(Utilities.shifter(newLoc, 0, 0));
        post(id.getLatID(), id.getLngID());
        // draw tiles in cross from the user
        for (int i = 1; i < bdUnit; i++) {
            id = new Tile.TileID(Utilities.shifter(newLoc, i * GameMapUI.latTileUnit, 0));
            post(id.getLatID(), id.getLngID());
            id = new Tile.TileID(Utilities.shifter(newLoc, (-i) * GameMapUI.latTileUnit, 0));
            post(id.getLatID(), id.getLngID());
            id = new Tile.TileID(Utilities.shifter(newLoc, 0, i * GameMapUI.lngTileUnit));
            post(id.getLatID(), id.getLngID());
            id = new Tile.TileID(Utilities.shifter(newLoc, 0, (-i) * GameMapUI.lngTileUnit));
            post(id.getLatID(), id.getLngID());
        }
        // draw polygons in corners
        for (int i = 1; i < bdUnit; i++) {
            for (int j = 1; j < bdUnit; j++) {
                id = new Tile.TileID(Utilities.shifter(newLoc, i * GameMapUI.latTileUnit, j * GameMapUI.lngTileUnit));
                post(id.getLatID(), id.getLngID());
                id = new Tile.TileID(Utilities.shifter(newLoc, i * (-GameMapUI.latTileUnit), j * GameMapUI.lngTileUnit));
                post(id.getLatID(), id.getLngID());
                id = new Tile.TileID(Utilities.shifter(newLoc, i * (-GameMapUI.latTileUnit), j * (-GameMapUI.lngTileUnit)));
                post(id.getLatID(), id.getLngID());
                id = new Tile.TileID(Utilities.shifter(newLoc, i * (GameMapUI.latTileUnit), j * (-GameMapUI.lngTileUnit)));
                post(id.getLatID(), id.getLngID());
            }
        }
    }

    /**
     * Get the resources of a tile from the server
     * @param tileLatID the lat id
     * @param tileLngID the lng id
     */
    private void post(int tileLatID, int tileLngID) {
        String content = "";
        try {
            URL url = new URL(context.getResources().getString(R.string.server) + "tiles/resources");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            JSONObject body = new JSONObject();
            JSONObject auth = new JSONObject();
            try {
                auth.put("username", LoginActivity.username);
                auth.put("password", LoginActivity.password);
                body.put("tileLatID", tileLatID);
                body.put("tileLngID", tileLngID);
                System.out.println(body.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
                try {

                    final String convert = auth.get("username").toString() + ":" + auth.get("password").toString();
                    System.out.println(convert);
                    connection.setRequestProperty("Authorization", "Basic " +
                            Base64.encodeToString(convert.getBytes(), Base64.NO_WRAP));
                } catch (JSONException e) {
                    System.out.println("JSON ERROR");
                }
            // post content to the server
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.connect();

            OutputStream out = connection.getOutputStream();
            out.write(body.toString().getBytes("UTF-8"));
            out.flush();
            out.close();
            InputStream in;
            // get the stream of data from the result of the post.
            if (connection.getResponseCode() == 400 || connection.getResponseCode() == 409
                    || connection.getResponseCode() == 401) {
                in = new BufferedInputStream(connection.getErrorStream());
            } else {
                in = new BufferedInputStream(connection.getInputStream());
            }
            // read the json response
            content = readStringInput(in);
            processResult(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the input from an input stream.
     * @param in the input stream to read from
     * @return the content from the stream
     * @throws IOException
     */
    private String readStringInput(InputStream in) throws IOException {
        int i;
        String result = "";
        while ((i = in.read()) != -1) {
            result += (char) i;
        }
        return result;
    }


    /**
     * Updates the map with the tile on the main UI thread.
     * @param result the Json Tile
     */
    public void processResult(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            final int tileLatID = jsonObject.getInt("tileLatID");
            final int tileLngID = jsonObject.getInt("tileLngID");
            final String tileUsername = jsonObject.getString("username");
            final int soldiers = jsonObject.getInt("soldiers");
            final int gold = jsonObject.getInt("gold");
            final int food = jsonObject.getInt("food");
            System.out.println("tile username: " + tileUsername + " username " + LoginActivity.username);
            Tile t;
            // post to the main UI thread to update the tile
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(tileUsername.equals("null")) {
                        Utilities.updateTile(colors.gray, tileLatID, tileLngID, null, mMap, tiles, soldiers, gold, food);
                    } else if (tileUsername.equalsIgnoreCase(LoginActivity.username)) {
                        Utilities.updateTile(colors.green, tileLatID, tileLngID, tileUsername, mMap, tiles, soldiers, gold, food);
                    } else {
                        Utilities.updateTile(colors.red, tileLatID, tileLngID, tileUsername, mMap, tiles, soldiers, gold, food);
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}