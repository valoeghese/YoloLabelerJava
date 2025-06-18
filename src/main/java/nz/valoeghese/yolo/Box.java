package nz.valoeghese.yolo;

public class Box {
    // not a record older java versions can also compile it
    public Box(int object, double x, double y, double x1, double y1) {
        this.object = object;
        this.x = x;
        this.y = y;
        this.x1 = x1;
        this.y1 = y1;
    }

    public int object;
    public double x, y, x1, y1;
}
