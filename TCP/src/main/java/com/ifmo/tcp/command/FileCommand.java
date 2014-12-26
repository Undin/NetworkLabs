package com.ifmo.tcp.command;

import java.io.*;

/**
 * Created by warrior on 31.10.14.
 */
public abstract class FileCommand extends Command {

    public FileCommand(byte[] data, File file) {
        super(data);
        try {
            mergeInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void mergeInputStream(InputStream inputStream) {
        this.inputStream = new SequenceInputStream(this.inputStream, inputStream);
    }
}
