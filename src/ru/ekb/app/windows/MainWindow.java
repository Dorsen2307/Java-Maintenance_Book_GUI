package ru.ekb.app.windows;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ru.ekb.app.utilites.*;

import java.nio.file.NoSuchFileException;
import java.sql.*;
import java.util.Objects;

public class MainWindow extends JFrame {
    private JFrame frame;
    private JPanel tablePanel, buttonsPanel, statusPanel, bottomPanel;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JButton addButton, deleteButton;

    public MainWindow(String winTitle, String iconPath, int w, int h) {
        super(winTitle);

        //создаем фрейм
        frame = new JFrame();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //стандартная реакция на закрытие

        //добавляем иконку
        try {
            ImageIcon iconFile = new ImageIcon(Objects.requireNonNull(MainWindow.class.getResource(iconPath)));
            frame.setIconImage(iconFile.getImage());
        } catch (NullPointerException e) {
            System.out.println("Проблема: " + e);
        }

        //устанавливаем размер окна
        setSize(w, h);

        // создаем модель таблицы
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);

        // проверяем существование файла БД
        try {
            if (FileDB.isFile()) { //
                JOptionPane.showMessageDialog(frame,
                        "<html><h3>Файл БД отсутствует.<br>Был создан новый файл БД!</h3>"
                );
                System.out.println("Файл БД отсутствует. Был создан новый файл БД!");
            }
            else {
                System.out.println("Файл БД существует.");
            }
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                    "<html><h3>При создании файла БД возникла ошибка...</h3>",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
            System.out.println("При создании файла БД возникла ошибка...");
        }

        if (!FileDB.connectDriver()) { // подключаемся к Драйверу
            return;
        }
        System.out.println("Драйвер найден.");

        Connection connection = FileDB.connectDB(); // соединяемся с БД

        // проверяем наличие таблицы 'main', если нет - создаем и считываем данные с таблицы
        ResultSet resultSet = FileDB.isTable(connection, "Main");
        if (resultSet != null) {
            try {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                // Добавляем имена колонок в модель таблицы
                for (int i = 1; i <= columnCount; i++) {
                    String convertedName = FileDB.convertedHeader(metaData.getColumnName(i)); // заменяем на рус.яз.
                    tableModel.addColumn(convertedName);
                }

                // Добавляем строки в модель таблицы
                while (resultSet.next()) {
                    Object[] row = new Object[columnCount]; // создаем объект, состоящий из данных в количестве columnCount
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = resultSet.getObject(i); // считываем в наш массив данные строки и т.д.
                    }
                    tableModel.addRow(row); // добавляем в модель таблицы строку
                }
            } catch (Exception e) {
                System.out.println("Ошибка данных: " + e);
            }
        }

        tablePanel = new JPanel(); // создаем основу для таблицы
        tablePanel.add(new JScrollPane(table)); // добавляем таблицу на панель со скроллом
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS)); // устанавливаем вертикальное построение

        // создаем кнопки
        addButton = new JButton("Добавить");
        deleteButton = new JButton("Удалить");

        // добавляем слушателя кнопкам
        addButton.setActionCommand("Add");
        addButton.addActionListener(new ButtonsListener());
        deleteButton.setActionCommand("Delete");
        deleteButton.addActionListener(new ButtonsListener());

        buttonsPanel = new JPanel(); // создаем панель кнопок
        buttonsPanel.add(addButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.setBackground(Color.LIGHT_GRAY); // серый окрас панели
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5)); // построение слева направо с отступами

        statusPanel = new JPanel(); // создаем панель для вывода статуса и информации
        statusLabel = new JLabel("Здесь будет статус или информация"); // создаем тикет
        statusPanel.add(statusLabel); // добавляем тикет на панель
        statusPanel.setBackground(Color.LIGHT_GRAY); // задаем цвет панели статуса
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5)); // построение слева направо с отступами
        // рисуем верхнюю границу панели для разделения
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));

        bottomPanel = new JPanel(); // создаем нижнюю панель для размещения панелей кнопок и статуса
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(buttonsPanel);
        bottomPanel.add(statusPanel);

        getContentPane().add(BorderLayout.CENTER, tablePanel); // добавляем панель с таблицей на окно по центру
        getContentPane().add(BorderLayout.SOUTH, bottomPanel); // добавляем нижнюю панель на окно внизу

        setLocationRelativeTo(null); //размещение окна по центру экрана
        setVisible(true);


    }

    private class ButtonsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // получаем команду и анализируем ее
            String command = e.getActionCommand();

            switch (command) {
                case "Add":
                    addRow();
                    break;
                case "Delete":
                    deleteRow();
                    break;
            }
        }

        private void addRow() {
            String[] inputData = new String[8]; // создаем массив для хранения введенных данных

            inputData[0] = JOptionPane.showInputDialog(frame, "Введите вид обслуживания:");
            if (inputData[0] == null) { // проверяем нажатие кнопки 'отмена'
                return;
            }
            inputData[1] = JOptionPane.showInputDialog(frame, "Введите регламент:");
            if (inputData[1] == null) {
                return;
            }
            inputData[2] = JOptionPane.showInputDialog(frame, "Введите дату по плану:");
            if (inputData[2] == null) {
                return;
            }
            inputData[3] = JOptionPane.showInputDialog(frame, "Введите дату последнего обслуживания:");
            if (inputData[3] == null) {
                return;
            }
            inputData[4] = JOptionPane.showInputDialog(frame, "Введите объем, необходимый для обслуживания:");
            if (inputData[4] == null) {
                return;
            }
            inputData[5] = JOptionPane.showInputDialog(frame, "Введите производителя и код продукта:");
            if (inputData[5] == null) {
                return;
            }
            inputData[6] = JOptionPane.showInputDialog(frame, "Введите количество в наличии:");
            if (inputData[6] == null) {
                return;
            }
            inputData[7] = JOptionPane.showInputDialog(frame, "Введите комментарий:");
            if (inputData[7] == null) {
                return;
            }

            // todo - если нужно чтобы конкретные поля были всегда заполнены, реализовать проверку
//            if (inputData[0].isEmpty()) {
//                JOptionPane.showMessageDialog(
//                        frame,
//                        "Все поля должны быть заполнены",
//                        "Ошибка",
//                        JOptionPane.ERROR_MESSAGE
//                );
//            }

            // добавляем новую строку в модель таблицы
            tableModel.addRow(inputData);

            // обновляем статус
            statusLabel.setText("Новые данные добавлены.");
        }

        private void deleteRow() {
            int selectedRow = table.getSelectedRow(); // получаем индекс выбранной строки
            tableModel.removeRow(selectedRow); // удаляем строку по индексу
            statusLabel.setText("Строка №" + (selectedRow + 1) + " удалена.");
        }

        //todo - добавить метод редактирования

    }
}
// todo - создать метод проверки файла БД и его создание при отсутствии
// todo - создать метод соединения с БД