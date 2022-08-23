package ru.home.des.chat.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

public class ServerSocketThread extends Thread {
    private int port;
    private final int timeout;

    public ServerSocketThread(String name, int port, int timeout) {
        super(name);
        this.port = port;
        this.timeout = timeout;
        start();
    }

    @Override
    public void run() {
        System.out.println("Server thread start");
        try (ServerSocket server = new ServerSocket(port)) {
            server.setSoTimeout(timeout);
            System.out.println("Server created");
            while (!isInterrupted()) {
                Socket socket;
                try {
                    socket = server.accept();
                } catch (SocketTimeoutException e){
                    e.printStackTrace();
                    continue;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
