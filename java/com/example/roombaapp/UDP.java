package com.example.roombaapp;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


/* UDP encapsulation
 * Each method starts a thread. */
public class UDP {
    private DatagramPacket packet;
    private DatagramSocket socket;
    public String buffer = null;
    public String ip = null;

    public void send(final String ip, final int port, final String content) {
        Thread sendThread = new Thread() {
            @Override
            public void run() {
                InetAddress address;
                try {
                    if (socket == null) socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    address = InetAddress.getByName(ip);
                    byte[] data = content.getBytes();
                    packet = new DatagramPacket(data, data.length, address, port);
                    socket.send(packet);
                } catch (UnknownHostException e) {
                    // e.printStackTrace();
                    Log.d("UDP", "error: unknown host");
                } catch (SocketException e) {
                    // e.printStackTrace();
                    Log.d("UDP", "error: socket");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        sendThread.start();
    }

    public void receive(final int port) {
        Thread receiveThread = new Thread() {
            @Override
            public void run() {
                try {
                    if (socket == null) socket = new DatagramSocket(port);
                    byte[] data = new byte[1024];
                    packet = new DatagramPacket(data, data.length);
                    while (true) {
                        socket.receive(packet);
                        ip = packet.getAddress().toString();
                        Log.d("UDP", ip);
                        buffer = new String(data, 0, packet.getLength());
                        Log.d("UDP", buffer);
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                    Log.d("UDP", "error: socket");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        receiveThread.start();
    }

    public void close() {
        socket.close();
        packet = null;
    }
}
