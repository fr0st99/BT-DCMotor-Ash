package com.ashyadav.FYPBT_app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/* Code Adapted from my 2nd year final project for ED5042 */

public class IntroActivity extends AppCompatActivity {


    EditText nameField;
    Button buttonStart;
    Button aboutButton;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_activity);

        buttonStart = findViewById(R.id.startButton);
        nameField = findViewById(R.id.usernamefield);
        aboutButton = findViewById(R.id.aboutButton);

        /* About app button */
        aboutButton.setOnClickListener(view -> startActivity(new Intent(IntroActivity.this, AboutApp.class)));



        buttonStart.setOnClickListener(v -> {



            sharedPreferences = getSharedPreferences("username", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("Value", nameField.getText().toString());
            editor.apply();

            if (TextUtils.isEmpty(nameField.getText())) {

                nameField.setError("Name cannot be left blank!");

            } else {

                showIntroMessage();

            }
        });



    }

    public void showIntroMessage() {
        
        new AlertDialog.Builder(IntroActivity.this)
                .setTitle("Welcome to DC Motor Genie")
                .setMessage("Instructions: \n" +
                        "1) Connect to Bluetooth \n" +
                        "\n"+
                        "2) Turn on DC motor \n" +
                        "\n"+
                        "3) All functions will become available \n" +
                        "\n" +
                        "4) Pre-set speeds are hardcoded and proof of concept only (no active feedback loop) \n" +
                        "\n" +
                        "App developed by Ashutosh Yadav Student ID: 18249094 \n")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startActivity(new Intent(IntroActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .create().show();



    }



}