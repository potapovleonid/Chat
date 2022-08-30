package ru.home.des.chat.server.core;

import ru.home.des.chat.library.Library;
import ru.home.des.chat.network.SocketThread;
import ru.home.des.chat.network.SocketThreadListener;

import java.net.Socket;

public class ClientThread extends SocketThread {

    private String nickname;
    private boolean isAuthorized;
    private boolean isReconnected;

    public ClientThread(SocketThreadListener listener, String name, Socket socket) {
        super(listener, name, socket);
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    void authAccept(String nickname){
        isAuthorized = true;
        this.nickname = nickname;
        sendMessage(Library.getAuthAccept(nickname));
    }

    void authFail(){
        sendMessage(Library.getAuthDenied());
        close();
    }

    void msgFormatError(String msg){
        sendMessage(Library.getMsgFormatError(msg));
        close();
    }

    void reconnected(){
        isAuthorized = true;
        close();
    }

    public boolean isReconnected() {
        return isReconnected;
    }
}
