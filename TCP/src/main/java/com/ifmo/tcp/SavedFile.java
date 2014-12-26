package com.ifmo.tcp;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by warrior on 31.10.14.
 */
public class SavedFile {

    private final File file;
    private byte[] md5;
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public SavedFile(File file) {
        this.file = file;
    }

    public void countMd5() {
        try (InputStream stream = new FileInputStream(file)) {
            md5 = DigestUtils.md5(stream);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getMd5() {
        return md5;
    }

    public File getFile() {
        return file;
    }

    public Lock readLock() {
        return readWriteLock.readLock();
    }

    public Lock writeLock() {
        return readWriteLock.writeLock();
    }

}
