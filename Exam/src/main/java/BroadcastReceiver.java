import com.ifmo.networklab.utils.BytesUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

/**
 * Created by warrior on 31.10.14.
 */
public class BroadcastReceiver {

    protected static final int DATAGRAM_PACKET_SIZE = 128;
    protected final int udpPort = 7777;

    private DatagramSocket receiveDatagramSocket;
    private Thread receiveThread;

    public BroadcastReceiver() {

    }

    public void startReceive() throws SocketException {
        receiveDatagramSocket = new DatagramSocket(udpPort);
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] bytes = new byte[DATAGRAM_PACKET_SIZE];
                DatagramPacket packet = new DatagramPacket(bytes, DATAGRAM_PACKET_SIZE);
                while (true) {
                    try {
                        receiveDatagramSocket.receive(packet);
                        String str = new String(packet.getData(), 0, "HELO".getBytes().length);
                        if (str.equals("HELO")) {
                            System.out.println("broadcast ip: " + BytesUtils.ipFromBytes(packet.getAddress().getAddress()));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
}
