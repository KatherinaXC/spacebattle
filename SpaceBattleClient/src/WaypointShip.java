
import ihs.apcs.spacebattle.Point;

/**
 *
 * @author s-zhouj
 */
public class WaypointShip extends BasicShip {

    /**
     * This is a stupid useless constructor that exists solely for the purpose
     * of MAKING THE PROGRAM NOT CRASH ok bye.
     */
    public WaypointShip() {
        super();
    }

    /**
     * Constructor for a WaypointShip, setting up the parameters worldWidth and
     * worldHeight.
     *
     * @param worldWidth the width of the world
     * @param worldHeight the height of the world
     */
    public WaypointShip(int worldWidth, int worldHeight) {
        super(worldWidth, worldHeight);
    }

    /**
     * Initializes the appropriate waypoints for the WaypointShip.
     */
    public void initializePoints() {
        this.waypoints = new Point[6];
        this.waypoints[0] = new Point(getWorldWidth() / 2, getWorldHeight() / 2);
        this.waypoints[1] = new Point(0 + 100, 0 + 100);
        this.waypoints[2] = new Point(getWorldWidth() - 100, 0 + 100);
        this.waypoints[3] = new Point(0 + 100, getWorldHeight() - 100);
        this.waypoints[4] = new Point(getWorldWidth() - 100, getWorldHeight() - 100);
        this.waypoints[5] = waypoints[0];
    }
}
