package com.example.android.mobileproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity implements AsyncResponse {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Intent intent = getIntent();
        String username = intent.getStringExtra(LoginActivity.EDITTEXT_USERNAME);
        String password = intent.getStringExtra(LoginActivity.EDITTEXT_PASSWORD);
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
        String username = ((EditText) findViewById(R.id.edittext_register_username)).getText().toString();
        String password = ((EditText) findViewById(R.id.edittext_register_password)).getText().toString();
        String password2 = ((EditText) findViewById(R.id.edittext_register_password2)).getText().toString();
        String warning = "";
        if ((username == null) ||(username.length() < 5) || (username.length() > 16 )) {
            warning = getString(R.string.register_usernameshort);
        } else if (password == null || password2 == null) {
            warning = getString(R.string.register_passwordshort);
        } else if (!password.equals(password2)) {
            warning = getString(R.string.register_passowrdnotsame);
        } else if (password.length() < 8 || password.length() > 16) {
            warning = getString(R.string.register_passwordshort);
        }
        //if there are no warnings, register the user
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
            System.out.println(getString(R.string.server));
            sc.execute(getString(R.string.server) + "register", ServerControl.POST, jsonObject.toString());
        }
        //Toast.makeText(this, warning, Toast.LENGTH_LONG).show();
    }

    @Override
    public void processResult(String result) {
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }
}
