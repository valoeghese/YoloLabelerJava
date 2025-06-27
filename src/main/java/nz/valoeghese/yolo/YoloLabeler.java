package nz.valoeghese.yolo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class YoloLabeler extends JPanel {
    public static void main(String[] args) throws IOException {
        // GUI
        JFrame frame = new JFrame();
        frame.setSize(new Dimension(1080, 720));

        Batch batch = new Batch(Paths.get("images"));
        frame.add(new YoloLabeler(batch));

        frame.setTitle("YOLO Labeler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
        frame.requestFocus();
    }

    private final DisplayLabelsPanel display;
    private final Batch batch;
    private JButton previous, next;
    private Path currentPath;

    YoloLabeler(Batch batch) throws IOException {
        this.batch = batch;
        this.setLayout(new BorderLayout());

        this.display = new DisplayLabelsPanel(batch);

        JToolBar bar = new JToolBar();
        bar.setFloatable(false);

        JButton createMode = new JButton("Create");
        JButton adjustMode = new JButton("Adjust");
        new SelectionGroup(createMode, adjustMode).bind(t -> {}, Modes.CREATE, Modes.ADJUST);
        bar.add(createMode);
        bar.add(adjustMode);
        bar.addSeparator();

        this.previous = new JButton("Previous");
        this.previous.setEnabled(false);
        this.previous.addActionListener(uncheckedIO(() -> {
            Path prev = this.previousPage(batch::listImages).orElse(null);
            if (prev != null) {
                this.load(prev);
            }
        }));
        this.next = new JButton("Next");
        this.next.setEnabled(false);
        this.next.addActionListener(uncheckedIO(() -> {
            Path next = this.nextPage(batch::listImages).orElse(null);
            if (next != null) {
                this.load(next);
            }
        }));

        bar.add(previous);
        bar.add(next);
        bar.addSeparator();

        JButton discardImage = new JButton("Discard this Image");
        bar.add(discardImage);

        this.add(bar, BorderLayout.NORTH);

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

        List<Path> images = batch.listImages().collect(Collectors.toList());

        if (!images.isEmpty())
        {
            currentPath = images.get(0);
            this.load(images.get(0));

            if (images.size() > 1) {
                this.next.setEnabled(true);
            }
        }
    }

    private void load(Path path) throws IOException {
        this.currentPath = path;
        this.display.loadImage(batch.loadImage(path));
    }

    private Optional<Path> previousPage(IOSupplier<Stream<Path>> stream) throws IOException {
        Path last = null;
        for (Iterator<Path> it = stream.get().iterator(); it.hasNext(); ) {
            Path p = it.next();

            if (p.equals(this.currentPath)) {
                this.previous.setEnabled(last != null);
                this.next.setEnabled(it.hasNext());
                return Optional.ofNullable(last);
            }
            last = p;
        }

        return Optional.empty();
    }

    private Optional<Path> nextPage(IOSupplier<Stream<Path>> stream) throws IOException {
        boolean flag = false;
        for (Iterator<Path> it = stream.get().iterator(); it.hasNext(); ) {
            Path p = it.next();

            if (flag) {
                this.previous.setEnabled(true);
                this.next.setEnabled(it.hasNext());
                return Optional.of(p);
            }
            if (p.equals(this.currentPath)) {
                flag = true;
            }
        }

        return Optional.empty();
    }

    @FunctionalInterface
    interface IOSupplier<T> {
        T get() throws IOException;
    }
    @FunctionalInterface
    interface IORunner {
        void run() throws IOException;
    }

    static ActionListener uncheckedIO(IORunner r) {
        return event -> {
            try {
                r.run();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
}
