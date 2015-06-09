
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

    //targeting information
    private double targetAngle;
    private double targetDistance;

    //radar data
    private RadarResults radar;
    private ArrayList<ObjectStatus> stationaryObstacles = new ArrayList<ObjectStatus>();

    //game and environment information
    private BasicEnvironment env;
    private ObjectStatus myStatus;

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
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.state = ShipState.RADAR;
        //End init
        return new RegistrationData("~8~", Color.WHITE, BattleShip.SHIP_IMAGE_SOVIET);
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
        this.env = be;
        this.myStatus = be.getShipStatus();
        if (be.getRadar() != null) {
            this.radar = be.getRadar();
            updateStationaryObstacles();
            this.state = ShipState.START;
        }
        ShipCommand result = null;
        while (result == null) {
            switch (this.state) {
                case RADAR:
                    result = new RadarCommand(5);
                    break;
                case START:
                    break;
                case TURN:
                    break;
                case SHOOT:
                    break;
                case THRUST:
                    break;
                case BRAKE:
                    break;
                case STOP:
                    break;
            }
        }
        return result;
    }

    private void updateStationaryObstacles() {
    }

}
