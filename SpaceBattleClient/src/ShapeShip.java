
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

    public static final double SHAPE_SIDE_LENGTH = 500;
    public static final int SHAPE_SIDE_COUNT = 10;
    public static final double SHAPE_CORNER_ANGLE = 63;
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
     * @param be the environment that the ship is in at the moment
     * @return ShipCommand
     */
    @Override
    public ShipCommand getNextCommand(BasicEnvironment be) {
        //set up nonswitchaltered variables
        shipStatus = be.getShipStatus();
        currentPosition = shipStatus.getPosition();
        //remember to initialize the points
        if (this.state == ShipState.INITIALIZEPOINTS) {
            this.waypoints = new Point[ShapeShip.SHAPE_SIDE_COUNT];
            int i = 0;
            do {
                this.waypoints[i] = this.targetDest(this.currentPosition, this.shape_corner_current, ShapeShip.SHAPE_SIDE_LENGTH);
                this.shape_corner_current += ShapeShip.SHAPE_CORNER_ANGLE;
                System.out.println("Point: " + this.waypoints[i]);
                i++;
            } while (i < this.waypoints.length);
            System.out.println("Initting points");
            this.state = ShipState.START;
        }
        return super.getNextCommand(be);
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
