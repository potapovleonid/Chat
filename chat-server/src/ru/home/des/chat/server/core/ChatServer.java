package ru.home.des.chat.server.core;

import org.postgresql.util.PSQLException;
import ru.home.des.chat.library.Library;
import ru.home.des.chat.network.ServerSocketThread;
import ru.home.des.chat.network.ServerSocketThreadListener;
import ru.home.des.chat.network.SocketThread;
import ru.home.des.chat.network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
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

    private void handleAuthorizedMessage(ClientThread user, String msg) {
        String[] message = msg.split(Library.DELIMITER);
        String msgType = message[0];

        switch (msgType) {
            case Library.TYPE_BROADCAST_CLIENT:
                sendToAllAuthorizedClients(Library.getTypeBroadcast(user.getNickname(), message[1]));
                break;
            case Library.TYPE_PRIVATE_MESSAGE:
                sendPrivateMessage(message[3], user.getNickname(), message[2]);
                break;
            default:
                user.sendMessage(Library.getMsgFormatError(msg));
        }
    }

    private void handleNonAuthorizedMessage(ClientThread user, String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        if (arr[0].equals(Library.REGISTRATION_REQUEST)) {
            user.sendMessage(SQLClient.addUser(arr[1], arr[2], arr[3]));
            return;
        }

        if (arr.length != 3 || !arr[0].equals(Library.AUTH_REQUEST)) {
            user.msgFormatError(msg);
            return;
        }

        String login = arr[1];
        String password = arr[2];
        String nickname = SQLClient.getNickname(login, password);
        if (nickname == null) {
            putLog("Invalid credentials for user " + login);
            user.authFail();
            return;
        } else {
            ClientThread oldClient = findClientByNickname(nickname);
            user.authAccept(nickname);
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
            ClientThread user = (ClientThread) s;
            if (!user.isAuthorized()) continue;
            s.sendMessage(msg);
        }
    }

    private synchronized void sendPrivateMessage(String msg, String sender, String client) {
        ClientThread user = findClientByNickname(client);
        if (user != null && user.isAuthorized())
            user.sendMessage(Library.getTypePrivateMessage(sender, msg));

        System.out.println(Library.getTypePrivateMessage(sender, msg));
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
