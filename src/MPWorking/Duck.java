package MPWorking;

import battlecode.common.*;
import MPWorking.Comms.*;
import MPWorking.Debug.*;
import MPWorking.fast.*;
import MPWorking.Util.*;

public class Duck extends Robot {
    static enum State {
        SETUP,
        EXPLORING,
        CAPTURING_FLAG,
    };

    MapLocation target;
    MapLocation enemyFlagPickupLoc = null;

    FlagInfo[] enemyFlags;
    MapLocation enemyFlagLoc;

    State currState = State.SETUP;

    public Duck(RobotController r) throws GameActionException {
        super(r);
    }

    public boolean takeTurn() throws GameActionException {
        if (!super.takeTurn()) {
            return false;
        }

        closestEnemy = getBestEnemy(enemies);
        loadEnemyFlagTarget();

        trySwitchState();
        Debug.printString("S: " + currState);
        doStateAction();

        return true;
    }

    public void trySwitchState() throws GameActionException {
        switch (currState) {
            case SETUP:
                if (rc.getRoundNum() > GameConstants.SETUP_ROUNDS)
                    currState = State.EXPLORING;
            case EXPLORING:
                if (enemyFlagLoc != null) {
                    currState = State.CAPTURING_FLAG;
                }
                break;
            case CAPTURING_FLAG:
                if (enemyFlagLoc == null && !rc.hasFlag()) {
                    currState = State.EXPLORING;
                }
                break;
        }
    }

    public void doStateAction() throws GameActionException {
        // If capturing flag, do not allow movement in micro
        // Do not do micro in setup
        if (currState != State.SETUP && MicroDuck.doMicro(currState != State.CAPTURING_FLAG))
            return;

        switch (currState) {
            case SETUP:
                if (rc.getRoundNum() < Util.RANDOM_EXPLORE_ROUNDS) {
                    loadSetupExploreTarget();
                } else {
                    loadExploreTarget2();
                }

                doExplore();
                placeTrapNearDam();
                break;
            case EXPLORING:
                loadExploreTarget2();
                doExplore();
                placeRandomTrap();
                break;
            case CAPTURING_FLAG:
                doCapture();
                break;
        }
    }

    public void loadEnemyFlagTarget() throws GameActionException {
        enemyFlags = rc.senseNearbyFlags(-1, opponent);
        enemyFlagLoc = null;
        MapLocation closestLoc = null;
        int closestDist = Integer.MAX_VALUE;
        for (FlagInfo flag : enemyFlags) {
            if (!flag.isPickedUp()) {
                MapLocation flagLoc = flag.getLocation();
                int dist = rc.getLocation().distanceSquaredTo(flagLoc);
                if (dist < closestDist) {
                    closestDist = dist;
                    closestLoc = flagLoc;
                }
            }
        }
        enemyFlagLoc = closestLoc;
    }

    public void tryClearEnemyFlagPickupLoc() throws GameActionException {
        if (!rc.isSpawned() || rc.hasFlag())
            return;

        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        boolean isOnSpawnLoc = false;
        MapLocation currLoc = rc.getLocation();
        for (MapLocation spawnLoc : spawnLocs) {
            if (spawnLoc.equals(currLoc)) {
                isOnSpawnLoc = true;
                break;
            }
        }

        assert isOnSpawnLoc;

        int numEnemyFlags = 0;
        int enemyFlagPickupLocSlot = -1;
        for (numEnemyFlags = 0; numEnemyFlags < Comms.ENEMY_FLAG_SLOTS; numEnemyFlags++) {
            MapLocation flagLoc = Comms.readEnemyFlagLocation(numEnemyFlags);
            if (!rc.onTheMap(flagLoc)) {
                break;
            } else if (flagLoc.equals(enemyFlagPickupLoc)) {
                enemyFlagPickupLocSlot = numEnemyFlags;
            }
        }

        // TODO
        // Picked up after it was dropped
        if (enemyFlagPickupLocSlot == -1) {
            Debug.println("Captured a dropped flag");
            return;
        }

        Debug.println("Captured flag from " + enemyFlagPickupLoc + " slot " + enemyFlagPickupLocSlot);
        // Write the numEnemyFlags - 1 slot to the enemy flag pickup loc
        int lastSlot = Comms.readEnemyFlagAll(numEnemyFlags - 1);
        Comms.writeEnemyFlagAll(enemyFlagPickupLocSlot, lastSlot);
        Comms.writeEnemyFlagAll(numEnemyFlags - 1, Comms.OFF_THE_MAP_LOC);
    }

    public void placeTrapNearDam() throws GameActionException {
        if (!rc.isActionReady())
            return;

        MapInfo[] mapInfos = rc.senseNearbyMapInfos(rc.getLocation(), Util.JUST_OUTSIDE_INTERACT_RADIUS);
        MapLocation locCandidate;
        for (MapInfo info : mapInfos) {
            if (Util.isDamLoc(info)) {
                for (Direction dir : Util.directions) {
                    locCandidate = info.getMapLocation().add(dir);
                    if (rc.canBuild(TrapType.EXPLOSIVE, locCandidate)) {
                        rc.build(TrapType.EXPLOSIVE, locCandidate);
                        return;
                    }
                }
            }
        }
    }

    public void placeRandomTrap() throws GameActionException {
        // Rarely attempt placing traps
        MapLocation prevLoc = rc.getLocation();
        if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && FastMath.nextInt(256) % 37 == 1)
            rc.build(TrapType.EXPLOSIVE, prevLoc);
    }

    public void doExplore() throws GameActionException {
        Nav.move(exploreTarget, true);
        turnsFollowedExploreTarget++;
        tryAttackBestEnemy();
    }

    public void doCapture() throws GameActionException {
        if (!rc.hasFlag()) {
            assert enemyFlagLoc != null;
            Nav.move(enemyFlagLoc, true);
            if (rc.canPickupFlag(enemyFlagLoc)) {
                enemyFlagPickupLoc = enemyFlagLoc;
                rc.pickupFlag(enemyFlagLoc);
                Debug.printString("Picked up flag");
                MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                target = Util.getClosestLoc(spawnLocs);
            }
        } else {
            MapLocation[] spawnLocs = rc.getAllySpawnLocations();
            boolean foundTarget = false;
            for (MapLocation spawnLoc : spawnLocs) {
                if (spawnLoc.equals(target)) {
                    foundTarget = true;
                    break;
                }
            }
            assert foundTarget;

            boolean hadFlagBeforeMove = rc.hasFlag();
            Nav.move(target, true);
            boolean hasFlagAfterMove = rc.hasFlag();

            if (hadFlagBeforeMove && !hasFlagAfterMove) {
                tryClearEnemyFlagPickupLoc();
            }
        }
    }
}
