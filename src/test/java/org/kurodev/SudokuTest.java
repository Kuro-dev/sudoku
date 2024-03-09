package org.kurodev;

import org.junit.jupiter.api.Test;
import org.kurodev.sudoku.Difficulty;
import org.kurodev.sudoku.Sudoku;
import org.kurodev.sudoku.SudokuFactory;
import org.kurodev.sudoku.SudokuWithSolution;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SudokuTest {
    @Test
    public void testStateRestorationForSudoku() throws IOException {
        SudokuWithSolution a = SudokuFactory.create(Difficulty.EASY, "test");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        a.getGame().writeState(baos);
        Sudoku gameRestored = Sudoku.ofState(new ByteArrayInputStream(baos.toByteArray()));
        assertEquals(a.getGame(), gameRestored);

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        a.getInitialGameState().writeState(baos2);
        Sudoku gameRestored2 = Sudoku.ofState(new ByteArrayInputStream(baos2.toByteArray()));
        assertEquals(a.getInitialGameState(), gameRestored2);
    }

    @Test
    public void testStateRestorationForGame() throws IOException {
        SudokuWithSolution a = SudokuFactory.create(Difficulty.EASY, "test");
        a.incrementTurn();
        a.incrementTurn();
        a.incrementTurn();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        a.save(baos);
        var gameRestored = SudokuFactory.load(new ByteArrayInputStream(baos.toByteArray()));
        assertEquals(a, gameRestored);
    }
}
