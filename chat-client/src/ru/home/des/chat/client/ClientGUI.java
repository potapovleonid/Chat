package ru.home.des.chat.client;

import ru.home.des.chat.library.Library;
import ru.home.des.chat.network.SocketThread;
import ru.home.des.chat.network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {
    /**
     * TODO add registration panel
     **/
    private static final int WIDTH = 550;
    private static final int HEIGHT = 350;

    private final JTextArea log = new JTextArea();
    private final JPanel panelTop = new JPanel(new GridLayout(3, 3));
    private final JTextField tfIPAddress = new JTextField("127.0.0.1");
    private final JTextField tfPort = new JTextField("8181");
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Always on top");
    private final JTextField tfLogin = new JTextField("leonid");
    private final JPasswordField tfPassword = new JPasswordField("leonid123");
    private final JButton btnLogin = new JButton("Login");
    private final JButton btnRegistration = new JButton("New account");

    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JButton btnDisconnect = new JButton("<html><b>Disconnect</b></html>");
    private final JTextField tfMessage = new JTextField();
    private final JButton btnSend = new JButton("Send");

    private final String REGISTRATION_WINDOW_TITLE = "Registration new account";
    private final JPanel panelRegTop = new JPanel(new GridLayout(3, 1));
    private final JPanel panelRegBottom = new JPanel();
    private final JTextField tfRegLogin = new JTextField("Your login");
    private final JTextField tfRegPass = new JTextField("Your password");
    private final JTextField tfRegNick = new JTextField("Your nickname");
    private final JButton btnCreateAcc = new JButton("Create");

    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm");
    private final String WINDOW_TITLE = "Chat";

    private final JFrame registrFrame = new JFrame();

    private boolean isRegisterProcess = false;
    private boolean isNeedClearLogAfterRegister = false;

    private final JList<String> userList = new JList<>();
    private SocketThread socketThread;

    private String privateMessage;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }

    private ClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(WIDTH, HEIGHT);
        setTitle(WINDOW_TITLE);

        JScrollPane scrollLog = new JScrollPane(log);
        JScrollPane scrollUsers = new JScrollPane(userList);

        scrollUsers.setPreferredSize(new Dimension(100, 0));

// Adding listeners for buttons and objects
        cbAlwaysOnTop.addActionListener(this);
        btnSend.addActionListener(this);
        tfMessage.addActionListener(this);
        btnLogin.addActionListener(this);
        btnDisconnect.addActionListener(this);
        btnRegistration.addActionListener(this);
        btnCreateAcc.addActionListener(this);
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                privateMessage = userList.getSelectedValue();
            }
        });

        panelTop.add(tfIPAddress);
        panelTop.add(tfPort);
        panelTop.add(cbAlwaysOnTop);
        panelTop.add(tfLogin);
        panelTop.add(tfPassword);
        panelTop.add(btnLogin);
        panelTop.add(btnRegistration);
        panelBottom.add(btnDisconnect, BorderLayout.WEST);
        panelBottom.add(tfMessage, BorderLayout.CENTER);
        panelBottom.add(btnSend, BorderLayout.EAST);

        add(scrollLog, BorderLayout.CENTER);
        add(scrollUsers, BorderLayout.EAST);
        add(panelTop, BorderLayout.NORTH);
        add(panelBottom, BorderLayout.SOUTH);

        log.setEditable(false);
        log.setVisible(false);
        userList.setVisible(false);
        panelBottom.setVisible(false);

        setVisible(true);

        registrFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        registrFrame.setLocationRelativeTo(null);
        registrFrame.setSize(new Dimension(400, 130));
        registrFrame.setTitle(REGISTRATION_WINDOW_TITLE);

        panelRegTop.add(tfRegLogin);
        panelRegTop.add(tfRegPass);
        panelRegTop.add(tfRegNick);

        panelRegBottom.add(btnCreateAcc);

        registrFrame.add(panelRegTop, BorderLayout.NORTH);
        registrFrame.add(panelRegBottom, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == cbAlwaysOnTop) {
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        } else if (source == btnSend || source == tfMessage) {
            sendMessage();
        } else if (source == btnLogin) {
            connect();
        } else if (source == btnDisconnect) {
            socketThread.close();
        } else if (source == btnRegistration) {
            isRegisterProcess = true;
            registrFrame.setVisible(true);
        } else if (source == btnCreateAcc) {
            connect();
            sendRegistrationMessage();
        } else {
            throw new RuntimeException("Unknown source: " + source);
        }
    }

    private void showException(Thread t, Throwable e) {
        String msg;
        StackTraceElement[] ste = e.getStackTrace();
        if (ste.length == 0) {
            msg = "Empty Stacktrace";
        } else {
            msg = "Exception in thread " + t.getName() + " " +
                    e.getClass().getCanonicalName() + ": " +
                    e.getMessage() + "\n\t" + ste[0];
        }
        JOptionPane.showMessageDialog(null, msg, "Exception", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(Thread t, Throwable e) {
        String msg;
        StackTraceElement[] ste = e.getStackTrace();
        if (ste.length == 0) {
            msg = "Empty Stacktrace";
        } else {
            msg = e.getMessage();
        }
        JOptionPane.showMessageDialog(null, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void connect() {
        try {
            Socket socket = new Socket(tfIPAddress.getText(), Integer.parseInt(tfPort.getText()));
            socketThread = new SocketThread(this, "Client", socket);
        } catch (IOException e) {
            showException(Thread.currentThread(), e);
            registrFrame.setVisible(false);
        }
    }

    private void sendMessage() {
        String msg = tfMessage.getText();
        if (msg.equals("")) return;
        tfMessage.setText("");
        tfMessage.grabFocus();
        if (privateMessage != null) {
            socketThread.sendMessage(Library.getTypePrivateMessage(privateMessage, msg));
            privateMessage = null;
            return;
        }
        socketThread.sendMessage(Library.getTypeBroadcastClient(msg));
    }

    private void sendRegistrationMessage() {
        if (tfRegLogin.getText().equals("") && tfRegPass.getText().equals("") && tfRegNick.getText().equals("")) {
            showInfo(Thread.currentThread(), new Throwable("Enter text in all fields"));
            return;
        }
        if (tfRegLogin.getText().split(" ").length != 1
                && 1 != tfRegPass.getText().split(" ").length
                && tfRegNick.getText().split(" ").length != 1) {
            showInfo(Thread.currentThread(), new Throwable("Your login, password or nickname isn't one word"));
            return;
        }
        socketThread.sendMessage(
                Library.getRegistrationRequest(tfRegLogin.getText(), tfRegPass.getText().hashCode(), tfRegNick.getText()));
    }

    private void putLog(String msg) {
        if ("".equals(msg)) return;
        SwingUtilities.invokeLater(() -> {
            log.append(msg + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        });
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        showException(t, e);
        System.exit(1);
    }

//  Socket thread methods

    @Override
    public void onSocketStart(SocketThread thread, Socket socket) {
    }

    @Override
    public void onSocketStop(SocketThread thread) {
        isRegisterProcess = false;
        putLog("Server's connect stopped");
        if (isNeedClearLogAfterRegister) {
            clearLog();
        }
    }

    @Override
    public void onSocketReady(SocketThread thread, Socket socket) {
        if (!isRegisterProcess) {
            String login = tfLogin.getText();
            String password = new String(tfPassword.getPassword());
            thread.sendMessage(Library.getAuthRequest(login, password.hashCode()));
            setVisibleTopPanel(!isVisible());
        }
    }

    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String msg) {
        parseMessage(msg);
    }

    @Override
    public void onSocketException(SocketThread thread, Exception exception) {
        if (exception.getMessage() != null && !exception.getMessage().equals("Socket closed")) {
            showException(thread, exception);
        }
        setVisibleTopPanel(true);
    }

    private void setVisibleTopPanel(boolean vision) {
        panelTop.setVisible(vision);
        panelBottom.setVisible(!vision);
        log.setVisible(!vision);
        userList.setVisible(!vision);
    }

    private void resultRegistration(String msg) {
        registrFrame.setVisible(false);
        isRegisterProcess = false;
        showInfo(socketThread, new Throwable(msg));
        isNeedClearLogAfterRegister = true;
        socketThread.close();
        socketThread.interrupt();
    }

    private void clearLog() {
        SwingUtilities.invokeLater(() -> {
            log.setText("");
        });
    }

    private void parseMessage(String msg) {
        String[] arrMsg = msg.split(Library.DELIMITER);
        String msgType = arrMsg[0];

        switch (msgType) {
            case Library.REGISTRATION_ACCEPT:
                resultRegistration("Account success registration");
                break;
            case Library.REGISTRATION_DENIED:
                resultRegistration("This login or password already use");
                break;
            case Library.AUTH_ACCEPT:
                putLog("Welcome " + arrMsg[1]);
                break;
            case Library.AUTH_DENIED:
                showInfo(socketThread, new Throwable("Unknown login or password"));
                setVisibleTopPanel(true);
                break;
            case Library.MSG_FORMAT_ERROR:
                putLog(msg);
                socketThread.close();
                break;
            case Library.TYPE_BROADCAST:
                putLog(DATE_FORMAT.format(Long.parseLong(arrMsg[1])) + " " + arrMsg[2] + ": " + arrMsg[3]);
                break;
            case Library.USER_LIST:
                String users = msg.substring(Library.USER_LIST.length() +
                        Library.DELIMITER.length());
                String[] usersArr = users.split(Library.DELIMITER);
                Arrays.sort(usersArr);
                userList.setListData(usersArr);
                break;
            case Library.TYPE_PRIVATE_MESSAGE:
                putLog(DATE_FORMAT.format(Long.parseLong(arrMsg[1])) + " [PM] " + arrMsg[2] + ": " + arrMsg[3]);
                break;
            default:
                throw new RuntimeException("Unknown message type: " + msg);
        }

    }

}
