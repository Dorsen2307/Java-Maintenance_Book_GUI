package ru.ekb.app.utilites;

import ru.ekb.app.windows.MainWindow;

import javax.swing.*;
import java.io.File;

public class fileDB {
    public static void isFile() throws Exception {
        File file = new File("mainDB.mysql");
        if (!file.exists()) {
            boolean created = createdFile(file);
            if (created) {
                JOptionPane.showMessageDialog("Файл БД создан.");
            }
        }
    }

    private static boolean createdFile(File file) throws Exception {
        return file.createNewFile();
    }

    public static void connectDB() throws Exception {
        String dbName = "mainDB";
        String userName = "root";
        String password = "root";


    }
}
