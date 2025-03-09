package ru.ekb.app.utilites;

import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.sql.Date;

public class FileDB {
    public static Connection connection;
    public static final String dbName = "maindb";
    public static String[] columnNames;

    public static void getConnectionDb(DefaultTableModel tableModel) {
        if (!connectDriver()) { // подключаемся к Драйверу
            return;
        }
        System.out.println("Драйвер найден.");

        connection = connectDB(); // соединяемся с БД
        if (connection == null) return;

        if (!isFile(connection, dbName)) return; // если БД не существует, то выходим

        // проверяем наличие таблицы 'main', если нет - создаем и считываем данные с таблицы
        ResultSet resultSet = isTable(connectDB(), "main", dbName);
        readAllData(tableModel, resultSet);
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            System.out.println("Ошибка при закрытии ресурсов: " + e.getMessage());
        }
    }

    private static void readAllData(DefaultTableModel tableModel, ResultSet resultSet) {
        if (resultSet != null) {
            System.out.println("Данные получены.");
            try {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                if (columnCount > 0) {
                    columnNames = new String[columnCount];

                    // Добавляем имена колонок в модель таблицы
                    for (int i = 1; i <= columnCount; i++) {
                        columnNames[i - 1] = metaData.getColumnName(i);
                        String convertedName = convertedHeader(columnNames[i - 1]); // заменяем на рус.яз.
                        tableModel.addColumn(convertedName);
                    }
                    System.out.println("Имена колонок конвертированы и добавлены в модель таблицы.");

                    // Добавляем строки в модель таблицы
                    setRowFromDB(tableModel, resultSet, columnCount);
                } else {
                    System.out.println("Нет доступных колонок в результате.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка данных: " + e);
            }
        } else {
            System.out.println("Данные отсутствуют.");
        }
        try {
            if (resultSet != null) resultSet.close();
            if (connection != null) connection.close();
        } catch (SQLException e) {
            System.out.println("Ошибка при закрытии ресурсов: " + e.getMessage());
        }
    }

    public static void setRowFromDB(DefaultTableModel tableModel, ResultSet resultSet, int columnCount) throws SQLException {
        while (resultSet.next()) {
            Object[] row = new Object[columnCount]; // создаем объект, состоящий из данных в количестве columnCount
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = resultSet.getObject(i); // считываем в наш массив данные строки и т.д.
            }
            tableModel.addRow(row); // добавляем в модель таблицы строку
        }
        System.out.println("Данные добавлены в модель таблицы.");
    }

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
            } else {
                System.out.println("База данных 'maindb' не найдена.");
                // если не БД обнаружена - создаем новую
                createdDB(statement, dbName);
            }
            return true;

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

    private static void createdDB(Statement statement, String dbName) {
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
            if (selectedDB(statement, dbName)) return false;
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
            return false;
        } catch (SQLException e) {
            System.out.println("Ошибка при выборе БД: " + e);
            return true;
        }
    }

    public static ResultSet getDataAll(Connection connection, String dbName) {
        String sqlCommand = "SELECT * FROM main;"; // команда получения всех данных из таблицы

        try {
            Statement statement = connection.createStatement();
            // выбираем БД
            if (selectedDB(statement, dbName)) return null;
            // запрашиваем данные
            return statement.executeQuery(sqlCommand);
        }
        catch (Exception e) {
            System.out.println("Ошибка запроса (getDataAll): " + e);
            return null;
        }
    }

    public static String addDataDB(String[] inputData) {
        String addDataQuery = "INSERT INTO main (" +
                "TypeService, Regulations, ScheduledDate, LastServiceDate, " +
                "Volume, ManufacturerCode, SparePartsStock, Comment" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            connection = connectDB();
            assert connection != null;
            // выбираем БД
            PreparedStatement preparedStatement = connection.prepareStatement(addDataQuery);
            if (selectedDB(preparedStatement, dbName)) return "Ошибка выбора БД...";

            // добавляем данные в БД
            String column1Value = inputData[0];
            String column2Value = inputData[1];
            Date column3Value = inputData[2].isEmpty() ? null : parseDate(inputData[2]);
            Date column4Value = inputData[3].isEmpty() ? null : parseDate(inputData[3]);
            double column5Value = Double.parseDouble(inputData[4]);
            String column6Value = inputData[5];
            double column7Value = Double.parseDouble(inputData[6]);
            String column8Value = inputData[7];

            preparedStatement.setString(1, column1Value);
            preparedStatement.setString(2, column2Value);
            preparedStatement.setDate(3, column3Value);
            preparedStatement.setDate(4, column4Value);
            preparedStatement.setDouble(5, column5Value);
            preparedStatement.setString(6, column6Value);
            preparedStatement.setDouble(7, column7Value);
            preparedStatement.setString(8, column8Value);
            preparedStatement.executeUpdate();

            return "Данные успешно добавлены в БД!";
        } catch (SQLException e) {
            System.out.println("Ошибка SQL (updateDataDB): " + e);
            return "Ошибка выполнения SQL команды.";
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.out.println("Ошибка при закрытии ресурсов: " + e.getMessage());
            }
        }
    }

    public static void updateDataDB(int idRow, int indexColumn, Object valueCell) {
        String columnName = columnNames[indexColumn]; // Получаем имя столбца
        String updateDataQuery = "UPDATE main SET " + columnName + " = ? WHERE Id = ?";

        try {
            connection = connectDB();
            assert connection != null;
            PreparedStatement preparedStatement = connection.prepareStatement(updateDataQuery);
            selectedDB(preparedStatement, dbName);
            System.out.println("columnNames[" + (indexColumn) + "]: " + columnNames[indexColumn]);

            // Устанавливаем значение для столбца в зависимости от типа данных
            if (valueCell instanceof String) {
                preparedStatement.setString(1, (String) valueCell);
            } else if (valueCell instanceof Integer) {
                preparedStatement.setInt(1, (Integer) valueCell);
            } else if (valueCell instanceof Double) {
                preparedStatement.setDouble(1, (Double) valueCell);
            } else if (valueCell instanceof Boolean) {
                preparedStatement.setBoolean(1, (Boolean) valueCell);
            } else {
                System.out.println("Неизвестный тип данных для обновления");
                return;
            }

            preparedStatement.setInt(2, idRow);
            preparedStatement.executeUpdate();
            System.out.println("Значение изменено.");
        } catch (SQLException e) {
            System.out.println("Ошибка SQL (updateDataDB): " + e);
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.out.println("Ошибка при закрытии ресурсов: " + e.getMessage());
            }
        }
    }

    private static Date parseDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        try {
            java.util.Date parseDate = dateFormat.parse(date); // преобразуем текст в java.util.Date
            return new Date(parseDate.getTime()); // преобразуем java.util.Date в java.sql.Date
        } catch (ParseException e) {
            System.out.println("Ошибка парсинга даты: " + e);
            return null;
        }
    }

    public static void deleteRowDB(String idRow) {
        String deleteRowQuery = "DELETE FROM main WHERE Id = ?;";

        try {
            connection = connectDB();
            assert connection != null;
            PreparedStatement preparedStatement = connection.prepareStatement(deleteRowQuery);
            selectedDB(preparedStatement, dbName);
            preparedStatement.setString(1, idRow); // устанавливаем значение для параметра по ?
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Ошибка SQL (deleteRowDB): " + e);
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.out.println("Ошибка при закрытии ресурсов: " + e.getMessage());
            }
        }
    }

    public static Object getCellValue(Object idRow, int columnIndex) {
        String cellValueQuery = "SELECT * FROM main WHERE Id = ?;";
        Object result = null;

        try {
            connection = connectDB();
            assert connection != null;
            PreparedStatement preparedStatement = connection.prepareStatement(cellValueQuery);
            selectedDB(preparedStatement, dbName);
            preparedStatement.setInt(1, (Integer) idRow);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                // проверяем, что номер столбца корректен
                if (columnIndex > 0 && columnIndex <= columnCount) {
                    int columnType = metaData.getColumnType(columnIndex); // получаем тип данных по индексу
//                    System.out.println(metaData.getColumnName(columnIndex));

                    // считываем значение в зависимости от типа данных
                    switch (columnType) {
                        case Types.VARCHAR:
                        case Types.CHAR:
                            result = resultSet.getString(columnIndex); // Считываем как строку
                            break;
                        case Types.INTEGER:
                            result = resultSet.getInt(columnIndex); // Считываем как целое число
                            break;
                        case Types.DOUBLE:
                            result = resultSet.getDouble(columnIndex); // Считываем как число с плавающей запятой
                            break;
                        case Types.BOOLEAN:
                            result = resultSet.getBoolean(columnIndex); // Считываем как логическое значение
                            break;
                        case Types.DATE:
                            result = resultSet.getDate(columnIndex); // считываем как дату
                            break;
                        default:
                            System.out.println("Неизвестный тип данных"); // Обработка неизвестных типов
                            break;
                    }
                } else {
                    System.out.println("Неверный номер столбца"); // Обработка случая, когда номер столбца невалиден
                }
            } else {
                System.out.println("Строка не найдена"); // Обработка случая, когда строка не найдена
            }

        } catch (SQLException e) {
            System.out.println("Ошибка SQL (getCellValue): " + e);
        } finally {
            try {
                if (connection != null) connection.close();
            } catch(SQLException e){
                System.out.println("Ошибка при закрытии соединения: " + e);
            }
        }

        return result; // Возвращаем полученное значение или null, если не найдено
    }
}
