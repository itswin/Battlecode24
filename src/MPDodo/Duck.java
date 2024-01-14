package MPDodo;

import battlecode.common.*;
import battlecode.server.GameMaker;
import MPDodo.Comms.*;
import MPDodo.Debug.*;
import MPDodo.fast.*;
import MPDodo.Util.*;

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
                break;
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
        // Do not do micro in setup
        boolean shouldDoMicro = currState != State.SETUP && currState != State.CAPTURING_FLAG;
        if (shouldDoMicro && MicroDuck.doMicro())
            return;

        switch (currState) {
            case SETUP:
                if (rc.getRoundNum() < Util.RANDOM_EXPLORE_ROUNDS) {
                    loadSetupExploreTarget();
                } else {
                    loadExploreTarget2();
                }

                fillWater();
                doExplore();
                break;
            case EXPLORING:
                loadExploreTarget2();
                doExplore();
                considerTransformBuilder();
                break;
            case CAPTURING_FLAG:
                fillWater();
                doCapture();
                break;
        }

        if (shouldDoMicro && MicroDuck.apply())
            return;
    }

    public void considerTransformBuilder() throws GameActionException {
        int minRound = Comms.readNextBuilderMinRoundNum();
        if (rc.getRoundNum() < minRound)
            return;

        if (rc.getCrumbs() < Util.NEXT_BUILDER_MIN_CRUMBS)
            return;

        int nextBuilderUnitNum = Comms.readNextBuilderUnitNum();
        if (nextBuilderUnitNum == unitNum) {
            Robot.changeTo = new BuilderDuck(rc);
            Comms.writeNextBuilderUnitNum(unitNum + 1);
            Comms.writeNextBuilderMinRoundNum(rc.getRoundNum() + Util.NEXT_BUILDER_COOLDOWN);
            Debug.println("Transforming to builder");
        }
    }

    public boolean shouldWait() {
        boolean tooSoon = turnSawLastClosestAttackingEnemy + WAITING_TIMEOUT >= rc.getRoundNum();
        return false;
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
