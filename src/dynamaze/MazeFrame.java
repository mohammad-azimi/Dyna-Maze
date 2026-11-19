package dynamaze;

import java.awt.Dimension;
import javax.swing.JFrame;

final class MazeFrame extends JFrame {
    MazeFrame() {
        super("Dyna Maze");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(new MazeWorkspace());
        setMinimumSize(new Dimension(990, 650));
        setSize(1160, 750);
        setLocationRelativeTo(null);
    }
}
