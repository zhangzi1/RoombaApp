package com.example.roombaapp;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class BeepControl extends AppCompatActivity {
    private TCP sender;
    private TCP checker;
    private TextView status_text;
    private boolean stop = false;
    private String ip;
    private String port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_beep);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //DrawerLayout
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //NavigationView
        NavigationView navView = findViewById(R.id.nav_view);
        View headerLayout = navView.inflateHeaderView(R.layout.nav_header);
        TextView nav_username = headerLayout.findViewById(R.id.username);
        TextView nav_email = headerLayout.findViewById(R.id.mail);
        ImageView nav_avatar = headerLayout.findViewById(R.id.icon_image);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Intent intent = null;
                switch (item.getItemId()) {
                    case R.id.nav_1:
                        intent = new Intent(BeepControl.this, BeepControl.class);
                        break;
                    case R.id.nav_2:
                        intent = new Intent(BeepControl.this, Setting.class);
                        break;
                    case R.id.nav_3:
                        intent = new Intent(BeepControl.this, Login.class);
                        break;
                    default:
                }
                startActivity(intent);
                finish();
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        /* ************************************************************************************** */

        // TextView
        status_text = findViewById(R.id.status_text);

        // SharedPreferences
        SharedPreferences pref = getApplicationContext().getSharedPreferences("Setting", 0);
        ip = pref.getString("ip", null);
        port = pref.getString("port", null);
        Log.d("Setting", "IP: " + ip + "  Port: " + port);

        // launch Setting?
        if (ip == null || port == null) {
            ip = "";
            port = "8866";
            Intent intent = new Intent(BeepControl.this, Setting.class);
            startActivity(intent);
            finish();
        }

        // start connection
        sender = new TCP(ip, Integer.parseInt(port));
        sender.setSocket();
        checker = new TCP(ip, Integer.parseInt(port));
        checker.setSocket();

        // connectivity-checking thread
        Thread check = new Thread() {
            @Override
            public void run() {
                boolean previous_state = checker.status;
                while (!stop) {
                    try {
                        Thread.sleep(1000);
                        checker.send("beacon");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (previous_state != checker.status) {
                        previous_state = !previous_state;
                        Message msg = new Message();
                        msg.obj = previous_state;
                        handler.sendMessage(msg);
                    }
                    if (!previous_state && !stop) {
                        // reconnect
                        if (sender.status)
                            sender.close();
                        if (checker.status)
                            checker.close();
                        sender = new TCP(ip, Integer.parseInt(port));
                        sender.setSocket();
                        checker = new TCP(ip, Integer.parseInt(port));
                        checker.setSocket();
                    }
                }
            }
        };
        check.start();

        // Button "BEEP"
        Button beep = findViewById(R.id.beep);
        beep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checker.status) {
                    sender.send("BEEP");
                    Toast.makeText(BeepControl.this, "Roomba beeping", Toast.LENGTH_SHORT).show();
                } else {
                    // AlertDialog
                    AlertDialog.Builder dialog = new AlertDialog.Builder(BeepControl.this);
                    dialog.setTitle("Connection failed!");
                    dialog.setMessage("Please check parameters or server status.");
                    dialog.setCancelable(true);
                    dialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    });
                    dialog.show();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sender.close();
        checker.close();
        stop = true;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if ((boolean) msg.obj) {
                status_text.setText("Connected");
            } else {
                status_text.setText("Disconnected");
            }
        }
    };

}
