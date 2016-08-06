/**
 * @author    Amir hossein Hajianpour <ahhajianpour1@gmail.com>
 * @author    Mohammad reza Hajianpour <hajianpour.mr@gmail.com>
 * @version   1.2
 * @since     1.0
 */

package com.twinity.PlanetWarsServer;

import java.util.ArrayList;
import java.util.Arrays;

public class World {

    // An instance of Map class to be used in World
    private Map _map;
    // FIXME: What is _currentTurn when we have implemented it on the server?
    private float _currentTurn = 1;

    /**
     * World Constructor
     * @param inMap Receives and saves a Map inside itself.
     */
    public World(Map inMap) {
        _map = inMap;
    }

    /**
     * Applies the input ArmyMovement array and updates the map accordingly.
     * @param inArmyMovement An ArmyMovement array to update the map with.
     * @param inMyId An int which is the owner of the ArmyMovement array
     */
    public void moveArmy(ArmyMovement[] inArmyMovement, int inMyId) {
        ArmyMovement[] validArmyMovement = armyMovementValidator(inArmyMovement, inMyId);
        updateMap(validArmyMovement, inMyId);
        // FIXME: Is _currentTurn in sync with player turns on the Server?
        _currentTurn += 0.5;
    }

    /**
     * Gets the map object residing in the World class
     * @return Returns a map object
     */
    public Map getMap() {
        return _map;
    }

    /**
     * Gets the current turn
     * @return Returns an int indicating the current turn
     */
    public int getCurrentTurn() {
        return (int) _currentTurn;
    }

    /**
     * Gets the remaining turns of the game
     * @return Returns an int indicating the remaining turns of the game.
     */
    public int getRemainingTurns() {
        return getMap().getTotalTurns() - getCurrentTurn();
    }

    /**
     * Updates the map with a valid ArmyMovement
     * @param inArmyMovement An ArmyMovement array to update the map with.
     * @param inMyId An int which is the current player's ID.
     */
    // TODO: Should separate game's rules from updating the map.
    private void updateMap(ArmyMovement[] inArmyMovement, int inMyId) {
        for (int i = 0; i < inArmyMovement.length; i++) {
            // Source node of the issued command
            Node src = getMap().getNode(inArmyMovement[i].getSource());
            // Destination node of the issued command
            Node dest = getMap().getNode(inArmyMovement[i].getDestination());
            // Amount of the soldiers issued to move
            int ac = inArmyMovement[i].getArmyCount();

            // If the movement's destination is a neutral node
            if (dest.getOwner() == 0) {
                // Set destination's army count to the movement's army count (NO WARS)
                dest.setArmyCount(ac);
                // Reduce the amount of issued army moved from source node
                src.setArmyCount(src.getArmyCount() - ac);
            }
            // If the destination node was mine
            else if (dest.getOwner() == inMyId) {
                // Add the amount of issued army count to the destination node (NO WARS)
                dest.setArmyCount(ac + dest.getArmyCount());
                // Reduce the amount of issued army count from source
                src.setArmyCount(src.getArmyCount() - ac);
            }
            // If the destination node was enemies, [WAR HAPPENS]
            else {
                // If issued army count is bigger than destination (enemy)'s army count
                if (ac >= dest.getArmyCount()) {
                    dest.setArmyCount((int) Math.ceil(ac - dest.getArmyCount() * Math.sqrt(dest.getArmyCount() / ac)));
                    dest.setOwner(src.getOwner());
                } else {
                    dest.setArmyCount((int) Math.ceil(dest.getArmyCount() - ac * Math.sqrt(ac / dest.getArmyCount())));
                }
            }
        }
    }

    /**
     * Validates ArmyMovement array and checks if the movements are sensible.
     * @param inArmyMovement An ArmyMovement array to be validated.
     * @param inMyId An int which is the current player's ID.
     * @return Returns an array of ArmyMovement with valid movements.
     */
    private ArmyMovement[] armyMovementValidator(ArmyMovement[] inArmyMovement, int inMyId) {
        ArrayList<ArmyMovement> am = new ArrayList<>(Arrays.asList(inArmyMovement));
        boolean valid = false;

        for (int i = 0; i < am.size(); i++) {
            // Checks to see if the indicated source index belongs to the player.
            for (int j = 0; j < getMap().getMyNodes(inMyId).length; j++) {
                if (am.get(i).getSource() == getMap().getMyNodes(inMyId)[j].getId()) {
                    valid = true;
                    // If the matching source found in one of my nodes, break the loop.
                    break;
                }
            }
            // If the movement is not valid, pop it out of the array
            if (!valid)
                am.remove(i);
            // If the first phase (Source validation) is passed
            else {
                valid = false;

                /**
                 * Check to see if the destination of the current ArmyMovement is in the
                 * node's adjacents with the same id as the ArmyMovement's source.
                 */
                for (int adj : getMap().getNode(am.get(i).getSource()).getAdjacents()) {
                    if (am.get(i).getDestination() == adj) {
                        valid = true;
                        break;
                    }
                }

                // If the destination is invalid, pop the current ArmyMovement out.
                if (!valid)
                    am.remove(i);

                /**
                 * Check if the ArmyMovement has currect values for army count.
                 * Correct them if needed.
                  */

                else {
                    // If the army count in ArmyMovement is below zero, set it to zero
                    if (am.get(i).getArmyCount() < 0)
                        am.get(i).setArmyCount(0);
                    // If the player is moving more army than he has, set it to the maximum he has.
                    else if (am.get(i).getArmyCount() > getMap().getNode(am.get(i).getSource()).getArmyCount())
                        am.get(i).setArmyCount(getMap().getNode(am.get(i).getSource()).getArmyCount());
                }
            }
        }
        return am.toArray(new ArmyMovement[am.size()]);
    }
}
