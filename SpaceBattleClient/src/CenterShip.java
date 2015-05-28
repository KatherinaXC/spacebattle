
import java.awt.Color;
import ihs.apcs.spacebattle.*;
import ihs.apcs.spacebattle.commands.*;

/**
 *
 * @author s-zhouj
 */
public class CenterShip extends BasicShip {

    /**
     * This is a stupid useless constructor that exists solely for the purpose
     * of MAKING THE PROGRAM NOT CRASH ok bye.
     */
    public CenterShip() {
        super();
    }

    /**
     * Constructor for a CenterShip, setting up the parameters worldWidth and
     * worldHeight.
     *
     * @param worldWidth the width of the world
     * @param worldHeight the height of the world
     */
    public CenterShip(int worldWidth, int worldHeight) {
        super(worldWidth, worldHeight);
    }

    /**
     * Initializes the appropriate waypoints for the CenterShip.
     */
    @Override
    public void initializePoints() {
        waypoints = new Point[1];
        waypoints[0] = new Point(getWorldWidth() / 2, getWorldHeight() / 2);
    }

}
