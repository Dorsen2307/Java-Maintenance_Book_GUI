package ru.ekb.app.utilites;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Properties;

public class FileDB {

    public static boolean isFile() throws Exception {
        File file = new File("src/ru/ekb/app/mainDB.sql");
        if (!file.exists()) {
            return file.createNewFile();
        }

        return false;
    }

    public static boolean connectDriver() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();

        return true;
    }

    public static Connection connectDB() throws Exception {
        try (Connection connection = getConnect()) {
            System.out.println("Успешное соединение с БД!");
            return connection;
        }
    }

    private static Connection getConnect() throws Exception {
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get("src/ru/ekb/app/database.properties"))) {
            properties.load(in);
        }
        String url = properties.getProperty("url");
        String userName = properties.getProperty("userName");
        String password = properties.getProperty("password");

        return DriverManager.getConnection(url, userName, password);
    }

    public static boolean isTable(Connection connection, String table) throws Exception {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet tables = meta.getTables(null, null, table, null);

        if (!tables.next()) {
            return createdTable(connection);
        }

        return false;
    }

    private static boolean createdTable(Connection connection) throws Exception {

    }
}
