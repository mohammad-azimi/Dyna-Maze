package dynamaze;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.ParseException;
import java.util.BitSet;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

final class MazeWorkspace extends JPanel {
    private final Maze maze = new Maze();
    private final MazePanel mazeView = new MazePanel(maze);
    private final CurvePanel curves = new CurvePanel();
    private final CardLayout pages = new CardLayout();
    private final JPanel content = new JPanel(pages);
    private final JComboBox<Integer> budget = new JComboBox<>(Experiment.BUDGETS);
    private final JSpinner seed = new JSpinner(
        new SpinnerNumberModel(Long.valueOf(Experiment.FIRST_SEED),
            Long.valueOf(Long.MIN_VALUE), Long.valueOf(Long.MAX_VALUE),
            Long.valueOf(1)));
    private final JButton simulation = Ui.button("Maze simulation", true);
    private final JButton comparison = Ui.button("Learning curves", false);
    private final JButton play = Ui.button("Play", false);
    private final JButton finish = Ui.button("Finish episode", false);
    private final JCheckBox policy = new JCheckBox("Show greedy policy", true);
    private final JLabel episode = Ui.label("", 22, true, Ui.INK);
    private final JLabel moves = Ui.label("", 22, true, Ui.INK);
    private final JLabel total = Ui.label("", 22, true, Ui.INK);
    private final JLabel model = Ui.label("", 22, true, Ui.INK);
    private final JLabel transition = Ui.label("", 15, true, Ui.INK);
    private final JLabel outcome = Ui.label("", 13, false, Ui.MUTED);
    private final JLabel summary = Ui.label("Episodes 2–50 are shown after the run.",
        14, false, Ui.MUTED);
    private final Timer timer = new Timer(150, e -> tick());
    private final BitSet trail = new BitSet();
    private DynaQAgent agent;
    private DynaQAgent.Step last;
    private boolean rapid;

    MazeWorkspace() {
        super(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(18, 24, 20, 24));
        setBackground(Ui.BACKGROUND);
        add(header(), BorderLayout.NORTH);
        content.setOpaque(false);
        content.add(simulationPage(), "maze");
        content.add(comparisonPage(), "curves");
        add(content, BorderLayout.CENTER);
        resetSimulation();
    }

    private JPanel header() {
        JPanel panel = new JPanel(new BorderLayout(0, 11));
        panel.setOpaque(false);
        JPanel title = new JPanel(new BorderLayout());
        title.setOpaque(false);
        title.add(Ui.label("Dyna Maze", 32, true, Ui.INK), BorderLayout.WEST);
        title.add(Ui.label("Tabular learning and planning", 15, false, Ui.MUTED),
            BorderLayout.EAST);
        panel.add(title, BorderLayout.NORTH);
        JPanel navigation = Ui.row(7);
        navigation.add(simulation);
        navigation.add(comparison);
        panel.add(navigation, BorderLayout.SOUTH);
        simulation.addActionListener(e -> showPage(true));
        comparison.addActionListener(e -> showPage(false));
        return panel;
    }

    private void showPage(boolean mazePage) {
        if (!mazePage) pause();
        pages.show(content, mazePage ? "maze" : "curves");
        Ui.colorButton(simulation, mazePage);
        Ui.colorButton(comparison, !mazePage);
    }

    private JPanel simulationPage() {
        JPanel page = new JPanel(new BorderLayout(14, 12));
        page.setOpaque(false);

        JPanel board = Ui.card(new BorderLayout(0, 10), 13);
        board.add(settings(), BorderLayout.NORTH);
        board.add(mazeView, BorderLayout.CENTER);
        JPanel legend = Ui.row(7);
        legend.add(Ui.label("S  Start", 13, true, Ui.INK));
        legend.add(Ui.label("G  Goal", 13, true, Ui.INK));
        legend.add(Ui.label("● Agent", 13, true, Ui.TEAL));
        legend.add(Ui.label("■ Wall", 13, true, MazePanel.WALL));
        legend.add(Ui.label("■ Visited", 13, true, Ui.BLUE));
        board.add(legend, BorderLayout.SOUTH);
        page.add(board, BorderLayout.CENTER);
        page.add(inspector(), BorderLayout.EAST);
        page.add(simulationControls(), BorderLayout.SOUTH);
        return page;
    }

    private JPanel settings() {
        JPanel controls = Ui.row(9);
        controls.add(Ui.label("Planning steps", 13, true, Ui.INK));
        budget.setSelectedItem(50);
        controls.add(budget);
        controls.add(Ui.label("Seed", 13, true, Ui.INK));
        seed.setEditor(new JSpinner.NumberEditor(seed, "#"));
        seed.setPreferredSize(new Dimension(110, 29));
        controls.add(seed);
        JButton reset = Ui.button("Reset", false);
        controls.add(reset);
        budget.addActionListener(e -> resetSimulation());
        reset.addActionListener(e -> resetSimulation());
        return controls;
    }

    private JPanel inspector() {
        JPanel panel = Ui.card(new BorderLayout(0, 14), 15);
        panel.setPreferredSize(new Dimension(276, 280));
        JPanel counters = new JPanel(new GridLayout(2, 2, 9, 13));
        counters.setOpaque(false);
        counters.add(metric("Episode", episode));
        counters.add(metric("Moves", moves));
        counters.add(metric("Total moves", total));
        counters.add(metric("Model entries", model));
        JPanel overview = new JPanel(new BorderLayout(0, 15));
        overview.setOpaque(false);
        overview.add(Ui.label("Run status", 19, true, Ui.INK), BorderLayout.NORTH);
        overview.add(counters, BorderLayout.CENTER);
        panel.add(overview, BorderLayout.NORTH);

        JPanel notes = new JPanel(new GridLayout(3, 1, 0, 8));
        notes.setOpaque(false);
        notes.add(Ui.label("Last real move", 13, true, Ui.MUTED));
        notes.add(transition);
        notes.add(outcome);
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(notes, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        String parameters = "\u03b1 0.10    \u03b3 0.95    \u03b5 0.10";
        panel.add(Ui.label(parameters, 14, true, Ui.INK),
            BorderLayout.SOUTH);
        return panel;
    }

    private static JPanel metric(String title, JLabel value) {
        JPanel panel = Ui.card(new BorderLayout(0, 3), 8);
        panel.add(Ui.label(title, 12, true, Ui.MUTED), BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    private JPanel simulationControls() {
        JPanel controls = Ui.row(8);
        JButton step = Ui.button("Step", true);
        controls.add(step);
        controls.add(play);
        controls.add(finish);
        policy.setOpaque(false);
        policy.setFont(Ui.font(14, false));
        controls.add(policy);
        step.addActionListener(e -> {
            pause();
            advance(1, false);
        });
        play.addActionListener(e -> {
            if (timer.isRunning() && !rapid) pause();
            else startTimer(false);
        });
        finish.addActionListener(e -> {
            if (timer.isRunning() && rapid) pause();
            else startTimer(true);
        });
        policy.addActionListener(e -> refresh());
        return controls;
    }

    private JPanel comparisonPage() {
        JPanel page = Ui.card(new BorderLayout(0, 10), 16);
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        JButton run = Ui.button("Run comparison", true);
        JPanel heading = Ui.row(9);
        heading.add(run);
        String settings = "30 runs per setting   ·   50 episodes   ·   seeds 1001–1030";
        heading.add(Ui.label(settings, 14, false, Ui.MUTED));
        top.add(heading, BorderLayout.NORTH);
        JPanel legend = Ui.row(15);
        legend.add(Ui.label("●  n = 0", 14, true, Ui.SERIES[0]));
        legend.add(Ui.label("●  n = 5", 14, true, Ui.SERIES[1]));
        legend.add(Ui.label("●  n = 50", 14, true, Ui.SERIES[2]));
        String reference = "Dashed line · 14-move shortest route";
        legend.add(Ui.label(reference, 13, false, Ui.MUTED));
        top.add(legend, BorderLayout.SOUTH);
        page.add(top, BorderLayout.NORTH);
        page.add(curves, BorderLayout.CENTER);
        page.add(summary, BorderLayout.SOUTH);
        run.addActionListener(e -> runComparison(run));
        return page;
    }

    private void runComparison(JButton run) {
        run.setEnabled(false);
        summary.setText("Running all 90 training runs...");
        new SwingWorker<Experiment.Result, Void>() {
            @Override
            protected Experiment.Result doInBackground() {
                return Experiment.run();
            }

            @Override
            protected void done() {
                try {
                    var result = get();
                    curves.setResult(result, maze.shortestDistance());
                    summary.setText(String.format(Locale.ROOT,
                        "Episode 1 mean %.1f (all settings)   |   "
                            + "Episode 5 (n = 0, 5, 50)  %.1f / %.1f / %.1f",
                        result.mean(0, 0), result.mean(0, 4), result.mean(1, 4),
                        result.mean(2, 4)));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    summary.setText("Comparison interrupted.");
                } catch (ExecutionException ex) {
                    summary.setText("Comparison failed. " + ex.getCause().getMessage());
                } finally {
                    run.setEnabled(true);
                }
            }
        }.execute();
    }

    private void resetSimulation() {
        try {
            seed.commitEdit();
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, "Enter a whole-number seed.",
                "Invalid seed", JOptionPane.WARNING_MESSAGE);
            return;
        }
        pause();
        agent = new DynaQAgent(maze, budget.getItemAt(budget.getSelectedIndex()),
            ((Number) seed.getValue()).longValue());
        last = null;
        trail.clear();
        trail.set(maze.start);
        refresh();
    }

    private void startTimer(boolean fast) {
        rapid = fast;
        play.setText(fast ? "Play" : "Pause");
        finish.setText(fast ? "Stop episode" : "Finish episode");
        timer.setDelay(fast ? 25 : 150);
        timer.setInitialDelay(0);
        timer.restart();
    }

    private void pause() {
        timer.stop();
        rapid = false;
        play.setText("Play");
        finish.setText("Finish episode");
    }

    private void tick() {
        advance(rapid ? 120 : 1, rapid);
    }

    private void advance(int count, boolean stopAtGoal) {
        for (int i = 0; i < count; i++) {
            if (last != null && last.outcome().terminal()) {
                trail.clear();
                trail.set(maze.start);
            }
            last = agent.step();
            trail.set(last.outcome().next());
            if (last.outcome().terminal() && stopAtGoal) {
                pause();
                break;
            }
        }
        refresh();
    }

    private void refresh() {
        boolean reached = last != null && last.outcome().terminal();
        episode.setText(Integer.toString(agent.completedEpisodes + (reached ? 0 : 1)));
        moves.setText(Integer.toString(last == null ? 0 : last.length()));
        total.setText(Long.toString(agent.totalMoves));
        model.setText(Integer.toString(agent.modelSize));
        if (last == null) {
            transition.setText("At the start");
            outcome.setText("Step or play to begin.");
        } else {
            String direction = last.move().name().toLowerCase(Locale.ROOT);
            transition.setText(Character.toUpperCase(direction.charAt(0))
                + direction.substring(1) + (last.from() == last.outcome().next()
                ? " (blocked)" : ""));
            outcome.setText(reached ? "Goal reached · reward +1" : "Reward 0");
        }
        mazeView.update(agent, last, trail, policy.isSelected());
    }
}
