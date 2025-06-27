package nz.valoeghese.yolo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Internal Model.
 */
public class Batch {
    Batch(Path root) throws IOException {
        this.root = root;
        this.labelsFile = this.root.resolve("object_labels.xml");
        // Directory Setup
        Files.createDirectories(this.root.resolve( "labels"));
        Files.createDirectories(this.root.resolve("discarded"));
        // Load labels
        if (Files.exists(labelsFile)) {
            try (InputStream reader = new BufferedInputStream(Files.newInputStream(labelsFile))) {
                labels.loadFromXML(reader);
            }

            this.categoriser.updateModel(labels);
        } else {
            Files.createFile(labelsFile);
        }
    }

    private final Path root, labelsFile;
    private Properties labels = new Properties();
    private LabellingContext categoriser = new LabellingContext();

    public Stream<Path> listImages() throws IOException {
        return Files.list(Paths.get("images"))
                .filter(Batch::isImage);
    }

    public Categoriser getCategoriser() {
        return this.categoriser;
    }

    private static boolean isImage(Path p) {
        String filename = p.getFileName().toString();
        String[] parts = filename.split("\\.");
        String ext = parts[parts.length - 1];
        return "jpeg".equals(ext) || "jpg".equals(ext) || ".gif".equals(ext) ||
                "bmp".equals(ext) || "png".equals(ext); // add more file types here
    }

    public YoloImage loadImage(Path path) throws IOException {
        String filename = path.getFileName().toString();
        String s[] = filename.split("\\.");
        String extension = s[s.length - 1];
        Path meta = Paths.get("images", "labels", filename.substring(0, filename.length() - extension.length() - 1) + ".txt");
        return new YoloImage(path, meta, this.categoriser);
    }

    public void addLabel(String label) throws IOException {
        if (this.labels.containsValue(label))
            return;

        this.labels.put(
                String.valueOf(this.labels.size()),
                label
        );
        try (OutputStream writer = new BufferedOutputStream(Files.newOutputStream(this.labelsFile))) {
            this.labels.storeToXML(writer, "YOLO Labeler labels");
        }

        this.categoriser.updateModel(this.labels);
    }

    public void removeLabel(String label) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void renameLabel(String label, String newName) {
        throw new UnsupportedOperationException("Not implemented.");
    }
}
