package ru.home.des.chat.server.core;

import ru.home.des.chat.network.ServerSocketThread;
import ru.home.des.chat.network.ServerSocketThreadListener;
import ru.home.des.chat.network.SocketThread;
import ru.home.des.chat.network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListener, SocketThreadListener {

    private ServerSocketThread server;
    private final Vector<SocketThread> allUsers = new Vector<>();

    public void start(int port) {
        if (server == null) {
            System.out.printf("Server started at port: %d\n", port);
            server = new ServerSocketThread(this, "Server", port, 2000);
        } else {
            System.out.println("Server already started");
        }
    }

    public void stop() {
        System.out.println("Server stopped\n");
        if (server != null && server.isAlive()) {
            server.interrupt();
            server = null;
        } else {
            System.out.println("Server not running");
        }
    }

    private void putLog(String msg) {
        System.out.println(msg);
    }

    @Override
    public void onServerStart(ServerSocketThread thread) {
        putLog("Server started");
    }

    @Override
    public void onServerStop(ServerSocketThread thread) {
        putLog("Server stoped");
    }

    @Override
    public void onServerSocketCreated(ServerSocketThread thread, ServerSocket server) {
        putLog("Server socket created");
    }

    @Override
    public void onServerTimeout(ServerSocketThread thread, ServerSocket server) {
    }

    @Override
    public void onSocketAccepted(ServerSocketThread thread, ServerSocket server, Socket socket) {
        putLog("Client connected");
        String name = "Socket thread " + socket.getInetAddress() + ":" + socket.getPort();
        new SocketThread(this, name, socket);
    }

    @Override
    public void onServerException(ServerSocketThread thread, Throwable exception) {
        exception.printStackTrace();
        putLog(exception.getMessage());
    }

//  Socket thread methods

    @Override
    public void onSocketStart(SocketThread thread, Socket socket) {
        putLog("Client connected");
        allUsers.add(thread);
    }

    @Override
    public void onSocketStop(SocketThread thread) {
        putLog("Client disconnected");
    }

    @Override
    public void onSocketReady(SocketThread thread, Socket socket) {
        putLog("Client is ready to chat");
    }

    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String msg) {
        for (SocketThread s : allUsers) {
            s.sendMessage(msg);
        }
    }

    @Override
    public void onSocketException(SocketThread thread, Exception exception) {
        putLog(exception.getMessage());
    }
}