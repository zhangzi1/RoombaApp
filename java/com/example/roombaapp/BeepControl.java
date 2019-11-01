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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;


public class BeepControl extends AppCompatActivity {
    private SenderThread sender;
    private SenderThread checker;
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

        // SharedPreferences
        SharedPreferences pref = getApplicationContext().getSharedPreferences("Setting", 0);

        // read from SP and connect
        ip = pref.getString("ip", null);
        port = pref.getString("port", null);
        Log.d("Setting", "IP: " + ip + "  Port: " + port);
        if (ip == null || port == null) {
            //ip = "100.64.9.83";
            //port = "8866";
            Intent intent = new Intent(BeepControl.this, Setting.class);
            startActivity(intent);
            finish();
        }
        sender = new SenderThread(ip, Integer.parseInt(port));
        sender.start();  // need a thread that constantly check the sender's status.
        checker = new SenderThread(ip, Integer.parseInt(port));
        checker.start();

        //
        status_text = findViewById(R.id.status_text);
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
                        sender = new SenderThread(ip, Integer.parseInt(port));
                        sender.start();
                        checker = new SenderThread(ip, Integer.parseInt(port));
                        checker.start();
                    }
                }
            }
        };
        check.start();

        // Button and onClick()
        Button beep = findViewById(R.id.beep);
        beep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checker.status) {
                    //sender.send("BEEP");
                    Thread send = new Thread() {
                        @Override
                        public void run() {
                            sender.send("BEEP");
                        }
                    };
                    send.start();
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
        /*
        Button reconnect = findViewById(R.id.reconnect);
        reconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sender.status)
                    sender.close();
                if (checker.status)
                    checker.close();
                sender = new SenderThread(ip, Integer.parseInt(port));
                sender.start();
                checker = new SenderThread(ip, Integer.parseInt(port));
                checker.start();
            }
        });
        */
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

class SenderThread extends Thread {
    private String ip;
    private int port;
    private Socket socket;
    private OutputStream outputStream;
    public boolean status = false;

    SenderThread(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(ip, port);
            outputStream = socket.getOutputStream();
            Log.d("SenderThread", "successful connection");
            status = true;
        } catch (UnknownHostException e) {
            //e.printStackTrace();
            Log.d("SenderThread", "error: unknown host");
            status = false;
        } catch (ConnectException e) {
            Log.d("SenderThread", "error: connection failed");
            status = false;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.d("SenderThread", "error: null pointer 1");
            status = false;
        }
    }

    void send(String content) {
        try {
            outputStream.write(content.getBytes());
            Log.d("SenderThread", "successful sending");
            status = true;
        } catch (SocketException e) {
            Log.d("SenderThread", "error: socket broken");
            status = false;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.d("SenderThread", "error: null stream");
            status = false;
        }
    }

    void close() {
        try {
            socket.close();
            outputStream.close();
            Log.d("SenderThread", "connection closed");
            status = false;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.d("SenderThread", "error: null stream");
            status = false;
        }
    }
}
