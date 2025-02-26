package ru.ekb.app.utilites;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

import static javax.swing.UIManager.put;

public class FileDB {

    public static boolean isFile() throws Exception {
        File file = new File("src/ru/ekb/app/mainDB.sql");
        if (!file.exists()) {
            return file.createNewFile();
        }

        return false;
    }

    public static boolean connectDriver(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            return true;
        } catch (ClassNotFoundException e) {
            System.out.println("Драйвер не найден: " + e);
            return false;
        } catch (NoSuchMethodException e) {
            System.out.println("Ошибка драйвера: " + e);
            return false;
        } catch (InvocationTargetException e) {
            System.out.println("Ошибка вызова цели драйвера: " + e);
            return false;
        } catch (InstantiationException e) {
            System.out.println("Ошибка создания экземпляра драйвера: " + e);
            return false;
        } catch (IllegalAccessException e) {
            System.out.println("Ошибка доступа драйвера: " + e);
            return false;
        }
    }

    public static Connection connectDB() {
        try {
            Connection connection = getConnect();
            System.out.println("Успешное соединение с БД!");
            return connection;
        }
        catch (Exception e) {
            System.out.println("Ошибка доступа к БД: " + e);
            return null;
        }
    }

    private static Connection getConnect(){
        Properties properties = new Properties();
        try {
            try (InputStream in = Files.newInputStream(Paths.get("src/ru/ekb/app/database.properties"))) {
                properties.load(in);
            }
            String url = properties.getProperty("url");
            String userName = properties.getProperty("userName");
            String password = properties.getProperty("password");

            return DriverManager.getConnection(url, userName, password);
        }
        catch (IOException e) {
            System.out.println("Ошибка ввода/вывода: " + e);
            return null;
        }
        catch (SQLException e) {
            System.out.println("Ошибка доступа к БД: " + e);
            return null;
        }


    }

    public static ResultSet isTable(Connection connection, String table) {
        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet tables = meta.getTables(null, null, table, null);

            if (!tables.next()) {
                createdTable(connection);
            } else {
                System.out.println("Таблица 'Main' существует.");
            }

            return getDataAll(connection);
        } catch (Exception e) {
            System.out.println("Ошибка: " + e);
            return null;
        }
    }

    private static void createdTable(Connection connection) throws Exception {
        // команда создания таблицы
        String sqlCommand = "CREATE TABLE Main (Id INT PRIMARY KEY AUTO_INCREMENT, " +
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
        System.out.println("Таблица 'Main' создана!");
    }

    public static String convertedHeader(String head) {
        Map<String, String > listHeads = new HashMap<>();
        listHeads.put("TypeService", "Вид обслуживания");
        listHeads.put("Regulations", "Регламент");
        listHeads.put("ScheduledDate", "Дата по плану");
        listHeads.put("LastServiceDate", "Последнее обслуживание");
        listHeads.put("Volume", "Объем");
        listHeads.put("ManufacturerCode", "Производитель/Код");
        listHeads.put("SparePartsStock", "Запчастей в наличии");
        listHeads.put("Comment", "Комментарий");

        return listHeads.get(head);
    }

    public static ResultSet getDataAll(Connection connection) {
        String sqlCommand = "SELECT * FROM Main"; // команда создания таблицы
        try {
            Statement statement = connection.createStatement();
            // запрашиваем данные
            return statement.executeQuery(sqlCommand);
        }
        catch (Exception e) {
            System.out.println("Ошибка запроса: " + e);
            return null;
        }
    }
}
