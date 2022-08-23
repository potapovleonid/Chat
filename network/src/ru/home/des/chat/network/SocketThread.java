package ru.home.des.chat.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketThread extends Thread implements SocketThreadListener {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private SocketThreadListener listener;

    public SocketThread(SocketThreadListener listener, String name, Socket socket) {
        super(name);
        this.listener = listener;
        this.socket = socket;
        start();
    }

    @Override
    public void run() {
        listener.onSocketStart(this, socket);

        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            listener.onSocketReady(this, socket);
            while (!isInterrupted()) {
                String msg = in.readUTF();
                listener.onReceiveString(this, socket, msg);
            }
        } catch (IOException e) {
            listener.onSocketException(this, e);
        } finally {
            close();
        }
    }

    public synchronized boolean sendMessage(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
            return true;
        } catch (IOException e) {
            listener.onSocketException(this, e);
            listener.onSocketStop(this);
            return false;
        }

    }

    private synchronized void close() {
        interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onSocketStart(SocketThread thread, Socket socket) {

    }

    @Override
    public void onSocketStop(SocketThread thread) {
        close();
    }

    @Override
    public void onSocketReady(SocketThread thread, Socket socket) {

    }

    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String msg) {

    }

    @Override
    public void onSocketException(SocketThread thread, Exception exception) {

    }
}
