package com.example.roombaapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Searching extends AppCompatActivity {
    private boolean stop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searching);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //UDP
        final UDP receiver = new UDP();
        receiver.receive(8865);

        final UDP sender = new UDP();
        Thread bdct = new Thread() {
            @Override
            public void run() {
                while (!stop) {
                    if (receiver.buffer != null) {
                        stop = true;
                        receiver.close();
                        // SharedPreferences
                        SharedPreferences pref = getApplicationContext().getSharedPreferences("Setting", 0);
                        final SharedPreferences.Editor editor = pref.edit();
                        editor.putString("ip", receiver.ip);
                        editor.putString("port", "8866");
                        editor.commit();
                        Intent intent = new Intent(Searching.this, GeneralPanel.class);
                        startActivity(intent);
                        finish();
                    }
                    sender.send("255.255.255.255", 8864, "Are you Roomba?");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        bdct.start();

        Button skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Searching.this, GeneralPanel.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
