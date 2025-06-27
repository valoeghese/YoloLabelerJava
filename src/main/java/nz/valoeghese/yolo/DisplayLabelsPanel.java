package nz.valoeghese.yolo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class DisplayLabelsPanel extends JPanel {
    DisplayLabelsPanel(Batch batch) {
        this.batch = batch;

        class K extends MouseAdapter implements SelectionBox {
            private Point startPoint, endPoint;

            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                System.out.println(startPoint);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                endPoint = e.getPoint();
                DisplayLabelsPanel.this.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (endPoint == null) {
                    startPoint = null;
                    return;
                }
                int width = Math.abs(startPoint.x - endPoint.x);
                int height = Math.abs(startPoint.y - endPoint.y);

                if (width > 2 && height > 2 && DisplayLabelsPanel.this.batch.getCategoriser().getCurrentCategory() != null) {
                    try {
                        DisplayLabelsPanel.this.metadata.addBox(new Box(
                                DisplayLabelsPanel.this.batch.getCategoriser().getCurrentCategory(),
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
                DisplayLabelsPanel.this.repaint();
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
        K adapter = new K();

        this.addMouseListener(adapter);
        this.addMouseMotionListener(adapter);
        this.currentSelecting = adapter;
    }

    private final Batch batch;

    private YoloImage metadata;
    private /* Nullable */ BufferedImage baseImage;

    private SelectionBox currentSelecting;

    public void loadImage(YoloImage image) throws IOException {
        this.metadata = image;
        this.baseImage = image.getImage();

        this.repaint();
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
        Point p = this.currentSelecting.start();
        Point p1 = this.currentSelecting.end();

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

    interface SelectionBox {
        Point start();
        Point end();
    }
}
