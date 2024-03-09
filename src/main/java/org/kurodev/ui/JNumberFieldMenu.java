package org.kurodev.ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class JNumberFieldMenu extends JPopupMenu {


    public void showMenu(JNumberField jNumberField, List<String> options) {
        populateMenu(options, jNumberField);
        show(jNumberField, 0, jNumberField.getHeight());
    }

    private void populateMenu(List<String> options, JNumberField jNumberField) {
        removeAll();
        if (options.isEmpty()) {
            JMenuItem menuItem = new JMenuItem("None");
            menuItem.setEnabled(false);
            add(menuItem);
        } else {
            Dimension menuItemSize = new Dimension(30, 30);
            options.forEach(option -> {
                JMenuItem menuItem = new JMenuItem(option);
                menuItem.setPreferredSize(menuItemSize);
                menuItem.addActionListener(e -> {
                    jNumberField.setText(option);
                    setVisible(false);
                });
                add(menuItem);
            });
        }

    }
}
