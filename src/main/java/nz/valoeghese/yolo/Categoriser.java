package nz.valoeghese.yolo;

import javax.swing.*;

public interface Categoriser {
    String getCurrentCategory();
    String getCategoryLabel(int idx);
    int getCategoryIdx(String label);
    JList<String> createInterface();
}
