package dynamaze;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.JPanel;

final class CurvePanel extends JPanel {
    private Experiment.Result result;
    private int reference;

    CurvePanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(880, 360));
        getAccessibleContext()
            .setAccessibleName("Mean moves to goal by training episode");
    }

    void setResult(Experiment.Result result, int reference) {
        this.result = result;
        this.reference = reference;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(Ui.font(13, false));
        g.setColor(Ui.MUTED);
        if (result == null) {
            g.drawString("Run the comparison to plot mean moves to goal.", 30, 55);
            g.dispose();
            return;
        }

        int left = 61;
        int top = 39;
        int width = getWidth() - left - 25;
        int height = getHeight() - top - 57;
        double ceiling = yCeiling();
        g.drawString("Mean moves to goal", 9, 20);
        for (int i = 0; i <= 5; i++) {
            int y = top + height - i * height / 5;
            g.setColor(Ui.BORDER);
            g.drawLine(left, y, left + width, y);
            g.setColor(Ui.MUTED);
            g.drawString(Integer.toString((int) (ceiling * i / 5)), 13, y + 4);
        }
        for (int e : new int[]{2, 10, 20, 30, 40, 50}) {
            int x = left + (e - 2) * width / (Experiment.EPISODES - 2);
            g.setColor(Ui.BORDER);
            g.drawLine(x, top, x, top + height);
            g.setColor(Ui.MUTED);
            g.drawString(Integer.toString(e), x - 6, top + height + 22);
        }
        g.drawString("Training episode", left + width / 2 - 51, getHeight() - 9);
        g.setColor(Ui.MUTED);
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER, 10f, new float[]{5f, 5f}, 0f));
        int referenceY = top + height - (int) Math.round(reference / ceiling * height);
        g.drawLine(left, referenceY, left + width, referenceY);

        for (int b = 0; b < Experiment.BUDGETS.length; b++) {
            g.setColor(Ui.SERIES[b]);
            g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
            Path2D line = new Path2D.Double();
            for (int e = 1; e < Experiment.EPISODES; e++) {
                double x = left + (e - 1.0) * width / (Experiment.EPISODES - 2);
                double y = top + height - result.mean(b, e) / ceiling * height;
                if (e == 1) line.moveTo(x, y);
                else line.lineTo(x, y);
            }
            g.draw(line);
        }
        g.dispose();
    }

    private double yCeiling() {
        double max = 0;
        for (int b = 0; b < Experiment.BUDGETS.length; b++) {
            for (int e = 1; e < Experiment.EPISODES; e++) {
                max = Math.max(max, result.mean(b, e));
            }
        }
        return Math.max(100, Math.ceil(max / 100) * 100);
    }
}
