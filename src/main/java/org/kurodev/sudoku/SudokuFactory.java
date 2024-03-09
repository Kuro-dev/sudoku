package org.kurodev.sudoku;

import org.kurodev.sudoku.gen.BacktrackingSudokuGenerator;
import org.kurodev.sudoku.gen.SudokuGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class SudokuFactory {

    public static SudokuWithSolution create(Difficulty difficulty, SudokuGenerator generator) {
        return create(difficulty, UUID.randomUUID().toString(), generator);
    }

    public static SudokuWithSolution create(Difficulty difficulty) {
        return create(difficulty, UUID.randomUUID().toString(), new BacktrackingSudokuGenerator());
    }

    public static SudokuWithSolution create(Difficulty difficulty, String seed) {
        return create(difficulty, seed, new BacktrackingSudokuGenerator());
    }

    public static SudokuWithSolution create(Difficulty difficulty, String seed, SudokuGenerator generator) {
        var out = new SudokuWithSolution(new Sudoku(), new Sudoku(), difficulty);
        generator.generate(out, seed);
        return out;
    }

    public static SudokuWithSolution load(InputStream in) throws IOException {
        return SudokuWithSolution.load(in);
    }

}
