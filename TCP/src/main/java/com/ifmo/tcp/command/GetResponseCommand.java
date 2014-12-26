package com.ifmo.tcp.command;

import com.ifmo.networklab.utils.BytesUtils;
import com.ifmo.tcp.SavedFile;

import java.io.File;

/**
 * Created by warrior on 31.10.14.
 */
public class GetResponseCommand extends FileCommand {

    public GetResponseCommand(SavedFile savedFile) {
        super(createData(savedFile), savedFile.getFile());
    }

    private static byte[] createData(SavedFile savedFile) {
        File file = savedFile.getFile();
        return BytesUtils.mergeBytes(GET_RESPONSE_ARRAY,
                                     BytesUtils.longToBytes(file.length()),
                                     savedFile.getMd5());
    }
}
