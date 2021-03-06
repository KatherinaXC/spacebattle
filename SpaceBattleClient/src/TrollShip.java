
import ihs.apcs.spacebattle.*;
import ihs.apcs.spacebattle.commands.*;
import java.awt.Color;

/**
 *
 * @author s-zhoujz
 */
public class TrollShip extends BasicSpaceship {

    private RadarResults radar;
    private boolean Shooting = false;

    public RegistrationData registerShip(int numImages, int worldWidth, int worldHeight) {
        return new RegistrationData("????????", Color.WHITE, OldBattleShip.SHIP_IMAGE_TARDIS);
    }

    @Override
    public ShipCommand getNextCommand(BasicEnvironment be) {
        return new ThrustCommand('B', 1, 1);
    }

}
