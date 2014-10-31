package com.ifmo.networklab.utils;

import java.nio.ByteBuffer;

/**
 * Created by warrior on 31.10.14.
 */
public class BytesUtils {

    public static byte[] longToBytes(long l) {
        return ByteBuffer.allocate(Long.BYTES).putLong(l).array();
    }

    public static byte[] intToBytes(int i) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(i).array();
    }

    public static int bytesToInt(byte[] bytes, int pos) {
        return ByteBuffer.wrap(bytes, pos, 4).getInt();
    }

    public static long bytesToLong(byte[] bytes, int pos) {
        return ByteBuffer.wrap(bytes, pos, 8).getLong();
    }

    public static byte[] mergeBytes(byte[] ... bytes) {
        int length = 0;
        for (byte[] b : bytes) {
            length += b.length;
        }
        byte[] data = new byte[length];
        int pos = 0;
        for (byte[] b : bytes) {
            System.arraycopy(b, 0, data, pos, b.length);
            pos += b.length;
        }
        return data;
    }

    public static String ipFromBytes(byte[] bytes, int pos) {
        StringBuilder builder = new StringBuilder(7);
        for (int i = 0; i < 3; i++) {
            builder.append(bytes[pos + i] & 0xFF);
            builder.append(".");
        }
        builder.append(bytes[pos + 3] & 0xFF);
        return builder.toString();
    }

    public static String macFromBytes(byte[] bytes, int pos) {
        StringBuilder builder = new StringBuilder(11);
        for (int i = 0; i < 5; i++) {
            builder.append(Integer.toHexString(bytes[pos + i] & 0xFF));
            builder.append(":");
        }
        builder.append(bytes[pos + 5] & 0xFF);
        return builder.toString();
    }
}
