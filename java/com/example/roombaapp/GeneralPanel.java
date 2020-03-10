package com.example.roombaapp;

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

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class GeneralPanel extends AppCompatActivity {
    private TCP sender;
    private TCP checker;
    private TextView status_text;
    private TextView battery_text;
    private TextView mode_text;
    private boolean stop = false;
    private String ip;
    private String port;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.panel_general);

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
                boolean mode = ((MyApplication) getApplication()).getMF();
                if (!mode && (item.getItemId() == R.id.nav_1 || item.getItemId() == R.id.nav_4)) {
                    // AlertDialog
                    AlertDialog.Builder dialog = new AlertDialog.Builder(GeneralPanel.this);
                    dialog.setTitle("It's in Automatic Mode!");
                    dialog.setMessage("Please switch mode first.");
                    dialog.setCancelable(true);
                    dialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    });
                    dialog.show();

                } else {
                    switch (item.getItemId()) {
                        case R.id.nav_1:
                            intent = new Intent(GeneralPanel.this, BeepControl.class);
                            ((MyApplication) getApplication()).setSender(sender);
                            ((MyApplication) getApplication()).setChecker(checker);
                            break;
                        case R.id.nav_2:
                            intent = new Intent(GeneralPanel.this, Setting.class);
                            ((MyApplication) getApplication()).setSender(null);
                            ((MyApplication) getApplication()).setChecker(null);
                            break;
                        case R.id.nav_3:
                            intent = new Intent(GeneralPanel.this, Login.class);
                            ((MyApplication) getApplication()).setSender(null);
                            ((MyApplication) getApplication()).setChecker(null);
                            break;
                        case R.id.nav_4:
                            intent = new Intent(GeneralPanel.this, ManualControl.class);
                            ((MyApplication) getApplication()).setSender(sender);
                            ((MyApplication) getApplication()).setChecker(checker);
                            break;
                        case R.id.nav_5:
                            intent = new Intent(GeneralPanel.this, GeneralPanel.class);
                            ((MyApplication) getApplication()).setSender(sender);
                            ((MyApplication) getApplication()).setChecker(checker);
                            break;
                        default:
                    }
                    startActivity(intent);
                    finish();
                }
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        //*****************************************************************************************

        // SharedPreferences
        SharedPreferences pref = getApplicationContext().getSharedPreferences("Setting", 0);
        ip = pref.getString("ip", null);
        port = pref.getString("port", null);
        Log.d("Setting", "IP: " + ip + "  Port: " + port);

        // launch Setting?
        if (ip == null || port == null) {
            ip = "";
            port = "8866";
            Intent intent = new Intent(GeneralPanel.this, Setting.class);
            startActivity(intent);
            finish();
        }

        // start connection
        sender = ((MyApplication) getApplication()).getSender();
        checker = ((MyApplication) getApplication()).getChecker();
        if (sender == null) {
            sender = new TCP(ip, Integer.parseInt(port));
            sender.setSocket();
        }
        if (checker == null) {
            checker = new TCP(ip, Integer.parseInt(port));
            checker.setSocket();
        }

        // connectivity-checking thread
        Thread check = new Thread() {
            @Override
            public void run() {
                boolean previous_state = checker.status;
                String previous_battery = checker.buffer;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                checker.receive();
                while (!stop) {
                    // send beacon
                    try {
                        Thread.sleep(1000);
                        checker.send("beacon");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // check status
                    if (previous_state != checker.status) {
                        previous_state = !previous_state;
                        Message msg = new Message();
                        msg.obj = previous_state;
                        connection_handler.sendMessage(msg);
                    }
                    // check battery
                    if (!previous_battery.equals(checker.buffer)) {
                        previous_battery = checker.buffer;
                        Message msg = new Message();
                        msg.obj = previous_battery;
                        battery_handler.sendMessage(msg);
                    }
                    // reconnection
                    if (!previous_state && !stop) {
                        if (sender.status)
                            sender.close();
                        if (checker.status)
                            checker.close();
                        sender = new TCP(ip, Integer.parseInt(port));
                        sender.setSocket();
                        checker = new TCP(ip, Integer.parseInt(port));
                        checker.setSocket();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        checker.receive();
                    }
                }
            }
        };
        check.start();

        // TextView
        status_text = findViewById(R.id.status_text);
        if (checker.status)
            status_text.setText("Connected");
        else
            status_text.setText("Disconnected");
        battery_text = findViewById(R.id.battery_text);
        battery_text.setText(checker.buffer);
        mode_text = findViewById(R.id.mode_text);
        if (((MyApplication) getApplication()).getMF()) mode_text.setText("Manual");
        else mode_text.setText("Automatic");

        // Button "Switch Mode"
        Button beep = findViewById(R.id.switcher);
        beep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AlertDialog
                AlertDialog.Builder dialog = new AlertDialog.Builder(GeneralPanel.this);
                if (((MyApplication) getApplication()).getMF()) {  // was Manual
                    ((MyApplication) getApplication()).setMF(false);
                    mode_text.setText("Automatic");
                    // do something

                } else {  // was Automatic
                    ((MyApplication) getApplication()).setMF(true);
                    mode_text.setText("Manual");
                    // do something
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop = true;
    }

    private Handler connection_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if ((boolean) msg.obj) {
                status_text.setText("Connected");
            } else {
                status_text.setText("Disconnected");
            }
        }
    };

    private Handler battery_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            battery_text.setText((String) msg.obj);
        }
    };

}
