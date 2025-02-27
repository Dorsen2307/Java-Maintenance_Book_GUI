package ru.ekb.app.utilites;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class FileDB {

    public static boolean isFile(Connection connection, String dbName) {
        Statement statement = null;
        ResultSet resultSet = null;
        String query = "SHOW DATABASES;";

        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            boolean dbExists = false;

            // проверяем, есть ли БД с именем mainDB
            while (resultSet.next()) {
                String dbNameRes = resultSet.getString(1);
                if (dbNameRes.equalsIgnoreCase("maindb")) {
                    dbExists = true;
                    break;
                }
            }

            if (dbExists) {
                System.out.println("База данных 'maindb' существует.");
                return true;
            } else {
                System.out.println("База данных 'maindb' не найдена.");
                // если не БД обнаружена - создаем новую
                createdDB(connection, statement, dbName);
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Ошибка соединения (isFile): " + e.getMessage());
            return false;
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                System.out.println("Ошибка при закрытии ресурсов: " + e.getMessage());
            }
        }
    }

    private static void createdDB(Connection connection, Statement statement, String dbName) {
        try {
            String createDatabaseQuery = "CREATE DATABASE " + dbName + ";";
            statement.executeUpdate(createDatabaseQuery);
            System.out.println("База данных '" + dbName + "' успешно создана.");
        } catch (SQLException e) {
            System.out.println("Ошибка при создании базы данных: " + e.getMessage());
        }
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
            if (connection != null) {
                System.out.println("Успешное соединение с БД!");
            }
            return connection;
        }
        catch (Exception e) {
            System.out.println("Ошибка доступа к БД (connectDB): " + e);
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
            System.out.println("Ошибка доступа к БД (getConnect): " + e);
            return null;
        }
    }

    public static ResultSet isTable(Connection connection, String table, String dbName) {
        boolean resultCreatedTable;

        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet tables = meta.getTables(null, null, table, null);

            if (!tables.next()) {
                resultCreatedTable = createdTable(connection, dbName);
            } else {
                System.out.println("Таблица 'main' существует.");
                resultCreatedTable = true;
            }

            if (resultCreatedTable) {
                return getDataAll(connection, dbName);
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("Ошибка (isTable): " + e);
            return null;
        }
    }

    private static boolean createdTable(Connection connection, String dbName) {
        Statement statement = null;
        // команда создания таблицы
        String createDatabaseQuery = "CREATE TABLE main (Id INT PRIMARY KEY AUTO_INCREMENT, " +
                "TypeService VARCHAR(100), " +
                "Regulations VARCHAR(20)," +
                "ScheduledDate DATE," +
                "LastServiceDate DATE," +
                "Volume INT," +
                "ManufacturerCode VARCHAR(500)," +
                "SparePartsStock INT," +
                "Comment VARCHAR(500));";

        try {
            statement = connection.createStatement();
            // выбираем БД
            if (!selectedDB(statement, dbName)) return false;
            // создаем таблицу
            statement.executeUpdate(createDatabaseQuery);
            System.out.println("Таблица 'main' создана!");
            return true;
        } catch (SQLException e) {
            System.out.println("Ошибка при создании таблицы (createdTable): " + e);
            return false;
        } finally {
            try {
                if (statement != null) statement.close();
            } catch (SQLException e) {
                System.out.println("Ошибка при закрытии ресурсов: " + e.getMessage());
            }
        }
    }

    public static String convertedHeader(String head) {
        Map<String, String > listHeads = new HashMap<>();
        listHeads.put("Id", "ИД");
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

    private static boolean selectedDB(Statement statement, String dbName) {
        try {
            // выбираем БД
            statement.executeUpdate("USE " + dbName + ";");
            System.out.println("База данных '" + dbName + "' выбрана.");
            return true;
        } catch (SQLException e) {
            System.out.println("Ошибка при выборе БД: " + e);
            return false;
        }
    }

    public static ResultSet getDataAll(Connection connection, String dbName) {
        String sqlCommand = "SELECT * FROM main;"; // команда получения всех данных из таблицы

        String useTableQuery = "USE maindb;";
        try {
            Statement statement = connection.createStatement();
            // выбираем БД
            if (!selectedDB(statement, dbName)) return null;
            // запрашиваем данные
            return statement.executeQuery(sqlCommand);
        }
        catch (Exception e) {
            System.out.println("Ошибка запроса (getDataAll): " + e);
            return null;
        }
    }
}
