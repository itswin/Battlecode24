package MPWorking;

import battlecode.common.*;
import MPWorking.fast.*;
import MPWorking.bfs.*;

public class Nav {
    static RobotController rc;

    static MapLocation lastCurrLoc;
    static MapLocation currentTarget;
    static int closestDistanceToDest;
    static int turnsSinceClosestDistanceDecreased;
    static int turnsGreedy;

    static final int BYTECODE_REMAINING = 1500;
    static final int BYTECODE_REMAINING_AMPLIFIER = 2000;

    static final int GREEDY_TURNS = 4;

    static final int id = 10136;

    // public static final int BFS34_COST = 6000;
    public static final int BFS20_COST = 4000;
    public static final int BFS10_COST = 2500;

    static void init(RobotController r) {
        rc = r;
        turnsGreedy = 0;
        closestDistanceToDest = Integer.MAX_VALUE;
        turnsSinceClosestDistanceDecreased = 0;
        VisitedTracker.reset();

        BFS20.init(r);
        BFS10.init(r);
    }

    // @requires loc is adjacent to currLoc
    public static Direction[] getDirsToAdjSquares(MapLocation loc) {
        switch (rc.getLocation().directionTo(loc)) {
            case SOUTH:
                return new Direction[] { Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.SOUTHEAST,
                        Direction.SOUTHWEST };
            case NORTH:
                return new Direction[] { Direction.NORTH, Direction.EAST, Direction.WEST, Direction.NORTHEAST,
                        Direction.NORTHWEST };
            case EAST:
                return new Direction[] { Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.NORTHEAST,
                        Direction.SOUTHEAST };
            case WEST:
                return new Direction[] { Direction.WEST, Direction.NORTH, Direction.SOUTH, Direction.NORTHWEST,
                        Direction.SOUTHWEST };
            case NORTHEAST:
                return new Direction[] { Direction.NORTHEAST, Direction.NORTH, Direction.EAST };
            case SOUTHEAST:
                return new Direction[] { Direction.SOUTHEAST, Direction.SOUTH, Direction.EAST };
            case SOUTHWEST:
                return new Direction[] { Direction.SOUTHWEST, Direction.SOUTH, Direction.WEST };
            case NORTHWEST:
                return new Direction[] { Direction.NORTHWEST, Direction.NORTH, Direction.WEST };
            default:
                return new Direction[] {};
        }
    }

    public static Direction getBestDir(MapLocation dest) throws GameActionException {
        return getBestDir(dest, 0, false);
    }

    public static Direction getBestDir(MapLocation dest, int bytecodeCushion, boolean ignoreWater)
            throws GameActionException {
        int bcLeft = Clock.getBytecodesLeft();
        Direction dir = null;
        // TODO
        if (bcLeft >= BFS20_COST + bytecodeCushion) {
            dir = BFS20.bestDir(dest);
        } else if (bcLeft >= BFS10_COST + bytecodeCushion) {
            dir = BFS10.bestDir(dest);
        }

        if (dir == null) {
            if (bytecodeCushion == 9999) {
                bytecodeCushion = BYTECODE_REMAINING;
            }
            if (bcLeft >= bytecodeCushion) {
                dir = getGreedyDirection(rc.getLocation().directionTo(dest), ignoreWater);
            } else {
                dir = Util.getFirstValidInOrderDirection(rc.getLocation().directionTo(dest));
            }
        }

        return dir;
    }

    public static Direction getGreedyDirection(Direction dir, boolean ignoreWater) throws GameActionException {
        Direction[] bestDirs = greedyDirection(dir);
        if (bestDirs.length > 0) {
            return bestDirs[0];
        } else {
            return Util.getFirstValidInOrderDirection(dir, ignoreWater);
        }
    }

    public static Direction[] greedyDirection(Direction dir)
            throws GameActionException {
        Direction left = dir.rotateLeft();
        Direction right = dir.rotateRight();

        MapLocation currLoc = rc.getLocation();

        int numToInsert = 0;
        Direction[] allDirs = new Direction[3];
        int allCooldowns[] = new int[3];
        if (rc.canMove(dir)) {
            allDirs[numToInsert++] = dir;
        }
        if (rc.canMove(left)) {
            allDirs[numToInsert++] = left;
        }
        if (rc.canMove(right)) {
            allDirs[numToInsert++] = right;
        }

        Direction[] dirs = new Direction[numToInsert];
        System.arraycopy(allDirs, 0, dirs, 0, numToInsert);
        int[] cooldowns = new int[numToInsert];
        System.arraycopy(allCooldowns, 0, cooldowns, 0, numToInsert);

        FastSort.sort(cooldowns);
        Direction[] out = new Direction[numToInsert];
        for (int i = 0; i < FastSort.size; i++) {
            out[i] = dirs[FastSort.indices[i]];
        }

        return out;
    }

    static MapLocation getGreedyTargetAway(MapLocation loc) throws GameActionException {
        Direction[] dirs = greedyDirection(rc.getLocation().directionTo(loc).opposite());
        return rc.getLocation().add(Util.getFirstMoveableDir(dirs));
    }

    static void reset() {
        turnsGreedy = 0;
        VisitedTracker.reset();
    }

    static void activateGreedy() {
        turnsGreedy = GREEDY_TURNS;
    }

    static void update(MapLocation target) {
        if (currentTarget == null || target.distanceSquaredTo(currentTarget) > 0) {
            closestDistanceToDest = rc.getLocation().distanceSquaredTo(target);
            turnsSinceClosestDistanceDecreased = 0;
            currentTarget = target;
            reset();
            return;
        }

        currentTarget = target;
        VisitedTracker.add(rc.getLocation());
        turnsGreedy--;

        int dist = rc.getLocation().distanceSquaredTo(target);
        if (dist < closestDistanceToDest) {
            closestDistanceToDest = dist;
            turnsSinceClosestDistanceDecreased = 0;
        } else {
            turnsSinceClosestDistanceDecreased++;
        }
    }

    static void move(MapLocation target) throws GameActionException {
        move(target, false, true, BYTECODE_REMAINING, false);
    }

    static void move(MapLocation target, boolean fillWater) throws GameActionException {
        move(target, false, true, BYTECODE_REMAINING, fillWater);
    }

    static void move(MapLocation target, int bytecodeCushion) throws GameActionException {
        move(target, false, true, bytecodeCushion, false);
    }

    static void move(MapLocation target, boolean greedy, boolean fillWater) throws GameActionException {
        move(target, greedy, true, BYTECODE_REMAINING, fillWater);
    }

    static void move(MapLocation target, boolean greedy, boolean avoidHQ,
            int bytecodeCushion, boolean fillWater) throws GameActionException {
        if (target == null)
            return;
        Debug.setIndicatorLine(rc.getLocation(), target, 255, 0, 200);
        if (!rc.isMovementReady())
            return;
        Debug.setIndicatorLine(rc.getLocation(), target, 0, 0, 200);
        if (rc.getLocation().distanceSquaredTo(target) == 0)
            return;

        MapLocation currLoc = rc.getLocation();
        // Set squares within action radius of an enemy HQ to be impassable
        MapLocation enemyHQ;
        RobotInfo robot;
        boolean[] imp = new boolean[Util.DIRS_CENTER.length];
        boolean setImpassable = false;

        // If we are already within action radius, don't bother
        if (greedy && setImpassable) {
            Pathfinding.setImpassable(imp);
        }

        update(target);

        boolean canBFS = turnsSinceClosestDistanceDecreased < 2 && turnsGreedy <= 0;
        if (!greedy && canBFS) {
            Direction dir = getBestDir(target, bytecodeCushion, fillWater);
            if (dir != null) {
                if (rc.canMove(dir)) {
                    if (!VisitedTracker.check(rc.adjacentLocation(dir))) {
                        rc.move(dir);
                        return;
                    } else {
                        activateGreedy();
                    }
                } else if (fillWater) {
                    MapLocation loc = rc.adjacentLocation(dir);
                    MapInfo info = rc.senseMapInfo(loc);
                    if (info.isWater() && rc.canFill(loc)) {
                        rc.fill(loc);
                        if (rc.canMove(dir)) {
                            if (!VisitedTracker.check(rc.adjacentLocation(dir))) {
                                rc.move(dir);
                                return;
                            } else {
                                activateGreedy();
                            }
                        }
                    }
                }
            }
        }

        if (Clock.getBytecodesLeft() >= BYTECODE_REMAINING) {
            Debug.println(Debug.PATHFINDING,
                    "getBestDir failed to get closer in 2 turns: Falling back to bugNav", id);
            Pathfinding.move(target);
        } else {
            Debug.setIndicatorDot(true, rc.getLocation(), 255, 255, 255);
            Debug.println("Didn't have enough BC");
        }
    }
}
