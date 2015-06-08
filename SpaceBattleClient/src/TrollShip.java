
import ihs.apcs.spacebattle.*;
import ihs.apcs.spacebattle.commands.*;
import java.awt.Color;

/**
 *
 * @author s-zhouj
 */
public class TrollShip extends BasicSpaceship {

    private RadarResults radar;
    private boolean Shooting = false;

    public RegistrationData registerShip(int numImages, int worldWidth, int worldHeight) {
        return new RegistrationData("YOLO", Color.YELLOW, BattleShip.SHIP_IMAGE_SOVIET);
    }

    @Override
    public ShipCommand getNextCommand(BasicEnvironment be) {
        return new ThrustCommand('B', 1, 1);
    }

}