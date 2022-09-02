package ru.home.des.chat.server.core;

import java.sql.*;
import java.util.Properties;

public class SQLClient {

    private static Connection connection;
    private static Statement statement;

    synchronized static void connect(){
        try {
//            Class.forName("org.postgresql.jdbc");
            String url = "jdbc:postgresql://localhost/chat-server";

            connection = DriverManager.getConnection(url, "postgres", "postgres");
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static void disconnect(){
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static String getNickname(String login, String password){
        String query = String.format("SELECT nickname FROM users_tbl WHERE login='%s' and password='%s'", login, password);
        try (ResultSet set = statement.executeQuery(query)) {
            if (set.next()){
                return set.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
