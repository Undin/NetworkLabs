package com.ifmo.tcp;

import java.io.File;
import java.io.IOException;
import java.net.*;

/**
 * Created by warrior on 31.10.14.
 */
public class Server {

    private static final int UDP_PORT = 7777;
    private static final int TCP_PORT = 7777;
    private static final String NAME = "Pendryak";
    private static final String PUBLIC_DIRECTORY = "public";

    private final BroadcastSender broadcastSender;
    private final TCPWorker tcpWorker;
    private final int tcpPort;

    private ServerSocket serverSocket;
    private Thread tcpConnectionThread;

    private Server(int udpPort, int tcpPort, byte[] ip, String name, File publicDirectory) {
        this.tcpPort = tcpPort;
        this.broadcastSender = new BroadcastSender(udpPort, ip, name, publicDirectory);
        this.tcpWorker = new TCPWorker(publicDirectory);
    }

    public void start() throws IOException {
        tcpWorker.start();
        broadcastSender.startSend();
        startListen();
    }

    public void stop() throws IOException {
        stopListen();
        broadcastSender.stopSend();
        tcpWorker.stop();
    }

    private void startListen() throws IOException {
        serverSocket = new ServerSocket(tcpPort);
        tcpConnectionThread = new Thread(() -> {
            while (true) {
                try {
                    Socket connectionSocket = serverSocket.accept();
                    System.out.println("accept");
                    tcpWorker.handleRequest(connectionSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        tcpConnectionThread.start();
    }

    private void stopListen() throws IOException {
        if (tcpConnectionThread != null) {
            tcpConnectionThread.interrupt();
            tcpConnectionThread = null;
        }
        serverSocket.close();
    }

    public static Server createServer(int udpPort, int tcpPort, String name, String publicDirectoryName) throws IOException {
        File publicDirectory = createDirectory(publicDirectoryName);
        byte[] ip = InetAddress.getLocalHost().getAddress();
        return new Server(udpPort, tcpPort, ip, name, publicDirectory);
    }

    private static File createDirectory(String directoryName) {
        File directory = new File(directoryName);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("can't create directory");
            }
        } else {
            if (!directory.isDirectory()) {
                throw new IllegalArgumentException(directory + " isn't directory");
            }
        }
        return directory;
    }

    public static void main(String[] args) throws IOException {
        Server server = createServer(UDP_PORT, TCP_PORT, NAME, PUBLIC_DIRECTORY);
        server.start();
    }
}
