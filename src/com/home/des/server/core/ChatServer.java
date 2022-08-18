package com.home.des.server.core;

public class ChatServer {

    public void start(int port){
        System.out.printf("Server started at port: %d\n", port);
    }

    public void stop(){
        System.out.printf("Server stopped\n");
    }


}
