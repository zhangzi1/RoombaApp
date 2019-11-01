package com.example.roombaapp;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;


/* TCP Encapsulation
 * Object must be bound with fixed ip and port.
 * Each member function starts a thread.
 * */
class TCP {
    private String ip;
    private int port;
    private Socket socket;
    private OutputStream outputStream;
    public boolean status = false;

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
                    outputStream = socket.getOutputStream();
                    Log.d("TCP", "successful connection");
                    status = true;
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
                    Log.d("TCP", "error: null pointer 1");
                    status = false;
                }
            }
        };
        setSocketThread.start();
    }

    public void send(String content1) {
        final String content = content1;
        Thread sendThread = new Thread() {
            @Override
            public void run() {
                try {
                    outputStream.write(content.getBytes());
                    Log.d("TCP", "successful sending");
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

    public void close() {
        Thread closeThread = new Thread() {
            @Override
            public void run() {
                try {
                    socket.close();
                    outputStream.close();
                    Log.d("TCP", "connection closed");
                    status = false;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.d("TCP", "error: null stream");
                    status = false;
                }
            }
        };
        closeThread.start();
    }

}
