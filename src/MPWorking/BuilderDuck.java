package MPWorking;

import battlecode.common.*;
import MPWorking.Comms.*;
import MPWorking.Debug.*;
import MPWorking.fast.*;
import MPWorking.Util.*;

public class BuilderDuck extends Robot {
    static enum State {
        SETUP,
        EXPLORING,
        WAITING,
    };

    MapLocation target;
    State currState = State.SETUP;

    public BuilderDuck(RobotController r) throws GameActionException {
        super(r);
    }

    public boolean takeTurn() throws GameActionException {
        if (!super.takeTurn()) {
            return false;
        }

        closestEnemy = getBestEnemy(enemies);

        trySwitchState();
        Debug.printString("BD");
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
                digForExp();
                doExplore();

                if (rc.getLevel(SkillType.BUILD) == 6)
                    placeTrapNearDam();
                break;
            case EXPLORING:
                loadExploreTarget2();
                doExplore();
                fillWater();
                digForExp();

                if (rc.getLevel(SkillType.BUILD) == 6)
                    placeRandomTrap();
                break;
            case WAITING:
                fillWater();
                formHull();
                break;
        }

        if (currState != State.SETUP && MicroDuck.apply())
            return;
    }

    public boolean shouldWait() {
        boolean tooSoon = turnSawLastClosestAttackingEnemy + WAITING_TIMEOUT >= rc.getRoundNum();
        return tooSoon || !rc.isActionReady();
    }

    public TrapType pickTrapType(MapLocation loc) throws GameActionException {
        if (rc.getCrumbs() < MicroDuck.STUN_TRAP_COST)
            return null;
        if (rc.getCrumbs() < MicroDuck.EXPLOSIVE_TRAP_COST)
            return TrapType.STUN;

        if (FastMath.rand256() % 2 == 0)
            return TrapType.EXPLOSIVE;
        else
            return TrapType.STUN;
    }

    public void placeTrapNearDam() throws GameActionException {
        if (!rc.isActionReady())
            return;

        MapInfo[] mapInfos = rc.senseNearbyMapInfos(rc.getLocation(), Util.JUST_OUTSIDE_INTERACT_RADIUS);
        MapLocation locCandidate;
        for (MapInfo info : mapInfos) {
            if (info.isDam()) {
                for (Direction dir : Util.directions) {
                    locCandidate = info.getMapLocation().add(dir);
                    TrapType trapType = pickTrapType(locCandidate);
                    if (trapType != null
                            && rc.canBuild(trapType, locCandidate)
                            && !Util.isAdjacentToTrap(locCandidate)) {
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
                && !Util.isAdjacentToTrap(prevLoc))
            rc.build(trapType, prevLoc);
    }

    public void doExplore() throws GameActionException {
        Nav.move(exploreTarget, true);
        turnsFollowedExploreTarget++;
        tryAttackBestEnemy();
    }

    public void digForExp() throws GameActionException {
        if (rc.getLevel(SkillType.BUILD) == 6)
            return;

        digHole();
    }
}
