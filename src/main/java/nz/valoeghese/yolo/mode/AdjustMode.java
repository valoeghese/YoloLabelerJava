package nz.valoeghese.yolo.mode;

import nz.valoeghese.yolo.Batch;
import nz.valoeghese.yolo.Box;
import nz.valoeghese.yolo.DisplayLabelsPanel;
import nz.valoeghese.yolo.Edge;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class AdjustMode extends Mode {
    public AdjustMode(DisplayLabelsPanel panel, Batch batch) {
        super(panel, batch);
    }

    private Box movingBox;
    private Edge movingEdge;

    @Override
    public void mousePressed(MouseEvent e) {
        Point clicked = e.getPoint();

        // get closest of close boxes
        final int tolerance = 3;
        double closest = 0;
        Box oClosest = null;
        Edge edge = null;

        for (Box box : this.panel.getYoloImage()) {
            double distX = Math.max(
                    eqCmp(box.x, e.getX(), tolerance),
                    eqCmp(box.x1, e.getX(), tolerance)
            );
            if (distX <= tolerance) {
                double distY = Math.max(
                        eqCmp(box.y, e.getY(), tolerance),
                        eqCmp(box.y1, e.getY(), tolerance)
                );
                if (distY <= tolerance) {
                    double dist;
                    if (distX < distY) {
                        dist = distX;
                        edge = closest(e.getX(), box.x, box.x1, Edge.LEFT, Edge.RIGHT);
                    } else {
                        dist = distY;
                        edge = closest(e.getY(), box.y, box.y1, Edge.TOP, Edge.BOTTOM);
                    }

                    if (dist < closest) {
                        closest = dist;
                        oClosest = box;
                    }
                }
            }
        }

        movingBox = null;
        if (oClosest != null) {
            movingBox = oClosest;
            movingEdge = edge;
        }
    }

    private double eqCmp(double value, double otherValue, int tolerance) {
        double a;
        if (value > otherValue) {
            a = value - otherValue;
        } else {
            a = otherValue - value;
        }

        if (a < tolerance)
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
        return null;
    }

    @Override
    public Point end() {
        return null;
    }
}
