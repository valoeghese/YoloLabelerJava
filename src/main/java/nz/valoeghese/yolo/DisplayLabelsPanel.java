package nz.valoeghese.yolo;

import nz.valoeghese.yolo.mode.Mode;
import nz.valoeghese.yolo.mode.NoMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class DisplayLabelsPanel extends JPanel {
    DisplayLabelsPanel(Batch batch) {
        this.batch = batch;
        this.setMode(new NoMode());
    }

    private final Batch batch;
    private Mode selectionMode;

    private YoloImage metadata;
    private /* Nullable */ BufferedImage baseImage;

    public YoloImage getYoloImage() {
        return this.metadata;
    }

    public void loadImage(YoloImage image) throws IOException {
        this.metadata = image;
        this.baseImage = image.getImage();
        this.setMinimumSize(new Dimension(baseImage.getWidth(), baseImage.getHeight()));
        this.setPreferredSize(new Dimension(baseImage.getWidth(), baseImage.getHeight()));
        this.getParent().repaint();

        this.repaint();
    }

    public void setMode(Mode mode) {
        MouseListener[] existing = this.getMouseListeners();
        if (existing.length > 0) {
            this.removeMouseListener(existing[0]);
            this.removeMouseMotionListener((MouseMotionListener) existing[0]);
        }

        this.selectionMode = mode;
        this.addMouseListener(mode);
        this.addMouseMotionListener(mode);
    }

    @Override
    protected void paintComponent(Graphics g) {
        final int width = this.getWidth();
        final int height = this.getHeight();

        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(0, 0, width, height);
        if (this.baseImage != null) {
            g.drawImage(this.baseImage, 0, 0, null);
        }

        // draw boxes
        Point p = this.selectionMode.start();
        Point p1 = this.selectionMode.end();

        if (p1 != null) {
            int x0 = Math.min(p.x, p1.x);
            int y0 = Math.min(p.y, p1.y);

            g.setColor(Color.GREEN);
            g.drawRect(x0, y0, Math.abs(p.x-p1.x), Math.abs(p.y-p1.y));
        }

        if (this.metadata != null) {
            g.setColor(Color.YELLOW);
            for (Box box : this.metadata) {
                g.drawRect((int) box.x, (int) box.y, (int) Math.abs(box.x1 - box.x), (int) Math.abs(box.y1 - box.y));
            }
        }
    }

    public interface SelectionBox {
        Point start();
        Point end();
    }
}
