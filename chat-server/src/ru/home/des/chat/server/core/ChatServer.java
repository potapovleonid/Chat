package ru.home.des.chat.server.core;

import ru.home.des.chat.network.ServerSocketThread;

public class ChatServer {

    private ServerSocketThread server;

    public void start(int port){
        if (server == null) {
            System.out.printf("Server started at port: %d\n", port);
//            server = new ServerSocketThread("Server", port);
        } else {
            System.out.println("Server already started");
        }
    }

    public void stop(){
        System.out.println("Server stopped\n");
        if (server != null && server.isAlive()) {
            server.interrupt();
            server = null;
        } else {
            System.out.println("Server not running");
        }
    }


}
