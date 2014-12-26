package com.ifmo.tcp;

import com.ifmo.tcp.command.Command;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by warrior on 01.11.14.
 */
public class TCPSender {

    public static void send(Socket socket, Command command) {
        OutputStream outputStream ;
        try {
            outputStream = socket.getOutputStream();
            IOUtils.copy(command.getInputStream(), outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
