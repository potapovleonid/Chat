package ru.home.des.chat.server.core;

import ru.home.des.chat.library.Library;

import java.sql.*;

public class SQLClient {

    private static Connection connection;
    private static Statement statement;

    synchronized static void connect(String ipDB, String nameDB, String login, String password) {
        try {
            String url = String.format("jdbc:postgresql://%s/%s", ipDB, nameDB);
            connection = DriverManager.getConnection(url, login, password);
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static String getNickname(String login, String password) {
        String query = String.format("SELECT nickname FROM users_tbl WHERE login='%s' and password='%s'", login, password);
        try (ResultSet set = statement.executeQuery(query)) {
            if (set.next()) {
                return set.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    synchronized static String addUser(String login, String password, String nickname) {
        String query = String.format("INSERT INTO users_tbl values ('%s', '%s', '%s')", login, password, nickname);
        try {
            int rows = statement.executeUpdate(query);
            if (rows != 0) {
                return Library.getRegistrationAccept();
            }
        } catch (SQLException e) {
            return Library.getRegistrationDenied();
        }
        return null;
    }

}
