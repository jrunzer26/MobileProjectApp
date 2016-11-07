package com.mobileproject.game;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainMenuActivity extends AppCompatActivity {

    public static final String SHAREDPREF_USERINFO = "USERINFO";
    private  MediaPlayer buttonClick;
    private  MediaPlayer bgMusic;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        init();
    }
    // Initialization
    private void init(){
        buttonClick = MediaPlayer.create(getApplicationContext(), R.raw.soho);
        bgMusic = MediaPlayer.create(getApplicationContext(), R.raw.bg);
        bgMusic.start();
    }

    // Listen to certain menu buttons clicking
    public void process(View view){

        // Play clicker sound
        buttonClick.start();

        switch (view.getId()){
            case R.id.mainGameMenuBtn1:
                buildMapSystem();
                break;
            case R.id.mainGameMenuBtn2:
                // TODO: Options menu
                break;
            //case R.id.mainGameMenuBtn3:
                // TODO: About menu
                //ImageButton btn = (ImageButton) findViewById(R.id.mainGameMenuBtn3);
                //break;
            case R.id.mainGameMenuBtn4:
                // TODO: Credits menu
                break;
            case R.id.mainGameMenuBtn5:
                // TODO: login out
                logoutCurrentUser();
                break;
            case R.id.mainGameMenuBtn6:
                bgMusic.stop();
                this.finish();
                this.onStop();
                break;
            default:
                break;
        }
    }

    /*****************************
     *   All menus operations:
     *
     * ***************************/
    private int logoutCurrentUser(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHAREDPREF_USERINFO, Context.MODE_PRIVATE);

        sharedPreferences.edit().clear().commit();

        intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
        return 1;
    }

    private int buildMapSystem(){
        intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("geo:43.944838,-78.896322"));
        intent.setClass(this, GameMapUI.class);
        startActivity(intent);
        return 1;
    }

}
