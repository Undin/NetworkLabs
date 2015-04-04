import com.ifmo.networklab.utils.BytesUtils;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class BroadcastSender {

    private static final long TIMEOUT = 5000;
    private static final String BROADCAST_IP = "255.255.255.255";

    private Timer sendTimer;
    private DatagramSocket sendDatagramSocket;

    public BroadcastSender() {
    }

    private DatagramPacket createDatagramPacket() {
        DatagramPacket packet = null;
        try {
            packet = new DatagramPacket("HELO".getBytes(), "HELO".getBytes().length, InetAddress.getByName(BROADCAST_IP), 7777);
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