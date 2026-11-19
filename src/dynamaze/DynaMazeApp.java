package dynamaze;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import javax.swing.SwingUtilities;

public final class DynaMazeApp {
    private DynaMazeApp() {}

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            SwingUtilities.invokeLater(() -> new MazeFrame().setVisible(true));
            return;
        }
        if (!args[0].equals("--experiment") || args.length > 2) {
            System.err.println(
                "Usage: java -jar DynaMaze.jar [--experiment [directory]]");
            System.exit(2);
        }
        Path directory = Path.of(args.length == 2 ? args[1] : "results");
        var maze = new Maze();
        var result = Experiment.run();
        result.writeCsv(directory);
        System.out.printf(Locale.ROOT,
            "%d states, shortest route %d moves, common episode 1 mean %.2f moves%n",
            maze.stateCount, maze.shortestDistance(), result.mean(0, 0));
        System.out.println("CSV files written to " + directory.toAbsolutePath());
    }
}
