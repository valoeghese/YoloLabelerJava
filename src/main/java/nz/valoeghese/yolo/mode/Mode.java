package nz.valoeghese.yolo.mode;

import nz.valoeghese.yolo.Batch;
import nz.valoeghese.yolo.DisplayLabelsPanel;

import java.awt.event.MouseAdapter;

public abstract class Mode extends MouseAdapter implements DisplayLabelsPanel.SelectionBox {
    public Mode(DisplayLabelsPanel panel, Batch batch) {
        this.panel = panel;
        this.batch = batch;
    }

    protected final Batch batch;
    protected final DisplayLabelsPanel panel;
}

