
import ihs.apcs.spacebattle.*;
import ihs.apcs.spacebattle.commands.*;

/**
 *
 * @author Aeranythe Echosong
 */
public class AsteroidShip extends BasicShip {

    protected RadarResults radarGeneral = null;
    protected ObjectStatus radarSpecific = null;
    protected Point semiTarget = null;
    protected ShipState state = ShipState.RADAR;

    protected double lastRadarLevel;

    public static final double MAX_DRIFT_SPEED = 20;
    public static final double SHOOT_RANGE = 500;
    public static final double SHOOT_ENERGY_THRESHOLD = 20;
    public static final double AIM_SPEED_FACTOR = 1.5;
    public static final double ANGLE_BOUNDS = 5;

    @Override
    public void initializePoints() {
        //We have no baubles to speak of :/
        this.waypoints = new Point[0];
    }

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
        //catches stateswitches during a case
        while (result == null) {
            //only use these if we have passed our radar stage
            if (radarSpecific != null) {
                this.semiTarget = this.targetDest(radarSpecific.getPosition(), radarSpecific.getOrientation(),
                        -radarSpecific.getSpeed() * AsteroidShip.AIM_SPEED_FACTOR);
                this.optimalVect = this.direction(shipStatus.getPosition(), this.semiTarget);
                this.optimalDirection = AsteroidShip.getAngle(optimalVect);
                this.distance = this.distance(shipStatus.getPosition(), this.semiTarget);
            }
            switch (this.state) {
                case RADAR:
                    result = whileRadar();
                    break;
                case START:
                    System.out.println("Start");
                    result = whileStart();
                    break;
                case TURN:
                    System.out.println("Turn target " + semiTarget);
                    result = whileTurn();
                    break;
                case SHOOT:
                    System.out.println("Shoot");
                    result = whileShoot();
                    break;
                case STOP:
                    System.out.println("Shot and retarget");
                    result = whileStop();
                    break;
                default:
                    System.out.println("Something went wrong?");
                    break;
            }
        }
        return result;
    }

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
        return null;
    }

    @Override
    protected ShipCommand whileTurn() {
        System.out.println(optimalDirection - currentDirection);
        if (!AsteroidShip.sameAngle(currentDirection, optimalDirection, AsteroidShip.ANGLE_BOUNDS)
                && !AsteroidShip.sameAngle(currentDirection + 180, optimalDirection, AsteroidShip.ANGLE_BOUNDS)) {
            //if i'm facing the wrong way (forwards or backwards) rotate
            double rotation = optimalDirection - currentDirection;
            //fix over-90 rotations (gotta use back AND front!)
            while (Math.abs(rotation) > 90) {
                if (rotation > 0) {
                    rotation = rotation - 180;
                } else {
                    rotation = rotation + 180;
                }
            }
            if (distance(this.currentPosition, this.semiTarget) > AsteroidShip.SHOOT_RANGE) {
                //if i'm out of range don't even try for that one
                this.state = ShipState.STOP;
            } else {
                //in range? switch to shooting
                this.state = ShipState.SHOOT;
            }
            return new RotateCommand(rotation);
        }
        this.state = ShipState.SHOOT;
        return null;
    }

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
    protected ShipCommand whileStop() {
        this.state = ShipState.RADAR;
        this.radarSpecific = null;
        return null;
    }

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

    public static boolean containsObjectID(RadarResults list, int id) {
        for (ObjectStatus test : list) {
            if (test.getId() == id) {
                return true;
            }
        }
        return false;
    }

}
