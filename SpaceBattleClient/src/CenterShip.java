
import java.awt.Color;
import ihs.apcs.spacebattle.*;
import ihs.apcs.spacebattle.commands.*;

/**
 *
 * @author s-zhouj
 */
public class CenterShip extends BasicSpaceship {

    private int worldWidth;
    private int worldHeight;
    @Override
    public RegistrationData registerShip(int numImages, int worldWidth, int worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        return new RegistrationData("", new Color(00, 41, 82), numImages);
    }

    @Override
    public ShipCommand getNextCommand(BasicEnvironment be) {
        return new IdleCommand(0.1);
    }

    @Override
    public void shipDestroyed() {
    }

}
