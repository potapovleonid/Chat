package ru.home.des.chat.network;

public class ServerSocketThread extends Thread {
    private int port;

    public ServerSocketThread(String name, int port) {
        super(name);
        this.port = port;
        start();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            System.out.println("Server socket thread is working");
            try {
                sleep(10000);
            } catch (InterruptedException e) {
                interrupt();
            }
        }
    }
}
