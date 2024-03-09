package org.kurodev.sudoku;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Container with a game and a possible solution.
 * That solution may not be the *only* solution to the game
 */
public final class SudokuWithSolution {
    private final Sudoku game;
    private final Sudoku initialGame;
    private final Difficulty difficulty;

    private int turns = 0;

    /**
     *
     */
    public SudokuWithSolution(Sudoku game, Sudoku initialGame, Difficulty difficulty) {
        this.game = game;
        this.initialGame = initialGame;
        this.difficulty = difficulty;
    }

    public SudokuWithSolution(Sudoku game, Sudoku initialGame, Difficulty difficulty, int turn) {
        this(game, initialGame, difficulty);
        turns = turn;
    }

    static SudokuWithSolution load(InputStream in) throws IOException {
        byte[] buf = new byte[4];
        in.read(buf);
        int turn = ByteBuffer.wrap(buf).getInt();
        Difficulty difficulty = Difficulty.valueOf(in.read());
        Sudoku game = new Sudoku();
        Sudoku possibleSolution = new Sudoku();
        game.readState(in);
        possibleSolution.readState(in);
        return new SudokuWithSolution(game, possibleSolution, difficulty, turn);
    }

    @Override
    public String toString() {
        return "SudokuWithSolution:\n" +
                "difficulty: " + difficulty +
                "\nfield=\n" + game +
                "\npossibleSolution=\n" + initialGame;
    }

    public void save(OutputStream out) throws IOException {
        out.write(ByteBuffer.allocate(4).putInt(turns).array());
        out.write(difficulty.ordinal());
        game.writeState(out);
        initialGame.writeState(out);
    }

    public Sudoku getGame() {
        return game;
    }

    public Sudoku getInitialGameState() {
        return initialGame;
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public int getTurns() {
        return turns;
    }

    public void setTurns(int turns) {
        this.turns = turns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SudokuWithSolution that = (SudokuWithSolution) o;
        return turns == that.turns && Objects.equals(game, that.game) && Objects.equals(initialGame, that.initialGame) && difficulty == that.difficulty;
    }

    @Override
    public int hashCode() {
        return Objects.hash(game, initialGame, difficulty, turns);
    }

    public void incrementTurn() {
        this.turns++;
    }

    public void resetGame() {
        this.game.copyState(this.initialGame);
    }
}
