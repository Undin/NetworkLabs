package com.ifmo.tcp.command;

import com.ifmo.networklab.utils.BytesUtils;

import java.io.File;

/**
 * Created by warrior on 31.10.14.
 */
public class PutCommand extends FileCommand {

    public PutCommand(File file) {
        super(createData(file), file);
    }

    private static byte[] createData(File file) {
        return BytesUtils.mergeBytes(PUT_ARRAY,
                                     BytesUtils.nullTerminatedString(file.getName()),
                                     BytesUtils.longToBytes(file.length()));
    }
}
