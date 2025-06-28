package nz.valoeghese.yolo.mode;

import nz.valoeghese.yolo.Batch;
import nz.valoeghese.yolo.Box;
import nz.valoeghese.yolo.DisplayLabelsPanel;
import nz.valoeghese.yolo.Edge;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.Optional;

public abstract class Mode extends MouseAdapter implements DisplayLabelsPanel.SelectionBox {
    public Mode(DisplayLabelsPanel panel, Batch batch) {
        this.panel = panel;
        this.batch = batch;
    }

    protected final Batch batch;
    protected final DisplayLabelsPanel panel;

    protected Optional<SelectResult> selectBox(Point pt) {
        // get closest of close boxes
        final int tolerance = 10;
        double closest = Double.MAX_VALUE;
        Box selected = null;
        Edge edge = null;

        for (Box box : this.panel.getYoloImage()) {
            double distX = Math.min(
                    eqCmp(box.x, pt.getX(), tolerance),
                    eqCmp(box.x1, pt.getX(), tolerance)
            );

            if (distX <= tolerance && box.y - tolerance < pt.getY() && box.y1 + tolerance > pt.getY()) {
                if (distX < closest) {
                    edge = closest(pt.getX(), box.x, box.x1, Edge.LEFT, Edge.RIGHT);
                    closest = distX;
                    selected = box;
                }
            }

            double distY = Math.min(
                    eqCmp(box.y, pt.getY(), tolerance),
                    eqCmp(box.y1, pt.getY(), tolerance)
            );

            if (distY <= tolerance && box.x - tolerance < pt.getX() && box.x1 + tolerance > pt.getX()) {
                if (distY < closest) {
                    edge = closest(pt.getY(), box.y, box.y1, Edge.TOP, Edge.BOTTOM);
                    closest = distY;
                    selected = box;
                }
            }
        }

        return selected == null ? Optional.empty() : Optional.of(new SelectResult(selected, edge));
    }

    protected static class SelectResult {
        SelectResult(Box box, Edge edge) {
            this.box = box;
            this.edge = edge;
        }

        public final Box box;
        public final Edge edge;
    }

    private static double eqCmp(double value, double otherValue, int tolerance) {
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

    private static <T> T closest(double v, double a, double b, T oA, T oB) {
        if (a + b - 2 * v > 0) {
            return oA;
        } else {
            return oB;
        }
    }
}

