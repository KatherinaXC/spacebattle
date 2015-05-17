
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

    //Ship private storage
    private ShipState state = ShipState.START;
    protected Point[] waypoints;
    private int current = 0;

    //Movement calculation variables (updated on every call to getNextCommand())
    ObjectStatus shipStatus;
    Point currentPosition;
    double currentDirection;
    double currentSpeed;
    Point optimalVect;
    double optimalDirection;
    double distance;

    public CenterShip() {
    }

    public CenterShip(int worldWidth, int worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    @Override
    public RegistrationData registerShip(int numImages, int worldWidth, int worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        //Initialize waypoints, specific to a CenterShip
        initializePoints();
        //End init
        return new RegistrationData("arachnidsGrip", new Color(00, 41, 82), numImages);
    }

    /**
     * Initializes the appropriate waypoints for the CenterShip.
     */
    private void initializePoints() {
        this.waypoints = new Point[1];
        this.waypoints[0] = new Point(this.worldWidth / 2, this.worldHeight / 2);
    }

    @Override
    public ShipCommand getNextCommand(BasicEnvironment be) {
        //set up nonswitchaltered variables
        shipStatus = be.getShipStatus();
        currentPosition = shipStatus.getPosition();
        currentDirection = shipStatus.getOrientation();
        currentSpeed = shipStatus.getSpeed();
        //catches stateswitches during a case
        while (true) {
            //set up switchaltered (read: waypoint affected) variables
            optimalVect = this.direction(shipStatus.getPosition(), waypoints[current]);
            optimalDirection = CenterShip.getAngle(optimalVect);
            distance = this.distance(shipStatus.getPosition(), waypoints[current]);
            ShipCommand result = null;
            switch (this.state) {
                case START:
                    //here for solidarity XD
                    this.state = ShipState.TURN;
                    break;
                case TURN:
                    result = whileTurn();
                    break;
                case THRUST:
                    result = whileThrust();
                    break;
                case COAST:
                    result = whileCoast();
                    break;
                case BRAKE:
                    result = whileBrake();
                    break;
                case STOP:
                    result = whileStop();
                    break;
            }
            if (result != null) {
                return result;
            }
        }
    }

    /**
     * Determines the appropriate ShipCommand to return in the phase of turning.
     * If there are no appropriate commands, it returns null and changes the
     * appropriate state variables to reflect that.
     *
     * @return movement command
     */
    private ShipCommand whileTurn() {
        if (Math.abs(currentDirection - (optimalDirection + 360) % 360) < 1) {
            //if i'm in the right direction already, just drive
            this.state = ShipState.THRUST;
        } else {
            //if i'm facing the wrong way rotate
            double rotation = optimalDirection - currentDirection;
            //fix over-180 rotations
            while (Math.abs(rotation) > 180) {
                if (rotation > 0) {
                    rotation = 360 - rotation;
                } else {
                    rotation = 360 + rotation;
                }
            }
            return new RotateCommand(rotation);
        }
        return null;
    }

    /**
     * Determines the appropriate ShipCommand to return in the phase of
     * thrusting. If there are no appropriate commands, it returns null and
     * changes the appropriate state variables to reflect that.
     *
     * @return movement command
     */
    private ShipCommand whileThrust() {
        if (currentSpeed > distance / 2) {
            //if i'm going too fast, stop
            this.state = ShipState.BRAKE;
        } else if (currentSpeed < shipStatus.getMaxSpeed()) {
            //if i can keep getting faster, speed up
            return new ThrustCommand('B', 0.5, 0.1);
        } else if (Math.abs(currentDirection - optimalDirection) > 0.1) {
            //if i'm off course brake (then restart)
            this.state = ShipState.BRAKE;
        } else {
            //if i am currently at max, just keep path
            this.state = ShipState.COAST;
        }
        return null;
    }

    /**
     * Determines the appropriate ShipCommand to return in the phase of
     * coasting. If there are no appropriate commands, it returns null and
     * changes the appropriate state variables to reflect that.
     *
     * @return movement command
     */
    private ShipCommand whileCoast() {
        if (distance > currentSpeed / 2) {
            //if the distance remaining isn't too close
            return new IdleCommand(0.1);
        } else if (Math.abs(currentDirection - optimalDirection) > 0.1) {
            //if i'm off course brake (then restart)
            this.state = ShipState.BRAKE;
        } else {
            //if i'm too close brake
            this.state = ShipState.BRAKE;
        }
        return null;
    }

    /**
     * Determines the appropriate ShipCommand to return in the phase of braking.
     * If there are no appropriate commands, it returns null and changes the
     * appropriate state variables to reflect that.
     *
     * @return movement command
     */
    private ShipCommand whileBrake() {
        if (currentSpeed < 0.001) {
            //if i'm there already
            if (atPoint(currentPosition, waypoints[current])) {
                this.state = ShipState.STOP;
                return new AllStopCommand();
            }
            //if i am no longer moving noticeably but not actually there, try again
            this.state = ShipState.START;
        } else if (Math.abs(currentDirection - optimalDirection) > 0.1) {
            //if i'm off course brake (eventually restart)
            return new BrakeCommand(0.1);
        } else {
            //if i can keep slowing down, do that
            return new BrakeCommand(0.1);
        }
        return null;
    }

    /**
     * Determines if there are any more steps to complete - if none, it will
     * return an IdleCommand.
     *
     * @return idle command
     */
    private ShipCommand whileStop() {
        if (waypoints.length > current + 1) {
            //if there's more points, increment and proceed
            current++;
            this.state = ShipState.START;
        } else {
            //if there's no more points, yay!
            return new IdleCommand(0.1);
        }
        return null;
    }

    /**
     * Called when the ship is destroyed. Apparently we don't need to do
     * anything in this method here, so it does nothing.
     */
    @Override
    public void shipDestroyed() {
    }

    /**
     * Returns if the two points are effectively the same. (Double calculation
     * logic.)
     *
     * @param current
     * @param goal
     * @return the two points are the same
     */
    public static boolean atPoint(Point current, Point goal) {
        int range = 10;
        return (Math.abs(current.getX() - goal.getX()) < range) && (Math.abs(current.getY() - goal.getY()) < range);
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
        boolean wrapwidth = Math.abs(dx) > centerx;
        boolean wrapheight = Math.abs(dy) > centery;
        //Wrap
        if (wrapwidth) {
            if (dx < 0) {
                dx += this.worldWidth;
            } else {
                dx -= this.worldWidth;
            }
        }
        if (wrapheight) {
            if (dy < 0) {
                dy += this.worldHeight;
            } else {
                dy -= this.worldHeight;
            }
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
    public Point targetDest(Point current, double angle, double distToGo) {
        double finalX = current.getX() + distToGo * (Math.cos(Math.toRadians(angle)));
        double finalY = current.getY() - distToGo * (Math.sin(Math.toRadians(angle)));
        finalX = CenterShip.wrap(finalX, this.worldWidth);
        finalY = CenterShip.wrap(finalY, this.worldHeight);
        return new Point(finalX, finalY);
    }

    /**
     * Returns the degree of the Point (which represents a vector).
     *
     * @param vector
     * @return degree of vector
     */
    public static double getAngle(Point vector) {
        double degree = Math.toDegrees(Math.atan(-vector.getY() / vector.getX()));
        if (vector.getX() < 0) {
            degree += 180;
        }
        return degree;
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
        CenterShip test = new CenterShip(1024, 768);

        System.out.println("Testing Vector Direction");
        Point p1 = new Point(0, 0);
        Point p2 = new Point(5, 0);
        System.out.println("Test\t" + p1 + p2);
        System.out.println("Vector\t" + test.direction(p1, p2));
        System.out.println("Distance\t" + test.distance(p1, p2));
        System.out.println("Degree\t" + CenterShip.getAngle(test.direction(p1, p2)));

        p1 = new Point(0, 0);
        p2 = new Point(1000, 0);
        System.out.println("Test\t" + p1 + p2);
        System.out.println("Vector\t" + test.direction(p1, p2));
        System.out.println("Distance\t" + test.distance(p1, p2));
        System.out.println("Degree\t" + CenterShip.getAngle(test.direction(p1, p2)));

        p1 = new Point(0, 0);
        p2 = new Point(0, 5);
        System.out.println("Test\t" + p1 + p2);
        System.out.println("Vector\t" + test.direction(p1, p2));
        System.out.println("Distance\t" + test.distance(p1, p2));
        System.out.println("Degree\t" + CenterShip.getAngle(test.direction(p1, p2)));

        p1 = new Point(0, 0);
        p2 = new Point(0, 700);
        System.out.println("Test\t" + p1 + p2);
        System.out.println("Vector\t" + test.direction(p1, p2));
        System.out.println("Distance\t" + test.distance(p1, p2));
        System.out.println("Degree\t" + CenterShip.getAngle(test.direction(p1, p2)));

        p1 = new Point(0, 0);
        p2 = new Point(1000, 700);
        System.out.println("Test\t" + p1 + p2);
        System.out.println("Vector\t" + test.direction(p1, p2));
        System.out.println("Distance\t" + test.distance(p1, p2));
        System.out.println("Degree\t" + CenterShip.getAngle(test.direction(p1, p2)));

        p1 = new Point(1000, 700);
        p2 = new Point(0, 0);
        System.out.println("Test\t" + p1 + p2);
        System.out.println("Vector\t" + test.direction(p1, p2));
        System.out.println("Distance\t" + test.distance(p1, p2));
        System.out.println("Degree\t" + CenterShip.getAngle(test.direction(p1, p2)));

        p1 = new Point(400, 100);
        p2 = new Point(100, 400);
        System.out.println("Test\t" + p1 + p2);
        System.out.println("Vector\t" + test.direction(p1, p2));
        System.out.println("Distance\t" + test.distance(p1, p2));
        System.out.println("Degree\t" + CenterShip.getAngle(test.direction(p1, p2)));

        p1 = new Point(100, 400);
        p2 = new Point(400, 100);
        System.out.println("Test\t" + p1 + p2);
        System.out.println("Vector\t" + test.direction(p1, p2));
        System.out.println("Distance\t" + test.distance(p1, p2));
        System.out.println("Degree\t" + CenterShip.getAngle(test.direction(p1, p2)));

        System.out.println();
        System.out.println("Testing Target Point");
        p1 = new Point(5, 5);
        System.out.println("Origin:\t" + p1);
        System.out.println("Right 5:\t" + test.targetDest(p1, 0, 5));
        System.out.println("Up 5:\t" + test.targetDest(p1, 90, 5));
        System.out.println("Left 5:\t" + test.targetDest(p1, 180, 5));
        System.out.println("Down 5:\t" + test.targetDest(p1, 270, 5));
        System.out.println("Right 1000:\t" + test.targetDest(p1, 0, 1000));
        System.out.println("Up 1000:\t" + test.targetDest(p1, 90, 1000));
        System.out.println("Left 1000:\t" + test.targetDest(p1, 180, 1000));
        System.out.println("Down 1000:\t" + test.targetDest(p1, 270, 1000));
    }

}
