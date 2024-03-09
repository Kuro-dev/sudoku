package org.kurodev;

import org.junit.jupiter.api.Test;
import org.kurodev.sudoku.Difficulty;
import org.kurodev.sudoku.SudokuFactory;
import org.kurodev.sudoku.SudokuWithSolution;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class GeneratorTest {
    @Test
    public void testSameSeedResultsInSameSudoku() {
        for (Difficulty difficulty : Difficulty.values()) {
            SudokuWithSolution s = SudokuFactory.create(difficulty, "Test");
            SudokuWithSolution s2 = SudokuFactory.create(difficulty, "Test");
            assertEquals(s, s2);
            assertTrue(s.getInitialGameState().isValidSudoku(), "Sudoku is not valid.");
            assertFalse(s.getGame().isValidSudoku(), "Sudoku is valid. but shouldn't be.");
        }

    }

    @Test
    public void testCopyIsIdentical() {
        SudokuWithSolution s = SudokuFactory.create(Difficulty.VERY_HARD, "Test");
        assertEquals(s.getInitialGameState(), s.getInitialGameState().copyState());
        assertEquals(s.getGame(), s.getGame().copyState());
        System.out.println(s);
    }

    @Test
    public void test241() throws IOException {
            SudokuWithSolution s = SudokuFactory.create(Difficulty.VERY_HARD, "Test");
            System.out.println(s.getInitialGameState());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            s.getInitialGameState().writeState(baos);
            System.out.println("\""+baos+"\"");
    }
}
