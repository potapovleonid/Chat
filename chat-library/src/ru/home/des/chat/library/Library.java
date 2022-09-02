package ru.home.des.chat.library;

public class Library {
    public static final String DELIMITER = "±";

    private static final String REGISTRATION_REQUEST = "/reg_request";
    private static final String REGISTRATION_ACCEPT = "/reg_accept";
    private static final String REGISTRATION_DENIED = "/reg_denied";

    private static final String CHANGE_PASSWORD_REQUEST = "/change_psw_request";
    private static final String CHANGE_PASSWORD_ACCEPT = "/change_psw_request";
    private static final String CHANGE_PASSWORD_DENIED = "/change_psw_request";

    private static final String CHANGE_NICKNAME_REQUEST = "/change_nick_request";
    private static final String CHANGE_NICKNAME_ACCEPT = "/change_nick_request";
    private static final String CHANGE_NICKNAME_DENIED = "/change_nick_request";

    public static final String AUTH_REQUEST = "/auth_request";
    public static final String AUTH_ACCEPT = "/auth_accept";
    public static final String AUTH_DENIED = "/auth_denied";

    public static final String MSG_FORMAT_ERROR = "/msg_format_error";

    public static final String TYPE_BROADCAST = "/bcast";
    public static final String TYPE_BROADCAST_CLIENT = "/client_msg";
    public static final String TYPE_PRIVATE_MESSAGE = "/private_msg";

    public static final String USER_LIST = "/user_list";

    public static String getRegistrationRequest(String login, String password, String nickname){
//        TODO password hash code
        return REGISTRATION_REQUEST + DELIMITER + login + DELIMITER + password + DELIMITER + nickname;
    }

    public static String getRegistrationAccept(String nickname){
        return REGISTRATION_ACCEPT + DELIMITER + nickname;
    }

    public static String getRegistrationDenied(){
        return REGISTRATION_DENIED;
    }

    public static String getAuthRequest(String login, String password) {
        return AUTH_REQUEST + DELIMITER + login + DELIMITER + password;
    }

    public static String getAuthAccept(String nickname) {
        return AUTH_ACCEPT + DELIMITER + nickname;
    }

    public static String getAuthDenied() {
        return AUTH_DENIED;
    }

    public static String getUserList(String users) {
        return USER_LIST + DELIMITER + users;
    }

    public static String getMsgFormatError(String msg) {
        return MSG_FORMAT_ERROR + DELIMITER + msg;
    }

    public static String getTypeBroadcast(String src, String msg) {
        return TYPE_BROADCAST + DELIMITER + System.currentTimeMillis() + DELIMITER + src + DELIMITER + msg;
    }

    public static String getTypePrivateMessage(String sender, String message) {
        return TYPE_PRIVATE_MESSAGE + DELIMITER + System.currentTimeMillis() + DELIMITER + sender + DELIMITER + message;
    }


    public static String getTypeBroadcastClient(String msg) {
        return TYPE_BROADCAST_CLIENT + DELIMITER + msg;
    }


}
