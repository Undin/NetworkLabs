package com.ifmo.tcp;

import com.ifmo.networklab.utils.CopyUtils;
import com.ifmo.tcp.command.*;

import java.io.*;
import java.net.Socket;
import java.util.*;

import static com.ifmo.tcp.TCPSender.*;

/**
 * Created by warrior on 31.10.14.
 */
public class TCPWorker {

    private static final int BUFFER_SIZE = 1024;
    private static final int TIMEOUT = 10000;

    private final File publicDirectory;

    private final List<Thread> threads = new ArrayList<>();

    private final Map<String, SavedFile> publicSavedFiles;
    private final Map<String, SavedFile> privateSavedFiles;

    public TCPWorker(File publicDirectory) {
        this.publicDirectory = publicDirectory;
        this.publicSavedFiles = new HashMap<>();
        this.privateSavedFiles = new HashMap<>();
    }

    public void start() {
        synchronized (publicSavedFiles) {
            File[] files = publicDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    String filename = file.getName();
                    SavedFile savedFile = new SavedFile(file);
                    savedFile.countMd5();
                    publicSavedFiles.put(filename, savedFile);
                }
            }
        }
    }

    public void stop() {
        for (Thread thread : threads) {
            thread.interrupt();
        }
    }

    public void handleRequest(Socket socket) {
        for (Iterator<Thread> i = threads.iterator(); i.hasNext();) {
            Thread th = i.next();
            if (!th.isAlive()) {
                i.remove();
            }
        }
        Thread thread = new Thread(() -> parseRequest(socket));
        threads.add(thread);
        thread.start();
    }

    private void parseRequest(Socket socket) {
        try {
            socket.setSoTimeout(TIMEOUT);
            System.out.println(socket.getInetAddress().toString());
            InputStream inputStream = socket.getInputStream();
            byte[] buffer = new byte[1];
            try {
                int res = inputStream.read(buffer, 0, 1);
                if (res != -1) {
                    switch (buffer[0]) {
                        case Command.LIST:
                            list(socket);
                            finishCommand();
                            break;
                        case Command.GET:
                            get(inputStream, socket);
                            finishCommand();
                            break;
                        case Command.PUT:
                            put(inputStream, socket);
                            finishCommand();
                            break;
                        default:
                            error(socket, ErrorCommand.MALFORMED_MESSAGE);
                    }
                } else {
                    error(socket, ErrorCommand.MALFORMED_MESSAGE);
                }
            } catch (IOException e) {
                e.printStackTrace();
                error(socket, ErrorCommand.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            error(socket, ErrorCommand.INTERNAL_SERVER_ERROR);
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void list(Socket socket) {
        System.out.println("LIST");
        Command command;
        synchronized (publicSavedFiles) {
            command = new ListResponseCommand(publicSavedFiles);
        }
        send(socket, command);
    }

    private void get(InputStream inputStream, Socket socket) {
        System.out.println("GET");
        byte[] buffer = new byte[BUFFER_SIZE];
        int res;
        int readed = 0;
        int len = 0;
        try {
            while ((res = inputStream.read(buffer, readed, BUFFER_SIZE - readed)) != -1) {
                readed += res;
                while (len < readed && buffer[len] != 0) {
                    len++;
                }
                if (buffer[len] == 0) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            error(socket, ErrorCommand.INTERNAL_SERVER_ERROR);
            return;
        }
        if (buffer[len] != 0) {
            Command command = new ErrorCommand(ErrorCommand.MALFORMED_MESSAGE);
            send(socket, command);
            return;
        }
        String filename = new String(buffer, 0, len);
        System.out.println("filename = " + filename);
        SavedFile savedFile;
        boolean locked;
        synchronized (publicSavedFiles) {
            savedFile = publicSavedFiles.get(filename);
            if (savedFile == null) {
                error(socket, ErrorCommand.FILE_NOT_FOUND);
                return;
            } else {
                locked = savedFile.readLock().tryLock();
            }
        }
        if (locked) {
            try {
                Command command = new GetResponseCommand(savedFile);
                send(socket, command);
                System.out.println("get completed");
            } finally {
                savedFile.readLock().unlock();
            }
        } else {
            error(socket, ErrorCommand.TOO_MANY_CONNECTION);
        }
    }

    private void put(InputStream inputStream, Socket socket) {
        System.out.println("PUT");
        ObjectDataInputStream input = new ObjectDataInputStream(inputStream);
        try {
            String name = input.readNullTerminatedString();
            System.out.println("file name: " + name);
            int pos = name.lastIndexOf(File.separatorChar);
            if (pos != -1) {
                error(socket, ErrorCommand.MALFORMED_MESSAGE);
                return;
            }
            SavedFile savedFile;
            boolean locked;
            synchronized (publicSavedFiles) {
                synchronized (privateSavedFiles) {
                    savedFile = publicSavedFiles.remove(name);
                    if (savedFile == null) {
                        savedFile = privateSavedFiles.get(name);
                        if (savedFile == null) {
                            File file = new File(publicDirectory, name);
                            savedFile = new SavedFile(file);
                        } else {
                            error(socket, ErrorCommand.TOO_MANY_CONNECTION);
                            return;
                        }
                    }
                    locked = savedFile.writeLock().tryLock();
                }
            }
            if (locked) {
                try {
                    long size = input.readLong();
                    try (OutputStream output = new FileOutputStream(savedFile.getFile())) {
                        CopyUtils.copy(input, output, size);
                        savedFile.countMd5();
                    }
                    synchronized (publicSavedFiles) {
                        synchronized (privateSavedFiles) {
                            privateSavedFiles.remove(name);
                            publicSavedFiles.put(name, savedFile);
                        }
                    }
                } catch (IOException e) {
                    savedFile.getFile().delete();
                } finally {
                    savedFile.writeLock().unlock();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void error(Socket socket, byte errorCode) {
        System.out.println("send " + ErrorCommand.errorCodeToString(errorCode));
        Command command = new ErrorCommand(errorCode);
        send(socket, command);
    }

    private static void finishCommand() {
        System.out.println("finish");
    }
}
