package ru.home.des.chat.server.gui;

import ru.home.des.chat.server.core.ChatServer;
import ru.home.des.chat.server.core.ChatServerListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, ChatServerListener {

    private final float version = 1.1f;

    private static final int POS_X = 800;
    private static final int POS_Y = 200;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 500;



    private final ChatServer chatServer = new ChatServer(this);
    private final JLabel lPortServer = new JLabel("Server's port for connections (1023 - 65535)");
    private final JSpinner tfPortServer = new JSpinner(new SpinnerNumberModel(
            new Integer(8181), // value
            new Integer(1023), // min
            new Integer(65535), // max
            new Integer(1)
    ));
    private final JLabel lIPAddress = new JLabel("IP Address DB");
    private final JTextField tfIPAddress = new JTextField();
    private final JLabel lNameDB = new JLabel("Name DB");
    private final JTextField tfDBName = new JTextField();
    private final JLabel lLogin = new JLabel("Login");
    private final JTextField tfLogin = new JTextField();
    private final JLabel lPassword = new JLabel("Password");
    private final JPasswordField tfPassword = new JPasswordField();
    private final JButton btnStart = new JButton("Start");
    private final JButton btnStop = new JButton("Stop");
    private final JPanel panelTop = new JPanel(new GridLayout(6, 2));
    private final JTextArea log = new JTextArea();


    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerGUI::new);
    }

    private ServerGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(POS_X, POS_Y, WIDTH, HEIGHT);
        setTitle("Chat server v" + version);

        setResizable(false);
        log.setEditable(false);

        lLogin.setHorizontalAlignment(JLabel.CENTER);
        lPassword.setHorizontalAlignment(JLabel.CENTER);
        lIPAddress.setHorizontalAlignment(JLabel.CENTER);
        lNameDB.setHorizontalAlignment(JLabel.CENTER);

        JScrollPane scrollPane = new JScrollPane(log);

        panelTop.add(lPortServer);
        panelTop.add(tfPortServer);
        panelTop.add(lIPAddress);
        panelTop.add(lNameDB);
        panelTop.add(tfIPAddress);
        panelTop.add(tfDBName);
        panelTop.add(lLogin);
        panelTop.add(lPassword);
        panelTop.add(tfLogin);
        panelTop.add(tfPassword);
        panelTop.add(btnStart);
        panelTop.add(btnStop);

        add(panelTop, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnStart.addActionListener(this);
        btnStop.addActionListener(this);

        setVisibleStart(true);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == btnStart) {
            if (tfLogin.getText().equals("") || tfPassword.getPassword().length == 0 ||
                    tfDBName.getText().equals("") || tfIPAddress.getText().equals("")) {
                JOptionPane.showMessageDialog(null, "One field for connections is empty",
                        "DB connection parameters", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
//            } else if (tfPortServer.getValue() || Integer.parseInt(tfPortServer.getText()) <= 0) {
//                JOptionPane.showMessageDialog(null, "Server port is empty or incorrectly",
//                        "Server port", JOptionPane.INFORMATION_MESSAGE);
//                return;
//            }
            chatServer.start((Integer) tfPortServer.getValue(), tfIPAddress.getText(), tfDBName.getText(),
                    tfLogin.getText(), String.copyValueOf(tfPassword.getPassword()));
        } else if (source == btnStop) {
            chatServer.stop();
        } else
            throw new RuntimeException("Unknown source: " + source);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        String msg;
        StackTraceElement[] ste = e.getStackTrace();
        msg = "Exception in thread " + t.getName() + " " + e.getClass().getCanonicalName() +
                ": " + e.getMessage() + "\n\t" + ste[0];
        JOptionPane.showMessageDialog(null, msg, "Exception", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

/**
 * Chat Server Listener methods
 **/

    @Override
    public void onChatServerMessage(String msg) {
        SwingUtilities.invokeLater(() -> {
            log.append(msg + "\n");

            log.setCaretPosition(log.getDocument().getLength());
        });
        if (msg != null) {
            if (msg.equals("Server started")) {
                setVisibleStart(false);
            } else if (msg.equals("Server stopped")) {
                setVisibleStart(true);
            }
        }
    }

    public void setVisibleStart(boolean visible){
        btnStart.setEnabled(visible);
        btnStop.setEnabled(!visible);
    }
}
