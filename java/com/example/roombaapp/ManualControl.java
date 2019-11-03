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
import android.view.MotionEvent;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


public class ManualControl extends AppCompatActivity {
    private TCP sender;
    private TCP checker;
    private TextView status_text;
    private boolean stop = false;
    private String ip;
    private String port;
    private boolean dialog_enable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_manual);

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
                        intent = new Intent(ManualControl.this, BeepControl.class);
                        break;
                    case R.id.nav_2:
                        intent = new Intent(ManualControl.this, Setting.class);
                        break;
                    case R.id.nav_3:
                        intent = new Intent(ManualControl.this, Login.class);
                        break;
                    case R.id.nav_4:
                        intent = new Intent(ManualControl.this, ManualControl.class);
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

        // VdeoView
        VideoView videoView = (VideoView) findViewById(R.id.video_view);

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
            Intent intent = new Intent(ManualControl.this, Setting.class);
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

        // Buttons
        Button forward = findViewById(R.id.forward);
        Button backward = findViewById(R.id.backward);
        Button left = findViewById(R.id.left);
        Button right = findViewById(R.id.right);
        //forward
        forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (checker.status) {
                        sender.send("FORWARD");
                        Toast.makeText(ManualControl.this, "Roomba moving forward", Toast.LENGTH_SHORT).show();
                    } else
                        dialog("Connection failed!", "Please check parameters or server status.");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (checker.status) {
                        sender.send("STOP");
                    } else
                        dialog("Connection failed!", "Please check parameters or server status.");
                }
                return true;
            }
        });
        //back
        backward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (checker.status) {
                        sender.send("BACKWARD");
                        Toast.makeText(ManualControl.this, "Roomba moving backward", Toast.LENGTH_SHORT).show();
                    } else
                        dialog("Connection failed!", "Please check parameters or server status.");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (checker.status) {
                        sender.send("STOP");
                    } else
                        dialog("Connection failed!", "Please check parameters or server status.");
                }
                return true;
            }
        });
        //left
        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (checker.status) {
                        sender.send("LEFT");
                        Toast.makeText(ManualControl.this, "Roomba turning left", Toast.LENGTH_SHORT).show();
                    } else
                        dialog("Connection failed!", "Please check parameters or server status.");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (checker.status) {
                        sender.send("STOP");
                    } else
                        dialog("Connection failed!", "Please check parameters or server status.");
                }
                return true;
            }
        });
        //right
        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (checker.status) {
                        sender.send("RIGHT");
                        Toast.makeText(ManualControl.this, "Roomba turning right", Toast.LENGTH_SHORT).show();
                    } else
                        dialog("Connection failed!", "Please check parameters or server status.");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (checker.status) {
                        sender.send("STOP");
                    } else
                        dialog("Connection failed!", "Please check parameters or server status.");
                }
                return true;
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

    private void dialog(String title, String message) {
        if (dialog_enable) {
            dialog_enable = false;
            // AlertDialog
            AlertDialog.Builder dialog = new AlertDialog.Builder(ManualControl.this);
            dialog.setTitle(title);
            dialog.setMessage(message);
            dialog.setCancelable(true);
            dialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog_enable = true;
                }
            });
            dialog.show();
        }
    }

    private Handler handler = new Handler() {
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
