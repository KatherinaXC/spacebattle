
import java.awt.Color;
import ihs.apcs.spacebattle.*;
import ihs.apcs.spacebattle.commands.*;

/**
 *
 * @author s-zhouj
 */
public class CenterShip extends BasicSpaceship {

    private static int worldWidth;
    private static int worldHeight;

    private ShipState state = ShipState.START;
    private ExactPoint[] waypoints;

    @Override
    public RegistrationData registerShip(int numImages, int worldWidth, int worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        return new RegistrationData("arachnidsGrip", new Color(00, 41, 82), numImages);
    }

    @Override
    public ShipCommand getNextCommand(BasicEnvironment be) {
        switch (this.state) {
            case START:
                //Initialize waypoints
                this.waypoints = new ExactPoint[1];
                this.waypoints[0] = new ExactPoint(this.worldWidth / 2, this.worldHeight / 2);
                this.state = ShipState.TURN;
                break;
            case TURN:
                break;
            case THRUST:
                break;
            case COAST:
                break;
            case BRAKE:
                break;
            case STOP:
                break;
        }
        return new IdleCommand(0.1);
    }

    @Override
    public void shipDestroyed() {
    }

    /**
     * Returns a point representing the shortest path from Point p1 to Point p2.
     *
     * @param p1 the starting point
     * @param p2 the ending point
     * @return vector representing shortest distance
     */
    public java.awt.Point direction(java.awt.Point p1, java.awt.Point p2) {
        //Set up variables
        int dx = p2.x - p1.x;
        int dy = p2.y - p1.y;
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
        return new java.awt.Point(dx, dy);
    }

    /**
     * Returns the shortest distance between Point p1 and Point p2
     *
     * @param p1 the starting point
     * @param p2 the ending point
     * @return the shortest distance between two points
     */
    public double distance(java.awt.Point p1, java.awt.Point p2) {
        java.awt.Point vector = direction(p1, p2);
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
    public static ExactPoint targetDest(java.awt.Point current, double angle, double distToGo) {
        double finalX = current.getX() + distToGo * (Math.cos(Math.toRadians(angle)));
        double finalY = current.getY() - distToGo * (Math.sin(Math.toRadians(angle)));
        finalX = CenterShip.wrap(finalX, CenterShip.worldWidth);
        finalY = CenterShip.wrap(finalY, CenterShip.worldHeight);
        return new ExactPoint(finalX, finalY);
    }

    /**
     * Takes a double and wraps it to fit into the given size parameter.
     *
     * @param current
     * @param size
     * @return fixed coord param
     */
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

    /**
     * Testing method for direction(), distance(), and targetDest()
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CenterShip test = new CenterShip();

        System.out.println("Testing Vector Direction");
        java.awt.Point p1 = new java.awt.Point(0, 0);
        java.awt.Point p2 = new java.awt.Point(5, 0);
        System.out.println("Test\t" + p1 + p2);
        System.out.println("Vector\t" + test.direction(p1, p2));
        System.out.println("Distance\t" + test.distance(p1, p2));
        p1 = new java.awt.Point(0, 0);
        p2 = new java.awt.Point(1000, 0);
        System.out.println("Test\t" + p1 + p2);
        System.out.println("Vector\t" + test.direction(p1, p2));
        System.out.println("Distance\t" + test.distance(p1, p2));
        p1 = new java.awt.Point(0, 0);
        p2 = new java.awt.Point(0, 5);
        System.out.println("Test\t" + p1 + p2);
        System.out.println("Vector\t" + test.direction(p1, p2));
        System.out.println("Distance\t" + test.distance(p1, p2));
        p1 = new java.awt.Point(0, 0);
        p2 = new java.awt.Point(0, 700);
        System.out.println("Test\t" + p1 + p2);
        System.out.println("Vector\t" + test.direction(p1, p2));
        System.out.println("Distance\t" + test.distance(p1, p2));
        p1 = new java.awt.Point(0, 0);
        p2 = new java.awt.Point(1000, 700);
        System.out.println("Test\t" + p1 + p2);
        System.out.println("Vector\t" + test.direction(p1, p2));
        System.out.println("Distance\t" + test.distance(p1, p2));

        System.out.println();
        System.out.println("Testing Target Point");
        p1 = new java.awt.Point(5, 5);
        System.out.println("Origin:\t" + p1);
        System.out.println("Right 5:\t" + CenterShip.targetDest(p1, 0, 5));
        System.out.println("Up 5:\t" + CenterShip.targetDest(p1, 90, 5));
        System.out.println("Left 5:\t" + CenterShip.targetDest(p1, 180, 5));
        System.out.println("Down 5:\t" + CenterShip.targetDest(p1, 270, 5));
        System.out.println("Right 1000:\t" + CenterShip.targetDest(p1, 0, 1000));
        System.out.println("Up 1000:\t" + CenterShip.targetDest(p1, 90, 1000));
        System.out.println("Left 1000:\t" + CenterShip.targetDest(p1, 180, 1000));
        System.out.println("Down 1000:\t" + CenterShip.targetDest(p1, 270, 1000));
    }
}
