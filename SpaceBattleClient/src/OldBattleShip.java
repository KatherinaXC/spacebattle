
import ihs.apcs.spacebattle.*;
import ihs.apcs.spacebattle.games.*;
import ihs.apcs.spacebattle.commands.*;
import java.awt.Color;
import java.util.*;

/**
 *
 * @author Aeranythe Echosong
 */
public class OldBattleShip extends BasicSpaceship {

    /**
     * The width of the world (X parameter), as passed to BattleShip by the
     * constructor.
     */
    protected int worldWidth;

    /**
     * The height of the world (Y parameter), as passed to BattleShip by the
     * constructor.
     */
    protected int worldHeight;

    //Ship private storage
    /**
     * The state variable used for the state machine.
     */
    protected ShipState state = ShipState.RADAR;

    protected BasicEnvironment env;
    protected BaubleHuntGameInfo gameinfo;

    /**
     * The ObjectStatus representing the ship. Updated on each call to
     * getNextCommand().
     */
    protected ObjectStatus shipStatus;

    protected RadarResults radarGeneral;
    protected ObjectStatus radarSpecific;
    protected ArrayList<ObjectStatus> stationaryObstacles = new ArrayList<ObjectStatus>();

    protected ShipTargeting currentGoal;

    /**
     * A temporary cross-method Point that represents the vector from the
     * current position to the target point, accounting for wrapping. Updated on
     * each call to getNextCommand().
     */
    protected Point optimalVect;

    /**
     * The direction that the optimal vector recommends. Updated on each call to
     * getNextCommand().
     */
    protected double optimalDirection;

    /**
     * The distance that the ship would need to fly to get to the destination
     * point. Updated on each call to getNextCommand().
     */
    protected double distance;

    //Movement parameter variables
    /**
     * The percent of current speed that the ship should retain on braking.
     */
    public static final double BRAKE_PERCENT = 0.03;

    /**
     * The duration that the ship should power its thrusters for.
     */
    public static final double THRUST_TIME = 0.1;

    /**
     * The percent of current speed that the ship should accelerate on
     * thrusting.
     */
    public static final double THRUST_SPEED = 0.1;
    protected double speed_scale = 0;

    /**
     * The amount of time that the ship will spend doing nothing on each idle
     * command.
     */
    public static final double IDLE_TIME = 0.01;

    /**
     * The margin of error that the ship will tolerate when calculating angles
     * of rotation.
     */
    public static final double ANGLE_BOUNDS = 15;
    public static double OBSTACLE_BOUNDS = 30;

    /**
     * The fastest speed that the ship will accept as 'stopping', since there is
     * no actual way to stop completely outside of an AllStopCommand.
     */
    public static final double EFFECTIVE_STOP = 0.001;

    /**
     * The radius which the ship must stop within on each given waypoint.
     */
    public static final double POINT_ACCURACY = 7;
    public static final double OBSTACLE_RANGE = 100;
    /**
     * The minimum energy available where the ship is willing to spend energy to
     * shoot. (We need energy for movement too!)
     */
    public static final double SHOOT_ENERGY_THRESHOLD = 10;

    //Random other variables
    /**
     * The registration number that will display a red ship with the hammer and
     * sickle on it.
     */
    public static final int SHIP_IMAGE_SOVIET = 3;

    /**
     * The registration number that will display a blue, "eggy loking thign"
     * [reference] ship with golden trim and nebulated interior.
     */
    public static final int SHIP_IMAGE_ORB = 4;

    /**
     * The registration number that will display the TARDIS as a ship.
     */
    public static final int SHIP_IMAGE_TARDIS = 5;

    /**
     * The registration number that will display Pac-Man as a ship.
     */
    public static final int SHIP_IMAGE_PACMAN = 6;

    /**
     * Color for text resembling that of Vriska Serket. It is also impossible to
     * read on the projector screen during class due to having such low
     * concentration of each color.
     */
    public static final Color SHIP_COLOR_COBALT = new Color(0, 64, 128);

    /**
     * Color for light, white-mint text.
     */
    public static final Color SHIP_COLOR_MINT = new Color(204, 240, 225);

    /**
     * Constructor for a BattleShip... self explanatory. Does nothing.
     */
    public OldBattleShip() {
    }

    /**
     * Registers the ship with the serverside. Called upon entry.
     *
     * @param numImages The number representing the appearance of the ship
     * @param worldWidth The width of the world
     * @param worldHeight The height of the world
     * @return RegistrationData for the world to handle
     */
    public RegistrationData registerShip(int numImages, int worldWidth, int worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        //End init
        return new RegistrationData("F8ck!!!!!!!!", Color.WHITE, OldBattleShip.SHIP_IMAGE_SOVIET);
    }

    /**
     * Returns the command that the ship decides on taking.
     *
     * @param be the current game environment
     * @return next action
     */
    public ShipCommand getNextCommand(BasicEnvironment be) {
        //set up nonswitchaltered variables
        this.env = be;
        this.shipStatus = be.getShipStatus();
        BasicGameInfo tempgameinfo = be.getGameInfo();
        if (tempgameinfo instanceof BaubleHuntGameInfo) {
            this.gameinfo = (BaubleHuntGameInfo) tempgameinfo;
        }
        //save the radar data if i have any
        if (be.getRadar() != null) {
            switch (be.getRadarLevel()) {
                case 3:
                    System.out.println("Setting RadarSpecific");
                    this.state = ShipState.START;
                    this.radarSpecific = be.getRadar().get(0);
                    break;
                case 4:
                    System.out.println("Setting RadarGeneral");
                    this.radarGeneral = be.getRadar();
                    this.stationaryObstacles = updateObstacles(stationaryObstacles);
                    break;
            }
        }
        ShipCommand result = null;
        //catches stateswitches during a case
        while (result == null) {
            obtainTargets();
            System.out.println("State: " + this.state);
            switch (this.state) {
                case RADAR:
                    result = whileRadar();
                    break;
                case START:
                    result = whileStart();
                    break;
                case TURN:
                    result = whileTurn();
                    break;
                case SHOOT:
                    result = whileShoot();
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
        }
        return result;
    }

    protected ShipCommand whileRadar() {
        String[] goaltypes = {"Bauble", "Asteroid"};
        int selectedID = closestID(this.shipStatus.getPosition(),
                this.shipStatus.getMovementDirection(),
                this.shipStatus.getSpeed(), goaltypes);
        if (selectedID == -1
                || this.radarGeneral == null
                || this.radarGeneral.size() == 0
                || this.env.getRadarLevel() == 3) {
            //if i have no useful overall radar so far, or my last check was a specific check
            System.out.println("Checking RadarGeneral");
            return new RadarCommand(4);
        } else {
            //if I have general radar but no specific target
            System.out.println("Checking RadarSpecific");
            return new RadarCommand(3, selectedID);
        }
    }

    /**
     * Determines the appropriate ShipCommand to return in the phase of
     * starting. If there are no appropriate commands, it returns null and
     * changes the appropriate state variables to reflect that.
     *
     * @return null (the start state is basically skipped here)
     */
    protected ShipCommand whileStart() {
        this.state = ShipState.TURN;
        return null;
    }

    /**
     * Determines the appropriate ShipCommand to return in the phase of turning.
     * If there are no appropriate commands, it returns null and changes the
     * appropriate state variables to reflect that.
     *
     * @return turn command or null
     */
    protected ShipCommand whileTurn() {
        if (!OldBattleShip.sameAngle(shipStatus.getOrientation(), this.optimalDirection, OldBattleShip.ANGLE_BOUNDS)) {
            //if i am in the wrong direction, change my direction
            if (this.currentGoal == ShipTargeting.FLYING) {
                this.state = ShipState.THRUST;
            } else {
                this.state = ShipState.SHOOT;
            }
            System.out.println("Rotating " + angleTo(this.shipStatus.getOrientation(), this.optimalDirection));
            System.out.println("Current Orientation " + this.shipStatus.getOrientation());
            System.out.println("Optimal Orientation " + this.optimalDirection);
            if (this.shipStatus.getSpeed() <= OldBattleShip.EFFECTIVE_STOP) {
                return new RotateCommand(angleTo(this.shipStatus.getOrientation(), this.optimalDirection));
            } else {
                return new SteerCommand(angleTo(this.shipStatus.getOrientation(), this.optimalDirection));
            }
        } else {
            if (this.currentGoal == ShipTargeting.FLYING) {
                this.state = ShipState.THRUST;
            } else {
                this.state = ShipState.SHOOT;
            }
        }
        return null;
    }

    protected ShipCommand whileShoot() {
        System.out.println("Shooting");
        this.state = ShipState.STOP;
        if (this.shipStatus.getEnergy() > OldBattleShip.SHOOT_ENERGY_THRESHOLD) {
            if (OldBattleShip.sameAngle(this.shipStatus.getOrientation(), this.optimalDirection, OldBattleShip.ANGLE_BOUNDS)) {
                return new FireTorpedoCommand('F');
            } else {
                return new FireTorpedoCommand('B');
            }
        }
        return null;
    }

    /**
     * Determines the appropriate ShipCommand to return in the phase of
     * thrusting. If there are no appropriate commands, it returns null and
     * changes the appropriate state variables to reflect that.
     *
     * @return thrust command or null
     */
    protected ShipCommand whileThrust() {
        double obstacleStatus = headingTowardsObstacle();
        if (obstacleStatus != -1) {
            return new WarpCommand(obstacleStatus * 2);
        } else if (this.shipStatus.getSpeed() > distance / 2) {
            //if i'm going too fast, stop
            System.out.println("Going to brake from thrust, going too fast");
            this.state = ShipState.BRAKE;
        } else if (!OldBattleShip.sameAngle(shipStatus.getOrientation(), this.optimalDirection, OldBattleShip.ANGLE_BOUNDS)) {
            //if i'm off course brake (then restart)
            System.out.println("Going to brake from thrust, wrong angle");
            this.state = ShipState.BRAKE;
        } else if (this.shipStatus.getSpeed() < shipStatus.getMaxSpeed()) {
            //if i can keep getting faster, speed up
            if (this.shipStatus.getSpeed() < this.distance / 2 && this.speed_scale < 10 - OldBattleShip.THRUST_SPEED - 0.005) {
                //to attempt to escape gravity wells/nebula relatively easily
                this.speed_scale += 0.001;
            } else {
                this.speed_scale = 0;
            }
            return new ThrustCommand('B', OldBattleShip.THRUST_TIME, OldBattleShip.THRUST_SPEED + this.speed_scale);
        } else {
            //if i am currently at max, just keep path
            System.out.println("Going to coast from thrust");
            this.state = ShipState.COAST;
        }
        return null;
    }

    /**
     * Determines the appropriate ShipCommand to return in the phase of
     * coasting. If there are no appropriate commands, it returns null and
     * changes the appropriate state variables to reflect that.
     *
     * @return idle command or null
     */
    protected ShipCommand whileCoast() {
        if (distance > 2 * this.shipStatus.getSpeed()) {
            //if the distance remaining isn't too close
            //TODO make this a radar check?
            return new IdleCommand(OldBattleShip.IDLE_TIME);
        } else if (!OldBattleShip.sameAngle(shipStatus.getOrientation(), this.optimalDirection, OldBattleShip.ANGLE_BOUNDS)) {
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
    protected ShipCommand whileBrake() {
        //Test Pathalter Edition V
        //this.state = ShipState.TURN;

        //Working Edition V
        obtainTargets();
        this.state = ShipState.TURN;
        return new BrakeCommand(OldBattleShip.BRAKE_PERCENT);
    }

    /**
     * Determines the appropriate ShipCommand to return in the phase of
     * stopping. If there are no appropriate commands, it returns null and
     * changes the appropriate state variables to reflect that.
     *
     * @return null
     */
    protected ShipCommand whileStop() {
        System.out.println("Stopping");
        this.state = ShipState.RADAR;
        this.radarGeneral = null;
        return null;
    }

    /**
     * Called when the ship is destroyed. Apparently we don't need to do
     * anything in this method here, so it does nothing.
     */
    public void shipDestroyed() {
        this.state = ShipState.RADAR;
    }

    protected boolean obtainTargets() {
        System.out.println("Getting targets");
        if (this.radarGeneral == null || this.radarSpecific == null) {
            return false;
        }
        boolean goingHome = this.gameinfo.getNumBaublesCarried() >= 5;
        Point nextTarget;
        if (goingHome) {
            //if I have to return home now, go home no matter what
            nextTarget = this.gameinfo.getHomeBasePosition();
        } else {
            //an asteroid or bauble
            nextTarget = this.targetDest(radarSpecific.getPosition(), radarSpecific.getOrientation(),
                    -radarSpecific.getSpeed() * distance(this.shipStatus.getPosition(), radarSpecific.getPosition()));
        }
        this.optimalVect = this.direction(shipStatus.getPosition(), nextTarget);
        //this.optimalVect = movementCancellation(this.shipStatus.getMovementDirection(),this.shipStatus.getSpeed(),optimalVect);
        this.optimalDirection = OldBattleShip.getAngle(optimalVect);
        this.distance = this.distance(shipStatus.getPosition(), nextTarget);
        if (this.radarSpecific.getType().equals("Bauble") || goingHome) {
            this.currentGoal = ShipTargeting.FLYING;
        } else {
            this.currentGoal = ShipTargeting.SHOOTING;
        }
        System.out.println("Going Home:" + goingHome);
        return true;
    }

    /**
     * Returns if the two points are effectively the same. (Double calculation
     * logic.)
     *
     * @param p1 first point to match
     * @param p2 second point to match
     * @return if the two points are the same
     */
    public static boolean samePoint(Point p1, Point p2) {
        return (Math.abs(p1.getX() - p2.getX()) < OldBattleShip.POINT_ACCURACY)
                && (Math.abs(p1.getY() - p2.getY()) < OldBattleShip.POINT_ACCURACY);
    }

    /**
     * Returns if the two angles are effectively the same. (Double calculation
     * logic.)
     *
     * @param a1 the first angle to match
     * @param a2 the second angle to match
     * @param anglebounds the accuracy to match to
     * @return if the two angles are the same
     */
    public static boolean sameAngle(double a1, double a2, double anglebounds) {
        return Math.abs(a1 - (a2 + 360) % 360) < anglebounds;
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
        double centerx = getWorldWidth() / 2;
        double centery = getWorldHeight() / 2;
        //Determine if I need to wrap
        boolean wrapwidth = Math.abs(dx) > centerx;
        boolean wrapheight = Math.abs(dy) > centery;
        //Wrap
        if (wrapwidth) {
            if (dx < 0) {
                dx += getWorldWidth();
            } else {
                dx -= getWorldWidth();
            }
        }
        if (wrapheight) {
            if (dy < 0) {
                dy += getWorldHeight();
            } else {
                dy -= getWorldHeight();
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
     * @return Point from the current at given angle with given distance away
     */
    public Point targetDest(Point current, double angle, double distToGo) {
        double finalX = current.getX() + distToGo * (Math.cos(Math.toRadians(angle)));
        double finalY = current.getY() - distToGo * (Math.sin(Math.toRadians(angle)));
        finalX = OldBattleShip.wrap(finalX, getWorldWidth());
        finalY = OldBattleShip.wrap(finalY, getWorldHeight());
        return new Point(finalX, finalY);
    }

    /**
     * Returns the degree of the Point (which represents a vector).
     *
     * @param vector a vector
     * @return degree of given vector
     */
    public static double getAngle(Point vector) {
        double degree = Math.toDegrees(Math.atan2(-vector.getY(), vector.getX()));
        if (vector.getX() < 0) {
            degree += 180;
        }
        return degree;
    }

    /**
     * Takes a double and wraps it to fit into the given size parameter.
     *
     * @param current the current double parameter
     * @param size the maximum associated world size parameter to wrap to
     * @return fixed coord param
     */
    public static double wrap(double current, double size) {
        return (current + ((int) Math.abs(current / size) + 1) * size) % size;
    }

    /**
     * Returns the world width. We can't have people changing the world width at
     * will!
     *
     * @return worldWidth
     */
    public double getWorldWidth() {
        return this.worldWidth;
    }

    /**
     * Returns the world height. We can't have people changing the world height
     * at will!
     *
     * @return worldHeight
     */
    public double getWorldHeight() {
        return this.worldHeight;
    }

    /**
     * Returns the ID of the closest object to the position indicated by the
     * given parameters in radarGeneral that matches the given object type.
     *
     * @param current current position of center object
     * @param angle current angle of center object
     * @param speed current speed of center object
     * @param type the type of object required
     * @return -1 for no possibility or ID of closest matched type object
     */
    public int closestID(Point current, double angle, double speed, String[] type) {
        //if there is no general radar don't bother
        if (this.radarGeneral == null) {
            return -1;
        }
        Point mydestination = targetDest(current, angle, speed);
        double min = Double.MAX_VALUE;
        int id = -1;
        for (ObjectStatus testing : this.radarGeneral) {
            if (distance(testing.getPosition(), mydestination) < min) {
                for (String testingtype : type) {
                    if (testingtype.equals(testing.getType())) {
                        min = distance(testing.getPosition(), mydestination);
                        id = testing.getId();
                    }
                }
            }
        }
        System.out.println("ClosestID " + id);
        return id;
    }

    protected ArrayList<ObjectStatus> updateObstacles(ArrayList<ObjectStatus> previous) {
        System.out.println("Updating obstacles");
        //pre: have general radar
        for (ObjectStatus test : this.radarGeneral) {
            if (test.getType().equals("Planet")) {
                boolean contains = false;
                for (ObjectStatus contained : previous) {
                    if (contained.getId() == test.getId()) {
                        contains = true;
                    }
                }
                if (!contains) {
                    previous.add(test);
                }
            }
        }
        return previous;
    }

    protected double withinObstacleRange() {
        System.out.println("Testing if I'm within obstacle range");
        for (ObjectStatus obstacle : this.stationaryObstacles) {
            double distancetoobstacle = distance(this.shipStatus.getPosition(), obstacle.getPosition());
            if (distancetoobstacle < OldBattleShip.OBSTACLE_RANGE) {
                return distancetoobstacle;
            }
        }
        return -1;
    }

    protected double headingTowardsObstacle() {
        System.out.println("Testing if I'm heading towards an obstacle");
        for (ObjectStatus obstacle : this.stationaryObstacles) {
            double distancetoobstacle = distance(this.shipStatus.getPosition(), obstacle.getPosition());
            if (distancetoobstacle < OldBattleShip.OBSTACLE_RANGE) {
                if (Math.abs(OldBattleShip.angleTo(this.shipStatus.getOrientation(), this.optimalDirection)) < OldBattleShip.OBSTACLE_BOUNDS) {
                    return distancetoobstacle;
                }
            }
        }
        return -1;
    }

    public static double angleTo(double currentOrientation, double optimalOrientation) {
        //if i'm facing the wrong way rotate
        double rotation = optimalOrientation - currentOrientation;
        //fix over-180 rotations
        while (Math.abs(rotation) > 180) {
            if (rotation > 0) {
                rotation = rotation - 360;
            } else {
                rotation = rotation + 360;
            }
        }
        return rotation;
    }

    public static Point movementCancellation(Point originalMovement, Point optimalVector) {
        double newX = optimalVector.getX() - originalMovement.getX();
        double newY = optimalVector.getY() - originalMovement.getY();
        return new Point(newX, newY);
    }

    public static Point movementCancellation(double currentDirection, double currentSpeed, Point optimalVector) {
        double originalX = Math.asin(Math.toDegrees(currentDirection)) * currentSpeed;
        double originalY = Math.acos(Math.toDegrees(currentDirection)) * currentSpeed;
        Point originalMovement = new Point(originalX, originalY);
        return OldBattleShip.movementCancellation(originalMovement, optimalVector);
    }
}
