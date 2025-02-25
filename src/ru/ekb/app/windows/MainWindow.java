package ru.ekb.app.windows;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ru.ekb.app.utilites.*;
import java.io.IOException;
import java.util.Objects;

public class MainWindow extends JFrame {
    private JFrame frame;
    private JPanel tablePanel, buttonsPanel, statusPanel, bottomPanel;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JButton addButton, deleteButton;

    private Object[][] data = new String[][] {
            {"Фильтр масляный", "7000 - 8000", "30.03.2025", "22.09.2024", "1 шт",
                    "263202F100, Masuma MFC2016, Mando EEOK0003Y", "1 шт", ""},
            {"Фильтр салонный", "10000", "30.03.2025", "22.03.2024", "1 шт",
                    "97133G8000, KORTEX KC0142, Goodwill AG634CFC", "1 шт", ""},
            {"Фильтр Воздушный", "10000", "30.03.2025", "22.03.2024", "1 шт",
                    "28113A9200, KORTEX KA0286, Goodwill AG485", "1 шт", ""},
            {"Масло двигателя", "7000 - 8000", "30.03.2025", "22.09.2024", "6,5 л",
                    "XTeer diesel ultra c3 5W-30, Sintec 5W-30 ACEA C2/C3", "7 л", ""},
    };
    private Object[] columnsHeader = new String[] {
            "Вид обслуживания", "Регламент", "Дата по плану", "Последнее обслуживание",
            "Объем", "Производитель/Код", "Запчастей в наличии", "Комментарий"
    };

    public MainWindow(String winTitle, String iconPath, int w, int h) {
        super(winTitle);

        // проверяем существование файла БД
        try {
            fileDB.isFile();
            fileDB.connectDB();
        }
        catch (Exception e) {
            statusLabel.setText(e.getMessage());

        }

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
        tableModel = new DefaultTableModel(data, columnsHeader); // и добавляем данные и заголовок
        table = new JTable(tableModel);

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
//            {"Вид обслуживания", "Регламент", "Дата по плану", "Последнее обслуживание",
//            "Объем", "Производитель/Код детали", "Запчастей в наличии", "Комментарий"};

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