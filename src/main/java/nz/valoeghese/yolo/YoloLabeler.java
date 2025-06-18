package nz.valoeghese.yolo;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class YoloLabeler extends JPanel {
    public static void main(String[] args) throws IOException {
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
