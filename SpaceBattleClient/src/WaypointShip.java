
import ihs.apcs.spacebattle.Point;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author s-zhouj
 */
public class WaypointShip extends BasicShip {

    public WaypointShip() {
        super();
    }

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
