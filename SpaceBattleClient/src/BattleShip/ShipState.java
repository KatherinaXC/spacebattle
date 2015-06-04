package BattleShip;

/**
 *
 * @author s-zhouj
 */
public enum ShipState {

    /**
     * State where the ship reads radar and decides which target to act upon.
     */
    RADAR,
    /**
     * Transition 'state' where the ship already knows its next course of action
     * and prepares for said action.
     */
    START,
    /**
     * State where the ship decides how much it should turn, if at all.
     */
    TURN,
    /**
     * State where the ship decides whether to shoot, if possible.
     */
    SHOOT,
    /**
     * State where the ship moves forward with acceleration.
     */
    THRUST,
    /**
     * State where the ship moves forward with no acceleration.
     */
    COAST,
    /**
     * State where the ship slows itself down, in preparation for a change in
     * motion direction or otherwise.
     */
    BRAKE,
    /**
     * State where the ship has accomplished a step in its goal and decides how
     * to progress from there.
     */
    STOP
}
