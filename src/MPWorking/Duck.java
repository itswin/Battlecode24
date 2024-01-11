package MPWorking;

import battlecode.common.*;
import MPWorking.Comms.*;
import MPWorking.Debug.*;
import MPWorking.fast.*;
import MPWorking.Util.*;

public class Duck extends Robot {
    static enum State {
        EXPLORING,
        CAPTURING_FLAG,
    };

    MapLocation target;

    State currState = State.EXPLORING;

    public Duck(RobotController r) throws GameActionException {
        super(r);
    }

    public boolean takeTurn() throws GameActionException {
        if (!super.takeTurn()) {
            return false;
        }

        closestEnemy = getBestEnemy(enemies);
        loadExploreTarget2();

        trySwitchState();
        Debug.printString("S: " + currState);
        doStateAction();

        return true;
    }

    public void trySwitchState() throws GameActionException {
        if (rc.hasFlag()) {
            currState = State.CAPTURING_FLAG;
            return;
        }

        switch (currState) {
            case EXPLORING:
                break;
            case CAPTURING_FLAG:
                currState = State.EXPLORING;
                break;
        }
    }

    public void doStateAction() throws GameActionException {
        // If capturing flag, do not allow movement in micro
        if (MicroDuck.doMicro(currState != State.CAPTURING_FLAG))
            return;

        switch (currState) {
            case EXPLORING:
                doExplore();
                break;
            case CAPTURING_FLAG:
                doCapture();
                break;
        }
    }

    public void doExplore() throws GameActionException {
        Nav.move(exploreTarget, true);

        if (rc.canPickupFlag(rc.getLocation())) {
            FlagInfo flagInfo = rc.senseNearbyFlags(0)[0];
            if (flagInfo.getTeam() == opponent) {
                rc.pickupFlag(rc.getLocation());
                Debug.printString("Picked up flag");

                MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                target = Util.getClosestLoc(spawnLocs);
            }
        }

        // Rarely attempt placing traps
        MapLocation prevLoc = rc.getLocation();
        if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && FastMath.nextInt(256) % 37 == 1)
            rc.build(TrapType.EXPLOSIVE, prevLoc);

        turnsFollowedExploreTarget++;
        tryAttackBestEnemy();
    }

    public void doCapture() throws GameActionException {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        boolean foundTarget = false;
        for (MapLocation spawnLoc : spawnLocs) {
            if (spawnLoc.equals(target)) {
                foundTarget = true;
                break;
            }
        }
        assert foundTarget;

        Nav.move(target);
    }
}
