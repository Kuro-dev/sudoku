package org.kurodev.sudoku;

public enum Difficulty {
    VERY_EASY(50, 60),
    EASY(36, 49),
    MEDIUM(32, 35),
    HARD(28, 31),
    VERY_HARD(24, 27),
    HARDEST(17, 23);

    private final int minClues;
    private final int maxClues;

    Difficulty(int minClues, int maxClues) {
        this.minClues = minClues;
        this.maxClues = maxClues;
    }

    public static Difficulty valueOf(int ordinal) {
        for (Difficulty value : values()) {
            if (value.ordinal() == ordinal) {
                return value;
            }
        }
        throw new IllegalArgumentException("Ordinal " + ordinal + " does not exist.");
    }

    public int getMinClues() {
        return minClues;
    }

    public int getMaxClues() {
        return maxClues;
    }
}
