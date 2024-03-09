package org.kurodev.sudoku.gen;

import org.kurodev.sudoku.SudokuWithSolution;

public interface SudokuGenerator {
    void generate(SudokuWithSolution game, String seed);
}
