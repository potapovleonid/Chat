package ru.home.des.chat.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketThread extends Thread {
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
        try {
            listener.onSocketStart(this, socket);

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
            listener.onSocketStop(this);
        }
    }

    public synchronized boolean sendMessage(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
            return true;
        } catch (IOException e) {
            listener.onSocketException(this, e);
            close();
            return false;
        }

    }

    public synchronized void close() {
        try {
            interrupt();
            socket.close();
        } catch (IOException e) {
            listener.onSocketException(this, e);
        }
    }

}
