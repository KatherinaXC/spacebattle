
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

    /**
     * The length of each side in the spirograph-looking shape that the ship
     * will draw.
     */
    public static final double SHAPE_SIDE_LENGTH = 150;

    /**
     * The number of sides to the spirograph-looking shape that the ship will
     * draw.
     */
    public static final int SHAPE_SIDE_COUNT = 20;

    /**
     * 180 - the angle between each side in the spirograph-looking shape that
     * the ship will draw.
     */
    public static final double SHAPE_CORNER_ANGLE = 126;

    /**
     * Constructor for a ShapeShip, setting up the parameters worldWidth and
     * worldHeight.
     *
     * @param worldWidth the width of the world
     * @param worldHeight the height of the world
     */
    public ShapeShip(int worldWidth, int worldHeight) {
        super(worldWidth, worldHeight);
    }

    /**
     * Initializes a series of points that determine the shape which the ship
     * will fly in/mark.
     */
    @Override
    public void initializePoints() {
        double shapeCornerCurrent = ShapeShip.SHAPE_CORNER_ANGLE;
        Point center = new Point(this.worldWidth / 2, this.worldHeight / 2);
        this.waypoints = new Point[ShapeShip.SHAPE_SIDE_COUNT + 2];
        int i = 0;
        do {
            this.waypoints[i] = this.targetDest(center, shapeCornerCurrent, ShapeShip.SHAPE_SIDE_LENGTH);
            shapeCornerCurrent += ShapeShip.SHAPE_CORNER_ANGLE;
            i++;
        } while (i < this.waypoints.length - 1);
        this.waypoints[ShapeShip.SHAPE_SIDE_COUNT + 1] = center;
    }

    /**
     * Overrides the super whileStart() so that the ship drops a laser beacon on
     * spawn, so that the lines actually connect.
     *
     * @return laser beacon command or null
     */
    @Override
    protected ShipCommand whileStart() {
        this.state = ShipState.TURN;
        if (current != 0 && current != ShapeShip.SHAPE_SIDE_COUNT + 1) {
            //don't deploy on the first one or last one so it doesn't look super weird
            return new DeployLaserBeaconCommand();
        }
        return null;
    }

    /**
     * Overrides the super whileStop() so that the ship actually drops a laser
     * beacon.
     *
     * @return laser beacon command or idle command
     */
    @Override
    protected ShipCommand whileStop() {
        if (waypoints.length > current + 1) {
            //if there's more points, increment and proceed
            current++;
            this.state = ShipState.START;
        }
        if (current > 1) {
            //if i'm past the first point (adjusted)
            return new DeployLaserBeaconCommand();
        }
        return new IdleCommand(BasicShip.IDLE_TIME);
    }
}
