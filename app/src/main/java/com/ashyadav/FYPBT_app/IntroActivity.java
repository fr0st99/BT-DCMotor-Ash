package com.ashyadav.FYPBT_app;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

/* Code Adapted from my 2nd year final project for ED5042 */

public class IntroActivity extends AppCompatActivity {


    EditText nameField;
    Button buttonStart;
    SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_activity);

        buttonStart = findViewById(R.id.startButton);
        nameField = findViewById(R.id.usernamefield);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                sharedPreferences = getSharedPreferences("username", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Value", nameField.getText().toString());
                editor.apply();

                if (TextUtils.isEmpty(nameField.getText())) {

                    nameField.setError("Name cannot be left blank!");

                } else {

                    startActivity(new Intent(IntroActivity.this, MainActivity.class));
                    finish();
                }
            }

        });

    }

}