package com.mobileproject.game;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ScrollView;

public class MainMenuActivity extends AppCompatActivity {

    public static final String SHAREDPREF_USERINFO = "USERINFO";
    private MediaPlayer buttonClick;
    private MediaPlayer bgMusic;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        soundInit();
        scrollViewController();
    }

    // Initialization
    private void soundInit(){
        buttonClick = MediaPlayer.create(getApplicationContext(), R.raw.soho);
        bgMusic = MediaPlayer.create(getApplicationContext(),R.raw.jocsnight);
        if(!bgMusic.isPlaying()){
            bgMusic.start();
        }

    }

    // Listen to certain menu buttons clicking
    public void process(View view){
        // Play clicker sound
        new Thread(new Runnable() {
            @Override
            public void run() {
                buttonClick.start();
            }
        }).start();
        switch (view.getId()){
            case R.id.mainGameMenuBtn1:
                buildMapSystem();
                bgMusic.stop();
                break;
            case R.id.mainGameMenuBtn2:
                // TODO: Options menu
                bgMusic.stop();
                startActivity(new Intent(this,OptionsMenuActivity.class));
                break;

            case R.id.mainGameMenuBtn3:
                // TODO: Credits menu
                bgMusic.pause();
                setContentView(R.layout.credits_layout);
                break;
            case R.id.mainGameMenuBtn4:
                // TODO: login out
                logoutCurrentUser();
                break;
            case R.id.mainGameMenuBtn5:
                bgMusic.stop();
                this.finish();
                break;
            case R.id.creditsBackBtn:
                setContentView(R.layout.activity_main_menu);
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

    private int scrollViewController(){
        ScrollView view = (ScrollView)findViewById(R.id.svCredits);
        return 1;
    }

}
