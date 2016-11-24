package com.mobileproject.game;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity implements AsyncResponse {

    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Intent intent = getIntent();
        // get the username and passowrd from the login activity if possible
        username = intent.getStringExtra(LoginActivity.EDITTEXT_USERNAME);
        password = intent.getStringExtra(LoginActivity.EDITTEXT_PASSWORD);
        if (username != null) {
            ((EditText) findViewById(R.id.edittext_register_username)).setText(username);
        }
        if (password != null) {
            ((EditText) findViewById(R.id.edittext_register_password)).setText(password);
        }
    }

    /**
     * Registers the user using the webservice.
     * @param view
     */
    public void register(View view) {
        Utilities.hideKeyboard(view, this);
        hideWarning();
        username = ((EditText) findViewById(R.id.edittext_register_username)).getText().toString();
        password = ((EditText) findViewById(R.id.edittext_register_password)).getText().toString();
        String password2 = ((EditText) findViewById(R.id.edittext_register_password2)).getText().toString();
        String warning = "";
        // check if the username and password is okay.
        if ((username == null) ||(username.length() < 5) || (username.length() > 16 )) {
            warning = getString(R.string.register_usernameshort);
        } else if (password == null || password2 == null) {
            warning = getString(R.string.register_passwordshort);
        } else if (!password.equals(password2)) {
            warning = getString(R.string.register_passowrdnotsame);
        } else if (password.length() < 8 || password.length() > 16) {
            warning = getString(R.string.register_passwordshort);
        }
        System.out.println("warning: "+warning);
        // if there are no warnings, register the user
        if (warning.equals("")){
            System.out.println("server control");
            ServerControl sc = new ServerControl(this);
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("username", username);
                jsonObject.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sc.execute(getString(R.string.server) + "register", ServerControl.POST, jsonObject.toString());
        } else {
            // show the warning
            ((TextView) findViewById(R.id.textview_register_warning)).setText(warning);
        }

    }

    /**
     * Sets the shared preferences of the register activity if the register was successful.
     * Finishes the activity or shows a warning from the server response.
     * @param result
     */
    @Override
    public void processResult(String result) {
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.SHAREDPREF_USERINFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LoginActivity.USERNAME, username);
        editor.putString(LoginActivity.PASSWORD, password);
        editor.commit();
        if (result.equals("OK"))
            finish();
        else
            ((TextView) findViewById(R.id.textview_register_warning)).setText(Utilities.getErr(result));
    }

    /**
     * Hides the warning on the screen.
     */
    private void hideWarning() {
        ((TextView) findViewById(R.id.textview_register_warning)).setText("");
    }
}
