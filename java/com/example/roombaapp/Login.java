package com.example.roombaapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // EditText
        final EditText username = findViewById(R.id.username_blank);
        final EditText password = findViewById(R.id.password_blank);
        password.setTransformationMethod(PasswordTransformationMethod.getInstance());  // password: ***

        // Button and onClick()
        Button login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Print info
                String inputUsername = username.getText().toString();
                String inputPassword = password.getText().toString();
                Log.d("Login", "Username: " + inputUsername + "  Password: " + inputPassword);
                // launch main activity
                Intent intent = new Intent(Login.this, Searching.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
