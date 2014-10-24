package com.ifmo.udp;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.List;

/**
 * Created by warrior on 26.09.14.
 */
public class Server {

    private static final boolean TO_CONSOLE = true;

    private static final int BUFFER_LENGTH = 128;

    private static byte[] buffer = new byte[BUFFER_LENGTH];

    private final List<Client> table = new ArrayList<>();

    public static void main(String args[]) throws Exception {
        DatagramSocket sendSocket = new DatagramSocket();
        DatagramSocket receiveSocket = new DatagramSocket(7777);
        byte[] ip = InetAddress.getLocalHost().getAddress();
        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        byte[] mac = networkInterface.getHardwareAddress();
        byte[] secondName = "Pendryak".getBytes();
        System.arraycopy(ip, 0, buffer, 0, 4);
        System.arraycopy(mac, 0, buffer, 4, 6);
        System.arraycopy(secondName, 0, buffer, 10, secondName.length);
        Server server = new Server();
        server.output();
        server.send(sendSocket, buffer);
        server.receive(receiveSocket);
    }

    private void send(final DatagramSocket socket, final byte[] buffer) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), 7777);
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 2000);
    }

    private void receive(final DatagramSocket receiveSocket) throws SocketException {
        final DatagramPacket datagramPacket = new DatagramPacket(new byte[BUFFER_LENGTH], BUFFER_LENGTH);
        while (true) {
            try {
                receiveSocket.receive(datagramPacket);
                byte[] data = datagramPacket.getData();
                synchronized (table) {
                    Client client = new Client(data);
                    int i = table.indexOf(client);
                    if (i == -1) {
                        table.add(client);
                    } else {
                        table.get(i).addMessage();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void output() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (TO_CONSOLE) {
                    System.out.print("\033\143");
                } else {
                    try {
                        Robot robot = new Robot();
                        robot.keyPress(KeyEvent.VK_META);
                        robot.keyPress(KeyEvent.VK_ALT);
                        robot.keyPress(KeyEvent.VK_COMMA);
                        robot.keyRelease(KeyEvent.VK_META);
                        robot.keyRelease(KeyEvent.VK_ALT);
                        robot.keyRelease(KeyEvent.VK_COMMA);
                    } catch (AWTException e) {
                        e.printStackTrace();
                    }
                }

                synchronized (table) {
                    List<Client> toRemove = new ArrayList<>();
                    for (Client client : table) {
                        boolean needToRemove = client.update();
                        if (needToRemove) {
                            toRemove.add(client);
                        }
                    }
                    toRemove.forEach(table::remove);
                    Collections.sort(table);
                    table.forEach(System.out::println);
                }
            }
        }, 0, 200);
    }
}
