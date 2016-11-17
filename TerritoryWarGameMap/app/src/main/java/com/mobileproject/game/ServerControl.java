package com.mobileproject.game;

import android.os.AsyncTask;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by 100520993 on 10/8/2016.
 */
public class ServerControl extends AsyncTask<String, Void, String> {
    public static final String GET = "GET";
    public static final String POST = "POST";
    private String type = null;
    private AsyncResponse asyncResponse;

    public ServerControl(AsyncResponse asyncResponse) {
        this.asyncResponse = asyncResponse;
    }
    /**
     *
     * @param params url, method, body, auth, type - the type to distinguish processing the result
     * @return response - the json string server response
     */
    protected String doInBackground(String... params) {
        String content = "";
        try {
            URL url = new URL(params[0]);
            if (params.length > 4)
                type = params[4];
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (params.length > 3) {
                try {
                    JSONObject jsonObject = new JSONObject(params[3]);
                    final String convert = jsonObject.get("username")+":"+jsonObject.get("password");
                    System.out.println(convert);
                    connection.setRequestProperty("Authorization", "Basic " +
                            Base64.encodeToString(convert.getBytes(), Base64.NO_WRAP));
                } catch (JSONException e) {
                    return "JSON ERROR";
                }
            }
            if (params[1].equals(GET)) {
                // get the content from the server
                InputStream in = connection.getInputStream();
                content = readStringInput(in);
                in.close();
            } else {
                // post content to the server
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.connect();

                OutputStream out = connection.getOutputStream();
                out.write(params[2].getBytes("UTF-8"));
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
                in.close();
            }
            connection.disconnect();
        } catch (IOException e) {
            System.out.println("IO problem");
            e.printStackTrace();
        }
        return content;
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
     * Use the callback to send data to the other activity.
     * @param result the json result
     */
    protected void onPostExecute(String result) {
        if (type != null)
            asyncResponse.processResult(type + ";" + result);
        else
            asyncResponse.processResult(result);
    }
}