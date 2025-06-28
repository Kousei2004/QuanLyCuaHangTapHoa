package com.example.da.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;

public class MySQLConnection {
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        String url = "jdbc:mysql://localhost:3306/store?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, password);
    }
}