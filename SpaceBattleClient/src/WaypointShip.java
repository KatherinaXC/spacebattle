
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
public class WaypointShip extends CenterShip {

    /**
     * Initializes the appropriate waypoints for the WaypointShip.
     */
    private void initializePoints() {
        this.waypoints = new Point[6];
        this.waypoints[0] = new Point(CenterShip.getWorldWidth() / 2, CenterShip.getWorldHeight() / 2);
        this.waypoints[1] = new Point(0 + 100, 0 + 100);
        this.waypoints[2] = new Point(CenterShip.getWorldWidth() - 100, 0 + 100);
        this.waypoints[3] = new Point(0 + 100, CenterShip.getWorldHeight() - 100);
        this.waypoints[3] = new Point(CenterShip.getWorldWidth() - 100, CenterShip.getWorldHeight() - 100);
        this.waypoints[5] = new Point(CenterShip.getWorldWidth() / 2, CenterShip.getWorldHeight() / 2);
    }
}
