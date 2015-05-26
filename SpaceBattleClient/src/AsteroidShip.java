
import ihs.apcs.spacebattle.*;
import ihs.apcs.spacebattle.commands.*;

/**
 *
 * @author Aeranythe Echosong
 */
public class AsteroidShip extends BasicShip {

    @Override
    public void initializePoints() {
        //We have no baubles to speak of.
        this.waypoints = new Point[0];
    }

    @Override
    public ShipCommand getNextCommand(BasicEnvironment be) {
        //set up nonswitchaltered variables
        shipStatus = be.getShipStatus();
        currentPosition = shipStatus.getPosition();
        currentDirection = shipStatus.getOrientation();
        currentSpeed = shipStatus.getSpeed();
        ShipCommand result = null;
        //catches stateswitches during a case
        while (result == null) {
            //set up switchaltered (read: radar affected) variables
            RadarResults radar = be.getRadar();
            switch (this.state) {
                case START:
                    //here for solidarity XD
                    result = whileStart();
                    break;
                case TURN:
                    result = whileTurn();
                    break;
                case THRUST:
                    result = whileThrust();
                    break;
                case COAST:
                    result = whileCoast();
                    break;
                case BRAKE:
                    result = whileBrake();
                    break;
                case STOP:
                    result = whileStop();
                    break;
            }
        }
        return result;
    }

}
