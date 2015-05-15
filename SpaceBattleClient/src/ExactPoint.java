
import java.awt.Point;

/**
 *
 * @author s-zhouj
 */
public class ExactPoint extends Point {

    private double x;
    private double y;

    public ExactPoint() {
        this.x = 0;
        this.y = 0;
    }

    public ExactPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public ExactPoint(Point p) {
        this.x = p.getX();
        this.y = p.getY();
    }

    public boolean equals(Object in) {
        if (in instanceof ExactPoint) {
            ExactPoint p = (ExactPoint) in;
            return Math.abs(p.getX() - this.getX()) < 0.0001 && Math.abs(p.getY() - this.getY()) < 0.0001;
        }
        return false;
    }

    public ExactPoint getLocation() {
        return new ExactPoint(this.getX(), this.getY());
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void translate(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public String toString() {
        return "[" + this.getX() + ", " + this.getY() + "]";
    }
}
