package nz.valoeghese.yolo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.Scanner;

public class YoloImage implements Iterable<Box> {
    // Full load (scaled)
    public YoloImage(Path image, Path boxes, Categoriser categoriser) throws IOException {
        this.p_boxes = boxes;
        this.categoriser = categoriser;
        // load
        this.image = ImageIO.read(image.toFile());
        this.boxes = new ArrayList<>();

        if (Files.exists(boxes)) {
            int width = this.image.getWidth();
            int height = this.image.getHeight();

            try (BufferedReader reader = Files.newBufferedReader(boxes)) {
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
        }
    }

    // Imageless mode (raw box coordinates)
    YoloImage(Path boxes, Categoriser categoriser) throws IOException {
        this.p_boxes = boxes;
        this.image = null;
        this.boxes = new ArrayList<>();
        this.categoriser = categoriser;

        if (Files.exists(boxes)) {
            try (BufferedReader reader = Files.newBufferedReader(boxes)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Scanner scanner = new Scanner(line);
                    this.boxes.add(
                            new Box(categoriser.getCategoryLabel(scanner.nextInt()),
                                    scanner.nextFloat(),
                                    scanner.nextFloat(),
                                    scanner.nextFloat(),
                                    scanner.nextFloat())
                    );
                }
            }
        }
    }

    private final Path p_boxes;
    private final BufferedImage image;
    private final ArrayList<Box> boxes;
    private Categoriser categoriser;

    void setCategoriser(Categoriser categoriser) {
        Objects.requireNonNull(categoriser, "Categoriser cannot be null");
        this.categoriser = categoriser;
    }

    public BufferedImage getImage() {
        Objects.requireNonNull(image, "Image is not loaded.");
        return this.image;
    }

    public Iterator<Box> iterator() {
        return this.boxes.iterator();
    }

    public void addBox(Box box) throws IOException {
        Objects.requireNonNull(image, "Image is not loaded.");
        this.boxes.add(box);

        final int width = this.image.getWidth();
        final int height = this.image.getHeight();

        // save (todo: more efficient saving? and add shutdown hook)
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(this.p_boxes, StandardOpenOption.APPEND))) {
            writer.printf("%d %f %f %f %f\n", this.categoriser.getCategoryIdx(box.object), box.x/width, box.y/height, box.x1/width, box.y1/height);
            writer.flush();
        }
    }

    public void reSave() throws IOException {
        if (this.image != null)
            throw new IllegalStateException("Image is loaded.");

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(this.p_boxes))) {
            for (Box box : this) {
                writer.printf("%d %f %f %f %f\n", this.categoriser.getCategoryIdx(box.object), box.x, box.y, box.x1, box.y1);
                writer.flush();
            }
        }
    }

    public void fullSave() throws IOException {
        Objects.requireNonNull(image, "Image is not loaded.");
        final int width = this.image.getWidth();
        final int height = this.image.getHeight();

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(this.p_boxes))) {
            for (Box box : this) {
                writer.printf("%d %f %f %f %f\n", this.categoriser.getCategoryIdx(box.object), box.x/width, box.y/height, box.x1/width, box.y1/height);
                writer.flush();
            }
        }
    }
}
