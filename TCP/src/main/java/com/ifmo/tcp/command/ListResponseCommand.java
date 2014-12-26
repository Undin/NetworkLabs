package com.ifmo.tcp.command;

import com.ifmo.networklab.utils.BytesUtils;
import com.ifmo.tcp.SavedFile;

import java.util.Map;

/**
 * Created by warrior on 31.10.14.
 */
public class ListResponseCommand extends Command {

    public ListResponseCommand(Map<String, SavedFile> files) {
        super(createCommandData(files));
    }

    private static byte[] createCommandData(Map<String, SavedFile> files) {
        byte[][] unmergedData = new byte[2 * files.size() + 2][];
        unmergedData[0] = LIST_RESPONSE_ARRAY;
        unmergedData[1] = BytesUtils.intToBytes(files.size());
        int i = 2;
        for (String filename : files.keySet()) {
            SavedFile file = files.get(filename);
            unmergedData[i] = file.getMd5();
            unmergedData[i + 1] = BytesUtils.nullTerminatedString(filename);
            i += 2;
        }
        return BytesUtils.mergeBytes(unmergedData);
    }
}
