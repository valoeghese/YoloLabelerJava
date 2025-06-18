package nz.valoeghese.yolo;

public class Box {
    public Box(double x, double y, double x1, double y1) {
        this.x = x;
        this.y = y;
        this.x1 = x1;
        this.y1 = y1;
    }

    public double x, y, x1, y1;
}
