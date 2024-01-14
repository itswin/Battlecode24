package MPWorking;

import battlecode.common.*;
import MPWorking.Comms.*;
import MPWorking.Debug.*;
import MPWorking.fast.*;
import MPWorking.Util.*;

public class FlagGuarderDuck extends Robot {
    static enum State {
        SETUP,
        GUARDING,
    };

    MapLocation target;
    State currState = State.SETUP;

    int flagNum;
    MapLocation homeFlag;
    int lastTurnSeenFlag;

    static final int SEEN_FLAG_TIMEOUT = 100;

    static final int RADIUS1 = 0;
    static final int RADIUS2 = 8;
    static final int RADIUS3 = 20;
    static final int[] RADII = { RADIUS1, RADIUS2, RADIUS3 };

    public FlagGuarderDuck(RobotController r, int num) throws GameActionException {
        super(r);
        flagNum = num;
        homeFlag = null;
        lastTurnSeenFlag = GameConstants.SETUP_ROUNDS;
    }

    public boolean takeTurn() throws GameActionException {
        if (!rc.isSpawned() && rc.getRoundNum() > GameConstants.SETUP_ROUNDS)
            lastTurnSeenFlag = rc.getRoundNum();

        if (!super.takeTurn()) {
            return false;
        }

        closestEnemy = getBestEnemy(enemies);

        if (spawnLocations != null && homeFlag == null) {
            homeFlag = spawnLocations[flagNum];
        }

        trySwitchState();
        Debug.printString("GD");
        Debug.printString("S: " + currState);
        doStateAction();

        return true;
    }

    public void trySwitchState() throws GameActionException {
        switch (currState) {
            case SETUP:
                if (rc.getRoundNum() > GameConstants.SETUP_ROUNDS)
                    currState = State.GUARDING;
                break;
            case GUARDING:
                if (!lookForFlag()) {
                    if (rc.getRoundNum() - lastTurnSeenFlag > SEEN_FLAG_TIMEOUT) {
                        Robot.changeTo = new Duck(rc);
                        Debug.println("Guarder " + unitNum + " giving up on " + homeFlag);
                    }
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
                if (rc.getRoundNum() > GameConstants.SETUP_ROUNDS - 20) {
                    doGuard();
                } else {
                    doExplore();
                }
                break;
            case GUARDING:
                doGuard();
                tryBuildTrap();
                break;
        }

        if (currState != State.SETUP && MicroDuck.apply())
            return;
    }

    public boolean lookForFlag() throws GameActionException {
        // If you can't see the flag, assume it is there.
        if (rc.getLocation().distanceSquaredTo(homeFlag) >= 20) {
            lastTurnSeenFlag = rc.getRoundNum();
            return true;
        }

        FlagInfo[] flags = rc.senseNearbyFlags(visionRadiusSquared, team);
        for (FlagInfo flag : flags) {
            if (flag.getLocation().equals(homeFlag)) {
                lastTurnSeenFlag = rc.getRoundNum();
                return true;
            }
        }
        return false;
    }

    public void doGuard() throws GameActionException {
        MapLocation currLoc = rc.getLocation();
        int radius = RADII[unitNum % RADII.length];
        if (currLoc.distanceSquaredTo(homeFlag) > radius) {
            moveSafely(homeFlag, radius);
        }
    }

    public void doExplore() throws GameActionException {
        Nav.move(exploreTarget, true);
        turnsFollowedExploreTarget++;
        tryAttackBestEnemy();
    }

    public void tryBuildTrap() throws GameActionException {
        if (enemies.length == 0)
            return;

        MapLocation[] locs = {
                homeFlag.add(Direction.NORTHEAST),
                homeFlag.add(Direction.WEST),
                homeFlag.add(Direction.SOUTHEAST),
        };

        for (MapLocation loc : locs) {
            if (rc.canSenseLocation(loc)) {
                MapInfo info = rc.senseMapInfo(loc);
                if (info.getTrapType() == TrapType.NONE) {
                    if (rc.canBuild(TrapType.STUN, loc)) {
                        rc.build(TrapType.STUN, loc);
                        return;
                    }
                }
            }
        }
    }
}
