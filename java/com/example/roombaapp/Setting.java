package com.example.roombaapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class Setting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

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
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Setting.this);
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

                } else if (mode && item.getItemId() == R.id.nav_6) {
                    // AlertDialog
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Setting.this);
                    dialog.setTitle("It's in Manual Mode!");
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
                            intent = new Intent(Setting.this, BeepControl.class);
                            break;
                        case R.id.nav_2:
                            intent = new Intent(Setting.this, Setting.class);
                            break;
                        case R.id.nav_3:
                            intent = new Intent(Setting.this, Login.class);
                            break;
                        case R.id.nav_4:
                            intent = new Intent(Setting.this, ManualControl.class);
                            break;
                        case R.id.nav_5:
                            intent = new Intent(Setting.this, GeneralPanel.class);
                            break;
                        case R.id.nav_6:
                            intent = new Intent(Setting.this, RandomWalk.class);
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

        /* ************************************************************************************** */

        // EditText
        final EditText ip_blank = findViewById(R.id.ip_blank);
        final EditText port_blank = findViewById(R.id.port_blank);

        // SharedPreferences
        SharedPreferences pref = getApplicationContext().getSharedPreferences("Setting", 0);
        final SharedPreferences.Editor editor = pref.edit();

        // read from SP
        ip_blank.setText(pref.getString("ip", null));
        port_blank.setText(pref.getString("port", null));

        // Button and onClick()
        Button send = findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // read from EditText and store
                String ip = ip_blank.getText().toString();
                String port = port_blank.getText().toString();
                editor.putString("ip", ip);
                editor.putString("port", port);
                editor.commit();
                Toast.makeText(Setting.this, "Parameters saved", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
