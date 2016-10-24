package com.example.android.mobileproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity implements AsyncResponse{
    public static final String EDITTEXT_USERNAME = "USERNAME";
    public static final String EDITTEXT_PASSWORD = "PASSWORD";
    public static final String SHAREDPREF_USERINFO = "USERINFO";
    public static final String USERNAME = "USERNAME";
    public static final String PASSWORD = "PASSWORD";
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // login to the game if the user has already logged in or has registered
        SharedPreferences sharedPreferences = getSharedPreferences(SHAREDPREF_USERINFO, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(USERNAME) && sharedPreferences.contains(PASSWORD)) {
            username = sharedPreferences.getString(USERNAME, "");
            password = sharedPreferences.getString(PASSWORD, "");
            serverLogin(username, password);
        }
        // reset all fields
        ((EditText) findViewById(R.id.edittext_login_username)).setText("");
        ((EditText) findViewById(R.id.edittext_login_password)).setText("");
        ((TextView) findViewById(R.id.textview_login_warning)).setText("");
    }

    /**
     * Logs the user into the applicaiton if the username and password is correct. Otherwise, shows error message.
     * @param view
     */
    public void login(View view) {
        Util.hideKeyboard(view, this);
        // get the username and password from the TextViews
        username = ((EditText) findViewById(R.id.edittext_login_username)).getText().toString();
        password = ((EditText) findViewById(R.id.edittext_login_password)).getText().toString();
        if (username.length() >= 1 && password.length() >= 1)
            serverLogin(username, password);
        else
            ((TextView) findViewById(R.id.textview_login_warning)).setText(getString(R.string.login_nocredentials));
    }

    /**
     * Log into the server.
     * @param username the username
     * @param password the password
     */
    private void serverLogin(String username, String password) {
        // try to build a json onject with the username and passowrd
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("password", password);
            System.out.println(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // login to the server
        ServerControl sc = new ServerControl(this);
        sc.execute(getString(R.string.server) + "login", ServerControl.POST, jsonObject.toString(), jsonObject.toString());
    }
    /**
     * Switches to the register activity.
     * @param view
     */
    public void register(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        // pass over the username and password the register activity
        String username = ((EditText) findViewById(R.id.edittext_login_username)).getText().toString();
        String password = ((EditText) findViewById(R.id.edittext_login_password)).getText().toString();
        intent.putExtra(EDITTEXT_USERNAME, username);
        intent.putExtra(EDITTEXT_PASSWORD, password);
        startActivity(intent);
    }

    /**
     * Checks if the user has the correct credentials, then proceeds to the game activity if the login was accepted.
     * @param result - the JSON from the server
     */
    @Override
    public void processResult(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            String loggedIn = jsonObject.getString("logged in: ");
            if (loggedIn.equals("true")) {
                // store the credentials for the application fi the credentials are correct
                SharedPreferences sharedPreferences = getSharedPreferences(SHAREDPREF_USERINFO, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(USERNAME, username);
                editor.putString(PASSWORD, password);
                editor.commit();
                // start the game activity
                Intent intent = new Intent (this, GameActivity.class);
                startActivity(intent);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            ((TextView) findViewById(R.id.textview_login_warning)).setText(Util.getErr(result));
        }
    }
}
