package dynamaze;

import java.util.ArrayDeque;
import java.util.Arrays;

final class Maze {
    private static final String[] LAYOUT = {
        ".......#G",
        "..#....#.",
        "S.#....#.",
        "..#......",
        ".....#...",
        "........."
    };

    static final int ROWS = LAYOUT.length;
    static final int COLS = LAYOUT[0].length();

    enum Move {
        UP(-1, 0), DOWN(1, 0), RIGHT(0, 1), LEFT(0, -1);

        final int dr;
        final int dc;

        Move(int dr, int dc) {
            this.dr = dr;
            this.dc = dc;
        }
    }

    static final Move[] MOVES = Move.values();

    record Transition(int next, int reward, boolean terminal) {}

    private final int[][] states = new int[ROWS][COLS];
    private final int[] cells = new int[ROWS * COLS];
    final int stateCount;
    final int start;
    final int goal;

    Maze() {
        int nextId = 0;
        int startId = -1;
        int goalId = -1;
        for (int row = 0; row < ROWS; row++) {
            Arrays.fill(states[row], -1);
            for (int col = 0; col < COLS; col++) {
                char cell = LAYOUT[row].charAt(col);
                if (cell == '#') continue;
                if (cell == 'S') startId = nextId;
                if (cell == 'G') goalId = nextId;
                states[row][col] = nextId;
                cells[nextId++] = row * COLS + col;
            }
        }
        stateCount = nextId;
        start = startId;
        goal = goalId;
    }

    int at(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return -1;
        return states[row][col];
    }

    Transition step(int state, Move move) {
        if (state == goal) throw new IllegalArgumentException("Goal is terminal.");
        int cell = cells[state];
        int next = at(cell / COLS + move.dr, cell % COLS + move.dc);
        if (next < 0) next = state;
        return new Transition(next, next == goal ? 1 : 0, next == goal);
    }

    // Independent reference length used to check learned routes.
    int shortestDistance() {
        int[] distance = new int[stateCount];
        Arrays.fill(distance, -1);
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        distance[start] = 0;
        queue.add(start);
        while (!queue.isEmpty()) {
            int state = queue.remove();
            if (state == goal) return distance[state];
            for (Move move : MOVES) {
                int next = step(state, move).next();
                if (distance[next] >= 0) continue;
                distance[next] = distance[state] + 1;
                queue.add(next);
            }
        }
        throw new IllegalStateException("The goal is unreachable.");
    }
}
