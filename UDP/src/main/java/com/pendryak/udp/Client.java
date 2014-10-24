package com.pendryak.udp;

import java.util.Deque;
import java.util.LinkedList;

public class Client implements Comparable<Client> {

    private static final long MAX_TIMEOUT = 20000;
    private static final long TIMEOUT = 2000;

    private final String ip;
    private final String mac;
    private final String name;
    private final long creationTime;
    private final Deque<Long> times = new LinkedList<>();
    private long sinceLastUpdate;
    private long lost;

    public Client(byte[] data) {
        creationTime = System.currentTimeMillis();
        times.addLast(creationTime);
        ip = bytesToString(data, 0, 4, '.', false);
        mac = bytesToString(data, 4, 6, ':', true);
        int length = 10;
        while (data[length] != 0) {
            length++;
        }
        name = new String(data, 10, length - 10);
    }

    @Override
    public int compareTo(Client o) {
        return Long.compare(lost, o.lost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Client client = (Client) o;

        if (ip != null ? !ip.equals(client.ip) : client.ip != null) return false;
        if (mac != null ? !mac.equals(client.mac) : client.mac != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + (mac != null ? mac.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ip: ").append(ip).
                append(" mac: ").append(mac).
                append(" name: ").append(name).
                append(" time: ").append(sinceLastUpdate).
                append(" lost: ").append(lost);
        return builder.toString();
    }

    public void addMessage() {
        times.addLast(System.currentTimeMillis());
    }

    public boolean update() {
        long curTime = System.currentTimeMillis();
        if (!times.isEmpty() && (sinceLastUpdate = curTime - times.getLast()) > MAX_TIMEOUT) {
            return true;
        }
        while (!times.isEmpty() && curTime - times.getFirst() > MAX_TIMEOUT) {
            times.removeFirst();
        }
        long start = Math.max(creationTime, curTime - MAX_TIMEOUT);
        long interval = curTime - start;
        long expectedPackets = (interval + TIMEOUT - 1) / TIMEOUT;
        lost = Math.max(0, expectedPackets - times.size());
        return false;
    }

    private static String bytesToString(byte[] bytes, int start, int length, char delim, boolean toHex) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < start + length - 1; i++) {
            builder.append(byteToString(bytes[i], toHex)).append(delim);
        }
        builder.append(byteToString(bytes[start + length - 1], toHex));
        return builder.toString();
    }

    private static String byteToString(byte b, boolean toHex) {
        int i = b & 0xFF;
        return toHex ? (i < 16 ? "0" : "") + Integer.toHexString(i).toUpperCase() : Integer.toString(i);
    }
}
