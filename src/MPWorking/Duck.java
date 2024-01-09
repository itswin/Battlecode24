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

        closestEnemy = getBestEnemy(EnemySensable);
        loadExploreTarget();

        trySwitchState();
        Debug.printString("S: " + currState);
        doStateAction();

        // Rarely attempt placing traps behind the robot.
        // MapLocation prevLoc = rc.getLocation().subtract(dir);
        // if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && FastMath.nextInt(256) % 37 ==
        // 1)
        // rc.build(TrapType.EXPLOSIVE, prevLoc);

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
            rc.pickupFlag(rc.getLocation());
            Debug.printString("Picked up flag");

            MapLocation[] spawnLocs = rc.getAllySpawnLocations();
            target = Util.getClosestLoc(spawnLocs);
        }

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
