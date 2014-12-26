package com.ifmo.tcp.command;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by warrior on 31.10.14.
 */
public abstract class Command {

    public static final byte LIST = 0x1;
    public static final byte GET = 0x2;
    public static final byte PUT = 0x3;
    public static final byte LIST_RESPONSE = 0x4;
    public static final byte GET_RESPONSE = 0x5;
    public static final byte ERROR = -1;
    
    protected static final byte[] LIST_ARRAY = new byte[]{LIST};
    protected static final byte[] GET_ARRAY = new byte[]{GET};
    protected static final byte[] PUT_ARRAY = new byte[]{PUT};
    protected static final byte[] LIST_RESPONSE_ARRAY = new byte[]{LIST_RESPONSE};
    protected static final byte[] GET_RESPONSE_ARRAY = new byte[]{GET_RESPONSE};
    protected static final byte[] ERROR_ARRAY = new byte[]{ERROR};

    protected InputStream inputStream;

    public Command(byte[] data) {
        inputStream = new ByteArrayInputStream(data);
    }

    public InputStream getInputStream() {
        return inputStream;
    }

}
