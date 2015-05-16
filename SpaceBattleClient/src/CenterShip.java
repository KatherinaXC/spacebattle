
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
    private Point[] waypoints;
    private int current = 0;

    @Override
    public RegistrationData registerShip(int numImages, int worldWidth, int worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        //Initialize waypoints, specific to a CenterShip
        this.waypoints = new Point[1];
        this.waypoints[0] = new Point(this.worldWidth / 2, this.worldHeight / 2);
        //End init
        return new RegistrationData("arachnidsGrip", new Color(00, 41, 82), numImages);
    }

    @Override
    public ShipCommand getNextCommand(BasicEnvironment be) {
        ObjectStatus shipStatus = be.getShipStatus();
        Point optimalVect = this.direction(shipStatus.getPosition(), waypoints[current]);
        double optimalDirection = -1 * (Math.toDegrees(Math.atan(optimalVect.getY() / optimalVect.getX())));
        double distance = this.distance(shipStatus.getPosition(), waypoints[current]);
        double currentDirection = shipStatus.getOrientation();
        double currentSpeed = shipStatus.getSpeed();
        while (true) {
            switch (this.state) {
                case TURN:
                    optimalDirection = (optimalDirection + 360) % 360;
                    System.out.println("TEST MY ANGLE TO DESTINATION IS " + optimalDirection);
                    System.out.println("TEST MY ANGLE IS " + currentDirection);
                    if (Math.abs(currentDirection - optimalDirection) < 1) {
                        this.state = ShipState.THRUST;
                    } else {
                        return new RotateCommand(optimalDirection - currentDirection);
                    }
                    break;
                case THRUST:
                    if (currentSpeed > distance / 2) {
                        this.state = ShipState.BRAKE;
                    } else if (currentSpeed < shipStatus.getMaxSpeed()) {
                        return new ThrustCommand('B', 0.1, 0.1);
                    } else {
                        this.state = ShipState.COAST;
                    }
                    break;
                case COAST:
                    if (distance > currentSpeed / 2) {
                        return new IdleCommand(0.1);
                    } else {
                        this.state = ShipState.BRAKE;
                    }
                    break;
                case BRAKE:
                    return new BrakeCommand(0.1);
                case STOP:
                    if (waypoints.length >= current) {
                        current++;
                        this.state = ShipState.START;
                    } else {
                        return new IdleCommand(0.1);
                    }
                    break;
                default:
                    // catchall
                    this.state = ShipState.TURN;
                    break;
            }
        }
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
    public Point direction(Point p1, Point p2) {
        //Set up variables
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double centerx = this.worldWidth / 2;
        double centery = this.worldHeight / 2;
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
        return Math.sqrt(vector.getX() * vector.getX() + vector.getY() * vector.getY());
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
        finalX = CenterShip.wrap(finalX, CenterShip.worldWidth);
        finalY = CenterShip.wrap(finalY, CenterShip.worldHeight);
        return new Point(finalX, finalY);
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
