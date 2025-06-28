package nz.valoeghese.yolo.mode;

import nz.valoeghese.yolo.Batch;
import nz.valoeghese.yolo.DisplayLabelsPanel;

import java.awt.*;
import java.awt.event.MouseEvent;

public class AdjustMode extends Mode {
    public AdjustMode(DisplayLabelsPanel panel, Batch batch) {
        super(panel, batch);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        System.out.println("adkjiust");
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
