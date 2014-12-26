package com.ifmo.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by warrior on 14.11.14.
 */
public class CopyUtils {

    private static final int BUFFER_LENGTH = 4096;

    public static void copy(InputStream input, OutputStream output, long len) throws IOException {
        byte[] buffer = new byte[BUFFER_LENGTH];
        int n, l;
        long copied = 0;
        if (len - copied > BUFFER_LENGTH) {
            l = BUFFER_LENGTH;
        } else {
            l = (int) (len - copied);
        }
        while (l > 0 && -1 != (n = input.read(buffer, 0, l))) {
            output.write(buffer, 0, n);
            copied += n;
            if (len - copied > BUFFER_LENGTH) {
                l = BUFFER_LENGTH;
            } else {
                l = (int) (len - copied);
            }
        }
    }
}
