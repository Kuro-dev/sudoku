package org.kurodev.ui;

import org.kurodev.sudoku.Difficulty;
import org.kurodev.sudoku.Sudoku;
import org.kurodev.sudoku.gen.Coordinate;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainWindow extends JFrame {
    private final SudokuHandler gameHandler = new SudokuHandler();
    private final Map<Coordinate, JNumberField> fields = new HashMap<>(9 + 9);
    private final JLabel movesLabel = new JLabel();
    private final JProgressBar progressBar = new JProgressBar();

    public void createAndShowGUI() {
        setTitle("Sudoku");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Runtime.getRuntime().addShutdownHook(new Thread(gameHandler::save));
        setLayout(new BorderLayout()); // Set layout to BorderLayout
        add(createToolbar(), BorderLayout.NORTH); // Add toolbar at the top
        add(createSudokuPanel(), BorderLayout.CENTER); // Sudoku panel in the center
        pack();
        setLocationRelativeTo(null); // Center the window
        setVisible(true);
        setSize(500, 500);

        if (gameHandler.load()) {
            loadGame();
        } else {
            startGame();
        }
    }

    private JToolBar createToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton startButton = new JButton("Start Game");
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        // Set margins for the toolbar components
        startButton.setMargin(new Insets(5, 10, 5, 10));
        movesLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Add action listeners to the buttons as needed
        startButton.addActionListener(e -> startGame());

        toolBar.add(startButton);
        toolBar.addSeparator();
        toolBar.add(movesLabel);
        toolBar.addSeparator(new Dimension(10, 0)); // Separator for margin
        toolBar.add(progressBar);

        return toolBar;
    }

    private JPanel createSudokuPanel() {
        JPanel sudokuPanel = new JPanel(new GridLayout(3, 3, 2, 2));
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        JNumberFieldMenu menu = new JNumberFieldMenu();

        // Iterate over the entire 9x9 grid
        for (int gridY = 0; gridY < 3; gridY++) {
            for (int gridX = 0; gridX < 3; gridX++) {
                JPanel subgrid = new JPanel(new GridLayout(3, 3));
                subgrid.setBorder(border);
                // Iterate within each subgrid
                for (int subY = 0; subY < 3; subY++) {
                    for (int subX = 0; subX < 3; subX++) {
                        // Calculate the actual coordinates in the 9x9 grid
                        int x = gridX * 3 + subX;
                        int y = gridY * 3 + subY;
                        final Coordinate c = new Coordinate(x, y);
                        JNumberField numberField = new JNumberField(menu, () -> this.getAvailableNumbers(c));
                        numberField.setEnabled(false);
                        numberField.setHorizontalAlignment(JTextField.CENTER);
                        subgrid.add(numberField);
                        fields.put(c, numberField);
                        numberField.getChangeListeners()
                                .add(integer -> handleInput(c, integer != null ? integer : 0));
                    }
                }
                sudokuPanel.add(subgrid);
            }
        }
        return sudokuPanel;
    }

    /**
     * Retrieves a list of available numbers
     */
    private List<String> getAvailableNumbers(Coordinate c) {
        Sudoku game = gameHandler.getGame();
        List<String> out = new ArrayList<>(9);
        for (int i = 1; i <= 9; i++) {
            if (game.isNumberValid(i, c)) {
                out.add(String.valueOf(i));
            }
        }
//        System.out.println(out);
        return out;
    }

    private void updateMoves() {
        movesLabel.setText("Moves: " + gameHandler.getTurn());
    }

    /**
     * Handle user input when changing the numbers in a field
     */
    private void handleInput(Coordinate pos, int num) {
        Sudoku game = gameHandler.getGame();
        game.setNum(num, pos);
        updateNumberColors(pos);
        if (gameHandler.getGame().isValidSudoku()) {
            //game is won
            fields.values().forEach(field -> field.setEnabled(false));
            movesLabel.setText("You won. Total moves: " + gameHandler.getTurn());
        } else if (num != 0) {
            gameHandler.incrementTurn();
            updateMoves();
        }
    }

    private void updateNumberColors(Coordinate pos) {
        gameHandler.getGame().getGroupPos(pos).forEach(this::updateColor);
        gameHandler.getGame().getLinePos(pos).forEach(this::updateColor);
    }

    private void updateColor(Coordinate pos) {
        Sudoku game = gameHandler.getGame();
        var field = fields.get(pos);
        int num = field.getNumber();
        SwingUtilities.invokeLater(() -> {
//            System.out.println("num " + num + " pos " + pos + " valid " + game.isNumberValid(num, pos));
            field.setError(num != 0 && !game.isNumberValid(num, pos));
        });
    }

    /**
     * Create a new game
     */
    private void startGame() {
        progressBar.setVisible(true);
        fields.values().forEach(field -> {
            field.setEnabled(false);
            field.setError(false);
            field.setLocked(false);
            field.setText("");
        });
        new Thread(() -> {
            gameHandler.startGame(Difficulty.MEDIUM);
            SwingUtilities.invokeLater(() -> {
                for (int y = 0; y < Sudoku.FIELD_SIZE; y++) {
                    for (int x = 0; x < Sudoku.FIELD_SIZE; x++) {
                        Coordinate c = new Coordinate(x, y);
                        JNumberField field = fields.get(c);
                        int num = gameHandler.getInitialState().getNum(c);
                        field.setLocked(num != 0);
                        field.setText(String.valueOf(num));
                    }
                }
                fields.values().forEach(field -> field.setEnabled(true));
                progressBar.setVisible(false);
                gameHandler.resetTurns();
                updateMoves();
            });
        }, "Generator Thread").start();
    }

    /**
     * Handle game state after loading from a save file
     */
    private void loadGame() {
        System.out.println("loaded game state");
        gameHandler.lockTurnCount(true);
        fields.values().forEach(field -> {
            field.setError(false);
            field.setLocked(false);
            field.setText("");
        });
        SwingUtilities.invokeLater(() -> {
            fields.values().forEach(field -> field.setEnabled(true));
            for (int y = 0; y < Sudoku.FIELD_SIZE; y++) {
                for (int x = 0; x < Sudoku.FIELD_SIZE; x++) {
                    Coordinate c = new Coordinate(x, y);
                    JNumberField field = fields.get(c);
                    int num = gameHandler.getInitialState().getNum(c);
                    if (num == 0) {
                        field.setText(String.valueOf(gameHandler.getGame().getNum(c)));
                    } else {
                        field.setText(String.valueOf(num));
                        field.setLocked(true);
                    }


                }
            }
            repaint();
            progressBar.setVisible(false);
        });
    }

    /**
     * Repaint the component
     */
    @Override
    public void repaint() {
        gameHandler.lockTurnCount(true);
        for (int y = 0; y < Sudoku.FIELD_SIZE; y++) {
            for (int x = 0; x < Sudoku.FIELD_SIZE; x++) {
                Coordinate c = new Coordinate(x, y);
                JNumberField field = fields.get(c);
                field.setText(String.valueOf(gameHandler.getGame().getNum(c)));
            }
        }
        gameHandler.lockTurnCount(false);
    }
}
