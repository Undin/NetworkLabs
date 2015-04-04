import com.ifmo.networklab.utils.BytesUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by warrior on 04.04.15.
 */
public class Master {

    public static final int PORT = 7777;

    private static List<Socket> sockets = new ArrayList<>();
    private static Random random = new Random();


    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            startTimer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            System.out.println(BytesUtils.ipFromBytes(inetAddress.getAddress(), 0));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                addSocket(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void addSocket(final Socket socket) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (sockets) {
                    sockets.add(socket);
                }
            }
        }).start();
    }

    public static void startTimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int color = random.nextInt();
                synchronized (sockets) {
                    for (Socket socket : sockets) {
                        try {
                            socket.getOutputStream().write(ByteBuffer.allocate(Integer.BYTES).putInt(color).array());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, 0, 2000);
    }
}
