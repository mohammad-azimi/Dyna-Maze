package dynamaze;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

final class Experiment {
    static final Integer[] BUDGETS = {0, 5, 50};
    static final int RUNS = 30;
    static final int EPISODES = 50;
    static final long FIRST_SEED = 1001;
    private static final int MOVE_LIMIT = 1_000_000;

    record Result(int[][][] lengths, int[][] greedyLengths) {
        double mean(int setting, int episodeIndex) {
            long total = 0;
            for (int[] run : lengths[setting]) total += run[episodeIndex];
            return (double) total / RUNS;
        }

        void writeCsv(Path directory) throws IOException {
            Files.createDirectories(directory);
            Path curves = directory.resolve("learning_curves.csv");
            try (var file = Files.newBufferedWriter(curves)) {
                file.write("episode,n0,n5,n50\n");
                for (int e = 0; e < EPISODES; e++) {
                    file.write(String.format(Locale.ROOT, "%d,%.6f,%.6f,%.6f%n",
                        e + 1, mean(0, e), mean(1, e), mean(2, e)));
                }
            }
            try (var file = Files.newBufferedWriter(directory.resolve("runs.csv"))) {
                file.write("planning_steps,run,seed,episode,moves\n");
                for (int b = 0; b < BUDGETS.length; b++) {
                    for (int r = 0; r < RUNS; r++) {
                        for (int e = 0; e < EPISODES; e++) {
                            long seed = FIRST_SEED + r;
                            file.write(BUDGETS[b] + "," + (r + 1) + "," + seed
                                + "," + (e + 1) + "," + lengths[b][r][e] + "\n");
                        }
                    }
                }
            }
            Path routes = directory.resolve("greedy_routes.csv");
            try (var file = Files.newBufferedWriter(routes)) {
                file.write("planning_steps,run,seed,greedy_moves\n");
                for (int b = 0; b < BUDGETS.length; b++) {
                    for (int r = 0; r < RUNS; r++) {
                        file.write(BUDGETS[b] + "," + (r + 1) + "," + (FIRST_SEED + r)
                            + "," + greedyLengths[b][r] + "\n");
                    }
                }
            }
        }
    }

    static Result run() {
        Maze maze = new Maze();
        int[][][] lengths = new int[BUDGETS.length][RUNS][EPISODES];
        int[][] greedy = new int[BUDGETS.length][RUNS];
        for (int b = 0; b < BUDGETS.length; b++) {
            for (int r = 0; r < RUNS; r++) {
                var agent = new DynaQAgent(maze, BUDGETS[b], FIRST_SEED + r);
                for (int e = 0; e < EPISODES; e++) {
                    lengths[b][r][e] = completeEpisode(agent);
                }
                if (lengths[b][r][0] != lengths[0][r][0]) {
                    throw new IllegalStateException("The first episodes must match.");
                }
                greedy[b][r] = agent.greedyDistance();
            }
        }
        return new Result(lengths, greedy);
    }

    private static int completeEpisode(DynaQAgent agent) {
        for (int i = 0; i < MOVE_LIMIT; i++) {
            var step = agent.step();
            if (step.outcome().terminal()) return step.length();
        }
        throw new IllegalStateException("An episode exceeded the move limit.");
    }
}
