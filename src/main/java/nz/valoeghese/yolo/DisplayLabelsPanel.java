package nz.valoeghese.yolo;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class DisplayLabelsPanel extends JPanel {
    private Path image;
    private /* Nullable */ BufferedImage baseImage;
    private BoxList metadata;

    public void loadImage(String image) throws IOException {
        this.image = Path.of("images", image);
        String s[] = image.split("\\.");
        String extension = s[s.length - 1];
        Path meta = Path.of("images", "labels", image.substring(0, image.length() - extension.length() - 1) + ".txt");

        this.baseImage = ImageIO.read(this.image.toFile());
        this.metadata = new BoxList(meta);

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
    }

    private static class BoxList implements Iterable<Box> {
        public BoxList(Path path) throws IOException {
            this.path = path;
            if (Files.exists(path)) {
                try (BufferedReader reader = Files.newBufferedReader(path)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Scanner scanner = new Scanner(line);
                        this.boxes.add(new Box(scanner.nextInt(),
                                scanner.nextFloat(),
                                scanner.nextFloat(),
                                scanner.nextFloat(),
                                scanner.nextFloat()));
                    }

                }
            } else {
                Files.createFile(this.path);
            }
        }

        private Path path;
        private List<Box> boxes;

        public Iterator<Box> iterator() {
            return this.boxes.iterator();
        }

        public void addBox(Box box) throws IOException {
            this.boxes.add(box);

            // save (todo: more efficient saving? add shutdown hook)
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path, StandardOpenOption.APPEND))) {
                writer.printf("%d %f %f %f %f\n", box.object, box.x, box.y, box.x1, box.y1);
                writer.flush();
            }
        }

        public void fullSave() throws IOException {
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
                for (Box box : this) {
                    writer.printf("%d %f %f %f %f\n", box.object, box.x, box.y, box.x1, box.y1);
                    writer.flush();
                }
            }
        }
    }
}
