package dynamaze;

import java.util.Random;

final class DynaQAgent {
    static final double ALPHA = 0.10;
    static final double GAMMA = 0.95;
    static final double EPSILON = 0.10;

    record Step(int from, Maze.Move move, Maze.Transition outcome, int length) {}

    private final Maze maze;
    private final int planningSteps;
    private final Random behavior;
    private final Random planning;
    private final double[][] q;
    private final Maze.Transition[][] model;
    private final int[] seenStates;
    private final int[][] seenActions;
    private final int[] actionCounts;
    private int seenCount;
    int modelSize;
    int state;
    int episodeMoves;
    int completedEpisodes;
    long totalMoves;

    DynaQAgent(Maze maze, int planningSteps, long seed) {
        this.maze = maze;
        this.planningSteps = planningSteps;
        behavior = new Random(seed);
        planning = new Random(seed ^ 0x9E3779B97F4A7C15L);
        int actions = Maze.MOVES.length;
        q = new double[maze.stateCount][actions];
        model = new Maze.Transition[maze.stateCount][actions];
        seenStates = new int[maze.stateCount];
        seenActions = new int[maze.stateCount][actions];
        actionCounts = new int[maze.stateCount];
        state = maze.start;
    }

    Step step() {
        int from = state;
        int action = chooseAction(from);
        Maze.Move move = Maze.MOVES[action];
        Maze.Transition outcome = maze.step(from, move);
        backup(from, action, outcome);
        if (planningSteps > 0) learnAndPlan(from, action, outcome);
        int length = ++episodeMoves;
        totalMoves++;
        state = outcome.next();
        if (outcome.terminal()) {
            completedEpisodes++;
            state = maze.start;
            episodeMoves = 0;
        }
        return new Step(from, move, outcome, length);
    }

    private int chooseAction(int state) {
        double[] values = q[state];
        if (behavior.nextDouble() < EPSILON) return behavior.nextInt(values.length);
        double best = maxValue(state);
        int ties = 0;
        for (double value : values) {
            if (value == best) ties++;
        }
        int choice = behavior.nextInt(ties);
        for (int action = 0; action < values.length; action++) {
            if (values[action] == best && choice-- == 0) return action;
        }
        throw new IllegalStateException("No greedy action.");
    }

    private double maxValue(int state) {
        double best = q[state][0];
        for (double value : q[state]) best = Math.max(best, value);
        return best;
    }

    private void backup(int state, int action, Maze.Transition outcome) {
        double target = outcome.reward();
        if (!outcome.terminal()) target += GAMMA * maxValue(outcome.next());
        q[state][action] += ALPHA * (target - q[state][action]);
    }

    private void learnAndPlan(int state, int action, Maze.Transition outcome) {
        if (model[state][action] == null) {
            if (actionCounts[state] == 0) seenStates[seenCount++] = state;
            seenActions[state][actionCounts[state]++] = action;
            modelSize++;
        }
        model[state][action] = outcome;
        for (int i = 0; i < planningSteps; i++) {
            int sampledState = seenStates[planning.nextInt(seenCount)];
            int index = planning.nextInt(actionCounts[sampledState]);
            int sampledAction = seenActions[sampledState][index];
            backup(sampledState, sampledAction, model[sampledState][sampledAction]);
        }
    }

    // The display and its evaluation use a fixed tie order without using the RNG.
    Maze.Move greedyMove(int state) {
        double best = maxValue(state);
        if (best == 0.0) return null;
        for (int action = 0; action < Maze.MOVES.length; action++) {
            if (q[state][action] == best) return Maze.MOVES[action];
        }
        throw new IllegalStateException("No positive action value.");
    }

    int greedyDistance() {
        boolean[] seen = new boolean[maze.stateCount];
        int current = maze.start;
        int distance = 0;
        while (current != maze.goal) {
            Maze.Move move = greedyMove(current);
            if (move == null || seen[current]) return -1;
            seen[current] = true;
            current = maze.step(current, move).next();
            distance++;
        }
        return distance;
    }
}
