package nz.valoeghese.yolo;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class DisplayLabelsPanel extends JPanel {
    DisplayLabelsPanel(Categoriser categoriser) {
        this.categoriser = categoriser;

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

                if (width > 2 && height > 2) {
                    try {
                        DisplayLabelsPanel.this.metadata.addBox(new Box(
                                DisplayLabelsPanel.this.categoriser.getCurrentCategory(),
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

    private final Categoriser categoriser;
    private Path image;
    private /* Nullable */ BufferedImage baseImage;
    private BoxList metadata;
    private SelectionBox currentSelecting;

    public void loadImage(Path image) throws IOException {
        this.image = image;
        String filename = image.getFileName().toString();
        String s[] = filename.split("\\.");
        String extension = s[s.length - 1];
        Path meta = Path.of("images", "labels", filename.substring(0, filename.length() - extension.length() - 1) + ".txt");

        this.baseImage = ImageIO.read(this.image.toFile());
        this.metadata = new BoxList(meta, this.categoriser, this.baseImage.getWidth(), this.baseImage.getHeight());

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

    private static class BoxList implements Iterable<Box> {
        public BoxList(Path path, Categoriser categoriser, int width, int height) throws IOException {
            this.path = path;
            this.width = width;
            this.height =height;
            this.categoriser = categoriser;
            this.boxes = new ArrayList<>();

            if (Files.exists(path)) {
                try (BufferedReader reader = Files.newBufferedReader(path)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Scanner scanner = new Scanner(line);
                        this.boxes.add(
                                new Box(categoriser.getCategoryLabel(scanner.nextInt()),
                                        scanner.nextFloat() * width,
                                        scanner.nextFloat() * height,
                                        scanner.nextFloat() * width,
                                        scanner.nextFloat() * height)
                        );
                    }

                }
            } else {
                Files.createFile(this.path);
            }
        }

        private final float width, height;
        private final Categoriser categoriser;
        private final List<Box> boxes;
        private Path path;

        public Iterator<Box> iterator() {
            return this.boxes.iterator();
        }

        public void addBox(Box box) throws IOException {
            this.boxes.add(box);

            // save (todo: more efficient saving? add shutdown hook)
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path, StandardOpenOption.APPEND))) {
                writer.printf("%d %f %f %f %f\n", this.categoriser.getCategoryIdx(box.object), box.x/width, box.y/height, box.x1/width, box.y1/height);
                writer.flush();
            }
        }

        public void fullSave() throws IOException {
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
                for (Box box : this) {
                    writer.printf("%d %f %f %f %f\n", this.categoriser.getCategoryIdx(box.object), box.x/width, box.y/height, box.x1/width, box.y1/height);
                    writer.flush();
                }
            }
        }
    }

    interface SelectionBox {
        Point start();
        Point end();
    }
}
