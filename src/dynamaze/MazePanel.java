package dynamaze;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.BitSet;
import javax.swing.JPanel;

final class MazePanel extends JPanel {
    static final Color WALL = new Color(0x283B50);
    static final Color START = new Color(0xFFE4A3);
    static final Color GOAL = new Color(0xBFE8D7);
    static final Color VISITED = new Color(0xCDE0F3);
    private static final Color FLOOR = new Color(0xEFF4F8);

    private final Maze maze;
    private DynaQAgent agent;
    private DynaQAgent.Step last;
    private BitSet trail = new BitSet();
    private boolean showPolicy = true;

    MazePanel(Maze maze) {
        this.maze = maze;
        setOpaque(false);
        setPreferredSize(new Dimension(640, 414));
        getAccessibleContext()
            .setAccessibleName("Six by nine maze with start and goal");
    }

    void update(DynaQAgent agent, DynaQAgent.Step last, BitSet trail,
            boolean showPolicy) {
        this.agent = agent;
        this.last = last;
        this.trail = trail;
        this.showPolicy = showPolicy;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        int cell = Math.min((getWidth() - 49) / Maze.COLS,
            (getHeight() - 30) / Maze.ROWS);
        if (cell < 10) {
            g.dispose();
            return;
        }
        int x0 = (getWidth() - Maze.COLS * cell + 19) / 2;
        int y0 = (getHeight() - Maze.ROWS * cell + 20) / 2;
        int position = last == null ? maze.start : last.outcome().next();
        g.setFont(Ui.font(12, false));
        for (int col = 0; col < Maze.COLS; col++) {
            center(g, Integer.toString(col + 1), x0 + col * cell + cell / 2, y0 - 12,
                Ui.MUTED);
        }
        for (int row = 0; row < Maze.ROWS; row++) {
            g.setFont(Ui.font(12, false));
            center(g, Integer.toString(row + 1), x0 - 15, y0 + row * cell + cell / 2,
                Ui.MUTED);
            for (int col = 0; col < Maze.COLS; col++) {
                int state = maze.at(row, col);
                int x = x0 + col * cell;
                int y = y0 + row * cell;
                int cx = x + cell / 2;
                int cy = y + cell / 2;
                g.setColor(cellColor(state));
                g.fillRoundRect(x + 2, y + 2, cell - 4, cell - 4, 10, 10);
                if (state == position) {
                    g.setColor(Ui.TEAL);
                    int radius = cell / 5;
                    g.fillOval(cx - radius, cy - radius, 2 * radius, 2 * radius);
                    if (state == maze.start || state == maze.goal) {
                        g.setFont(Ui.font(13, true));
                        center(g, state == maze.start ? "S" : "G", cx, cy, Ui.WHITE);
                    }
                } else if (state == maze.start || state == maze.goal) {
                    g.setFont(Ui.font(20, true));
                    center(g, state == maze.start ? "S" : "G", cx, cy, Ui.INK);
                } else if (state >= 0 && showPolicy && agent != null) {
                    int size = Math.max(5, cell / 7);
                    drawArrow(g, agent.greedyMove(state), cx, cy, size);
                }
            }
        }
        g.dispose();
    }

    private Color cellColor(int state) {
        if (state < 0) return WALL;
        if (state == maze.start) return START;
        if (state == maze.goal) return GOAL;
        return trail.get(state) ? VISITED : FLOOR;
    }

    private static void center(Graphics2D g, String text, int x, int y, Color color) {
        var font = g.getFontMetrics();
        g.setColor(color);
        g.drawString(text, x - font.stringWidth(text) / 2,
            y + (font.getAscent() - font.getDescent()) / 2);
    }

    private static void drawArrow(Graphics2D g, Maze.Move move, int x, int y, int size) {
        if (move == null) return;
        int tipX = x + size * move.dc;
        int tipY = y + size * move.dr;
        int tailX = x - size * move.dc;
        int tailY = y - size * move.dr;
        int headX = tipX - (size / 2) * move.dc;
        int headY = tipY - (size / 2) * move.dr;
        int side = size / 2;
        g.setColor(Ui.MUTED);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(tailX, tailY, tipX, tipY);
        g.drawLine(tipX, tipY, headX + side * move.dr, headY + side * move.dc);
        g.drawLine(tipX, tipY, headX - side * move.dr, headY - side * move.dc);
    }
}
