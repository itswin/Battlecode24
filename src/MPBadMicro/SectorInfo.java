package MPBadMicro;

import MPBadMicro.fast.*;
import battlecode.common.*;

public class SectorInfo {
    private int idx;
    private MapLocation center;
    private MapLocation[] corners;
    private boolean[] visitedCorners;

    private boolean found;
    private int enemies;
    private int lastRoundEnemyAdded;

    private int lastRoundVisited;
    private static final int VISITED_TIMEOUT = 50;

    private boolean controlStatusSet[];

    private static final int MAX_SECTOR_AREA = 36;

    public SectorInfo(int index) {
        idx = index;
        center = Robot.sectorCenters[idx];
        corners = new MapLocation[4];
        visitedCorners = new boolean[4];
        int secWidth = Robot.sectorWidths[idx % (Robot.sectorWidthsLength)];
        int secHeight = Robot.sectorHeights[idx / (Robot.sectorWidthsLength)];
        int leftX = center.x - secWidth / 2;
        int rightX = center.x - secWidth / 2 + secWidth - 1;
        int topY = center.y - secHeight / 2;
        int bottomY = center.y - secHeight / 2 + secHeight - 1;
        corners[0] = new MapLocation(leftX, topY);
        corners[1] = new MapLocation(rightX, topY);
        corners[2] = new MapLocation(leftX, bottomY);
        corners[3] = new MapLocation(rightX, bottomY);
        found = false;
        enemies = 0;
        lastRoundEnemyAdded = 0;
        controlStatusSet = new boolean[Comms.ControlStatus.NUM_CONTROL_STATUS];
        lastRoundVisited = Integer.MIN_VALUE;
    }

    public boolean hasReports() {
        return found;
    }

    public void addFlag() {
        found = true;
        setControlStatus(Comms.ControlStatus.ENEMY_FLAG);
    }

    public void addEnemy(int cStatus) {
        found = true;
        enemies++;
        setControlStatus(cStatus);
        lastRoundEnemyAdded = Robot.rc.getRoundNum();
    }

    public int getControlStatus() {
        boolean isEnemyInfoStale = Robot.rc.getRoundNum() > lastRoundEnemyAdded + Util.ENEMY_INFO_STALE_TIMEOUT;
        if (controlStatusSet[Comms.ControlStatus.ENEMY_FLAG]) {
            return Comms.ControlStatus.ENEMY_FLAG;
        } else if (controlStatusSet[Comms.ControlStatus.ENEMY_SPAWN_LOC]) {
            return Comms.ControlStatus.ENEMY_SPAWN_LOC;
        } else if (controlStatusSet[Comms.ControlStatus.ENEMY] &&
                !isEnemyInfoStale) {
            return Comms.ControlStatus.ENEMY;
        } else if (controlStatusSet[Comms.ControlStatus.EMPTY]) {
            return Comms.ControlStatus.EMPTY;
        } else if (controlStatusSet[Comms.ControlStatus.EXPLORING]) {
            return Comms.ControlStatus.EXPLORING;
        } else {
            return Comms.ControlStatus.UNKNOWN;
        }
    }

    public void setControlStatus(int cStatus) {
        found = true;
        controlStatusSet[cStatus] = true;
    }

    public void resetControlStatus() {
        found = true;
        controlStatusSet = new boolean[Comms.ControlStatus.NUM_CONTROL_STATUS];
    }

    public void exploreSector() {
        lastRoundVisited = Robot.rc.getRoundNum();
        found = true;
        setControlStatus(Comms.ControlStatus.UNKNOWN);
    }

    public boolean hasVisited() {
        return lastRoundVisited >= 0;
    }

    public boolean hasVisitedRecently() {
        return Robot.rc.getRoundNum() < lastRoundVisited + VISITED_TIMEOUT;
    }

    public int numEnemies() {
        return enemies;
    }

    public void reset() {
        enemies = 0;
        lastRoundEnemyAdded = 0;
        found = false;
        controlStatusSet = new boolean[Comms.ControlStatus.NUM_CONTROL_STATUS];
    }

    public MapLocation getNextCorner() {
        for (int i = 0; i < 4; i++) {
            if (!visitedCorners[i]) {
                return corners[i];
            }
        }
        return null;
    }

    public void visitCorner(MapLocation corner) {
        for (int i = 0; i < 4; i++) {
            if (corners[i].equals(corner)) {
                visitedCorners[i] = true;
            }
        }
    }

    public void resetCorners() {
        visitedCorners = new boolean[4];
    }
}
