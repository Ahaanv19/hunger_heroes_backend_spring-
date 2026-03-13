package com.open.spring.mvc.mines;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * Represents a Minesweeper-like board game with mines randomly placed based on stakes.
 */
@Slf4j
@Getter
public class MinesBoard {
    private static final int BOARD_SIZE = 5;

    private static final double LOW_INITIAL = 0.7;
    private static final double MEDIUM_INITIAL = 0.6;
    private static final double HIGH_INITIAL = 0.5;
    private static final double LOW_MULTIPLIER = 1.1;
    private static final double MEDIUM_MULTIPLIER = 1.125;
    private static final double HIGH_MULTIPLIER = 1.15;

    private final String stakes; // Stakes level ("low", "medium", "high")
    private final int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
    private int mines; // Number of mines on the board
    private int cleared = -1; // Cleared cells count

    /**
     * Initializes a MinesBoard object with specified difficulty stakes and places mines.
     */
    public MinesBoard(String stakes) {
        this.stakes = stakes;
        Random rng = new Random();

        // Determine the number of mines based on stakes
        this.mines = switch (stakes) {
            case "low" -> rng.nextInt(4, 7);
            case "medium" -> rng.nextInt(6, 9);
            case "high" -> rng.nextInt(8, 11);
            default -> throw new IllegalArgumentException("Invalid stakes level: " + stakes);
        };

        placeMines();
        log.debug("Board initialized with stakes: {} and mines: {}", stakes, mines);
    }

    /**
     * Places mines randomly on the board, ensuring no two mines share the same cell.
     */
    private void placeMines() {
        Random rng = new Random();
        int xCoord, yCoord;

        for (int i = 0; i < mines; i++) {
            while (true) {
                xCoord = rng.nextInt(BOARD_SIZE);
                yCoord = rng.nextInt(BOARD_SIZE);

                if (board[xCoord][yCoord] == 0) {
                    board[xCoord][yCoord] = 1;
                    break;
                }
            }
        }

        log.debug("Mines placed on the board.");
    }

    /**
     * Checks if a specific cell contains a mine.
     * 
     * @param xCoord x-coordinate of the cell to check
     * @param yCoord y-coordinate of the cell to check
     * @return true if the cell contains a mine, false otherwise
     */
    public boolean checkMine(int xCoord, int yCoord) {
        cleared++;
        log.info("Checked cell ({}, {}): contains mine? {}", xCoord, yCoord, board[xCoord][yCoord] == 1);
        return board[xCoord][yCoord] == 1;
    }

    /**
     * Calculates winnings based on cleared cells and difficulty stakes.
     * 
     * @return calculated winnings, or -1 if stakes are invalid
     */
    public double winnings() {
        double pts = switch (stakes) {
            case "low" -> LOW_INITIAL * Math.pow(LOW_MULTIPLIER, cleared);
            case "medium" -> MEDIUM_INITIAL * Math.pow(MEDIUM_MULTIPLIER, cleared);
            case "high" -> HIGH_INITIAL * Math.pow(HIGH_MULTIPLIER, cleared);
            default -> -1;
        };

        log.info("Winnings calculated: {}", pts);
        return pts;
    }

    /**
     * Prints the board to the console for debugging purposes.
     */
    private void printBoard() {
        log.debug("Printing board:");
        for (int[] row : board) {
            for (int col : row) {
                System.out.print(col + " ");
            }
            System.out.println();
        }
    }

    /**
     * Unit test for the MinesBoard class.
     * 
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        MinesBoard board = new MinesBoard("high");
        board.placeMines();
        board.printBoard();
    }
}
