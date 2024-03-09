package org.kurodev.sudoku;

import org.kurodev.sudoku.gen.Coordinate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Sudoku {
    public static final int FIELD_SIZE = 9;

    private final int[][] field = new int[FIELD_SIZE][FIELD_SIZE];

    Sudoku() {

    }

    public static Sudoku ofState(InputStream in) throws IOException {
        var out = new Sudoku();
        out.readState(in);
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sudoku sudoku = (Sudoku) o;
        return Arrays.deepEquals(field, sudoku.field);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(field);
    }

    public void setNum(int num, Coordinate pos) {
        if (num < 0) throw new IllegalArgumentException("Num must be positive but was " + num);
        if (pos.y() >= FIELD_SIZE || pos.x() >= FIELD_SIZE)
            throw new IndexOutOfBoundsException("Coordinate " + pos + " out of bounds");

        field[pos.y()][pos.x()] = num;
    }

    public int getNum(Coordinate pos) {
        return field[pos.y()][pos.x()];
    }

    public boolean isNumberValid(Coordinate pos) {
        return isNumberValid(getNum(pos), pos);
    }

    public boolean isNumberValid(int num, Coordinate pos) {
        if (num == 0) return false;
        var list = getLinePos(pos, false);
        list.addAll(getGroupPos(pos, false));

        return list.stream()
                .map(this::getNum)
                .noneMatch(integer -> integer == num);
    }

    public Set<Integer> getLineNums(Coordinate coordinate) {
        return getLinePos(coordinate).stream()
                .map(this::getNum)
                .filter(integer -> integer != 0)
                .collect(Collectors.toSet());
    }

    public Set<Coordinate> getLinePos(Coordinate coordinate) {
        return getLinePos(coordinate, true);
    }

    public Set<Coordinate> getLinePos(Coordinate coordinate, boolean includeParamCoord) {
        Set<Coordinate> res = new HashSet<>();
        for (int i = 0; i < FIELD_SIZE; i++) {
            var a = new Coordinate(i, coordinate.y());
            var b = new Coordinate(coordinate.x(), i);
            if (includeParamCoord || !a.equals(coordinate))
                res.add(a);
            if (includeParamCoord || !b.equals(coordinate))
                res.add(b);
        }
        return res;
    }

    private int getSubgroupCenter(int i) {
        int[] numbers = {1, 4, 7};
        int closest = numbers[0];
        int minDiff = Math.abs(numbers[0] - i);

        for (int num : numbers) {
            int diff = Math.abs(num - i);
            if (diff < minDiff) {
                minDiff = diff;
                closest = num;
            }
        }

        return closest;
    }

    public Set<Integer> getGroupNums(Coordinate coordinate) {
        return getGroupPos(coordinate).stream()
                .map(this::getNum)
                .filter(integer -> integer != 0)
                .collect(Collectors.toSet());
    }

    public Set<Coordinate> getGroupPos(Coordinate coordinate) {
        return getGroupPos(coordinate, true);
    }

    public Set<Coordinate> getGroupPos(Coordinate coordinate, boolean includeParamCoord) {
        Set<Coordinate> res = new HashSet<>();
        // Find top-left of subgroup
        int baseX = coordinate.x() / 3 * 3;
        int baseY = coordinate.y() / 3 * 3;

        for (int y = baseY; y < baseY + 3; y++) {
            for (int x = baseX; x < baseX + 3; x++) {
                if (!includeParamCoord && x == coordinate.x() && y == coordinate.y()) {
                    // Skip adding the original coordinate if includeParam is false
                    continue;
                }
                res.add(new Coordinate(x, y));
            }
        }

        return res;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < FIELD_SIZE; i++) {
            if (i % 3 == 0 && i != 0) {
                sb.append("------+-------+------\n");
            }
            for (int j = 0; j < FIELD_SIZE; j++) {
                if (j % 3 == 0 && j != 0) {
                    sb.append("| ");
                }
                sb.append(field[i][j] == 0 ? "_ " : field[i][j] + " ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public void clearNum(Coordinate pos) {
        setNum(0, pos);
    }

    public boolean isValidSudoku() {
        for (int y = 0; y < FIELD_SIZE; y++) {
            for (int x = 0; x < FIELD_SIZE; x++) {
                var pos = new Coordinate(x, y);
                int num = getNum(pos);
                if (!isNumberValid(num, pos)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Creates a deep copy of this object and returns it
     */
    public Sudoku copyState() {
        Sudoku out = new Sudoku();
        for (int i = 0; i < this.field.length; i++) {
            System.arraycopy(this.field[i], 0, out.field[i], 0, this.field[i].length);
        }
        return out;
    }

    /**
     * Clones the field from the other Sudoku into this one
     *
     * @param other The sudoku to copy
     */
    public void copyState(Sudoku other) {
        for (int i = 0; i < this.field.length; i++) {
            System.arraycopy(other.field[i], 0, this.field[i], 0, other.field[i].length);
        }
    }

    /**
     * Stores the sudoku field in 4-bit blocks
     */
    public void writeState(OutputStream stream) throws IOException {
        //write all the first numbers first
        stream.write(getNum(new Coordinate(0, 0)));
        for (int y = 1; y < FIELD_SIZE; y++) {
            int firstNum = getNum(new Coordinate(0, y++));
            firstNum = firstNum << 4;
            firstNum += getNum(new Coordinate(0, y));
            stream.write(firstNum);
        }

        for (int y = 0; y < FIELD_SIZE; y++) {
            for (int x = 1; x < FIELD_SIZE; x++) {
                int aByte = getNum(new Coordinate(x++, y));
                aByte = aByte << 4;
                int num2 = getNum(new Coordinate(x, y));
                aByte += num2;
                stream.write(aByte);
            }
        }
    }

    public void readState(InputStream in) throws IOException {
        final int halfCeil = (FIELD_SIZE / 2) + 1;
        byte[] buf = new byte[41];//the amount of bytes necessary to store one sudoku
        int read = in.read(buf);
        assert read == buf.length : "Read was: " + read + " Expected:" + buf.length;
        //set all the first numbers
        int tempY = 1;
        setNum(buf[0], new Coordinate(0, 0)); //first number is just a byte
        for (int i = 1; i < halfCeil; i++) {
            byte num = buf[i];
            int a = (num >> 4) & 0x0F;
            int b = num & 0xf;
            setNum(a, new Coordinate(0, tempY++));
            setNum(b, new Coordinate(0, tempY++));
        }
        // Set the remaining numbers
        for (int i = halfCeil; i < buf.length; i++) {
            int y = (i - halfCeil) / ((FIELD_SIZE - 1) / 2);
            int x = 1 + 2 * ((i - halfCeil) % ((FIELD_SIZE - 1) / 2));

            byte num = buf[i];
            int a = (num >> 4) & 0x0F;
            int b = num & 0xf;
            setNum(a, new Coordinate(x, y)); // Upper 4 bits
            x++;
            setNum(b, new Coordinate(x, y)); // Lower 4 bits
        }

    }
}
