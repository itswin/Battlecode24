package MPBadMicro;

import battlecode.common.*;
import java.util.Random;
import MPBadMicro.fast.*;

public class Util {
    static Random rng;

    private static RobotController rc;

    static int MAP_WIDTH;
    static int MAP_HEIGHT;
    static int MAP_AREA;
    static int MAP_MAX_DIST_SQUARED;

    static final int MAX_AREA_FOR_FAST_INIT = 625;
    static final int MAX_AREA_FOR_SEMI_FAST_INIT = 1000;

    static final int CLEAR_ENEMY_INFO_PERIOD = 10;
    static final int ENEMY_INFO_STALE_TIMEOUT = 5;

    static final double MIN_COOLDOWN_MULT_DIFF = 0.1;
    static final double SYM_TO_COMB_DIST_RATIO = 2;
    static final double SYM_TO_COMB_HOME_AGGRESSIVE_DIST_RATIO = 0.5;
    static final double SYM_TO_COMB_HOME_PASSIVE_DIST_RATIO = 1.5;
    static final double COMB_TO_HOME_DIST = 10;

    static final int JUST_OUTSIDE_INTERACT_RADIUS = 5;

    static final int EXP_TARGET_DIST_TO_LEAVE_HQ = 15;

    static final int RANDOM_EXPLORE_ROUNDS = 100;

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static final Direction[] X_DIRECTIONS = {
            Direction.CENTER,
            Direction.NORTHEAST,
            Direction.SOUTHEAST,
            Direction.SOUTHWEST,
            Direction.NORTHWEST,
    };

    static final Direction[] CARDINAL_DIRECTIONS = {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST,
    };

    /** Array containing all the possible movement directions. */
    static final Direction[] DIRS_CENTER = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
            Direction.CENTER,
    };

    static final class SymmetryType {
        // Values in accordance with the bit pos in comms. DO NOT CHANGE
        public static final int VERTICAL = 4;
        public static final int HORIZONTAL = 2;
        public static final int ROTATIONAL = 1;
        public static final int ALL = 7;
    }

    static final int JUST_OUTSIDE_OF_VISION_RADIUS = 34;

    static void init(RobotController r) {
        rc = r;
        rng = new Random(rc.getRoundNum() * 23981 + rc.getID() * 10289);

        MAP_HEIGHT = rc.getMapHeight();
        MAP_WIDTH = rc.getMapWidth();
        MAP_AREA = MAP_HEIGHT * MAP_WIDTH;
        MAP_MAX_DIST_SQUARED = MAP_HEIGHT * MAP_HEIGHT + MAP_WIDTH * MAP_WIDTH;
    }

    static int distance(MapLocation A, MapLocation B) {
        return Math.max(Math.abs(A.x - B.x), Math.abs(A.y - B.y));
    }

    static int manhattan(MapLocation A, MapLocation B) {
        return Math.abs(A.x - B.x) + Math.abs(A.y - B.y);
    }

    // Returns the location on the opposite side from loc wrt to your own location
    static MapLocation invertLocation(MapLocation loc) {
        int dx = loc.x - rc.getLocation().x;
        int dy = loc.y - rc.getLocation().y;
        return rc.getLocation().translate(-dx, -dy);
    }

    // Returns the location rotated 90 degrees clockwise wrt to your own location
    static MapLocation rotateLoc90(MapLocation loc) {
        int dx = loc.x - rc.getLocation().x;
        int dy = loc.y - rc.getLocation().y;
        int rotated_dx = dy;
        int rotated_dy = -dx;
        return rc.getLocation().translate(rotated_dx, rotated_dy);
    }

    static MapLocation[] getValidSymmetryLocs(MapLocation hqLoc, int symmetry) throws GameActionException {
        MapLocation verticalFlip = new MapLocation(hqLoc.x, MAP_HEIGHT - hqLoc.y - 1);
        MapLocation horizontalFlip = new MapLocation(MAP_WIDTH - hqLoc.x - 1, hqLoc.y);
        MapLocation rotation = new MapLocation(MAP_WIDTH - hqLoc.x - 1, MAP_HEIGHT - hqLoc.y - 1);
        switch (symmetry) {
            case SymmetryType.VERTICAL | SymmetryType.HORIZONTAL | SymmetryType.ROTATIONAL:
                return new MapLocation[] {
                        verticalFlip,
                        horizontalFlip,
                        rotation,
                };
            case SymmetryType.VERTICAL | SymmetryType.HORIZONTAL:
                return new MapLocation[] {
                        verticalFlip,
                        horizontalFlip,
                };
            case SymmetryType.VERTICAL | SymmetryType.ROTATIONAL:
                return new MapLocation[] {
                        verticalFlip,
                        rotation,
                };
            case SymmetryType.HORIZONTAL | SymmetryType.ROTATIONAL:
                return new MapLocation[] {
                        horizontalFlip,
                        rotation,
                };
            case SymmetryType.VERTICAL:
                return new MapLocation[] {
                        verticalFlip,
                };
            case SymmetryType.HORIZONTAL:
                return new MapLocation[] {
                        horizontalFlip,
                };
            case SymmetryType.ROTATIONAL:
                return new MapLocation[] {
                        rotation,
                };
            default:
                // This shouldn't happen
                Debug.printString("ERROR: No valid symmetry");
                return new MapLocation[] {
                        verticalFlip,
                        horizontalFlip,
                        rotation,
                };
        }
    }

    static MapLocation getRandomAttackLoc(Direction dir) throws GameActionException {
        MapLocation currLoc = rc.getLocation();
        MapLocation[] locs = null;
        MapLocation mainLoc;
        switch (dir) {
            case NORTH:
            case SOUTH:
            case EAST:
            case WEST:
                mainLoc = currLoc.translate(dir.dx * 4, dir.dy * 4);
                locs = new MapLocation[] {
                        mainLoc, mainLoc.add(dir.opposite().rotateLeft()), mainLoc.add(dir.opposite().rotateRight())
                };
                break;
            case NORTHEAST:
            case SOUTHEAST:
            case SOUTHWEST:
            case NORTHWEST:
                mainLoc = currLoc.translate(dir.dx * 2, dir.dy * 2);
                locs = new MapLocation[] {
                        mainLoc, mainLoc.add(dir.rotateLeft()), mainLoc.add(dir.rotateRight())
                };
                break;
            default:
                locs = new MapLocation[] {
                        currLoc.add(Direction.NORTH)
                };
                // Debug.println("ERROR: Invalid direction");
                break;
        }

        return locs[FastMath.nextInt(locs.length)];
    }

    static int getNumOpenCollectSpots(MapLocation wellLoc) throws GameActionException {
        MapLocation loc;
        int count = 0;
        for (int i = Direction.DIRECTION_ORDER.length; --i >= 0;) {
            loc = wellLoc.add(Direction.DIRECTION_ORDER[i]);
            // Off the map don't count
            if (!rc.onTheMap(loc))
                continue;
            // Assume that if we can't sense the location, it's open
            if (!rc.canSenseLocation(loc) || (rc.sensePassability(loc) && rc.senseRobotAtLocation(loc) == null)) {
                count++;
            }
        }
        return count;
    }

    static int getMaxCollectSpots(MapLocation wellLoc) throws GameActionException {
        MapLocation loc;
        int count = 0;
        for (int i = Direction.DIRECTION_ORDER.length; --i >= 0;) {
            loc = wellLoc.add(Direction.DIRECTION_ORDER[i]);
            // Off the map don't count
            if (!rc.onTheMap(loc))
                continue;
            // Unpassable squares don't count.
            // Assume that if we can't sense the location, it's open
            if (!rc.canSenseLocation(loc) || rc.sensePassability(loc)) {
                count++;
            }
        }
        return count;
    }

    static MapLocation getCollectLocClosestTo(MapLocation crumbLoc, MapLocation loc) throws GameActionException {
        MapLocation currLoc = rc.getLocation();
        MapLocation bestCollect = null;
        double bestDist = Double.MAX_VALUE;
        MapLocation collectLoc;
        double dist;
        RobotInfo robot;

        for (int i = Direction.DIRECTION_ORDER.length; --i >= 0;) {
            collectLoc = crumbLoc.add(Direction.DIRECTION_ORDER[i]);
            if (!rc.canSenseLocation(collectLoc) ||
                    !rc.sensePassability(collectLoc) ||
                    !currLoc.isAdjacentTo(collectLoc))
                continue;
            robot = rc.senseRobotAtLocation(collectLoc);
            if (robot != null && robot.ID != rc.getID())
                continue;
            dist = collectLoc.distanceSquaredTo(loc);
            if (dist < bestDist) {
                bestCollect = collectLoc;
                bestDist = dist;
            }
        }

        return bestCollect;
    }

    static MapLocation moveTowardsMe(MapLocation loc) {
        Direction dirToMe = loc.directionTo(rc.getLocation());
        return loc.add(dirToMe);
    }

    static int clip(int n, int lo, int hi) {
        return Math.min(Math.max(n, lo), hi);
    }

    static double clip(double n, double lo, double hi) {
        return Math.min(Math.max(n, lo), hi);
    }

    static MapLocation clipToWithinMap(MapLocation loc) {
        return new MapLocation(clip(loc.x, 0, MAP_WIDTH - 1), clip(loc.y, 0, MAP_HEIGHT - 1));
    }

    public static boolean onTheMap(MapLocation location) {
        return 0 <= location.x && location.x < MAP_WIDTH &&
                0 <= location.y && location.y < MAP_HEIGHT;
    }

    public static boolean onEdgeOfMap(MapLocation location) {
        return location.x == 0 || location.x == MAP_WIDTH - 1 ||
                location.y == 0 || location.y == MAP_HEIGHT - 1;
    }

    static Direction randomDirection() {
        return directions[Util.rng.nextInt(Util.directions.length)];
    }

    static Direction randomDirection(Direction[] newDirections) {
        return newDirections[Util.rng.nextInt(newDirections.length)];
    }

    static Direction[] getInOrderDirections(Direction target_dir) {
        return new Direction[] { target_dir, target_dir.rotateRight(), target_dir.rotateLeft(),
                target_dir.rotateRight().rotateRight(), target_dir.rotateLeft().rotateLeft() };
    }

    static Direction[] getInOrderDirections(Direction target_dir, boolean rotateRightFirst) {
        if (rotateRightFirst) {
            return new Direction[] { target_dir, target_dir.rotateRight(), target_dir.rotateLeft(),
                    target_dir.rotateRight().rotateRight(), target_dir.rotateLeft().rotateLeft() };
        } else {
            return new Direction[] { target_dir, target_dir.rotateLeft(), target_dir.rotateRight(),
                    target_dir.rotateLeft().rotateLeft(), target_dir.rotateRight().rotateRight() };
        }
    }

    static Direction getFirstValidInOrderDirection(Direction dir) {
        for (Direction newDir : Util.getInOrderDirections(dir)) {
            if (rc.canMove(newDir)) {
                return newDir;
            }
        }
        return Direction.CENTER;
    }

    static Direction getFirstValidInOrderDirection(Direction dir, boolean ignoreWater) throws GameActionException {
        for (Direction newDir : Util.getInOrderDirections(dir)) {
            if (rc.canMove(newDir)) {
                return newDir;
            } else if (ignoreWater) {
                MapLocation adjLoc = rc.adjacentLocation(dir);
                if (rc.senseMapInfo(adjLoc).isWater()) {
                    return newDir;
                }
            }
        }
        return Direction.CENTER;
    }

    static Direction getFirstMoveableDir(Direction[] dirs) {
        for (Direction dir : dirs) {
            if (rc.canMove(dir)) {
                return dir;
            }
        }
        return Direction.CENTER;
    }

    static boolean isDirAdj(Direction dir, Direction dir2) {
        switch (dir) {
            case NORTH:
                switch (dir2) {
                    case NORTH:
                    case NORTHEAST:
                    case NORTHWEST:
                        return true;
                    default:
                        return false;
                }
            case NORTHEAST:
                switch (dir2) {
                    case NORTH:
                    case NORTHEAST:
                    case EAST:
                        return true;
                    default:
                        return false;
                }
            case EAST:
                switch (dir2) {
                    case NORTHEAST:
                    case EAST:
                    case SOUTHEAST:
                        return true;
                    default:
                        return false;
                }
            case SOUTHEAST:
                switch (dir2) {
                    case EAST:
                    case SOUTHEAST:
                    case SOUTH:
                        return true;
                    default:
                        return false;
                }
            case SOUTH:
                switch (dir2) {
                    case SOUTHEAST:
                    case SOUTH:
                    case SOUTHWEST:
                        return true;
                    default:
                        return false;
                }
            case SOUTHWEST:
                switch (dir2) {
                    case SOUTH:
                    case SOUTHWEST:
                    case WEST:
                        return true;
                    default:
                        return false;
                }
            case WEST:
                switch (dir2) {
                    case SOUTHWEST:
                    case WEST:
                    case NORTHWEST:
                        return true;
                    default:
                        return false;
                }
            case NORTHWEST:
                switch (dir2) {
                    case WEST:
                    case NORTHWEST:
                    case NORTH:
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }
    }

    static boolean seesObstacleInWay(MapLocation target) throws GameActionException {
        MapLocation loc = rc.getLocation();
        Direction dir = loc.directionTo(target);
        MapLocation currLoc = loc;
        while (currLoc.distanceSquaredTo(target) > 2) {
            currLoc = currLoc.add(dir);
            if (!rc.canSenseLocation(currLoc) || !rc.sensePassability(currLoc))
                return true;
        }
        return false;
    }

    static int getValidSymmetries() throws GameActionException {
        return Comms.readSymmetryAll();
    }

    static MapLocation getClosestLoc(MapLocation[] locs) throws GameActionException {
        MapLocation currLoc = rc.getLocation();
        MapLocation bestLoc = null;
        int bestDist = Integer.MAX_VALUE;
        int dist;
        for (MapLocation loc : locs) {
            dist = currLoc.distanceSquaredTo(loc);
            if (dist < bestDist) {
                bestLoc = loc;
                bestDist = dist;
            }
        }
        return bestLoc;
    }

    static boolean isDamLoc(MapInfo info) throws GameActionException {
        return !info.isPassable() && !info.isWall() && !info.isWater();
    }
}
