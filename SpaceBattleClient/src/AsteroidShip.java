
import ihs.apcs.spacebattle.*;
import ihs.apcs.spacebattle.commands.*;

/**
 *
 * @author Aeranythe Echosong
 */
public class AsteroidShip extends BasicShip {

    protected RadarResults radarGeneral = null;
    protected RadarResults radarSpecific = null;

    @Override
    public void initializePoints() {
        //We have no baubles to speak of :/
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
            switch (be.getRadarLevel()) {
                //save the radar data if i have any
                case 3:
                    this.radarSpecific = be.getRadar();
                    break;
                case 4:
                    this.radarGeneral = be.getRadar();
                    break;
            }
            if (this.radarGeneral == null || this.radarGeneral.size() == 0) {
                //if i have no useful overall radar so far
                return new RadarCommand(4);
            } else if (this.radarSpecific == null || this.state == ShipState.STOP) {
                //if I have general radar but no specific target, or have shot down the last target
                //pick the optimal target based on closeness + velocity
                int selectedID = closestID(this.radarGeneral, currentPosition, currentDirection, currentSpeed);
                return new RadarCommand(3, selectedID);
            }
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

    @Override
    protected ShipCommand whileTurn() {

    }

    @Override
    protected ShipCommand whileThrust() {
    }

    @Override
    protected ShipCommand whileCoast() {
    }

    @Override
    protected ShipCommand whileBrake() {
    }

    @Override
    protected ShipCommand whileStop() {
    }

    public int closestID(Point current, double angle, double speed) {
        Point mydestination = targetDest(current, angle, speed);
        ObjectStatus closest = this.radarGeneral.get(0);
        for (ObjectStatus testing : this.radarGeneral) {
            if (distance(testing.getPosition(), mydestination) < distance(closest.getPosition(), mydestination)) {
                closest = testing;
            }
        }
        return closest.getId();
    }

    public boolean aimedAtMe() {
        for (ObjectStatus testing : this.radarGeneral) {
        }
    }

}
