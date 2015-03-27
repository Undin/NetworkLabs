package com.ifmo.tcp;

import com.ifmo.networklab.utils.CopyUtils;
import com.ifmo.tcp.command.*;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import sun.net.util.IPAddressUtil;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by warrior on 31.10.14.
 */
public class Client {

    private static final int UDP_PORT = 7777;
    private static final int TCP_PORT = 7777;

    public static final String HOSTS_COMMAND = "HOSTS";
    public static final String LIST_COMMAND = "LIST";
    public static final String GET_COMMAND = "GET";
    public static final String PUT_COMMAND = "PUT";
    public static final String EXIT = "EXIT";

    public static final String INCORRECT_COMMAND = "incorrect command";
    public static final String UNKNOWN_RESPONSE_CODE = "unknown response code";
    public static final String FINISH_COMMAND = "finish command";

    public static final String DEFAULT_DIRECTORY = "./received/";
    public static final long UPD_TIMEOUT = 20000;
    public static final int TCP_TIMEOUT = 5000;

    private final BufferedReader consoleReader;
    private final BroadcastReceiver broadcastReceiver;
    private final List<Node> hosts = new ArrayList<>();
    private final int tcpPort;

    private File receivedDirectory;

    public Client(int udpPort, int tcpPort) {
        consoleReader = new BufferedReader(new InputStreamReader(System.in));
        broadcastReceiver = new BroadcastReceiver(udpPort);
        this.tcpPort = tcpPort;
    }

    public void start() throws SocketException {
        receivedDirectory = new File(DEFAULT_DIRECTORY);
        receivedDirectory.mkdirs();
        broadcastReceiver.setBroadcastReceiverListener((Node node) -> {
            synchronized (hosts) {
                hosts.remove(node);
                hosts.add(node);
            }
        });
        broadcastReceiver.startReceive();
        while (true) {
            try {
                if (parseCommand(consoleReader.readLine())) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        broadcastReceiver.stopReceive();
    }

    private boolean parseCommand(String command) {
        boolean exit = false;
        StringTokenizer tokenizer = new StringTokenizer(command);
        if (tokenizer.hasMoreTokens()) {
            String commandIdentifier = tokenizer.nextToken().toUpperCase();
            switch (commandIdentifier) {
                case HOSTS_COMMAND:
                    hosts();
                    finishCommand();
                    break;
                case LIST_COMMAND:
                    list(tokenizer);
                    finishCommand();
                    break;
                case GET_COMMAND:
                    get(tokenizer);
                    finishCommand();
                    break;
                case PUT_COMMAND:
                    put(tokenizer);
                    finishCommand();
                    break;
                case EXIT:
                    exit = true;
                    break;
                default:
                    incorrectCommand();
            }
        } else {
            incorrectCommand();
        }
        return exit;
    }

    private void hosts() {
        long time = System.currentTimeMillis();
        synchronized (hosts) {
            System.out.println(hosts.size());
            for (Iterator<Node> iterator = hosts.iterator(); iterator.hasNext(); ) {
                Node node = iterator.next();
                if (time - node.getTimestamp() > UPD_TIMEOUT) {
                    iterator.remove();
                } else {
                    System.out.println(String.format("name: %s, ip: %s, filecount: %d", node.getName(), node.getStringIp(), node.getFileCount()));
                }
            }
        }
    }

    private void list(StringTokenizer tokenizer) {
        InetAddress inetAddress = getIp(tokenizer);
        if (inetAddress != null) {
            try (Socket socket = new Socket(inetAddress, tcpPort)) {
                socket.setSoTimeout(TCP_TIMEOUT);
                TCPSender.send(socket, new ListCommand());
                InputStream inputStream = socket.getInputStream();
                ObjectDataInputStream input = new ObjectDataInputStream(inputStream);
                List<String> fileNames = new ArrayList<>();
                List<String> md5List = new ArrayList<>();
                byte commandCode = input.readByte();
                switch (commandCode) {
                    case Command.LIST_RESPONSE:
                        int number = input.readInt();
                        for (int i = 0; i < number; i++) {
                            md5List.add(Hex.encodeHexString(input.readMd5()));
//                            System.out.println("md5: " + md5List.get(i));
                            fileNames.add(input.readNullTerminatedString());
//                            System.out.println("file: " + fileNames.get(i));
                            System.out.println("file: " + fileNames.get(i) + ", md5: " + md5List.get(i));
                        }
//                        System.out.println("files: " + number);
//                        for (int i = 0; i < number; i++) {
//                            System.out.println("file: " + fileNames.get(i) + ", md5: " + md5List.get(i));
//                        }
                        break;
                    case Command.ERROR:
                        byte errorCode = input.readByte();
                        System.out.println(ErrorCommand.errorCodeToString(errorCode));
                        break;
                    default:
                        unknownResponseCode();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            incorrectCommand();
        }
    }

    private void get(StringTokenizer tokenizer) {
        InetAddress inetAddress = getIp(tokenizer);
        if (inetAddress != null) {
            if (tokenizer.hasMoreTokens()) {
                String filename = tokenizer.nextToken();
                File file = new File(receivedDirectory, filename);
                Command command = new GetCommand(filename);
                try (Socket socket = new Socket(inetAddress, tcpPort)) {
                    socket.setSoTimeout(TCP_TIMEOUT);
                    TCPSender.send(socket, command);
                    ObjectDataInputStream input = new ObjectDataInputStream(socket.getInputStream());
                    byte commandCode = input.readByte();
                    switch (commandCode) {
                        case Command.GET_RESPONSE:
                            long size = input.readLong();
                            byte[] md5 = input.readMd5();
                            try (FileOutputStream output = new FileOutputStream(file)) {
                                CopyUtils.copy(input, output, size);
                            }
                            byte[] correctMd5 = DigestUtils.md5(new FileInputStream(file));
                            if (!Arrays.equals(md5, correctMd5)) {
                                System.out.println("received md5: " + Hex.encodeHexString(md5) + ", " +
                                                    "real md5: " + Hex.encodeHexString(correctMd5));
                            }
                            break;
                        case Command.ERROR:
                            byte errorCode = input.readByte();
                            System.out.println(ErrorCommand.errorCodeToString(errorCode));
                            break;
                        default:
                            unknownResponseCode();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            incorrectCommand();
        }
    }

    private void put(StringTokenizer tokenizer) {
        InetAddress inetAddress = getIp(tokenizer);
        if (inetAddress != null) {
            if (tokenizer.hasMoreTokens()) {
                String path = tokenizer.nextToken();
                File file = new File(path);
                try (Socket socket = new Socket(inetAddress, tcpPort)) {
                    socket.setSoTimeout(TCP_TIMEOUT);
                    Command command = new PutCommand(file);
                    TCPSender.send(socket, command);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                incorrectCommand();
            }
        } else {
            incorrectCommand();
        }

    }

    private static InetAddress getIp(StringTokenizer tokenizer) {
        if (tokenizer.hasMoreElements()) {
            String ip = tokenizer.nextToken();
            if (IPAddressUtil.isIPv4LiteralAddress(ip)) {
                try {
                    return InetAddress.getByName(ip);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    System.out.println("unreachable host: " + ip);
                    return null;
                }
            }
        }
        return null;
    }

    private static void finishCommand() {
        System.out.println(FINISH_COMMAND);
    }

    private static void incorrectCommand() {
        System.out.println(INCORRECT_COMMAND);
    }

    private static void unknownResponseCode() {
        System.out.println(UNKNOWN_RESPONSE_CODE);
    }

    public static void main(String[] args) throws SocketException {
        Client client = new Client(UDP_PORT, TCP_PORT);
        client.start();
    }
}
