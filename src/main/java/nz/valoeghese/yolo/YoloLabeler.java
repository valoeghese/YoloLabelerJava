package nz.valoeghese.yolo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Optional;

public class YoloLabeler extends JPanel {
    public static void main(String[] args) throws IOException {
        // GUI
        JFrame frame = new JFrame();
        frame.setSize(new Dimension(1080, 720));

        Batch batch = new Batch(Path.of("images"));
        frame.add(new YoloLabeler(batch));

        frame.setTitle("YOLO Labeler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
        frame.requestFocus();
    }

    private final DisplayLabelsPanel display;

    YoloLabeler(Batch batch) throws IOException {
        this.setLayout(new BorderLayout());

        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        this.add(bar, BorderLayout.NORTH);

        this.display = new DisplayLabelsPanel(batch);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(this.display);

        JPanel panel = new JPanel();
        JButton button = new JButton("+");
        panel.setLayout(new BorderLayout());
        panel.add(batch.getCategoriser().createInterface(), BorderLayout.CENTER);
        panel.add(button, BorderLayout.SOUTH);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    batch.addLabel("New Label");
                } catch (IOException ex) {
                    throw new UncheckedIOException("Error saving labels", ex);
                }
            }
        });

        splitPane.setRightComponent(panel);
        splitPane.setResizeWeight(1.0);
        this.add(splitPane, BorderLayout.CENTER);

        splitPane.setDividerLocation(880);

        Optional<Path> first = batch.listImages().findFirst();
        if (first.isPresent())
            this.display.loadImage(batch.loadImage(first.get()));
    }
}
