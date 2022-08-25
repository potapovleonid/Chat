package ru.home.des.chat.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerSocketThread extends Thread {
    private int port;
    private final int timeout;
    private final ServerSocketThreadListener listener;

    public ServerSocketThread(ServerSocketThreadListener listener, String name, int port, int timeout) {
        super(name);
        this.listener = listener;
        this.port = port;
        this.timeout = timeout;
        start();
    }

    @Override
    public void run() {

        try (ServerSocket server = new ServerSocket(port)) {
            listener.onServerStart(this);

            server.setSoTimeout(timeout);
            listener.onServerSocketCreated(this, server);
            while (!isInterrupted()) {
                Socket socket;
                try {
                    socket = server.accept();
                } catch (SocketTimeoutException e){
                    listener.onServerTimeout(this, server);
                    continue;
                }
                listener.onSocketAccepted(this, server, socket);
            }
        } catch (IOException e) {
            listener.onServerException(this, e);
        } finally {
            listener.onServerStop(this);
        }
    }
}
