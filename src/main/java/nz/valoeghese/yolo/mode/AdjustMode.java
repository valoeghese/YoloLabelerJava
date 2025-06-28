package nz.valoeghese.yolo.mode;

import nz.valoeghese.yolo.Batch;
import nz.valoeghese.yolo.Box;
import nz.valoeghese.yolo.DisplayLabelsPanel;
import nz.valoeghese.yolo.Edge;

import java.awt.*;
import java.awt.event.MouseEvent;
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
                return new Point((int) movingBox.x, (int) Math.min(movingBox.y + delta, movingBox.y1));
            case LEFT:
                return new Point((int) Math.min(movingBox.x + delta, movingBox.x1), (int) movingBox.y);
            default:
                return new Point((int) movingBox.x, (int) movingBox.y);
        }
    }

    @Override
    public Point end() {
        if (movingBox == null)
            return null;
        switch (movingEdge) {
            case BOTTOM:
                return new Point((int) movingBox.x1, (int) Math.max(movingBox.y, movingBox.y1 + delta));
            case RIGHT:
                return new Point((int) Math.max(movingBox.x, movingBox.x1 + delta), (int) movingBox.y1);
            default:
                return new Point((int) movingBox.x1, (int) movingBox.y1);
        }
    }
}
