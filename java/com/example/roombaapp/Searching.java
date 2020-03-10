package com.example.roombaapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

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

        WifiManager.MulticastLock lock;
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        lock = manager.createMulticastLock("udpservice");

        //UDP
        final UDP udp = new UDP();
        Thread bdct = new Thread() {
            @Override
            public void run() {
                while (!stop) {
                    udp.send("0.0.0.0", 8865, "Are you Roomba?");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        bdct.start();
    }
}
