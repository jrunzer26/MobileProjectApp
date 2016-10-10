package com.example.android.mobileproject;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
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
    private AsyncResponse asyncResponse;

    public ServerControl(AsyncResponse asyncResponse) {
        this.asyncResponse = asyncResponse;
    }
    /**
     *
     * @param params url, method, body
     * @return
     */
    protected String doInBackground(String... params) {
        String content = "";
        try {
            URL url = new URL(params[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            System.out.println(params[1]);
            if (params[1].equals(GET)) {
                InputStream in = connection.getInputStream();
                content = readStringInput(in);
                in.close();
            } else {
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.connect();

                OutputStream out = connection.getOutputStream();
                out.write(params[2].getBytes("UTF-8"));
                out.flush();
                out.close();
                InputStream in;
                if (connection.getResponseCode() == 400) {
                    in = new BufferedInputStream(connection.getErrorStream());
                } else {
                    in = new BufferedInputStream(connection.getInputStream());
                }
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
    private String readStringInput(InputStream in) throws IOException {
        int i;
        String result = "";
        while ((i = in.read()) != -1) {
            result += (char) i;
        }
        return result;
    }

    protected void onPostExecute(String result) {
        System.out.println("Result: " + result);
        asyncResponse.processResult(result);
    }
}