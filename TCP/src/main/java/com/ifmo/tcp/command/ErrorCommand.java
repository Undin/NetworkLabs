package com.ifmo.tcp.command;

import com.ifmo.networklab.utils.BytesUtils;

/**
 * Created by warrior on 31.10.14.
 */
public class ErrorCommand extends Command {

    public static final byte FILE_NOT_FOUND = 0x1;
    public static final byte TOO_MANY_CONNECTION = 0x2;
    public static final byte MALFORMED_MESSAGE = 0x3;
    public static final byte INTERNAL_SERVER_ERROR = -1;

    public ErrorCommand(byte errorCode) {
        super(BytesUtils.mergeBytes(ERROR_ARRAY, new byte[]{errorCode}));
    }

    public static String errorCodeToString(byte code) {
        switch (code) {
            case FILE_NOT_FOUND:
                return "File not found";
            case TOO_MANY_CONNECTION:
                return "Too many open connections";
            case MALFORMED_MESSAGE:
                return "Malformed message";
            case INTERNAL_SERVER_ERROR:
                return "internal server error";
            default:
                return "Unknown error code";
        }
    }
}
