package nz.valoeghese.yolo;

public class Box {
    // not a record older java versions can also compile it
    private Box(String object, double x0, double y0, double x1, double y1) {
        this.object = object;
        if (x0 > x1) throw new IllegalArgumentException("x1 cannot be less than x0");
        if (y0 > y1) throw new IllegalArgumentException("y1 cannot be less than y0");
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    public String object;
    public double x0, y0, x1, y1;

    public double xCentre() {
        return (x0 + x1) * 0.5;
    }
    public double yCentre() {
        return (y0 + y1) * 0.5;
    }
    public double width() {
        return x1 - x0;
    }
    public double height() {
        return y1 - y0;
    }

    /**
     * Read a box from YOLO format.
     * @param object the object type.
     * @param x centre x.
     * @param y centre y.
     * @param w width.
     * @param h height.
     * @return a new box object from the given parameters.
     */
    public static Box yolo(String object, double x, double y, double w, double h) {
        if (w < 0) throw new IllegalArgumentException("Width cannot be less than 0.");
        if (h < 0) throw new IllegalArgumentException("Height cannot be less than 0.");
        return new Box(object, x - w/2, y - h/2, x + w/2, y + h/2);
    }

    /**
     * Create a box from 2 corners.
     * @param object the object type.
     * @param x0 the top-left corner x.
     * @param y0 the top-left corner y.
     * @param x1 the bottom-right corner x.
     * @param y1 the bottom-right corner y.
     * @return a new box object from the given parameters.
     */
    public static Box spanning(String object, double x0, double y0, double x1, double y1) {
        return new Box(object, x0, y0, x1, y1);
    }
}
