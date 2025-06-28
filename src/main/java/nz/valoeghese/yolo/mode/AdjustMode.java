package nz.valoeghese.yolo.mode;

import nz.valoeghese.yolo.Batch;
import nz.valoeghese.yolo.Box;
import nz.valoeghese.yolo.DisplayLabelsPanel;
import nz.valoeghese.yolo.Edge;

import java.awt.*;
import java.awt.event.MouseEvent;

public class AdjustMode extends Mode {
    public AdjustMode(DisplayLabelsPanel panel, Batch batch) {
        super(panel, batch);
    }

    private Box movingBox;
    private Edge movingEdge;
    private int click, delta;

    @Override
    public void mouseReleased(MouseEvent e) {
        movingBox = null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (movingEdge == Edge.TOP || movingEdge == Edge.BOTTOM)
            delta = e.getY() - click;
        else
            delta = e.getX() - click;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point clicked = e.getPoint();

        // get closest of close boxes
        final int tolerance = 10;
        double closest = Double.MAX_VALUE;
        Box oClosest = null;
        Edge edge = null;

        for (Box box : this.panel.getYoloImage()) {
            double distX = Math.min(
                    eqCmp(box.x, e.getX(), tolerance),
                    eqCmp(box.x1, e.getX(), tolerance)
            );

            if (distX <= tolerance && box.y - tolerance < e.getY() && box.y1 + tolerance > e.getY()) {
                if (distX < closest) {
                    edge = closest(e.getX(), box.x, box.x1, Edge.LEFT, Edge.RIGHT);
                    closest = distX;
                    oClosest = box;
                }
            }

            double distY = Math.min(
                    eqCmp(box.y, e.getY(), tolerance),
                    eqCmp(box.y1, e.getY(), tolerance)
            );

            if (distY <= tolerance && box.x - tolerance < e.getX() && box.x1 + tolerance > e.getX()) {
                if (distY < closest) {
                    edge = closest(e.getY(), box.y, box.y1, Edge.TOP, Edge.BOTTOM);
                    closest = distY;
                    oClosest = box;
                }
            }
        }

        if (edge == Edge.TOP || edge == Edge.BOTTOM)
            click = clicked.y;
        else
            click = clicked.x;

        movingBox = null;
        if (oClosest != null) {
            movingBox = oClosest;
            System.out.println("oh hyeah ");
            movingEdge = edge;
            delta = 0;
        }
    }

    private double eqCmp(double value, double otherValue, int tolerance) {
        double a;
        if (value > otherValue) {
            a = value - otherValue;
        } else {
            a = otherValue - value;
        }

        if (a <= tolerance)
            return a;

        return Double.MAX_VALUE;
    }

    private <T> T closest(double v, double a, double b, T oA, T oB) {
        if (a + b - 2 * v < 0) {
            return oA;
        } else {
            return oB;
        }
    }

    @Override
    public Point start() {
        if (movingBox == null)
            return null;
        switch (movingEdge) {
            case TOP:
                return new Point((int) movingBox.x, (int) (movingBox.y + delta));
            case LEFT:
                return new Point((int) (movingBox.x + delta), (int) movingBox.y);
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
                return new Point((int) movingBox.x1, (int) (movingBox.y1 + delta));
            case RIGHT:
                return new Point((int) (movingBox.x1 + delta), (int) movingBox.y1);
            default:
                return new Point((int) movingBox.x1, (int) movingBox.y1);
        }
    }
}
