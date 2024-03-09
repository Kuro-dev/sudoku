package org.kurodev.sudoku.gen;

import org.kurodev.sudoku.Difficulty;
import org.kurodev.sudoku.Sudoku;
import org.kurodev.sudoku.SudokuWithSolution;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

public class BacktrackingSudokuGenerator implements SudokuGenerator {
    private static final Set<Integer> NUMBERS = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

    public void generate(SudokuWithSolution container, String seed) {
        Random rng = new SecureRandom(seed.getBytes(StandardCharsets.UTF_8));
        generatePossibleSolution(container.getInitialGameState(), rng);
        generateHints(container.getInitialGameState(), container.difficulty(), rng);
        container.getGame().copyState(container.getInitialGameState());
    }

    private void generateHints(Sudoku game, Difficulty difficulty, Random rng) {
        int hintAmount = rng.nextInt(difficulty.getMaxClues() - difficulty.getMinClues()) + difficulty.getMinClues();
        assert hintAmount >= difficulty.getMinClues() : "hint amount is too few: " + hintAmount + " minimum: " + difficulty.getMinClues();
        assert hintAmount <= difficulty.getMaxClues() : "hint amount is too big: " + hintAmount + " maximum: " + difficulty.getMaxClues();

        List<Coordinate> stack = new ArrayList<>();

        for (int y = 0; y < Sudoku.FIELD_SIZE; y++) {
            for (int x = 0; x < Sudoku.FIELD_SIZE; x++) {
                Coordinate coord = new Coordinate(x, y);
                stack.add(coord);
            }
        }
        while (stack.size() > hintAmount) {
            int removeIndex = rng.nextInt(stack.size());
            Coordinate removed = stack.remove(removeIndex);
            game.clearNum(removed);
        }
    }

    private void generatePossibleSolution(Sudoku game, Random rng) {
        Deque<Coordinate> stack = new ArrayDeque<>();
        Deque<Coordinate> visited = new ArrayDeque<>();
        Map<Coordinate, Set<Integer>> triedNumbers = new HashMap<>();

        for (int y = 0; y < Sudoku.FIELD_SIZE; y++) {
            for (int x = 0; x < Sudoku.FIELD_SIZE; x++) {
                Coordinate coord = new Coordinate(x, y);
                stack.push(coord);
                triedNumbers.put(coord, new HashSet<>());
            }
        }

        int iterations = 0;
        while (!stack.isEmpty()) {
            iterations++;
            if (iterations > 1_000_000) {
                throw new RuntimeException("Unable to generate Sudoku puzzle");
            }
            Coordinate pos = stack.peek();
            Set<Integer> tried = triedNumbers.get(pos);
            List<Integer> available = availableNumbers(game, pos).stream()
                    .filter(num -> !tried.contains(num))
                    .toList();

            if (available.isEmpty()) {
                game.clearNum(pos);
                tried.clear(); // Clear tried numbers for this cell
                stack.addFirst(stack.pop());
                if (!visited.isEmpty()) {
                    Coordinate prevPos = visited.peek();
                    int num = game.getNum(prevPos);
                    //prevPos should be a filled field that has been visited before.
                    assert num != 0 : "Previous field should not be 0 at " + prevPos;
                    triedNumbers.get(prevPos).add(num); // Add the number at prevPos to its tried set
                    game.clearNum(prevPos); // Clear the number at prevPos
                    stack.push(visited.pop()); //push the last visited cell back on the stack to reevaluate it.
                }
            } else {
                int num = available.get(rng.nextInt(available.size()));
                game.setNum(num, pos);
                tried.add(num); // Add this number to the tried set
                visited.push(stack.pop());
            }
        }
        System.out.println("generation finished after " + iterations + " iterations");
    }


    private List<Integer> availableNumbers(Sudoku game, Coordinate c) {
        var usedNumbers = game.getLineNums(c);
        usedNumbers.addAll(game.getGroupNums(c));

        List<Integer> list = NUMBERS.stream()
                .filter(integer -> !usedNumbers.contains(integer))
                .filter(integer -> integer != 0)
                .sorted()
                .collect(Collectors.toList());
        return list;
    }
}
