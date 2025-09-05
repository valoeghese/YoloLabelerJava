package nz.valoeghese.yolo.mode;

import nz.valoeghese.yolo.Batch;
import nz.valoeghese.yolo.Box;
import nz.valoeghese.yolo.DisplayLabelsPanel;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Optional;

public class MoveMode extends Mode {
    public MoveMode(DisplayLabelsPanel panel, Batch batch) {
        super(panel, batch);
    }

    private Box movingBox;
    private Point click;
    private int deltaX;
    private int deltaY;

    @Override
    public void mouseReleased(MouseEvent e) {
        if (movingBox != null) {
            Point start = this.start();
            Point end   = this.end();

            if (end.x <= 0 || end.y <= 0) {
                this.panel.getYoloImage().removeBox(movingBox);
            } else {
                movingBox.x0 = start.x;
                movingBox.y0 = start.y;
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
        if (click == null)
            return;
        deltaX = e.getX() - click.x;
        deltaY = e.getY() - click.y;
        this.panel.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // get closest of close boxes
        Optional<SelectResult> result = this.selectBox(e.getPoint());

        movingBox = null;
        if (result.isPresent()) {
            movingBox = result.get().box;
            deltaY = 0;
            deltaX = 0;
            click = e.getPoint();
            this.panel.repaint();
        }
    }

    @Override
    public Point start() {
        if (movingBox == null)
            return null;

        return new Point((int)Math.round(movingBox.x0) + deltaX, (int) Math.round(movingBox.y0) + deltaY);
    }

    @Override
    public Point end() {
        if (movingBox == null)
            return null;

        return new Point((int) Math.round(movingBox.x1) + deltaX, (int) Math.round(movingBox.y1) + deltaY);
    }
}
