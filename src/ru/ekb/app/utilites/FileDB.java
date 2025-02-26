package ru.ekb.app.utilites;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
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

    public static void isTable(Connection connection, String table) throws Exception {
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet tables = meta.getTables(null, null, table, null);

        if (!tables.next()) {
            createdTable(connection);
        }

        System.out.println("Таблица 'main' существует.");
    }

    private static void createdTable(Connection connection) throws Exception {
        // команда создания таблицы
        String sqlCommand = "CREATE TABLE main (Id INT PRIMARY KEY AUTO_INCREMENT, " +
                "TypeService VARCHAR(50), " +
                "Regulations VARCHAR(20)," +
                "ScheduledDate DATE," +
                "LastServiceDate DATE," +
                "Volume INT," +
                "ManufacturerCode TEXT," +
                "SparePartsStock INT," +
                "Comment TEXT)";

        Statement statement = connection.createStatement();
        // создаем таблицу
        statement.executeUpdate(sqlCommand);
        System.out.println("Таблица 'main' создана!");
    }
}
