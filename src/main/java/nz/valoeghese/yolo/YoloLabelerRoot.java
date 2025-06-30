package nz.valoeghese.yolo;

import nz.valoeghese.yolo.mode.AdjustMode;
import nz.valoeghese.yolo.mode.CreateMode;
import nz.valoeghese.yolo.mode.MoveMode;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class YoloLabelerRoot extends JPanel {
    public static void main(String[] args) throws IOException {
        // GUI
        JFrame frame = new JFrame();
        frame.setSize(new Dimension(1080, 720));

        Batch batch = new Batch(Paths.get("images"));
        frame.add(new YoloLabelerRoot(frame, batch));

        frame.setTitle("YOLO Labeler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
        frame.requestFocus();
    }

    private final DisplayLabelsPanel display;
    private final Batch batch;
    private final JLabel currentImageName;
    private JButton previous, next;
    private Path currentPath;

    YoloLabelerRoot(JFrame frame, Batch batch) throws IOException {
        this.batch = batch;
        this.setLayout(new BorderLayout());

        this.display = new DisplayLabelsPanel(batch);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.display.getYoloImage().fullSave();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }));

        JToolBar bar = new JToolBar();
        bar.setFloatable(false);

        JButton createMode = new JButton("Create");
        JButton adjustMode = new JButton("Adjust");
        JButton moveMode = new JButton("Move");
        new SelectionGroup(createMode, adjustMode, moveMode).bind(this.display::setMode,
                new CreateMode(this.display, batch),
                new AdjustMode(this.display, batch),
                new MoveMode  (this.display, batch));
        bar.add(createMode);
        bar.add(adjustMode);
        bar.add(moveMode);
        bar.addSeparator();

        // Hotkeys
        createMode.setMnemonic('c');
        adjustMode.setMnemonic('a');
        moveMode.setMnemonic('v');
        //

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
        JScrollPane jScP = new JScrollPane();
        JPanel bridge = new JPanel();
        bridge.setLayout(new FlowLayout(FlowLayout.LEFT));
        bridge.add(this.display);
        jScP.getViewport().add(bridge);
        jScP.getVerticalScrollBar().setUnitIncrement(12);
        jScP.getHorizontalScrollBar().setUnitIncrement(12);
        splitPane.setLeftComponent(jScP);

        JPanel panel = new JPanel();
        JButton button = new JButton("+");
        panel.setLayout(new BorderLayout());
        this.currentImageName = new JLabel();
        this.currentImageName.setMinimumSize(new Dimension(0, 30));
        this.currentImageName.setPreferredSize(new Dimension(0, 30));
        this.currentImageName.setBorder(new BevelBorder(BevelBorder.RAISED));
        panel.add(currentImageName, BorderLayout.NORTH);
        JList<String> labels = batch.getCategoriser().createInterface();

        JPopupMenu labelInterface = createLabelMenu(frame, batch, labels);

//        labels.setComponentPopupMenu(labelInterface);
        labels.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    // https://gist.github.com/mikekucera/7363d3a3abaa64c61d06
                    Point pt = e.getPoint();
                    int rClicked = labels.locationToIndex(pt);
                    System.out.println(rClicked);
                    if (rClicked != -1 && labels.getCellBounds(rClicked,rClicked).contains(pt)) {
                        labels.setSelectedIndex(rClicked);
                        labelInterface.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
        panel.add(labels, BorderLayout.CENTER);
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

        // Load initial image
        Optional<Path> lastSessionLastEdited = batch.getLastSessionLastEdited();

        boolean unloaded = true;
        if (lastSessionLastEdited.isPresent()) {
            Path p = lastSessionLastEdited.get();

            if (Files.exists(p)) {
                System.out.println("Session found. Loading last edited file");
                this.load(p);

                // update buttons
                List<Path> images = batch.listImages().collect(Collectors.toList());
                Path last = null;
                boolean lastIsP = false;
                for (Path image : images) {
                    boolean thisIsP = Files.isSameFile(p, image);

                    if (last != null && thisIsP) { // there exists an image before p
                        this.previous.setEnabled(true);
                    }
                    if (lastIsP) { // there exists an image after p
                        this.next.setEnabled(true);
                    }

                    last = image;
                    lastIsP = thisIsP;
                }

                unloaded = false;
            }
        }

        if (unloaded) {
            List<Path> images = batch.listImages().collect(Collectors.toList());

            if (!images.isEmpty()) {
                currentPath = images.get(0);
                this.load(images.get(0));

                if (images.size() > 1) {
                    this.next.setEnabled(true);
                }
            }
        }
    }

    private static JPopupMenu createLabelMenu(JFrame frame, Batch batch, JList<String> labels) {
        JPopupMenu labelInterface = new JPopupMenu();
        JMenuItem renameMenu = new JMenuItem("Rename Label");
        JMenuItem closeMenu = new JMenuItem("Close");
        labelInterface.add(renameMenu);
        labelInterface.add(closeMenu);
        renameMenu.addActionListener(e -> {
            String renaming = labels.getSelectedValue();
            String s = JOptionPane.showInputDialog(
                    frame,
                    "Choose a new name for '" + renaming + "'",
                    "Rename Label",
                    JOptionPane.PLAIN_MESSAGE);
            if (s != null && !s.isEmpty()) {
                try {
                    batch.renameLabel(labels.getSelectedValue(), s);
                } catch (IOException|IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(
                            frame,
                            ex.getClass().getSimpleName() + ": " + ex.getMessage(),
                            "Unable to Rename Label",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        closeMenu.addActionListener(e -> labelInterface.setVisible(false));
        return labelInterface;
    }

    private void load(Path path) throws IOException {
        this.currentPath = path;
        this.currentImageName.setText(path.getFileName().toString());
        this.batch.setLastEdited(path);
        this.display.loadImage(batch.loadImage(path));
    }

    private Optional<Path> previousPage(IOSupplier<Stream<Path>> stream) throws IOException {
        Path last = null;
        for (Iterator<Path> it = stream.get().iterator(); it.hasNext(); ) {
            Path p = it.next();

            if (p.equals(this.currentPath)) {
                this.previous.setEnabled(last != null);
                this.next.setEnabled(true);
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
