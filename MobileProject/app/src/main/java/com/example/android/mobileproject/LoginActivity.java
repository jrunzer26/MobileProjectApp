package com.example.android.mobileproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends AppCompatActivity {

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
        // TODO: 10/6/2016
        startActivity(new Intent(this, MainActivity.class));
    }

    /**
     * Switches to the register activity.
     * @param view
     */
    public void register(View view) {
        // TODO: 10/6/2016
    }


}
