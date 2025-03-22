package ru.ekb.app.utilites;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TextAreaRenderer extends JTextArea implements TableCellRenderer {
    public TextAreaRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row, int column) {
        setText(value != null ? value.toString() : "");
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
        return this;
    }
}
