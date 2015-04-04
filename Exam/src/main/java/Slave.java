import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

/**
 * Created by warrior on 04.04.15.
 */
public class Slave {

    private static final int PORT = 7777;

    private static JFrame frame = new JFrame();

    private static BroadcastReceiver receiver = new BroadcastReceiver();

    public static void main(String[] args) {
        try {
            receiver.startReceive();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(400, 400);
        frame.getContentPane().setBackground(Color.BLUE);
        frame.setVisible(true);

        while (true) {
            Scanner scanner = new Scanner(System.in);
            String ip = scanner.nextLine();
            try {
                Socket socket = new Socket(ip, PORT);
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                while (true) {
                    int color = inputStream.readInt();
                    System.out.println("color: " + color);
                    frame.getContentPane().setBackground(new Color(color));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

