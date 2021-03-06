package com.ifmo.udp;

import com.ifmo.networklab.utils.BytesUtils;

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
        ip = BytesUtils.ipFromBytes(data, 0);
        mac = BytesUtils.macFromBytes(data, 4);
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
}
