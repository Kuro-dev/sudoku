package org.kurodev;

import org.kurodev.ui.MainWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        MainWindow window = new MainWindow();
        SwingUtilities.invokeLater(window::createAndShowGUI);
    }
}
