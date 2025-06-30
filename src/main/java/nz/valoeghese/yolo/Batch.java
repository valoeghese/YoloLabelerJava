package nz.valoeghese.yolo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Internal Model.
 */
public class Batch {
    Batch(Path root) throws IOException {
        this.root = root;
        this.labelsFile = this.root.resolve("object_labels.xml");
        this.discardDirectory = this.root.resolve("discarded");
        // Directory Setup
        Files.createDirectories(this.root.resolve( "labels"));
        Files.createDirectories(this.discardDirectory);
        // Load labels
        if (Files.exists(labelsFile)) {
            try (InputStream reader = new BufferedInputStream(Files.newInputStream(labelsFile))) {
                labels.loadFromXML(reader);
            }

            this.labelCount = this.categoriser.updateModel(labels);
            String lsle = labels.getProperty("lastEdited");
            this.lastSessionLastEdited = lsle == null ? null : this.root.resolve(lsle);
        } else {
            Files.createFile(labelsFile);
            this.lastSessionLastEdited = null;
        }
    }

    private final Path root, labelsFile, discardDirectory;
    private final Path lastSessionLastEdited;
    private Properties labels = new Properties();
    private int labelCount = 0;
    private LabellingContext categoriser = new LabellingContext();

    public Stream<Path> listImages() throws IOException {
        return Files.list(Paths.get("images"))
                .filter(Batch::isImage);
    }

    public Categoriser getCategoriser() {
        return this.categoriser;
    }

    public Optional<Path> getLastSessionLastEdited() {
        return Optional.ofNullable(this.lastSessionLastEdited);
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
        String[] s = filename.split("\\.");
        String extension = s[s.length - 1];
        Path meta = Paths.get("images", "labels", filename.substring(0, filename.length() - extension.length() - 1) + ".txt");
        return new YoloImage(path, meta, this.categoriser);
    }

    public void discardImage(Path path) throws IOException {
        Files.move(path, this.discardDirectory.resolve(this.root.relativize(path)));
    }

    public void setLastEdited(Path path) throws IOException {
        Path p = this.root.relativize(path);
        String relativePath = p.toString();
        if (!relativePath.equals(this.labels.getProperty("lastEdited"))) {
            this.labels.setProperty("lastEdited", relativePath);
        }

        this.saveXML();
    }

    public void addLabel(String label) throws IOException {
        if (this.labels.containsValue(label))
            return;

        this.labels.put(
                String.valueOf(this.labelCount++),
                label
        );
        this.saveXML();

        this.categoriser.updateModel(this.labels);
    }

    private void saveXML() throws IOException {
        try (OutputStream writer = new BufferedOutputStream(Files.newOutputStream(this.labelsFile))) {
            this.labels.storeToXML(writer, "YOLO Labeler labels");
        }
    }

    public void removeLabel(String label) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    class InvalidValue extends IllegalArgumentException {
        InvalidValue(String message) {
            super(message);
        }
    }

    public void renameLabel(String label, String newName) throws IOException {
        String key = null;
        Properties copy = new Properties();
        for (Map.Entry<Object, Object> entry : this.labels.entrySet()) {
            if (entry.getValue().equals(label)) {
                key = (String)entry.getKey();
            }
            if (entry.getValue().equals(newName)) {
                throw new InvalidValue("name \"" + newName + "\" is already taken!");
            }
            copy.put(entry.getKey(), entry.getValue());
        }
        Objects.requireNonNull(key, "label \"" + label + "\" could not be found.");

        copy.put(key, newName);
        // save
        try (OutputStream writer = new BufferedOutputStream(Files.newOutputStream(this.labelsFile))) {
            copy.storeToXML(writer, "YOLO Labeler labels");
        }
        // no error
        this.labels.setProperty(key, newName);
        this.categoriser.updateModel(this.labels);
    }
}
