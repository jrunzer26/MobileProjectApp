package com.example.android.mobileproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class GameStubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_stub);
    }

    /**
     * Clears the shared preferences.
     * @param view
     */
    public void logout(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.SHAREDPREF_USERINFO, Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().commit();
        finish();
    }
}
