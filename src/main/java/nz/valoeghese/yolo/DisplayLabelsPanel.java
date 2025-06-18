package nz.valoeghese.yolo;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DisplayLabelsPanel extends JPanel {
    public DisplayLabelsPanel() throws IOException {
        Files.createDirectories(Path.of("images", "labels"));
    }

    private Path image, meta;
    private /* Nullable */ BufferedImage baseImage;
    private Properties properties;

    public void loadImage(String image) throws IOException {
        this.image = Path.of("images", image);
        String s[] = image.split("\\.");
        String extension = s[s.length - 1];
        this.meta = Path.of("images", "labels", image.substring(0, image.length() - extension.length() - 1) + ".txt");

        this.baseImage = ImageIO.read(this.image.toFile());
        this.properties = new Properties();
        if (Files.exists(this.meta)) {
            try (BufferedReader reader = Files.newBufferedReader(this.meta)) {
                this.properties.load(reader);
            }
        }

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
}
