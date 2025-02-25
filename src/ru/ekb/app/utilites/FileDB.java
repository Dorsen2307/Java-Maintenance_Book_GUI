package ru.ekb.app.utilites;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static java.sql.DriverManager.getConnection;

public class FileDB {

    public static boolean isFile() throws Exception {
        File file = new File("src/ru/ekb/app/mainDB.mysql");
        if (!file.exists()) {
            return file.createNewFile();
        }

        return false;
    }

    public static boolean connectDriver() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();

        return true;
    }

    public static void connectDB() throws Exception {
        try (Connection connection = getConnection()) {
            System.out.println("Успешное соединение с БД!");
        }
    }

    public static Connection getConnection() throws Exception {
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get("src/ru/ekb/app/database.properties"))) {
            properties.load(in);
        }
        String nameDB = properties.getProperty("nameDB");
        String url = properties.getProperty("url");
        String userName = "";
//        String userName = properties.getProperty("userName");
        String password = "";
//        String password = properties.getProperty("password");

        return DriverManager.getConnection(url + nameDB, userName, password);
    }
}
