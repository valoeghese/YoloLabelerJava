package nz.valoeghese.yolo.mode;

import nz.valoeghese.yolo.Batch;
import nz.valoeghese.yolo.DisplayLabelsPanel;

import java.awt.*;

public class NoMode extends Mode {
    public NoMode() {
        super(null, null);
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
