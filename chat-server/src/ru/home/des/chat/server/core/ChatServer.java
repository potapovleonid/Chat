package ru.home.des.chat.server.core;

import ru.home.des.chat.library.Library;
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

    private final ChatServerListener listener;

    public ChatServer(ChatServerListener listener) {
        this.listener = listener;
    }

    public void start(int port) {
        if (server == null || !server.isAlive()) {
            server = new ServerSocketThread(this, "Server", port, 2000);
            putLog(String.format("Server started at port: %d", port));
        } else {
            putLog("Server already started");
        }
    }

    public void stop() {
        if (server != null && server.isAlive()) {
            server.interrupt();
            server = null;
        } else {
            putLog("Server not running");
        }
    }

    private void putLog(String msg) {
        listener.onChatServerMessage(msg);
    }

    private synchronized String getUsers() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < allUsers.size(); i++) {
            ClientThread client = (ClientThread) allUsers.get(i);
            if (!client.isAuthorized()) continue;
            sb.append(client.getNickname()).append(Library.DELIMITER);
        }
        return sb.toString();
    }

//  Server socket thread listener methods

    @Override
    public void onServerStart(ServerSocketThread thread) {
        SQLClient.connect();
        putLog("Server started");
    }

    @Override
    public void onServerStop(ServerSocketThread thread) {
        SQLClient.disconnect();
        putLog("Server stopped");
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
        new ClientThread(this, name, socket);
    }

    @Override
    public void onServerException(ServerSocketThread thread, Throwable exception) {
        exception.printStackTrace();
        putLog(exception.getMessage());
    }

//  Socket thread listener methods

    @Override
    public void onSocketStart(SocketThread thread, Socket socket) {
        putLog("Client connected");

        allUsers.add(thread);
    }

    @Override
    public void onSocketStop(SocketThread thread) {
        ClientThread client = (ClientThread) thread;
        putLog("Client disconnected");
        allUsers.remove(thread);
        if (client.isAuthorized()) {
            sendToAllAuthorizedClients(Library.getTypeBroadcast("Server",
                    client.getNickname() + " disconnected"));
        }
        thread.close();
        sendToAllAuthorizedClients(Library.getUserList(getUsers()));
    }

    @Override
    public void onSocketReady(SocketThread thread, Socket socket) {
        putLog("Client is ready to chat");
    }

    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String msg) {
        ClientThread client = (ClientThread) thread;
        if (client.isAuthorized()) {
            handleAutorizeMessage(client, msg);
        } else {
            handleNonAutorizeMessage(client, msg);
        }
    }

    private void handleAutorizeMessage(ClientThread client, String msg) {
        sendToAllAuthorizedClients(Library.getTypeBroadcast(client.getNickname(), msg));
    }

    private void handleNonAutorizeMessage(ClientThread client, String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        if (arr.length != 3 || !arr[0].equals(Library.AUTH_REQUEST)) {
            client.msgFormatError(msg);
            return;
        }
//#TODO in library
        String login = arr[1];
        String password = arr[2];
        String nickname = SQLClient.getNickname(login, password);
        if (nickname == null) {
            putLog("Invalid credentials for user " + login);
            client.authFail();
            return;
        }
        client.authAccept(nickname);
        sendToAllAuthorizedClients(Library.getTypeBroadcast("Server", nickname + " connected"));
        sendToAllAuthorizedClients(Library.getUserList(getUsers()));
    }

    private void sendToAllAuthorizedClients(String msg) {
        for (SocketThread s : allUsers) {
            ClientThread client = (ClientThread) s;
            if (!client.isAuthorized()) continue;
            s.sendMessage(msg);
        }
    }

    @Override
    public void onSocketException(SocketThread thread, Exception exception) {
        putLog(exception.getMessage());
    }
}
