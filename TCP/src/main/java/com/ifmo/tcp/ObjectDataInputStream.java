package com.ifmo.tcp;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by warrior on 14.11.14.
 */
public class ObjectDataInputStream extends DataInputStream {

    private byte[] buffer = new byte[1024];
    /**
     * Creates a DataInputStream that uses the specified
     * underlying InputStream.
     *
     * @param in the specified input stream
     */
    public ObjectDataInputStream(InputStream in) {
        super(in);
    }

    public String readNullTerminatedString() throws IOException {
        int i = 0;
        while (true) {
            buffer[i] = readByte();
            if (buffer[i] == 0) {
                return new String(buffer, 0, i);
            }
            i++;
        }
    }

    public byte[] readMd5() throws IOException {
        readFully(buffer, 0, 16);
        byte[] md5 = new byte[16];
        System.arraycopy(buffer, 0, md5, 0, 16);
        return md5;
    }
}
