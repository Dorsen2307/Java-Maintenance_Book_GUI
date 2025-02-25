package ru.ekb.app.windows;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class PopupWindow extends JFrame {
    public PopupWindow(String title, String text, String path, int w, int h) {
        JFrame frame = new JFrame();

        try {
            ImageIcon iconFile = new ImageIcon(Objects.requireNonNull(PopupWindow.class.getResource(path)));
            frame.setIconImage(iconFile.getImage());
        }
        catch (NullPointerException e) {
            System.out.println("Проблема: " + e);
        }

        frame.setTitle(title);
        frame.setSize(w, h);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel label = new JLabel();
        label.setText(text);
        label.setHorizontalAlignment(SwingConstants.CENTER); // выравниваем текст по горизонтали
        label.setVerticalAlignment(SwingConstants.CENTER); // выравниваем текст по вертикали

        frame.add(label, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
