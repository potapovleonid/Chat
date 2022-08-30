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

    public boolean start(int port) {
        if (server == null || !server.isAlive()) {
            server = new ServerSocketThread(this, "Server", port, 2000);
            putLog(String.format("Server started at port: %d", port));
            return true;
        } else {
            putLog("Server already started");
            return false;
        }
    }

    public boolean stop() {
        if (server != null && server.isAlive()) {
            server.interrupt();
            server = null;
            return true;
        } else {
            putLog("Server not running");
            return false;
        }
    }

    private void putLog(String msg) {
        listener.onChatServerMessage(msg);
    }

    private synchronized String getUsers() {
        StringBuilder sb = new StringBuilder();
        for (SocketThread user : allUsers) {
            ClientThread client = (ClientThread) user;
            if (!client.isAuthorized()) continue;
            sb.append(client.getNickname()).append(Library.DELIMITER);
        }
        return sb.toString();
    }

    private synchronized ClientThread findClientByNickname(String nickname) {
        for (SocketThread user : allUsers) {
            ClientThread client = (ClientThread) user;
            if (!client.isAuthorized()) continue;
            if (client.getNickname().equals(nickname))
                return client;
        }
        return null;
    }

    private void handleAuthorizedMessage(ClientThread client, String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        String msgType = arr[0];

        switch (msgType) {
            case Library.TYPE_BROADCAST_CLIENT:
                sendToAllAuthorizedClients(Library.getTypeBroadcast(client.getNickname(), arr[1]));
                break;
            default:
                client.sendMessage(Library.getMsgFormatError(msg));
        }
    }

    private void handleNonAuthorizedMessage(ClientThread client, String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        if (arr.length != 3 || !arr[0].equals(Library.AUTH_REQUEST)) {
            client.msgFormatError(msg);
            return;
        }

        String login = arr[1];
        String password = arr[2];
        String nickname = SQLClient.getNickname(login, password);
        if (nickname == null) {
            putLog("Invalid credentials for user " + login);
            client.authFail();
            return;
        } else {
            ClientThread oldClient = findClientByNickname(nickname);
            client.authAccept(nickname);
            if (oldClient == null) {
                sendToAllAuthorizedClients(Library.getTypeBroadcast("Server", nickname + " connected"));
            } else {
                oldClient.reconnected();
                allUsers.remove(oldClient);
            }
        }

        sendToAllAuthorizedClients(Library.getUserList(getUsers()));
    }

    private synchronized void sendToAllAuthorizedClients(String msg) {
        for (SocketThread s : allUsers) {
            ClientThread client = (ClientThread) s;
            if (!client.isAuthorized()) continue;
            s.sendMessage(msg);
        }
    }


    /**
     * Server socket thread listener methods
     **/

    @Override
    public synchronized void onServerStart(ServerSocketThread thread) {
        SQLClient.connect();
        putLog("Server started");
    }

    @Override
    public synchronized void onServerStop(ServerSocketThread thread) {
        SQLClient.disconnect();
        putLog("Server stopped");

    }

    @Override
    public synchronized void onServerSocketCreated(ServerSocketThread thread, ServerSocket server) {
        putLog("Server socket created");
    }

    @Override
    public synchronized void onServerTimeout(ServerSocketThread thread, ServerSocket server) {
    }

    @Override
    public synchronized void onSocketAccepted(ServerSocketThread thread, ServerSocket server, Socket socket) {
        putLog("Client connected");
        String name = "Socket thread " + socket.getInetAddress() + ":" + socket.getPort();
        new ClientThread(this, name, socket);
    }

    @Override
    public synchronized void onServerException(ServerSocketThread thread, Throwable exception) {
        exception.printStackTrace();
        putLog(exception.getMessage());
    }

//  Socket thread listener methods

    @Override
    public synchronized void onSocketStart(SocketThread thread, Socket socket) {
        putLog("Client connected");

        allUsers.add(thread);
    }

    @Override
    public synchronized void onSocketStop(SocketThread thread) {
        ClientThread client = (ClientThread) thread;
        putLog("Client disconnected");
        allUsers.remove(thread);
        if (client.isAuthorized() && !client.isReconnected()) {
            sendToAllAuthorizedClients(Library.getTypeBroadcast("Server",
                    client.getNickname() + " disconnected"));
        }
        sendToAllAuthorizedClients(Library.getUserList(getUsers()));
    }

    @Override
    public synchronized void onSocketReady(SocketThread thread, Socket socket) {
        putLog("Client is ready to chat");
    }

    @Override
    public synchronized void onReceiveString(SocketThread thread, Socket socket, String msg) {
        ClientThread client = (ClientThread) thread;
        if (client.isAuthorized()) {
            handleAuthorizedMessage(client, msg);
        } else {
            handleNonAuthorizedMessage(client, msg);
        }
    }

    @Override
    public void onSocketException(SocketThread thread, Exception exception) {
        putLog(exception.getMessage());
    }
}
