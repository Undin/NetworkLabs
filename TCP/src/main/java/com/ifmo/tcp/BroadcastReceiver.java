package com.ifmo.tcp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Created by warrior on 31.10.14.
 */
public class BroadcastReceiver {

    public interface BroadcastReceiverListener {
        public void getNode(Node node);
    }

    protected static final int DATAGRAM_PACKET_SIZE = 128;
    protected final int udpPort;

    private DatagramSocket receiveDatagramSocket;
    private Thread receiveThread;

    private BroadcastReceiverListener listener;
    private final Object lock = new Object();

    public BroadcastReceiver(int udpPort) {
        this.udpPort = udpPort;
    }

    public void startReceive() throws SocketException {
        receiveDatagramSocket = new DatagramSocket(udpPort);
        receiveThread = new Thread(() -> {
            byte[] bytes = new byte[DATAGRAM_PACKET_SIZE];
            DatagramPacket packet = new DatagramPacket(bytes, DATAGRAM_PACKET_SIZE);
            while (true) {
                try {
                    receiveDatagramSocket.receive(packet);
                    Node node = new Node(packet.getData());
                    if (Arrays.equals(node.getIp(), packet.getAddress().getAddress())) {
                        synchronized (lock) {
                            if (listener != null) {
                                listener.getNode(node);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        receiveThread.start();
    }

    public void stopReceive() {
        if (receiveThread != null) {
            receiveThread.interrupt();
            receiveThread = null;
        }
        receiveDatagramSocket.close();
    }

    public void setBroadcastReceiverListener(BroadcastReceiverListener listener) {
        synchronized (lock) {
            this.listener = listener;
        }
    }
}
