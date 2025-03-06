package ru.ekb.app.windows;

import ru.ekb.app.utilites.FileDB;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Objects;

public class MainWindow extends JFrame {
    private final JFrame frame;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JLabel statusLabel;

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

        // подключаемся к БД и считываем все данные
        FileDB.getConnectionDb(tableModel);

        JPanel tablePanel = new JPanel(); // создаем основу для таблицы
        tablePanel.add(new JScrollPane(table)); // добавляем таблицу на панель со скроллом
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS)); // устанавливаем вертикальное построение

        // создаем кнопки
        JButton addButton = new JButton("Добавить");
        JButton deleteButton = new JButton("Удалить");

        // добавляем слушателя кнопкам
        addButton.setActionCommand("Add");
        addButton.addActionListener(new ButtonsListener());
        deleteButton.setActionCommand("Delete");
        deleteButton.addActionListener(new ButtonsListener());

        JPanel buttonsPanel = new JPanel(); // создаем панель кнопок
        buttonsPanel.add(addButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.setBackground(Color.LIGHT_GRAY); // серый окрас панели
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5)); // построение слева направо с отступами

        JPanel statusPanel = new JPanel(); // создаем панель для вывода статуса и информации
        statusLabel = new JLabel("Здесь будет статус или информация"); // создаем тикет
        statusPanel.add(statusLabel); // добавляем тикет на панель
        statusPanel.setBackground(Color.LIGHT_GRAY); // задаем цвет панели статуса
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5)); // построение слева направо с отступами
        // рисуем верхнюю границу панели для разделения
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));

        JPanel bottomPanel = new JPanel(); // создаем нижнюю панель для размещения панелей кнопок и статуса
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

            System.out.println("Активирована кнопка 'Добавить'");

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

            // обновляем данные в БД
            String resultUpdateDataDB = FileDB.updateDataDB(tableModel, inputData);
            statusLabel.setText(resultUpdateDataDB);
            System.out.println(resultUpdateDataDB);

            // обновляем данные в модели
            updateDataModel();
        }

        private void deleteRow() {
            int selectedRow = table.getSelectedRow(); // получаем индекс выбранной строки
            Object idRow = tableModel.getValueAt(selectedRow, 1); // получаем id строки

            FileDB.deleteRowDB((String) idRow);

            statusLabel.setText("Строка с id#" + idRow + " удалена.");

            // обновляем данные в БД
            updateDataModel();
        }

        private void updateDataModel() {
            tableModel.setRowCount(0); // очищаем модель

            try {
                ResultSet resultSet = FileDB.isTable(FileDB.connectDB(), "main", FileDB.dbName);
                assert resultSet != null : "Ошибка resultSet=null на стадии обновления данных в модели.";
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                FileDB.getRowFromDB(tableModel, resultSet, columnCount);
                System.out.println("Данные модели обновлены.");
            } catch (SQLException e) {
                System.out.println("Ошибка SQL (addRow).");
            }
        }

        //todo - добавить метод редактирования

    }
}
// todo - создать метод проверки файла БД и его создание при отсутствии
// todo - создать метод соединения с БД