package com.example.roombaapp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/* TCP Encapsulation
 * Object must be bound with fixed ip and port for full duplex communication.
 * Each method starts a thread. */
class TCP {
    private String ip;
    private int port;
    private Socket socket;
    public boolean status = false;
    public String buffer = "----";

    TCP(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void setSocket() {
        Thread setSocketThread = new Thread() {
            @Override
            public void run() {
                try {
                    socket = new Socket(ip, port);
                    status = true;
                    // receive();
                } catch (UnknownHostException e) {
                    //e.printStackTrace();
                    Log.d("TCP", "error: unknown host");
                    status = false;
                } catch (ConnectException e) {
                    Log.d("TCP", "error: connection failed");
                    status = false;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.d("TCP", "error: null pointer");
                    status = false;
                }
            }
        };
        setSocketThread.start();
    }

    public void send(final String content) {
        Thread sendThread = new Thread() {
            @Override
            public void run() {
                try {
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(content.getBytes());
                    status = true;
                } catch (SocketException e) {
                    Log.d("TCP", "error: socket broken");
                    status = false;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.d("TCP", "error: null stream");
                    status = false;
                }
            }
        };
        sendThread.start();
    }

    public void receive() {
        Thread receiveThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        InputStream inputStream = socket.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String msg = bufferedReader.readLine();
                        Log.d("TCP", msg);
                        buffer = msg;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.d("TCP", "null pointer");
                }
            }
        };
        receiveThread.start();
    }

    public void close() {
        Thread closeThread = new Thread() {
            @Override
            public void run() {
                try {
                    socket.close();
                    status = false;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.d("TCP", "error: null pointer");
                    status = false;
                }
            }
        };
        closeThread.start();
    }

}
