package nz.valoeghese.yolo;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class YoloLabeler extends JPanel {
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
    }

    private final JList<String> list = new JList<>();
    private final DisplayLabelsPanel display = new DisplayLabelsPanel();
}
