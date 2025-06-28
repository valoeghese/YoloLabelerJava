package nz.valoeghese.yolo;

public class Box {
    // not a record older java versions can also compile it
    public Box(String object, double x, double y, double x1, double y1) {
        this.object = object;
        if (x > x1) throw new IllegalArgumentException("x1 cannot be less than x");
        if (y > y1) throw new IllegalArgumentException("y1 cannot be less than y");
        this.x = x;
        this.y = y;
        this.x1 = x1;
        this.y1 = y1;
    }

    public String object;
    public double x, y, x1, y1;

    public double width() {
        return x1 - x;
    }
    public double height() {
        return y1 - y;
    }
}
