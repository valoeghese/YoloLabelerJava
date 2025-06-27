package nz.valoeghese.yolo;

public interface Categoriser {
    String getCurrentCategory();
    String getCategoryLabel(int idx);
    int getCategoryIdx(String label);
}
