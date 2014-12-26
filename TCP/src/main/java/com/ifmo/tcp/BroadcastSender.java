package com.ifmo.tcp;

import com.ifmo.networklab.utils.BytesUtils;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by warrior on 31.10.14.
 */
public class BroadcastSender {

    private static final long TIMEOUT = 5000;
    private static final String BROADCAST_IP = "255.255.255.255";

    private final byte[] ip;
    private final int udpPort;
    private final byte[] name;
    private final File publicDirectory;

    private Timer sendTimer;
    private DatagramSocket sendDatagramSocket;

    public BroadcastSender(int udpPort, byte[] ip, String name, File publicDirectory) {
        this.udpPort = udpPort;
        this.ip = ip;
        this.name = BytesUtils.nullTerminatedString(name);
        this.publicDirectory = publicDirectory;
    }

    private DatagramPacket createDatagramPacket() {
        byte[] data;
        synchronized (publicDirectory) {
            data = BytesUtils.mergeBytes(ip,
                   BytesUtils.intToBytes(publicDirectory.list().length),
                   BytesUtils.longToBytes(publicDirectory.lastModified()),
                   name);
        }

        DatagramPacket packet = null;
        try {
            packet = new DatagramPacket(data, data.length, InetAddress.getByName(BROADCAST_IP), udpPort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return packet;
    }

    public void startSend() throws SocketException {
        sendDatagramSocket = new DatagramSocket();
        sendTimer = new Timer();
        sendTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendDatagramSocket.send(createDatagramPacket());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, TIMEOUT);
    }

    public void stopSend() {
        if (sendTimer != null) {
            sendTimer.cancel();
            sendTimer = null;
        }
        sendDatagramSocket.close();
    }
}
