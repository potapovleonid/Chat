package ru.home.des.chat.client;

import ru.home.des.chat.library.Library;
import ru.home.des.chat.network.SocketThread;
import ru.home.des.chat.network.SocketThreadListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class ClientGUI implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {
    private final float version = 1.1f;

    /**
     * Authority window values
     **/
    private final JFrame authFrame = new JFrame();
    private final JLabel lAuthDescription =
            new JLabel("<html><p style='text-align: center'>Welcome on chat.<br>Please authority with " +
                    "you credentials or if you don't have one - click on 'New account'</p></html>");

    private GroupLayout authLayout = new GroupLayout(authFrame.getContentPane());

    private final JTextField tfAuthIPAddress = new JTextField("127.0.0.1");
    private final JTextField tfAuthPort = new JTextField("8181");
    private final JTextField tfAuthLogin = new JTextField("");
    private final JPasswordField tfAuthPassword = new JPasswordField("");
    private final JButton btnAuthLogin = new JButton("Login");
    private final JButton btnAuthRegistration = new JButton("New account");

    /**
     * Chat Frame values
     **/
    private static final int WIDTH = 550;
    private static final int HEIGHT = 350;
    private final String WINDOW_TITLE = "Chat v" + version;

    private final JFrame chatFrame = new JFrame();

    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JTextArea log = new JTextArea();
    private final JList<String> userList = new JList<>();
    private final JButton btnDisconnect = new JButton("<html><b>Disconnect</b></html>");
    private final JTextField tfMessage = new JTextField();
    private final JButton btnSend = new JButton("Send");

    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm");

    /**
     * Registry window values
     **/
    private final JFrame regFrame = new JFrame();

    private final JPanel panelRegTop = new JPanel();
    private final JPanel panelRegCenter = new JPanel(new GridLayout(3, 2));
    private final JPanel panelRegBottom = new JPanel();

    private final JLabel lRegDescription =
            new JLabel("<html><p style='text-align: center'>Input the credentials you want.<br>Each field " +
                    "can contains one word without spaces.</p></html>");

    private final JLabel lRegLogin = new JLabel("Login");
    private final JTextField tfRegLogin = new JTextField("");
    private final JLabel lRegPass = new JLabel("Password");
    private final JPasswordField tfRegPass = new JPasswordField("");
    private final JLabel lRegNick = new JLabel("Nickname");
    private final JTextField tfRegNick = new JTextField("");
    private final JButton btnRegCreateAcc = new JButton("<html><b>Create</b></html>");
    private final JButton btnRegCancel = new JButton("Cancel");

    private boolean isRegisterProcess = false;
    private boolean isNeedClearLogAfterRegister = false;

    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Always on top");
    /**
     * Settings window values
     **/

    private final JWindow optionPanel = new JWindow();

    private SocketThread socketThread;

    private String privateMessage;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }

    private ClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        createAuthFrame();
        createRegistryFrame();
        createChatFrame();
//        TODO create authorization, settings frames
    }

    public void createAuthFrame() {
        authFrame.setTitle("Welcome to chat");
        authFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        authFrame.setLocationRelativeTo(null);
        authFrame.setResizable(false);
        authFrame.setSize(550, 180);

        tfAuthLogin.setHorizontalAlignment(SwingConstants.CENTER);
        tfAuthPassword.setHorizontalAlignment(SwingConstants.CENTER);
        tfAuthIPAddress.setHorizontalAlignment(SwingConstants.CENTER);
        tfAuthPort.setHorizontalAlignment(SwingConstants.CENTER);

        lAuthDescription.setHorizontalAlignment(SwingConstants.CENTER);

        btnAuthRegistration.addActionListener(this);
        btnAuthLogin.addActionListener(this);

        authFrame.getContentPane().setLayout(authLayout);

        authLayout.setAutoCreateGaps(true);
        authLayout.setAutoCreateContainerGaps(true);

        authLayout.setHorizontalGroup(authLayout.createParallelGroup()
                .addGroup(authLayout.createSequentialGroup()
                        .addGroup(authLayout.createParallelGroup()
                                .addComponent(lAuthDescription)))
                .addGroup(authLayout.createSequentialGroup()
                        .addGroup(authLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(tfAuthIPAddress)
                                .addComponent(tfAuthLogin)
                                .addComponent(btnAuthLogin))
                        .addGroup(authLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(tfAuthPort)
                                .addComponent(tfAuthPassword)
                                .addComponent(btnAuthRegistration)))
        );
        authLayout.linkSize(SwingConstants.HORIZONTAL, btnAuthLogin, btnAuthRegistration);
        authLayout.setVerticalGroup(authLayout.createSequentialGroup()
                .addGroup(authLayout.createSequentialGroup()
                        .addGroup(authLayout.createParallelGroup()
                                .addComponent(lAuthDescription)))
                .addGroup(authLayout.createSequentialGroup()
                        .addGroup(authLayout.createParallelGroup()
                                .addComponent(tfAuthIPAddress)
                                .addComponent(tfAuthPort))
                        .addGroup(authLayout.createParallelGroup()
                                .addComponent(tfAuthLogin)
                                .addComponent(tfAuthPassword))
                        .addGroup(authLayout.createParallelGroup()
                                .addComponent(btnAuthLogin)
                                .addComponent(btnAuthRegistration)))
        );

        authFrame.setVisible(true);
    }

    private void createRegistryFrame() {
        regFrame.setTitle("Registration new account");
        regFrame.setLocationRelativeTo(null);
        regFrame.setSize(400, 200);
        regFrame.setResizable(false);

        regFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        btnRegCreateAcc.addActionListener(this);
        btnRegCancel.addActionListener(this);

        lRegLogin.setHorizontalAlignment(JLabel.CENTER);
        lRegPass.setHorizontalAlignment(JLabel.CENTER);
        lRegNick.setHorizontalAlignment(JLabel.CENTER);

        panelRegTop.add(lRegDescription);

        panelRegCenter.add(lRegLogin);
        panelRegCenter.add(tfRegLogin);
        panelRegCenter.add(lRegPass);
        panelRegCenter.add(tfRegPass);
        panelRegCenter.add(lRegNick);
        panelRegCenter.add(tfRegNick);

        panelRegBottom.add(btnRegCreateAcc);
        panelRegBottom.add(btnRegCancel);

        panelRegCenter.setBorder(new EmptyBorder(10, 10, 10, 10));

        regFrame.add(panelRegTop, BorderLayout.NORTH);
        regFrame.add(panelRegCenter, BorderLayout.CENTER);
        regFrame.add(panelRegBottom, BorderLayout.SOUTH);
    }

    private void createChatFrame() {
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.setLocationRelativeTo(null);
        chatFrame.setSize(WIDTH, HEIGHT);
        chatFrame.setTitle(WINDOW_TITLE);

        JScrollPane scrollLog = new JScrollPane(log);
        JScrollPane scrollUsers = new JScrollPane(userList);

        scrollUsers.setPreferredSize(new Dimension(100, 0));

        cbAlwaysOnTop.addActionListener(this);
        btnSend.addActionListener(this);
        tfMessage.addActionListener(this);
        btnDisconnect.addActionListener(this);
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                privateMessage = userList.getSelectedValue();
                chatFrame.setTitle(WINDOW_TITLE + ": " + "You are now writing to PM for " + privateMessage);
            }
        });

        panelBottom.add(btnDisconnect, BorderLayout.WEST);
        panelBottom.add(tfMessage, BorderLayout.CENTER);
        panelBottom.add(btnSend, BorderLayout.EAST);

        chatFrame.add(scrollLog, BorderLayout.CENTER);
        chatFrame.add(scrollUsers, BorderLayout.EAST);
        chatFrame.add(panelBottom, BorderLayout.SOUTH);

        log.setEditable(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == cbAlwaysOnTop) {
            chatFrame.setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        } else if (source == btnSend || source == tfMessage) {
            sendMessage();
        } else if (source == btnAuthLogin) {
            connect();
        } else if (source == btnDisconnect) {
            socketThread.close();
        } else if (source == btnAuthRegistration) {
                hideAndVisibleFrames(authFrame, regFrame);
                isRegisterProcess = true;
                connect();
        } else if (source == btnRegCreateAcc) {
            if (checkRegistryFields()) {
                sendRegistrationMessage();
            }
        } else if (source == btnRegCancel) {
            socketThread.close();
            isRegisterProcess = false;
            hideAndVisibleFrames(regFrame, authFrame);
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
            Socket socket = new Socket(tfAuthIPAddress.getText(), Integer.parseInt(tfAuthPort.getText()));
            socketThread = new SocketThread(this, "Client", socket);
        } catch (IOException e) {
            if (e.getMessage().equals("Connection refused: connect")){
                hideAndVisibleFrames(regFrame, authFrame);
                showInfo(Thread.currentThread(), new Throwable("Server it's not run"));
            } else {
                showException(Thread.currentThread(), e);
            }
            isRegisterProcess = false;
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
            chatFrame.setTitle(WINDOW_TITLE);
            return;
        }
        socketThread.sendMessage(Library.getTypeBroadcastClient(msg));
    }

    private void sendRegistrationMessage() {
        socketThread.sendMessage(
                Library.getRegistrationRequest(tfRegLogin.getText(), tfRegPass.getText().hashCode(), tfRegNick.getText()));
    }

    private boolean checkRegistryFields() {
        if (tfRegLogin.getText().equals("") && tfRegPass.getText().equals("") && tfRegNick.getText().equals("")) {
            showInfo(Thread.currentThread(), new Throwable("Enter text in all fields"));
            return false;
        }
        if (tfRegLogin.getText().split(" ").length != 1
                || 1 != tfRegPass.getText().split(" ").length
                || tfRegNick.getText().split(" ").length != 1) {
            showInfo(Thread.currentThread(), new Throwable("Input login, password or nickname isn't one word"));
            return false;
        }
        return true;
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
            String login = tfAuthLogin.getText();
            String password = new String(tfAuthPassword.getPassword());
            thread.sendMessage(Library.getAuthRequest(login, password.hashCode()));
            hideAndVisibleFrames(authFrame, chatFrame);
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
        if (isRegisterProcess) {
            hideAndVisibleFrames(regFrame, authFrame);
        } else {
            hideAndVisibleFrames(chatFrame, authFrame);
        }
    }

    private void hideAndVisibleFrames(JFrame hideFrame, JFrame viewFrame) {
        hideFrame.setVisible(false);
        viewFrame.setVisible(true);
    }

    private void resultRegistration(String msg) {
        showInfo(socketThread, new Throwable(msg));
        isNeedClearLogAfterRegister = true;
    }

    private void clearLog() {
        SwingUtilities.invokeLater(() -> {
            log.setText("");
        });
    }

    private void clearRegistryFields() {
        tfRegLogin.setText("");
        tfRegPass.setText("");
        tfRegNick.setText("");
    }

    private void parseMessage(String msg) {
        String[] arrMsg = msg.split(Library.DELIMITER);
        String msgType = arrMsg[0];

        switch (msgType) {
            case Library.REGISTRATION_ACCEPT:
                resultRegistration("Account success registration");
                clearRegistryFields();
                hideAndVisibleFrames(regFrame, authFrame);
                isRegisterProcess = false;
                socketThread.close();
                break;
            case Library.REGISTRATION_DENIED:
                resultRegistration("Input login or nickname already use");
                break;
            case Library.AUTH_ACCEPT:
                putLog("Welcome " + arrMsg[1]);
                break;
            case Library.AUTH_DENIED:
                showInfo(socketThread, new Throwable("Unknown login or password"));
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
