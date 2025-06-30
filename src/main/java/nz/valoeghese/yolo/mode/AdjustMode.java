package nz.valoeghese.yolo.mode;

import nz.valoeghese.yolo.Batch;
import nz.valoeghese.yolo.Box;
import nz.valoeghese.yolo.DisplayLabelsPanel;
import nz.valoeghese.yolo.Edge;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Optional;

public class AdjustMode extends Mode {
    public AdjustMode(DisplayLabelsPanel panel, Batch batch) {
        super(panel, batch);
    }

    private Box movingBox;
    private Edge movingEdge;
    private int click, delta;

    @Override
    public void mouseReleased(MouseEvent e) {
        if (movingBox != null) {
            Point start = this.start();
            Point end   = this.end();

            if (end.x - start.x < 1 || end.y - start.y < 1) {
                this.panel.getYoloImage().removeBox(movingBox);
            } else {
                movingBox.x = start.x;
                movingBox.y = start.y;
                movingBox.x1 = end.x;
                movingBox.y1 = end.y;
            }

            this.panel.markDirty();

            movingBox = null;
            this.panel.repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (movingBox == null)
            return;

        if (movingEdge == Edge.TOP || movingEdge == Edge.BOTTOM)
            delta = e.getY() - click;
        else
            delta = e.getX() - click;
        this.panel.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point clicked = e.getPoint();

        Optional<SelectResult> result = this.selectBox(clicked);

        if (result.isPresent()) {
            Edge edge = result.get().edge;
            Box box = result.get().box;

            if (edge == Edge.TOP || edge == Edge.BOTTOM)
                click = clicked.y;
            else
                click = clicked.x;

            movingBox = null;
            if (box != null) {
                movingBox = box;
                movingEdge = edge;
                delta = 0;
                this.panel.repaint();
            }
        }
    }

    @Override
    public Point start() {
        if (movingBox == null)
            return null;

        switch (movingEdge) {
            case TOP:
                return point(movingBox.x, clamp(movingBox.y + delta, 0, movingBox.y1));
            case LEFT:
                return point(clamp(movingBox.x + delta, 0, movingBox.x1), movingBox.y);
            default:
                return point(movingBox.x, movingBox.y);
        }
    }

    @Override
    public Point end() {
        if (movingBox == null)
            return null;

        final BufferedImage i = this.panel.getYoloImage().getImage();
        switch (movingEdge) {
            case BOTTOM:
                return point(movingBox.x1, clamp(movingBox.y1 + delta, movingBox.y, i.getHeight() - 1));
            case RIGHT:
                return point(clamp(movingBox.x1 + delta, movingBox.x, i.getWidth() - 1), movingBox.y1);
            default:
                return point(movingBox.x1, movingBox.y1);
        }
    }

    private static double clamp(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}
