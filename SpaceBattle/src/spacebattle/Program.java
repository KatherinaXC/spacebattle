package spacebattle;

import java.awt.Point;

/**
 *
 * @author s-zhouj
 */
public class Program {

    public static final int worldWidth = 1024;
    public static final int worldHeight = 768;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Program test = new Program();

        System.out.println("Testing Vector Direction");
        Point p1 = new Point(0, 0);
        Point p2 = new Point(5, 0);
        System.out.println("Test\t" + p1 + p2);
        System.out.println("Vector\t" + test.direction(p1, p2));
        System.out.println("Distance\t" + test.distance(p1, p2));
        p1 = new Point(0, 0);
        p2 = new Point(1000, 0);
        System.out.println("Test\t" + p1 + p2);
        System.out.println("Vector\t" + test.direction(p1, p2));
        System.out.println("Distance\t" + test.distance(p1, p2));
        p1 = new Point(0, 0);
        p2 = new Point(0, 5);
        System.out.println("Test\t" + p1 + p2);
        System.out.println("Vector\t" + test.direction(p1, p2));
        System.out.println("Distance\t" + test.distance(p1, p2));
        p1 = new Point(0, 0);
        p2 = new Point(0, 700);
        System.out.println("Test\t" + p1 + p2);
        System.out.println("Vector\t" + test.direction(p1, p2));
        System.out.println("Distance\t" + test.distance(p1, p2));
        p1 = new Point(0, 0);
        p2 = new Point(1000, 700);
        System.out.println("Test\t" + p1 + p2);
        System.out.println("Vector\t" + test.direction(p1, p2));
        System.out.println("Distance\t" + test.distance(p1, p2));

        System.out.println();
        System.out.println("Testing Target Point");
        p1 = new Point(5, 5);
        System.out.println("Origin:\t" + p1);
        System.out.println("Right 5:\t" + Program.targetDest(p1, 0, 5));
        System.out.println("Up 5:\t" + Program.targetDest(p1, 90, 5));
        System.out.println("Left 5:\t" + Program.targetDest(p1, 180, 5));
        System.out.println("Down 5:\t" + Program.targetDest(p1, 270, 5));
        System.out.println("Right 1000:\t" + Program.targetDest(p1, 0, 1000));
        System.out.println("Up 1000:\t" + Program.targetDest(p1, 90, 1000));
        System.out.println("Left 1000:\t" + Program.targetDest(p1, 180, 1000));
        System.out.println("Down 1000:\t" + Program.targetDest(p1, 270, 1000));
    }

    /**
     * Returns a point representing the shortest path from Point p1 to Point p2.
     *
     * @param p1 the starting point
     * @param p2 the ending point
     * @return vector representing shortest distance
     */
    public Point direction(Point p1, Point p2) {
        //Set up variables
        int dx = Math.abs(p2.x - p1.x);
        int dy = Math.abs(p2.y - p1.y);
        int centerx = this.worldWidth / 2;
        int centery = this.worldHeight / 2;
        //Determine if I need to wrap
        boolean wrapwidth = dx > centerx;
        boolean wrapheight = dy > centery;
        //Wrap
        if (wrapwidth) {
            dx -= this.worldWidth;
        }
        if (wrapheight) {
            dy -= this.worldHeight;
        }
        //Return a new Point
        return new Point(dx, dy);
    }

    /**
     * Returns the shortest distance between Point p1 and Point p2
     *
     * @param p1 the starting point
     * @param p2 the ending point
     * @return the shortest distance between two points
     */
    public double distance(Point p1, Point p2) {
        Point vector = direction(p1, p2);
        return Math.sqrt(vector.x * vector.x + vector.y * vector.y);
    }

    /**
     * Returns the Point that the object will arrive at, given the parameters.
     *
     * @param current the current location
     * @param angle the angle that the object is facing (Cartesian plane)
     * @param distToGo the distance to travel forward
     * @return
     */
    public static Point targetDest(Point current, double angle, double distToGo) {
        double finalX = current.getX() + distToGo * (Math.cos(Math.toRadians(angle)));
        double finalY = current.getY() - distToGo * (Math.sin(Math.toRadians(angle)));
        finalX = Program.wrap(finalX, Program.worldWidth);
        finalY = Program.wrap(finalY, Program.worldHeight);
        return new ExactPoint(finalX, finalY);
    }

    public static double wrap(double current, double size) {
        while (current < 0 || current >= size) {
            if (current < 0) {
                current += size;
            } else if (current >= size) {
                current -= size;
            }
        }
        return current;
    }
}
