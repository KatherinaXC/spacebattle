
import ihs.apcs.spacebattle.*;
import ihs.apcs.spacebattle.commands.*;
import ihs.apcs.spacebattle.games.*;

/**
 *
 * @author s-zhouj
 */
public class BaubleShip extends BasicShip {

    /**
     * The saved results of the last level-4 radar scan (containing all objects'
     * positions and types).
     */
    protected RadarResults radarGeneral = null;

    /**
     * The saved results of the last level-3 radar scan (containing all of the
     * target object's information)
     */
    protected ObjectStatus radarSpecific = null;

    /**
     * (Overridden) The state variable used for the state machine, starting at
     * ShipState.RADAR.
     */
    protected ShipState state = ShipState.RADAR;

    /**
     * The last radar scan's level, saved for trans-method use.
     */
    protected double lastRadarLevel;

    protected BaubleShipTargeting targetmode = BaubleShipTargeting.FLYING;

    /**
     * The selected next destination for FLYING.
     */
    protected Point nextPhysicalTarget;

    /**
     * The selected next destination for SHOOTING.
     */
    protected Point nextShootingTarget;

    protected Point nextAimedTarget;
    /**
     * The information for the Bauble Hunt game.
     */
    protected BaubleHuntGameInfo gameinfo;

    /**
     * The furthest range from which the ship is willing to pursue shooting a
     * target.
     */
    public static final double SHOOT_RANGE = 500;

    /**
     * The minimum energy available where the ship is willing to spend energy to
     * shoot. (We need energy for movement too!)
     */
    public static final double SHOOT_ENERGY_THRESHOLD = 10;

    /**
     * The factor by which the speed of a target object is multiplied to
     * forecast its position.
     */
    public static final double AIM_SPEED_FACTOR = 1.5;

    /**
     * The margin of error that the ship will tolerate when calculating angles
     * of rotation and shooting.
     */
    public static final double ANGLE_BOUNDS = 5;

    public static final int MAX_BAUBLE_CAPACITY = 5;

    /**
     * This is a stupid useless constructor that exists solely for the purpose
     * of MAKING THE PROGRAM NOT CRASH ok bye.
     */
    public BaubleShip() {
        super();
    }

    /**
     * Constructor for an AsteroidShip, setting up the parameters worldWidth and
     * worldHeight.
     *
     * @param worldWidth the width of the world
     * @param worldHeight the height of the world
     */
    public BaubleShip(int worldWidth, int worldHeight) {
        super(worldWidth, worldHeight);
    }

    @Override
    public void initializePoints() {
        //we are using a different list for this :?
        this.waypoints = new Point[0];
    }

    /**
     * (Override) Returns the command that the ship decides on taking, while
     * saving previously gathered radar data.
     *
     * @param be the current game environment
     * @return next action
     */
    @Override
    public ShipCommand getNextCommand(BasicEnvironment be) {
        //set up nonswitchaltered variables
        this.shipStatus = be.getShipStatus();
        this.currentPosition = shipStatus.getPosition();
        this.currentDirection = shipStatus.getOrientation();
        this.currentSpeed = shipStatus.getSpeed();
        this.currentEnergy = shipStatus.getEnergy();
        this.lastRadarLevel = be.getRadarLevel();
        ShipCommand result = null;
        System.out.println("STATE: " + this.state);
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
                    break;
            }
        }
        //save the gameinfo
        BasicGameInfo tempinfo = be.getGameInfo();
        if (tempinfo instanceof BaubleHuntGameInfo) {
            this.gameinfo = (BaubleHuntGameInfo) tempinfo;
        }
        //catches stateswitches during a case
        while (result == null) {
            //save the closest asteroid target for shooting
            if (radarSpecific != null) {
                this.nextShootingTarget = this.targetDest(radarSpecific.getPosition(), radarSpecific.getOrientation(),
                        -radarSpecific.getSpeed() * AsteroidShip.AIM_SPEED_FACTOR);
            }
            //save the appropriate bauble or homebase target for flying
            if (this.gameinfo.getNumBaublesCarried() < BaubleShip.MAX_BAUBLE_CAPACITY) {
                this.nextPhysicalTarget = optimalBauble();
            } else {
                this.nextPhysicalTarget = this.gameinfo.getHomeBasePosition();
            }
            //calculate optimal direction/vector and such based on my state and my shoot status
            //TODO
            if (this.targetmode == BaubleShipTargeting.SHOOTING || this.nextPhysicalTarget == null) {
                this.nextAimedTarget = this.nextShootingTarget;
            } else {
                this.nextAimedTarget = this.nextPhysicalTarget;
            }
            this.optimalVect = this.direction(this.currentPosition, this.nextAimedTarget);
            this.optimalDirection = BaubleShip.getAngle(optimalVect);
            this.distance = this.distance(this.currentPosition, this.nextAimedTarget);
            switch (this.state) {
                case RADAR:
                    result = whileRadar();
                    break;
                case START:
                    System.out.println("Start");
                    result = whileStart();
                    break;
                case TURN:
                    System.out.println("Turn target " + this.nextShootingTarget);
                    result = whileTurn();
                    break;
                case SHOOT:
                    System.out.println("Shoot");
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
                default:
                    System.out.println("Something went wrong?");
                    break;
            }
        }
        return result;
    }

    /**
     * Determines the appropriate ShipCommand to return in the phase of
     * obtaining radar. If there are no appropriate commands, it returns null
     * and changes the appropriate state variables to reflect that.
     *
     * @return radar command level 3 or 4
     */
    protected ShipCommand whileRadar() {
        int selectedID = closestID(currentPosition, currentDirection, currentSpeed, "Asteroid");
        if (selectedID == -1 || this.radarGeneral == null || this.radarGeneral.size() == 0 || this.lastRadarLevel == 3) {
            //if i have no useful overall radar so far, or just took a specific check
            System.out.println("Checking RadarGeneral");
            return new RadarCommand(4);
        } else {
            //if I have general radar but no specific target
            System.out.println("Checking RadarSpecific");
            return new RadarCommand(3, selectedID);
        }
    }

    @Override
    protected ShipCommand whileStart() {
        this.state = ShipState.TURN;
        this.targetmode = BaubleShipTargeting.FLYING;
        return null;
    }

    /**
     * Determines the appropriate ShipCommand to return in the phase of turning.
     * If there are no appropriate commands, it returns null and changes the
     * appropriate state variables to reflect that.
     *
     * @return rotation command or null
     */
    @Override
    protected ShipCommand whileTurn() {
        if (!AsteroidShip.sameAngle(currentDirection, optimalDirection, AsteroidShip.ANGLE_BOUNDS)
                && !AsteroidShip.sameAngle(currentDirection + 180, optimalDirection, AsteroidShip.ANGLE_BOUNDS)) {
            //if i'm facing the wrong way (forwards or backwards) rotate
            double rotation = optimalDirection - currentDirection;
            if (this.targetmode == BaubleShipTargeting.SHOOTING) {
                //shooting mode:
                //fix over-90 rotations (gotta use back AND front!)
                while (Math.abs(rotation) > 90) {
                    if (rotation > 0) {
                        rotation = rotation - 180;
                    } else {
                        rotation = rotation + 180;
                    }
                }
                if (distance(this.currentPosition, this.nextShootingTarget) > AsteroidShip.SHOOT_RANGE) {
                    //if i'm out of range don't even try for that one
                    this.state = ShipState.STOP;
                } else {
                    //in range? switch to shooting
                    this.state = ShipState.SHOOT;
                }
            } else {
                //flying mode:
                //fix over-180 rotations (gotta use back AND front!)
                while (Math.abs(rotation) > 180) {
                    if (rotation > 0) {
                        rotation = rotation - 360;
                    } else {
                        rotation = rotation + 360;
                    }
                }
                this.state = ShipState.THRUST;
            }
            System.out.println(rotation);
            return new RotateCommand(rotation);
        }
        if (this.targetmode == BaubleShipTargeting.SHOOTING) {
            this.state = ShipState.SHOOT;
        } else {
            this.state = ShipState.THRUST;
        }
        return null;
    }

    /**
     * Determines the appropriate ShipCommand to return in the phase of
     * shooting. If there are no appropriate commands, it returns null and
     * changes the appropriate state variables to reflect that.
     *
     * @return torpedo command or null
     */
    protected ShipCommand whileShoot() {
        this.state = ShipState.STOP;
        if (this.currentEnergy > AsteroidShip.SHOOT_ENERGY_THRESHOLD) {
            if (AsteroidShip.sameAngle(this.currentDirection, this.optimalDirection, AsteroidShip.ANGLE_BOUNDS)) {
                return new FireTorpedoCommand('F');
            } else {
                return new FireTorpedoCommand('B');
            }
        }
        return null;
    }

    @Override
    protected ShipCommand whileThrust() {
        if (currentSpeed > distance / 2 || BaubleShip.samePoint(this.currentPosition, this.nextAimedTarget)) {
            //if i'm going too fast or reached it, stop
            System.out.println("Going to brake, going too fast");
            this.state = ShipState.BRAKE;
        } else if (currentSpeed < shipStatus.getMaxSpeed()) {
            //if i can keep getting faster, speed up
            System.out.println("Thrusting from the back");
            return new ThrustCommand('B', BasicShip.THRUST_TIME, BasicShip.THRUST_SPEED);
        } else if (!BasicShip.sameAngle(currentDirection, optimalDirection, BasicShip.ANGLE_BOUNDS)) {
            //if i'm off course brake (then restart)
            System.out.println("Going to brake, wrong angle");
            this.state = ShipState.BRAKE;
        } else {
            //if i am currently at max, just keep path
            System.out.println("Going to coast");
            this.state = ShipState.COAST;
        }
        return null;
    }

    @Override
    protected ShipCommand whileCoast() {
        if (distance > 2 * currentSpeed) {
            //if the distance remaining isn't too close
            return new IdleCommand(BasicShip.IDLE_TIME);
        } else if (!BasicShip.sameAngle(currentDirection, optimalDirection, BasicShip.ANGLE_BOUNDS)) {
            //if i'm off course brake (then restart)
            this.state = ShipState.BRAKE;
        } else {
            //if i'm too close brake
            this.state = ShipState.BRAKE;
        }
        return null;
    }

    @Override
    protected ShipCommand whileBrake() {
        if (currentSpeed < BasicShip.EFFECTIVE_STOP) {
            //if i'm there already
            if (samePoint(currentPosition, this.waypoints[current])) {
                this.state = ShipState.STOP;
                return new AllStopCommand();
            } else {
                //if i am no longer moving noticeably but not actually there, try again
                this.state = ShipState.TURN;
            }
        } else if (!BasicShip.sameAngle(currentDirection, optimalDirection, BasicShip.ANGLE_BOUNDS)) {
            //if i'm off course brake (eventually restart)
            return new BrakeCommand(BasicShip.BRAKE_PERCENT);
        } else {
            //if i can keep slowing down, do that
            return new BrakeCommand(BasicShip.BRAKE_PERCENT);
        }
        return null;
    }

    /**
     * Determines the appropriate ShipCommand to return in the phase of
     * stopping. If there are no appropriate commands, it returns null and
     * changes the appropriate state variables to reflect that.
     *
     * @return null
     */
    @Override
    protected ShipCommand whileStop() {
        this.state = ShipState.RADAR;
        this.radarSpecific = null;
        return null;
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
    public int closestID(Point current, double angle, double speed, String type) {
        if (this.radarGeneral == null) {
            return -1;
        }
        Point mydestination = targetDest(current, angle, speed);
        double min = Double.MAX_VALUE;
        int id = -1;
        for (ObjectStatus testing : this.radarGeneral) {
            if (distance(testing.getPosition(), mydestination) < min && testing.getType().equals(type)) {
                min = distance(testing.getPosition(), mydestination);
                id = testing.getId();
            }
        }
        return id;
    }

    public Point optimalBauble() {
        if (this.gameinfo.getBaublePositions().isEmpty()) {
            return null;
        }
        Point closest = this.gameinfo.getBaublePositions().get(0);
        double mindistance = Double.MAX_VALUE;
        for (Point test : this.gameinfo.getBaublePositions()) {
            if (distance(test, this.currentPosition) < mindistance) {
                mindistance = distance(test, this.currentPosition);
                closest = test;
            }
        }
        return closest;
    }
}
