package org.kurodev.ui;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JNumberField extends JTextField implements FocusListener {
    private final Color filledColor = Color.WHITE.darker();
    private final Color lockedColor = Color.GREEN.brighter().brighter();
    private final Color errorColor = Color.RED;
    private final List<Consumer<Integer>> changeListeners = new ArrayList<>();
    private boolean hasError = false;
    private boolean locked;

    public JNumberField(JNumberFieldMenu menu, Supplier<List<String>> menuOptionsSupplier) {
        super();
        ((PlainDocument) this.getDocument()).setDocumentFilter(new IntegerFilter());
        updateBackgroundColor();
        this.addFocusListener(this);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3)
                    SwingUtilities.invokeLater(() -> {
                        if (!locked)
                            menu.showMenu(JNumberField.this, menuOptionsSupplier.get());
                    });
            }
        });
    }

    public List<Consumer<Integer>> getChangeListeners() {
        return changeListeners;
    }

    public void setError(boolean hasError) {
        this.hasError = hasError;
        updateBackgroundColor();
    }

    private void updateBackgroundColor() {
        if (locked) {
            this.setBackground(lockedColor);
        } else if (hasError) {
            this.setBackground(errorColor);
        } else if (!getText().isEmpty()) {
            this.setBackground(filledColor);
        } else {
            this.setBackground(Color.WHITE);
        }
    }

    public int getNumber() {
        var res = this.getText();
        if (res.isEmpty()) return 0;
        else return Integer.parseInt(res);
    }

    private void notifyListeners(String content) {
        if (!locked) {
            Integer value = content != null && !content.isEmpty() ? Integer.parseInt(content) : null;
            changeListeners.forEach(listener -> listener.accept(value));
        }

    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        setEnabled(!locked);
        setDisabledTextColor(Color.black);
        updateBackgroundColor();
    }

    @Override
    public void focusGained(FocusEvent e) {
        this.selectAll();
    }

    @Override
    public void focusLost(FocusEvent e) {

    }

    private class IntegerFilter extends DocumentFilter {

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) {
                return;
            }
            String newText = getText(0, fb.getDocument().getLength()) + string;
            if (test(newText)) {
                super.insertString(fb, offset, string, attr);
            }
            notifyListeners(newText);
            updateBackgroundColor();
            selectAll();
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) {
                return;
            }
            String oldText = getText(0, fb.getDocument().getLength());
            String newText = oldText.substring(0, offset) + text + oldText.substring(offset + length);
            if (test(newText)) {
                super.replace(fb, offset, length, text, attrs);
            }
            notifyListeners(newText);
            updateBackgroundColor();
            selectAll();
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            super.remove(fb, offset, length);
            updateBackgroundColor();
            notifyListeners(null);
        }

        private boolean test(String text) {
            // Only allow single digit numbers from 1 to 9
            return text.isEmpty() || text.matches("[1-9]") && text.length() == 1;
        }
    }
}

