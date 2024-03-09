package org.kurodev.ui;

import org.kurodev.sudoku.Difficulty;
import org.kurodev.sudoku.Sudoku;
import org.kurodev.sudoku.SudokuFactory;
import org.kurodev.sudoku.SudokuWithSolution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SudokuHandler {
    private static final Path SAVE_FILE = Path.of("./game.sudoku");

    private SudokuWithSolution sudoku;
    private boolean locked;

    public void startGame(Difficulty difficulty) {
        sudoku = SudokuFactory.create(difficulty);
        resetTurns();
    }

    public Sudoku getGame() {
        return sudoku.getGame();
    }

    public Sudoku getInitialState() {
        return sudoku.getInitialGameState();
    }

    public int getTurn() {
        return sudoku.getTurns();
    }

    public void resetTurns() {
        sudoku.setTurns(0);
    }

    public void incrementTurn() {
        if (!locked)
            sudoku.incrementTurn();
    }

    public void save() {
        try (var out = Files.newOutputStream(SAVE_FILE);) {
            sudoku.save(out);
        } catch (IOException e) {
            System.err.println("Failed to save game");
            e.printStackTrace();
        }
    }

    public boolean load() {
        if (Files.exists(SAVE_FILE)) {
            try (var in = Files.newInputStream(SAVE_FILE)) {
                sudoku = SudokuFactory.load(in);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public void lockTurnCount(boolean lockTurnCount) {
        this.locked = lockTurnCount;
    }
}
