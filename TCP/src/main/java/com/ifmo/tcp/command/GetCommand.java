package com.ifmo.tcp.command;

import com.ifmo.networklab.utils.BytesUtils;

/**
 * Created by warrior on 31.10.14.
 */
public class GetCommand extends Command {

    public GetCommand(String filename) {
        super(BytesUtils.mergeBytes(GET_ARRAY, filename.getBytes(), new byte[]{0}));
    }
}
