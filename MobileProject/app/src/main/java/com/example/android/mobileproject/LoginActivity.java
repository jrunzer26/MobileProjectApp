package com.example.android.mobileproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements AsyncResponse{
    public static final String EDITTEXT_USERNAME = "USERNAME";
    public static final String EDITTEXT_PASSWORD = "PASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    /**
     * Logs the user into the applicaiton if the username and password is correct. Otherwise, shows error message.
     * @param view
     */
    public void login(View view) {
        JSONObject jsonObject = new JSONObject();
        String username = ((EditText) findViewById(R.id.edittext_login_username)).getText().toString();
        String password = ((EditText) findViewById(R.id.edittext_login_password)).getText().toString();
        try {
            jsonObject.put("username", username);
            jsonObject.put("password", password);
            System.out.println(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ServerControl sc = new ServerControl(this);
        sc.execute(getString(R.string.server) + "login", ServerControl.POST, jsonObject.toString());
    }

    /**
     * Switches to the register activity.
     * @param view
     */
    public void register(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        String username = ((EditText) findViewById(R.id.edittext_login_username)).getText().toString();
        String password = ((EditText) findViewById(R.id.edittext_login_password)).getText().toString();
        intent.putExtra(EDITTEXT_USERNAME, username);
        intent.putExtra(EDITTEXT_PASSWORD, password);
        startActivity(intent);

    }

    @Override
    public void processResult(String result) {
        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
    }
}
