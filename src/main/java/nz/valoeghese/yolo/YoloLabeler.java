package nz.valoeghese.yolo;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public class YoloLabeler extends JPanel implements Categoriser {
    static final Properties labels = new Properties();
    static Path labelsFile;

    public static void main(String[] args) throws IOException {
        // Directory Setup
        Files.createDirectories(Path.of("images", "labels"));
        Files.createDirectories(Path.of("images", "discarded"));

        labelsFile = Path.of("images", "object_labels.txt");
        if (Files.exists(labelsFile)) {
            try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(labelsFile))) {
                labels.load(reader);
            }
        } else {
            Files.createFile(labelsFile);
        }

        // GUI
        JFrame frame = new JFrame();
        frame.setSize(new Dimension(1080, 720));

        frame.add(new YoloLabeler());

        frame.setTitle("YOLO Labeler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
        frame.requestFocus();
    }

    YoloLabeler() throws IOException {
        this.setLayout(new BorderLayout());

        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        this.add(bar, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(display);
        splitPane.setRightComponent(list);
        splitPane.setResizeWeight(1.0);
        this.add(splitPane, BorderLayout.CENTER);

        splitPane.setDividerLocation(880);

        Optional<Path> first = Files.list(Path.of("images"))
                .filter(YoloLabeler::isImage)
                .findFirst();
        if (first.isPresent())
            this.display.loadImage(first.get());
    }

    private final JList<String> list = new JList<>();
    private final DisplayLabelsPanel display = new DisplayLabelsPanel(this);

    @Override
    public String getCurrentCategory() {
        return this.list.getSelectedValue();//this.list.getSelectedIndex();
    }

    @Override
    public int getCategoryIdx(String label) throws IllegalArgumentException {
        for (int i = 0; i < label.length(); i++) {
            if (label.equals(list.getModel().getElementAt(i))) {
                return i;
            }
        }

        throw new IllegalArgumentException("No index");
    }

    @Override
    public String getCategoryLabel(int idx) throws IndexOutOfBoundsException {
        return list.getModel().getElementAt(idx);
    }

    private static boolean isImage(Path p) {
        String filename = p.getFileName().toString();
        String[] parts = filename.split("\\.");
        String ext = parts[parts.length - 1];
        return "jpeg".equals(ext) || "jpg".equals(ext) || ".gif".equals(ext) ||
                "bmp".equals(ext) || "png".equals(ext); // add more file types here
    }
}
