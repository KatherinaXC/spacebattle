
import ihs.apcs.spacebattle.*;
import ihs.apcs.spacebattle.commands.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Winterstorm
 */
public class ShapeShip extends BasicShip {

    public static final double SHAPE_SIDE_LENGTH = 50.;
    public static final int SHAPE_SIDE_COUNT = 10;
    public static final double SHAPE_CORNER_ANGLE = 90.;
    private double shape_corner_current = ShapeShip.SHAPE_CORNER_ANGLE;

    public ShapeShip() {
        super();
    }

    public ShapeShip(int worldWidth, int worldHeight) {
        super(worldWidth, worldHeight);
    }

    @Override
    public void initializePoints() {
        this.state = ShipState.INITIALIZEPOINTS;
    }

    /**
     * Overrides getNextCommand() to account for the initialization of points
     * (which each depend on the previous point's location).
     *
     * @param be
     * @return
     */
    @Override
    public ShipCommand getNextCommand(BasicEnvironment be) {
        //set up nonswitchaltered variables
        shipStatus = be.getShipStatus();
        currentPosition = shipStatus.getPosition();
        //remember to initialize the points
        if (this.state == ShipState.INITIALIZEPOINTS) {
        }
        return super.getNextCommand(be);
    }

    /**
     * Allstops if the ship has reached a point. (We can't have ships flying too
     * far, can we?)
     *
     * @return
     */
    @Override
    public ShipCommand whileBrake() {
        if (atPoint(currentPosition, waypoints[current])) {
            this.state = ShipState.STOP;
            return new AllStopCommand();
        } else if (this.currentSpeed < ShapeShip.EFFECTIVE_STOP) {
            this.state = ShipState.TURN;
        } else if (Math.abs(currentDirection - optimalDirection) > BasicShip.ANGLE_BOUNDS) {
            //if i'm off course brake (eventually restart)
            return new BrakeCommand(BasicShip.BRAKE_PERCENT);
        } else {
            //if i can keep slowing down, do that
            return new BrakeCommand(BasicShip.BRAKE_PERCENT);
        }
        return null;
    }

    /**
     * Overrides the super whileStop() so that the ship actually drops a laser
     * beacon.
     *
     * @return
     */
    @Override
    public ShipCommand whileStop() {
        if (waypoints.length > current + 1) {
            //if there's more points, increment and proceed
            current++;
            this.state = ShipState.START;
        } else {
            //if there's no more points, yay!
            return new IdleCommand(BasicShip.IDLE_TIME);
        }
        return new DeployLaserBeaconCommand();
    }
}
