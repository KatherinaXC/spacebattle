
import ihs.apcs.spacebattle.*;
import ihs.apcs.spacebattle.games.*;
import ihs.apcs.spacebattle.commands.*;
import java.awt.Color;
import java.util.*;

/**
 *
 * @author Joyce Zhou
 */
public class BattleShip extends BasicSpaceship {

    //internal ship information
    private double worldWidth;
    private double worldHeight;
    private ShipState state;
    private double scalingSpeed = 0;

    //targeting information
    private ShipState targetingAction;
    private Point targetPosition;
    private Point targetVector;
    private double targetAngleAbsolute;
    private double targetDistance;

    //radar data
    private RadarResults radar;
    private ArrayList<ObjectStatus> stationaryObstacles = new ArrayList<>();

    //game and environment information
    private BasicEnvironment env;
    private ObjectStatus shipStatus;
    private BaubleHuntGameInfo gameinfo;

    //function static variables
    public static final String[] targets = {"Bauble", "Asteroid", "Ship"};
    public static double ANGLE_BOUNDS = 10;
    public static final double BRAKE_PERCENT = 0.03;
    public static final double THRUST_TIME = 0.1;
    public static final double THRUST_SPEED = 0.1;

    //display static variables
    public static final int SHIP_IMAGE_SOVIET = 3;
    public static final int SHIP_IMAGE_ORB = 4;
    public static final int SHIP_IMAGE_TARDIS = 5;
    public static final int SHIP_IMAGE_PACMAN = 6;
    public static final Color SHIP_COLOR_COBALT = new Color(0, 64, 128);
    public static final Color SHIP_COLOR_MINT = new Color(204, 240, 225);

    /**
     * Registers the ship with the serverside. Called upon entry.
     *
     * @param numImages The number representing the appearance of the ship
     * @param worldWidth The width of the world
     * @param worldHeight The height of the world
     * @return RegistrationData for the world to handle
     */
    @Override
    public RegistrationData registerShip(int numImages, int worldWidth, int worldHeight) {
        //Set internal world width and height
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.state = ShipState.RADAR;
        //End init
        return new RegistrationData("turntechGodhead", Color.RED, BattleShip.SHIP_IMAGE_SOVIET);
    }

    /**
     * Called when the ship is destroyed. Apparently we don't need to do
     * anything in this method here, so it does nothing.
     */
    @Override
    public void shipDestroyed() {
        this.state = ShipState.RADAR;
    }

    /**
     * Returns the command that the ship decides on taking.
     *
     * @param be the current game environment
     * @return next action
     */
    @Override
    public ShipCommand getNextCommand(BasicEnvironment be) {
        //Grab data from the BE and store it
        this.env = be;
        this.shipStatus = be.getShipStatus();
        BasicGameInfo bgameinfo = be.getGameInfo();
        if (bgameinfo instanceof BaubleHuntGameInfo) {
            this.gameinfo = (BaubleHuntGameInfo) bgameinfo;
        }
        //Update radar
        if (be.getRadar() != null) {
            this.radar = be.getRadar();
            updateStationaryObstacles();
            this.state = ShipState.START;
        }
        //Main result loop
        ShipCommand result = null;
        while (result == null) {
            //If I have some type of radar to work with, update my targets
            if (this.state != ShipState.RADAR) {
                updateTargets();
            }
            switch (this.state) {
                case RADAR:
                    result = new RadarCommand(5);
                    break;
                case START:
                    //honestly this is here just for tradition's sake :P
                    this.state = ShipState.TURN;
                    break;
                case TURN:
                    //Preset the state for the next iteration
                    if (this.targetingAction == ShipState.SHOOT) {
                        this.state = ShipState.SHOOT;
                    } else {
                        this.state = ShipState.THRUST;
                    }
                    //if i need to turn, do that
                    if (!BattleShip.sameAngle(shipStatus.getOrientation(), this.targetAngleAbsolute, BattleShip.ANGLE_BOUNDS)) {
                        result = new RotateCommand(BattleShip.angleTo(this.shipStatus.getOrientation(), this.targetAngleAbsolute));
                    }
                    break;
                case SHOOT:
                    //Shooting is done on a casebycase basis
                    this.state = ShipState.STOP;
                    if (this.shipStatus.getEnergy() > AsteroidShip.SHOOT_ENERGY_THRESHOLD) {
                        //We want to shoot both front and back
                        if (BattleShip.sameAngle(this.shipStatus.getOrientation(), this.targetAngleAbsolute, BattleShip.ANGLE_BOUNDS)) {
                            result = new FireTorpedoCommand('F');
                        } else {
                            result = new FireTorpedoCommand('B');
                        }
                    }
                    break;
                case THRUST:
                    //If we are being pulled, warp out of there!!!!!!!!
                    ObjectStatus obstacle = obstaclePull();
                    if (obstacle != null) {
                        result = new WarpCommand(obstacle.getPullStrength() * 2);
                    } else if (this.shipStatus.getSpeed() > this.targetDistance / 2) {
                        //if i'm going too fast, stop
                        this.state = ShipState.BRAKE;
                    } else if (!BattleShip.sameAngle(shipStatus.getOrientation(), this.targetAngleAbsolute, BaubleShip.ANGLE_BOUNDS)) {
                        //if i'm off course brake (eventually then restart)
                        this.state = ShipState.BRAKE;
                    } else if (this.shipStatus.getSpeed() < shipStatus.getMaxSpeed()) {
                        //if i can keep getting faster, speed up
                        if (this.shipStatus.getSpeed() < this.targetDistance / 2
                                && this.scalingSpeed < 1 - BattleShip.THRUST_SPEED - 0.005) {
                            //to attempt to escape nebula relatively easily, this won't work for BHs
                            this.scalingSpeed += 0.001;
                        } else {
                            this.scalingSpeed = 0;
                        }
                        result = new ThrustCommand('B', BattleShip.THRUST_TIME, BattleShip.THRUST_SPEED + this.scalingSpeed);
                    }
                    break;
                case BRAKE:
                    //Brake and reevaluate
                    updateTargets();
                    this.state = ShipState.STOP;
                    result = new BrakeCommand(BattleShip.BRAKE_PERCENT);
                    break;
                case STOP:
                    //Reset radar state to do a clean search
                    this.state = ShipState.RADAR;
                    this.radar = null;
                    break;
            }
        }
        return result;
    }

    /**
     * Reads the current radar data and saves any applicable obstacles (planets,
     * black holes, not nebulae) to my list of obstacles.
     */
    private void updateStationaryObstacles() {
        //Get a list with all planets and black holes
        List<ObjectStatus> toTest = this.radar.getByType("Planet");
        toTest.addAll(this.radar.getByType("BlackHole"));
        //Run through the list, patching stationaryObstacles as we go
        for (ObjectStatus current : toTest) {
            if (!this.stationaryObstacles.contains(current)) {
                this.stationaryObstacles.add(current);
            }
        }
    }

    /**
     * Sets applicable fields so that the getNextCommand can calculate which
     * action to take based off of the current thrust/shoot imperative and the
     * target.
     */
    private void updateTargets() {
        //Basic variables so we can read them easily and not retype :P
        boolean goingHome = this.gameinfo.getNumBaublesCarried() >= 3;
        ObjectStatus targetStatus = selectTarget();
        //if we actually have a target to shoot at, great
        if (targetStatus != null) {
            if (goingHome) {
                //if I have to return home now, go home no matter what
                this.targetPosition = this.gameinfo.getHomeBasePosition();
            } else {
                //a bauble or asteroid
                targetStatus = selectTarget();
                this.targetPosition = targetStatus.getPosition();
            }
            //Set motion/target variables
            this.targetVector = this.direction(shipStatus.getPosition(), this.targetPosition);
            this.targetAngleAbsolute = BattleShip.getAngle(this.targetVector);
            this.targetDistance = this.distance(shipStatus.getPosition(), this.targetPosition);
            //Set motion/target hitmode
            if (targetStatus.getType().equals("Bauble") || goingHome) {
                this.targetingAction = ShipState.THRUST;
            } else {
                this.targetingAction = ShipState.SHOOT;
            }
        } else {
            //if we have no target
            this.targetingAction = ShipState.STOP;
        }
    }

    /**
     * Evaluates which target to pursue.
     *
     * @return the ObjectStatus of the selected target
     */
    private ObjectStatus selectTarget() {
        //Use my velocity to account for some efficiency? Could also help avoid collisions
        Point mydestination = targetDest(this.shipStatus.getPosition(),
                this.shipStatus.getMovementDirection(),
                this.shipStatus.getSpeed());
        //Main minimizer loop
        double min = Double.MAX_VALUE;
        ObjectStatus target = null;
        for (ObjectStatus testing : this.radar) {
            if (distance(testing.getPosition(), mydestination) < min) {
                for (String testingtype : BattleShip.targets) {
                    if (testingtype.equals(testing.getType())) {
                        min = distance(testing.getPosition(), mydestination);
                        target = testing;
                    }
                }
            }
        }
        return target;
    }

    /**
     * Returns the ObjectStatus of a given obstacle if it is pulling the ship
     * away from its course.
     *
     * @return ObjectStatus of a planet or black hole
     */
    private ObjectStatus obstaclePull() {
        //Run through the stationaryObstacles, checking ranges
        for (ObjectStatus obstacle : this.stationaryObstacles) {
            if (distance(this.shipStatus.getPosition(), obstacle.getPosition()) < obstacle.getPullStrength()) {
                return obstacle;
            }
        }
        return null;
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
     * @return Point from the current at given angle with given distance away
     */
    public Point targetDest(Point current, double angle, double distToGo) {
        double finalX = current.getX() + distToGo * (Math.cos(Math.toRadians(angle)));
        double finalY = current.getY() - distToGo * (Math.sin(Math.toRadians(angle)));
        finalX = BaubleShip.wrap(finalX, this.worldWidth);
        finalY = BaubleShip.wrap(finalY, this.worldHeight);
        return new Point(finalX, finalY);
    }

    /**
     * Returns the (rectified) angle difference between the current orientation
     * and the optimal orientation.
     *
     * @param currentOrientation
     * @param optimalOrientation
     * @return angle difference in optimal turn format
     */
    public static double angleTo(double currentOrientation, double optimalOrientation) {
        //if i'm facing the wrong way rotate
        double rotation = optimalOrientation - currentOrientation;
        //*rectify* (word of the week/day/hour/minute) over-180 rotations
        while (Math.abs(rotation) > 180) {
            if (rotation > 0) {
                rotation = rotation - 360;
            } else {
                rotation = rotation + 360;
            }
        }
        return rotation;
    }

    /**
     * Returns the degree of the Point (which represents a vector).
     *
     * @param vector a vector
     * @return degree of given vector
     */
    public static double getAngle(Point vector) {
        double degree = Math.toDegrees(Math.atan(-vector.getY() / vector.getX()));
        if (vector.getX() < 0) {
            degree += 180;
        }
        return degree;
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

}
