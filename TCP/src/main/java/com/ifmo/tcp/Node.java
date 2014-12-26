package com.ifmo.tcp;


import static com.ifmo.networklab.utils.BytesUtils.*;

/**
 * Created by warrior on 31.10.14.
 */
public class Node {

    private final byte[] ip = new byte[4];
    private final String stringIp;
    private final String name;
    private int fileCount;
    private long lastModified;
    private long timestamp;

    public Node(byte[] bytes) {
        timestamp = System.currentTimeMillis();
        System.arraycopy(bytes, 0, ip, 0, 4);
        stringIp = ipFromBytes(ip, 0);
        fileCount = bytesToInt(bytes, 4);
        lastModified = bytesToLong(bytes, 8);
        int len = 0;
        while (bytes[len + 16] != 0) {
            len++;
        }
        name = new String(bytes, 16, len);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (stringIp != null ? !stringIp.equals(node.stringIp) : node.stringIp != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return stringIp != null ? stringIp.hashCode() : 0;
    }

    public String getStringIp() {
        return stringIp;
    }

    public String getName() {
        return name;
    }

    public int getFileCount() {
        return fileCount;
    }

    public long getLastModified() {
        return lastModified;
    }

    public byte[] getIp() {
        return ip;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Node{" +
                "stringIp='" + stringIp + '\'' +
                ", name='" + name + '\'' +
                ", fileCount=" + fileCount +
                ", lastModified=" + lastModified +
                '}';
    }
}
