package nz.valoeghese.yolo.mode;

import nz.valoeghese.yolo.Batch;
import nz.valoeghese.yolo.Box;
import nz.valoeghese.yolo.DisplayLabelsPanel;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class CreateMode extends Mode {
    public CreateMode(DisplayLabelsPanel panel, Batch batch) {
        super(panel, batch);
    }

    private Point startPoint, endPoint;

    @Override
    public void mousePressed(MouseEvent e) {
        startPoint = e.getPoint();
        System.out.println(startPoint);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // clamp
        int x = e.getPoint().x;
        int y = e.getPoint().y;
        int w = this.panel.getYoloImage().getImage().getWidth();
        int h = this.panel.getYoloImage().getImage().getHeight();
        if (x < 0) {
            x = 0;
        }
        if (x >= w) {
            x = w - 1;
        }
        if (y < 0) {
            y = 0;
        }
        if (y >= h) {
            y = h - 1;
        }

        endPoint = new Point(x, y);
        this.panel.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (endPoint == null) {
            startPoint = null;
            return;
        }
        int width = Math.abs(startPoint.x - endPoint.x);
        int height = Math.abs(startPoint.y - endPoint.y);

        if (width > 2 && height > 2 && this.batch.getCategoriser().getCurrentCategory() != null) {
            try {
                this.panel.getYoloImage().addBox(Box.spanning(
                        this.batch.getCategoriser().getCurrentCategory(),
                        Math.min(startPoint.x, endPoint.x),
                        Math.min(startPoint.y, endPoint.y),
                        Math.max(startPoint.x, endPoint.x),
                        Math.max(startPoint.y, endPoint.y)
                ));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        startPoint = null;
        endPoint = null;
        this.panel.repaint();
    }

    @Override
    public Point start() {
        return this.startPoint;
    }

    @Override
    public Point end() {
        return this.endPoint;
    }
}
