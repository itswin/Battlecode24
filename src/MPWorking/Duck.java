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
        WAITING,
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
                if (shouldWait()) {
                    currState = State.WAITING;
                } else if (enemyFlagLoc != null) {
                    currState = State.CAPTURING_FLAG;
                }
                break;
            case CAPTURING_FLAG:
                if (enemyFlagLoc == null && !rc.hasFlag()) {
                    currState = State.EXPLORING;
                }
                break;
            case WAITING:
                if (!shouldWait()) {
                    currState = State.EXPLORING;
                }
                break;
        }
    }

    public void doStateAction() throws GameActionException {
        // Do not do micro in setup
        if (currState != State.SETUP && MicroDuck.doMicro())
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
            case WAITING:
                fillWater();
                formHull();
                break;
        }

        if (currState != State.SETUP && MicroDuck.apply())
            return;
    }

    public void fillWater() throws GameActionException {
        if (!rc.isActionReady())
            return;

        MapInfo[] mapInfos = rc.senseNearbyMapInfos(rc.getLocation(), GameConstants.INTERACT_RADIUS_SQUARED);
        for (MapInfo info : mapInfos) {
            if (info.isWater() && rc.canFill(info.getMapLocation())) {
                rc.fill(info.getMapLocation());
                return;
            }
        }
    }

    public boolean shouldWait() {
        boolean tooSoon = turnSawLastClosestAttackingEnemy + WAITING_TIMEOUT >= rc.getRoundNum();
        return tooSoon || !rc.isActionReady();
    }

    public void formHull() throws GameActionException {
        if (lastClosestEnemy == null)
            return;

        // We want to form a hull around the last closest attacking enemy
        // To do this, we pref to move to the location closest to the loc which
        // is NOT in vision radius of the location
        int bestDist = Integer.MAX_VALUE;
        Direction bestDir = Direction.CENTER;
        MapLocation loc;
        int dist;
        for (Direction dir : Util.directions) {
            loc = lastClosestEnemy.add(dir);
            dist = currLoc.distanceSquaredTo(loc);
            if (loc.isWithinDistanceSquared(lastClosestEnemy, visionRadiusSquared))
                continue;
            if (dist < bestDist) {
                bestDist = dist;
                bestDir = dir;
            }
        }

        Nav.move(currLoc.add(bestDir), true);
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

    public TrapType pickTrapType(MapLocation loc) throws GameActionException {
        if (rc.getCrumbs() < TrapType.STUN.buildCost)
            return null;
        if (rc.getCrumbs() < TrapType.EXPLOSIVE.buildCost)
            return TrapType.STUN;

        if (FastMath.rand256() % 2 == 0)
            return TrapType.EXPLOSIVE;
        else
            return TrapType.STUN;
    }

    public boolean isAdjacentToTrap(MapLocation loc) throws GameActionException {
        MapInfo[] adjacentLocs = rc.senseNearbyMapInfos(loc, 2);
        for (MapInfo info : adjacentLocs) {
            if (info.getTrapType() != TrapType.NONE)
                return true;
        }
        return false;
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
                    TrapType trapType = pickTrapType(locCandidate);
                    if (trapType != null && rc.canBuild(trapType, locCandidate) && !isAdjacentToTrap(locCandidate)) {
                        rc.build(trapType, locCandidate);
                        return;
                    }
                }
            }
        }
    }

    public void placeRandomTrap() throws GameActionException {
        // Rarely attempt placing traps
        MapLocation prevLoc = rc.getLocation();
        TrapType trapType = pickTrapType(prevLoc);
        if (trapType != null && rc.canBuild(trapType, prevLoc) && FastMath.nextInt(256) % 37 == 1
                && !isAdjacentToTrap(prevLoc))
            rc.build(trapType, prevLoc);
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
