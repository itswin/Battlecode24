package MPWorking;

import battlecode.common.*;
import MPWorking.Util.*;
import MPWorking.Comms.*;
import MPWorking.Debug.*;

import MPWorking.fast.*;

public class Robot {
    static boolean spawned = false;
    static int unitNum = 0;

    static RobotController rc;
    static int turnCount;
    static int roundNum;

    static int homeIdx;
    static MapLocation home;
    static MapLocation[] spawnLocations;
    static RobotInfo[] enemies;
    static RobotInfo[] attackableEnemies;
    static RobotInfo[] allies;
    static MapLocation currLoc;

    static int symmetryAll;
    static MapLocation[] enemySpawnLocs;
    static boolean symmetryChanged;

    static int actionRadiusSquared;
    static int visionRadiusSquared;

    static RobotInfo maybeDuck = null;

    static Team team;
    static Team opponent;

    static SectorDatabase sectorDatabase;

    // Sectors are 6x6, 5x6, 6x5, or 5x5
    static int numSectors;
    static int[] sectorHeights;
    static int[] sectorWidths;
    static int sectorWidthsLength;
    static float xStep;
    static float yStep;
    static int[] whichXLoc;
    static int[] whichYLoc;
    static MapLocation[] sectorCenters;
    static int[] sectorResources;
    static int[] sectorControls;
    static int[] markedSectorsBuffer;
    static int[] sectorPermutation;
    static int sectorToReport;

    static boolean exploreMode;

    static FastLocSet seenSymmetricLocs;
    static boolean goingToSymLoc;
    static MapLocation exploreTarget;

    static int turnsFollowedExploreTarget = 0;
    static int EXPLORE_TARGET_TIMEOUT = GameConstants.GAME_MAX_NUMBER_OF_ROUNDS;
    static final int EXPLORE_TIMEOUT_MULT = 10;

    // static int numFriendlyLaunchers;
    static RobotInfo closestEnemy;
    // static int numAggressiveFriendlies;
    // static int numFriendlies;
    // static int numCloseFriendlies;
    // static int numEnemies;
    // static MapLocation closestAttackingEnemy;
    static MapLocation lastClosestEnemy;
    static int turnSawLastClosestAttackingEnemy;
    // static int enemyAttackingHealth;
    // static int lastEnemyAttackingHealth;
    // static int friendlyAttackingHealth;
    // static int numEnemyLaunchersAttackingUs;
    public static final int LAST_ATTACKING_ENEMY_TIMEOUT = 2;
    public static final int WAITING_TIMEOUT = 4;

    static FastLocSet emptySymLocs;

    static final int MIN_BC_TO_FLUSH_SECTOR_DB = 1500;
    static final int BC_TO_WRITE_SECTOR = 150;
    static final int MIN_BC_TO_FLUSH = 1200;

    static FastIntIntMap combatSectorToTurnWritten;

    public Robot(RobotController r) throws GameActionException {
        rc = r;
        turnCount = 0;
        actionRadiusSquared = GameConstants.ATTACK_RADIUS_SQUARED;
        visionRadiusSquared = GameConstants.VISION_RADIUS_SQUARED;
        team = rc.getTeam();
        opponent = team.opponent();

        // TODO
        home = rc.getLocation();

        // homeIdx = 0;
        // for (int i = 0; i < 4; i++) {
        // if (Comms.readOurHqLocation(i).equals(home)) {
        // homeIdx = i;
        // break;
        // }
        // }

        symmetryAll = -1;
        enemySpawnLocs = new MapLocation[0];

        setupSectors();
        precomputeSectorCenters();
        sectorResources = new int[numSectors];
        sectorControls = new int[numSectors];
        markedSectorsBuffer = new int[numSectors];
        sectorToReport = 0;

        sectorDatabase = new SectorDatabase(numSectors);

        // Precompute math for whichSector
        whichXLoc = new int[Util.MAP_WIDTH];
        whichYLoc = new int[Util.MAP_HEIGHT];
        for (int i = Util.MAP_WIDTH; --i >= 0;) {
            whichXLoc[i] = (int) (i / xStep);
        }
        for (int i = Util.MAP_HEIGHT; --i >= 0;) {
            whichYLoc[i] = (int) (i / yStep) * sectorWidths.length;
        }

        exploreMode = false;

        symmetryChanged = false;
        emptySymLocs = new FastLocSet();
        turnSawLastClosestAttackingEnemy = -50;
        combatSectorToTurnWritten = new FastIntIntMap();
        seenSymmetricLocs = new FastLocSet();
        spawnLocations = null;
    }

    public void initTurn() throws GameActionException {
        Pathfinding.initTurn();
    }

    public boolean isSmallMap() {
        return Util.MAP_AREA <= Util.MAX_AREA_FOR_FAST_INIT;
    }

    public boolean isSemiSmallMap() {
        return Util.MAP_AREA <= Util.MAX_AREA_FOR_SEMI_FAST_INIT;
    }

    public boolean takeTurn() throws GameActionException {
        turnCount += 1;
        if (!rc.isSpawned()) {
            MapLocation[] spawnLocs = rc.getAllySpawnLocations();
            // Pick a random spawn location to attempt spawning in.
            MapLocation randomLoc = spawnLocs[FastMath.nextInt(spawnLocs.length)];
            if (rc.canSpawn(randomLoc)) {
                rc.spawn(randomLoc);
                Explore.init(rc);

                if (!spawned) {
                    spawned = true;
                    Explore.assignExplore3Dir(Explore.EXPLORE_DIRECTIONS[unitNum % Explore.EXPLORE_DIRECTIONS.length]);
                    writeFlagLocs();
                }
            } else {
                return false;
            }
        }

        doUnitNumSpecificTasks();
        loadSpawnLocations();

        roundNum = rc.getRoundNum();
        enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        allies = rc.senseNearbyRobots(-1, rc.getTeam());
        currLoc = rc.getLocation();
        Debug.setIndicatorDot(Debug.INDICATORS, home, 0, 255, 0);
        setSectorStates();
        resetLocalEnemyInformation();

        // Must recalculate
        int currSymmetryAll = Util.getValidSymmetries();
        if (currSymmetryAll != symmetryAll) {
            symmetryChanged = true;
            updateSymmetryLocs();
        } else {
            symmetryChanged = false;
        }

        return true;
    }

    public void doUnitNumSpecificTasks() throws GameActionException {
        doUnitOneTasks();
        clearOldEnemyInfo();
        setPrioritySectors();
    }

    public void doUnitOneTasks() throws GameActionException {
        if (unitNum != 1)
            return;
        buyUpgrades();
        setInitialExploreSectors();
        Comms.initComms();
        displayCombatSectors();
    }

    /**
     * Sets initial explore sectors to center and symmetry locs
     */
    public void setInitialExploreSectors() throws GameActionException {
        if (rc.getRoundNum() != 2)
            return;

        int exploreSectorIndex = getNextEmptyExploreSectorIdx(0);

        // Add the center and the all the symmetry locations
        MapLocation center = new MapLocation(Util.MAP_WIDTH / 2, Util.MAP_HEIGHT / 2);
        int[] sectors = new int[enemySpawnLocs.length + 1];
        sectors[0] = whichSector(center);
        for (int i = 0; i < enemySpawnLocs.length; i++)
            sectors[i + 1] = whichSector(enemySpawnLocs[i]);

        for (int i = 0; i < enemySpawnLocs.length + 1; i++) {
            int sector = sectors[i];
            int controlStatus = Comms.readSectorControlStatus(sector);

            if (exploreSectorIndex < Comms.EXPLORE_SECTOR_SLOTS
                    && controlStatus == Comms.ControlStatus.UNKNOWN) {
                Comms.writeExploreSectorIndex(exploreSectorIndex, sector);
                Comms.writeExploreSectorClaimStatus(exploreSectorIndex, Comms.ClaimStatus.UNCLAIMED);
                Comms.writeSectorControlStatus(sector, Comms.ControlStatus.EXPLORING);
                // Debug.println("Added explore sector: " + sectorCenters[sector] + " at index:
                // " + exploreSectorIndex);
                exploreSectorIndex = getNextEmptyExploreSectorIdx(exploreSectorIndex + 1);
            }
        }
    }

    /**
     * Clears old ENEMY_PASSIVE or ENEMY_AGGRESIVE information from sectors
     * every Util.CLEAR_ENEMY_INFO_PERIOD rounds
     */
    public void clearOldEnemyInfo() throws GameActionException {
        // Ducks 2-4 clear old enemy info
        if (unitNum == 1 || unitNum > 4)
            return;

        for (int sectorIdx = rc.getRoundNum()
                % Util.CLEAR_ENEMY_INFO_PERIOD; sectorIdx < numSectors; sectorIdx += Util.CLEAR_ENEMY_INFO_PERIOD) {
            int controlStatus = Comms.readSectorControlStatus(sectorIdx);
            if (controlStatus == Comms.ControlStatus.ENEMY) {
                // Mark old combat sectors as need to be explored.
                // Debug.println("Clearing enemy info sector at : " + sectorCenters[sectorIdx]);
                Comms.writeSectorControlStatus(sectorIdx, Comms.ControlStatus.EXPLORING);
            }
        }

        // Clear old combat sectors if they have no enemy info
        int[] combatSectorsWritten = combatSectorToTurnWritten.getKeys();
        for (int combatSectorIdx : combatSectorsWritten) {
            int turn = combatSectorToTurnWritten.getVal(combatSectorIdx);
            int sectorIdx = Comms.readCombatSectorIndex(combatSectorIdx);
            if (Comms.isEnemyControlStatus(Comms.readSectorControlStatus(sectorIdx))) {
                combatSectorToTurnWritten.addReplace(combatSectorIdx, rc.getRoundNum());
            } else if (turn + Util.ENEMY_INFO_STALE_TIMEOUT < rc.getRoundNum()) {
                Comms.writeCombatSectorIndex(combatSectorIdx, Comms.UNDEFINED_SECTOR_INDEX);
                combatSectorToTurnWritten.remove(combatSectorIdx);
                // Debug.println("Clearing combat sector at : " +
                // sectorCenters[combatSectorIdx]);
            }
        }
    }

    /**
     * Sets the priority sectors list
     * 
     * @throws GameActionException
     */
    public void setPrioritySectors() throws GameActionException {
        // Sectors aren't initialized until round 2
        if (rc.getRoundNum() == 1)
            return;

        // Ducks 2-4 set priority sectors
        if (unitNum == 1 || unitNum > 4) {
            return;
        }

        int combatSectorIndex = getNextEmptyCombatSectorIdx(0);
        int exploreSectorIndex = getNextEmptyExploreSectorIdx(0);

        // Alternate sweeping each half of the sectors every turn
        int mode = (unitNum + rc.getRoundNum()) % 3;
        int startIdx = 0;
        int endIdx = 0;
        switch (mode) {
            case 0:
                startIdx = 0;
                endIdx = numSectors / 3;
                break;
            case 1:
                startIdx = numSectors / 3;
                endIdx = numSectors * 2 / 3;
                break;
            case 2:
                startIdx = numSectors * 2 / 3;
                endIdx = numSectors;
                break;
            default:
                Debug.println("[Error] Unexpected case in setPriorityQueue!");
        }

        for (int i = startIdx; i < endIdx; i++) {
            int controlStatus = Comms.readSectorControlStatus(i);
            // Combat sector
            combatSector: if (combatSectorIndex < Comms.COMBAT_SECTOR_SLOTS
                    && Comms.isEnemyControlStatus(controlStatus)) {
                // If the sector is already a combat sector, don't add it again
                for (int j = Comms.COMBAT_SECTOR_SLOTS - 1; j >= 0; j--) {
                    if (Comms.readCombatSectorIndex(j) == i) {
                        break combatSector;
                    }
                }

                Comms.writeCombatSectorIndex(combatSectorIndex, i);
                Comms.writeCombatSectorClaimStatus(combatSectorIndex, Comms.ClaimStatus.UNCLAIMED);
                combatSectorToTurnWritten.add(combatSectorIndex, rc.getRoundNum());
                // Comms.writeCombatSectorTurn(combatSectorIndex, rc.getRoundNum());
                combatSectorIndex = getNextEmptyCombatSectorIdx(combatSectorIndex + 1);
            }
            // Explore sector
            exploreSector: if (exploreSectorIndex < Comms.EXPLORE_SECTOR_SLOTS
                    && controlStatus == Comms.ControlStatus.EXPLORING) {
                // If the sector is already a explore sector, don't add it again
                for (int j = Comms.EXPLORE_SECTOR_SLOTS - 1; j >= 0; j--) {
                    if (Comms.readExploreSectorIndex(j) == i) {
                        break exploreSector;
                    }
                }
                Comms.writeExploreSectorIndex(exploreSectorIndex, i);
                Comms.writeExploreSectorClaimStatus(exploreSectorIndex, Comms.ClaimStatus.UNCLAIMED);
                exploreSectorIndex = getNextEmptyExploreSectorIdx(exploreSectorIndex + 1);
            }
        }
    }

    public void printFlagLocs() throws GameActionException {
        for (int i = 0; i < Comms.OUR_FLAG_SLOTS; i++) {
            MapLocation flagLoc = Comms.readOurFlagLocation(i);
            if (rc.onTheMap(flagLoc)) {
                Debug.println("Our flag (" + i + "): " + flagLoc);
                Debug.setIndicatorDot(Debug.INDICATORS, flagLoc, 0, 255, 0);
            }
        }
    }

    public void printEnemyFlagLocs() throws GameActionException {
        for (int i = 0; i < Comms.ENEMY_FLAG_SLOTS; i++) {
            MapLocation flagLoc = Comms.readEnemyFlagLocation(i);
            if (rc.onTheMap(flagLoc)) {
                Debug.println("Enemy flag (" + i + "): " + flagLoc);
                Debug.setIndicatorDot(Debug.INDICATORS, flagLoc, 0, 255, 0);
            }
        }
    }

    public int getNextOurFlagIdx(MapLocation loc) throws GameActionException {
        for (int i = 0; i < Comms.OUR_FLAG_SLOTS; i++) {
            MapLocation flagLoc = Comms.readOurFlagLocation(i);
            if (!rc.onTheMap(flagLoc)) {
                return i;
            } else if (flagLoc.equals(loc)) {
                return -1;
            }
        }
        return -1;
    }

    public int getNextEnemyFlagIdx(MapLocation loc) throws GameActionException {
        for (int i = 0; i < Comms.ENEMY_FLAG_SLOTS; i++) {
            MapLocation flagLoc = Comms.readEnemyFlagLocation(i);
            if (!rc.onTheMap(flagLoc)) {
                return i;
            } else if (flagLoc.equals(loc)) {
                return -1;
            }
        }
        return -1;
    }

    public void writeFlagLocs() throws GameActionException {
        FlagInfo[] flags = rc.senseNearbyFlags(-1, team);
        for (FlagInfo flag : flags) {
            int idx = getNextOurFlagIdx(flag.getLocation());
            if (idx != -1) {
                Comms.writeOurFlagLocation(idx, flag.getLocation());
                Debug.println("Writing our flag (" + idx + "): " + flag.getLocation());
                return;
            }
        }
    }

    public void buyUpgrades() throws GameActionException {
        if (rc.canBuyGlobal(GlobalUpgrade.ACTION)) {
            rc.buyGlobal(GlobalUpgrade.ACTION);
        } else if (rc.canBuyGlobal(GlobalUpgrade.HEALING)) {
            rc.buyGlobal(GlobalUpgrade.HEALING);
        }
    }

    public void endTurn() throws GameActionException {
        MapTracker.initialize();
        flushSectorDatabase();
        MapTracker.markSeen();
    }

    public void loadSpawnLocations() throws GameActionException {
        // Do not load this on round 1.
        if (rc.getRoundNum() == 1 || spawnLocations != null)
            return;

        // Debug.println("Loading spawn locations");
        // Central spawn locations are just the initial flag locations
        spawnLocations = new MapLocation[] {
                Comms.readOurFlagLocation(0),
                Comms.readOurFlagLocation(1),
                Comms.readOurFlagLocation(2),
        };
    }

    /*
     * Prioritizes miners in general.
     * Unless you're close enough to an Archon, then prioritize soldiers.
     */
    public RobotInfo getBestEnemy() throws GameActionException {
        return getBestEnemy(rc.senseNearbyRobots(actionRadiusSquared, rc.getTeam().opponent()));
    }

    /*
     * Prioritizes attacking enemies in the given order.
     * Prioritizes attacking lower health enemies.
     */
    public void loadEnemies(RobotInfo[] enemies) {
        maybeDuck = null;
        RobotInfo enemy;
        for (int i = enemies.length - 1; i >= 0; i--) {
            enemy = enemies[i];
            if (maybeDuck == null || maybeDuck.health > enemy.health) {
                maybeDuck = enemy;
            }
        }
    }

    public RobotInfo getBestEnemy(RobotInfo[] sensable) throws GameActionException {
        loadEnemies(sensable);

        RobotInfo res = null;

        // Prioritize these the least
        if (maybeDuck != null)
            res = maybeDuck;

        return res;
    }

    public boolean isEnemyBetter(RobotInfo robot1, RobotInfo robot2) throws GameActionException {
        if (robot1 == null)
            return false;
        if (robot2 == null)
            return true;

        return robot1.health < robot2.health;
    }

    public RobotInfo getClosestRobot(RobotInfo[] robots) {
        RobotInfo robot;
        RobotInfo closestRobot = null;
        int leastDistance = Integer.MAX_VALUE;
        int currDistance;

        for (int i = robots.length - 1; i >= 0; i--) {
            robot = robots[i];
            currDistance = robot.getLocation().distanceSquaredTo(currLoc);
            if (leastDistance > currDistance) {
                leastDistance = currDistance;
                closestRobot = robot;
            }
        }

        return closestRobot;
    }

    public boolean tryAttackBestEnemy(RobotInfo bestEnemy) throws GameActionException {
        if (bestEnemy != null) {
            if (rc.canAttack(bestEnemy.getLocation())) {
                rc.attack(bestEnemy.getLocation());
                // Debug.printString("Attacking: " + bestEnemy.getLocation().toString());
                return true;
            } else {
                // Debug.printString("Enemy: " + bestEnemy.getLocation().toString());
            }
        }

        return false;
    }

    public boolean tryAttackBestEnemy() throws GameActionException {
        return tryAttackBestEnemy(getBestEnemy());
    }

    static boolean tryMoveDest(Direction[] target_dir) throws GameActionException {
        // Debug.println(Debug.info, "Dest direction: " + dir);
        for (Direction dir : target_dir) {
            if (rc.canMove(dir)) {
                rc.move(dir);
                return true;
            }
        }
        return false;
    }

    public void flushSectorDatabase() throws GameActionException {
        if (Clock.getBytecodesLeft() < MIN_BC_TO_FLUSH_SECTOR_DB)
            return;

        Comms.initBufferPool();
        int numSectorsReported = 0;
        final int MAX_SECTORS_REPORTED = 50;

        while (sectorToReport < numSectors &&
                numSectorsReported < MAX_SECTORS_REPORTED &&
                Clock.getBytecodesLeft() > numSectorsReported * BC_TO_WRITE_SECTOR + MIN_BC_TO_FLUSH) {
            SectorInfo entry = sectorDatabase.at(sectorToReport);
            if (entry.hasReports()) {
                int oldControlStatus = Comms.readSectorControlStatus(sectorToReport);
                int newControlStatus = Comms.pickControlStatus(entry.getControlStatus(),
                        oldControlStatus);
                Comms.writeBPSectorControlStatus(sectorToReport, newControlStatus);
                entry.reset();
                numSectorsReported++;
            }

            sectorToReport++;
        }

        if (sectorToReport == numSectors)
            sectorToReport = 0;

        Comms.flushBufferPool();
    }

    public void setupSectors() {
        sectorHeights = computeSectorSizes(Util.MAP_HEIGHT);
        sectorWidths = computeSectorSizes(Util.MAP_WIDTH);
        sectorWidthsLength = sectorWidths.length;
        numSectors = sectorHeights.length * sectorWidthsLength;
        yStep = Util.MAP_HEIGHT / ((float) sectorHeights.length);
        xStep = Util.MAP_WIDTH / ((float) sectorWidths.length);
    }

    /**
     * Precompute x, y coordinates of centers of all sectors
     */
    public void precomputeSectorCenters() {
        sectorCenters = new MapLocation[numSectors];
        int yCenter = 0;
        int i;
        int idx = 0;
        int[] xCenters = computeSectorCenters(Util.MAP_WIDTH);
        int[] yCenters = computeSectorCenters(Util.MAP_HEIGHT);
        for (int j = sectorHeights.length; --j >= 0;) {
            idx = j * sectorWidths.length + sectorWidths.length - 1;
            yCenter = yCenters[j];
            for (i = sectorWidths.length; --i >= 0; idx--) {
                sectorCenters[idx] = new MapLocation(xCenters[i], yCenter);
            }
        }
    }

    /**
     * Returns sector given a location.
     * 
     * NOTE: THIS FUNCTION IS ONLY FOR REFERENCE. IF CALLED FREQUENTLY, INLINE THIS
     * FUNCTION!!
     * 
     * @param loc
     * @return
     */
    public int whichSector(MapLocation loc) {
        return whichXLoc[loc.x] + whichYLoc[loc.y];
    }

    public void initSectorPermutation() {
        switch (sectorWidths.length) {
            case 4:
                switch (sectorHeights.length) {
                    case 4:
                        sectorPermutation = new int[] { 10, 5, 3, 12, 0, 15, 1, 7, 13, 9, 6, 4, 11, 14, 2, 8 };
                        break;
                    case 5:
                        sectorPermutation = new int[] { 10, 9, 3, 16, 0, 19, 6, 5, 18, 13, 17, 11, 2, 1, 4, 15, 12, 8,
                                7, 14 };
                        break;
                    case 6:
                        sectorPermutation = new int[] { 14, 9, 3, 20, 0, 23, 2, 7, 1, 12, 11, 22, 8, 16, 21, 15, 13,
                                17, 4, 19, 18, 5, 10, 6 };
                        break;
                    case 7:
                        sectorPermutation = new int[] { 14, 13, 3, 24, 0, 27, 9, 15, 22, 21, 26, 6, 18, 10, 20, 19, 4,
                                2, 1, 23, 11, 12, 16, 8, 17, 7, 5, 25 };
                        break;
                    case 8:
                        sectorPermutation = new int[] { 18, 13, 3, 28, 0, 31, 14, 27, 30, 23, 5, 20, 12, 2, 17, 21, 19,
                                29, 7, 24, 4, 15, 26, 6, 25, 9, 22, 10, 8, 1, 11, 16 };
                        break;
                    case 9:
                        sectorPermutation = new int[] { 18, 17, 3, 32, 0, 35, 31, 28, 24, 10, 4, 8, 13, 25, 5, 7, 12,
                                16, 30, 20, 23, 1, 22, 33, 6, 21, 14, 34, 11, 29, 26, 15, 19, 27, 2, 9 };
                        break;
                    case 10:
                        sectorPermutation = new int[] { 22, 17, 3, 36, 0, 39, 34, 29, 6, 23, 16, 13, 4, 20, 1, 14, 30,
                                27, 33, 37, 38, 2, 28, 31, 24, 10, 25, 12, 7, 15, 26, 18, 5, 9, 21, 11, 35, 8, 32, 19 };
                        break;
                }
                break;
            case 5:
                switch (sectorHeights.length) {
                    case 4:
                        sectorPermutation = new int[] { 12, 7, 4, 15, 0, 19, 17, 2, 14, 6, 13, 8, 3, 10, 5, 11, 9, 1,
                                18, 16 };
                        break;
                    case 5:
                        sectorPermutation = new int[] { 12, 4, 20, 0, 24, 19, 18, 10, 23, 15, 2, 3, 5, 6, 8, 7, 14, 17,
                                21, 9, 11, 22, 1, 13, 16 };
                        break;
                    case 6:
                        sectorPermutation = new int[] { 17, 12, 4, 25, 0, 29, 9, 26, 27, 7, 16, 13, 1, 21, 10, 19, 20,
                                22, 15, 8, 23, 14, 5, 3, 11, 18, 6, 28, 2, 24 };
                        break;
                    case 7:
                        sectorPermutation = new int[] { 17, 4, 30, 0, 34, 12, 33, 32, 26, 10, 11, 5, 6, 23, 27, 8, 18,
                                15, 16, 21, 7, 2, 3, 20, 19, 28, 29, 25, 24, 31, 22, 1, 9, 14, 13 };
                        break;
                    case 8:
                        sectorPermutation = new int[] { 22, 17, 4, 35, 0, 39, 27, 11, 21, 10, 30, 7, 26, 23, 32, 38,
                                25, 3, 12, 8, 1, 34, 6, 31, 9, 14, 16, 5, 19, 13, 36, 24, 18, 20, 33, 37, 2, 29, 28,
                                15 };
                        break;
                    case 9:
                        sectorPermutation = new int[] { 22, 4, 40, 0, 44, 11, 20, 16, 41, 6, 27, 3, 1, 17, 37, 39, 9,
                                8, 15, 2, 21, 24, 35, 13, 5, 18, 38, 43, 14, 7, 19, 28, 30, 25, 26, 10, 31, 36, 12, 33,
                                29, 23, 34, 32, 42 };
                        break;
                    case 10:
                        sectorPermutation = new int[] { 27, 22, 4, 45, 0, 49, 5, 35, 14, 37, 15, 40, 13, 38, 24, 17,
                                26, 47, 39, 2, 3, 31, 6, 20, 11, 41, 30, 12, 29, 28, 23, 19, 7, 32, 8, 21, 44, 10, 33,
                                18, 34, 36, 1, 9, 42, 46, 25, 43, 16, 48 };
                        break;
                }
                break;
            case 6:
                switch (sectorHeights.length) {
                    case 4:
                        sectorPermutation = new int[] { 15, 8, 5, 18, 0, 23, 17, 10, 14, 9, 13, 11, 16, 21, 4, 22, 3,
                                12, 2, 6, 19, 7, 20, 1 };
                        break;
                    case 5:
                        sectorPermutation = new int[] { 15, 14, 5, 24, 0, 29, 22, 18, 3, 16, 13, 17, 20, 4, 2, 27, 28,
                                1, 12, 11, 21, 9, 19, 6, 23, 26, 8, 10, 25, 7 };
                        break;
                    case 6:
                        sectorPermutation = new int[] { 21, 14, 5, 30, 0, 35, 22, 26, 9, 16, 32, 17, 31, 2, 12, 19, 34,
                                13, 20, 1, 4, 15, 10, 24, 28, 18, 7, 23, 6, 11, 27, 25, 33, 3, 8, 29 };
                        break;
                    case 7:
                        sectorPermutation = new int[] { 21, 20, 5, 36, 0, 41, 7, 40, 29, 38, 30, 24, 33, 13, 22, 8, 10,
                                4, 3, 37, 26, 39, 25, 32, 17, 31, 11, 2, 18, 12, 9, 34, 6, 19, 27, 35, 23, 15, 28, 14,
                                1, 16 };
                        break;
                    case 8:
                        sectorPermutation = new int[] { 27, 20, 5, 42, 0, 47, 10, 3, 6, 18, 12, 23, 34, 4, 41, 24, 8,
                                45, 2, 14, 38, 31, 46, 44, 16, 39, 13, 32, 21, 40, 17, 43, 9, 36, 22, 15, 26, 30, 35,
                                25, 1, 19, 29, 7, 37, 28, 33, 11 };
                        break;
                    case 9:
                        sectorPermutation = new int[] { 27, 26, 5, 48, 0, 53, 49, 52, 20, 37, 1, 9, 31, 50, 36, 23, 10,
                                41, 38, 45, 46, 11, 16, 2, 12, 40, 8, 25, 3, 33, 44, 35, 19, 34, 29, 43, 14, 18, 4, 24,
                                32, 47, 6, 42, 13, 22, 15, 30, 28, 39, 17, 21, 51, 7 };
                        break;
                    case 10:
                        sectorPermutation = new int[] { 33, 26, 5, 54, 0, 59, 36, 40, 25, 28, 31, 12, 56, 38, 44, 57,
                                41, 52, 3, 27, 11, 35, 21, 16, 6, 46, 23, 45, 19, 8, 30, 24, 20, 50, 17, 9, 1, 4, 7, 49,
                                32, 48, 18, 47, 10, 42, 37, 51, 22, 43, 29, 55, 15, 53, 14, 39, 2, 34, 13, 58 };
                        break;
                }
                break;
            case 7:
                switch (sectorHeights.length) {
                    case 4:
                        sectorPermutation = new int[] { 17, 10, 6, 21, 0, 27, 19, 16, 14, 26, 8, 2, 22, 9, 25, 5, 18,
                                4, 11, 12, 3, 24, 20, 15, 7, 23, 13, 1 };
                        break;
                    case 5:
                        sectorPermutation = new int[] { 17, 6, 28, 0, 34, 11, 23, 31, 16, 30, 27, 32, 18, 9, 21, 4, 25,
                                29, 8, 10, 2, 7, 26, 3, 14, 20, 22, 33, 13, 12, 24, 15, 19, 5, 1 };
                        break;
                    case 6:
                        sectorPermutation = new int[] { 24, 17, 6, 35, 0, 41, 40, 21, 20, 39, 29, 10, 15, 13, 9, 31,
                                12, 16, 18, 34, 19, 32, 5, 36, 4, 22, 27, 37, 14, 33, 3, 8, 2, 28, 1, 26, 30, 7, 23, 11,
                                25, 38 };
                        break;
                    case 7:
                        sectorPermutation = new int[] { 24, 6, 42, 0, 48, 9, 21, 15, 33, 25, 43, 46, 10, 36, 28, 41,
                                45, 31, 20, 18, 14, 1, 16, 2, 37, 22, 12, 30, 32, 13, 35, 39, 44, 29, 27, 40, 5, 11, 17,
                                7, 3, 47, 8, 38, 34, 26, 4, 19, 23 };
                        break;
                    case 8:
                        sectorPermutation = new int[] { 31, 24, 6, 49, 0, 55, 32, 48, 36, 20, 11, 35, 18, 8, 45, 33,
                                28, 15, 10, 50, 22, 21, 39, 7, 4, 30, 26, 44, 16, 51, 12, 13, 19, 27, 17, 47, 41, 23,
                                40, 53, 52, 42, 34, 2, 9, 46, 43, 54, 37, 38, 5, 25, 3, 14, 1, 29 };
                        break;
                    case 9:
                        sectorPermutation = new int[] { 31, 6, 56, 0, 62, 43, 58, 10, 37, 32, 28, 11, 22, 57, 40, 49,
                                27, 14, 50, 4, 24, 53, 17, 41, 20, 3, 18, 39, 16, 30, 13, 45, 5, 2, 38, 42, 55, 60, 7,
                                46, 19, 51, 44, 1, 33, 21, 61, 34, 36, 48, 12, 9, 29, 52, 15, 47, 26, 35, 23, 54, 59, 8,
                                25 };
                        break;
                    case 10:
                        sectorPermutation = new int[] { 38, 31, 6, 63, 0, 69, 55, 62, 17, 49, 56, 21, 44, 3, 22, 19,
                                28, 39, 47, 46, 30, 12, 50, 40, 58, 53, 23, 57, 8, 65, 13, 52, 10, 24, 37, 32, 14, 54,
                                33, 51, 2, 67, 68, 5, 64, 11, 45, 48, 59, 9, 29, 34, 15, 60, 35, 43, 26, 27, 20, 25, 18,
                                36, 41, 42, 4, 66, 1, 16, 61, 7 };
                        break;
                }
                break;
            case 8:
                switch (sectorHeights.length) {
                    case 4:
                        sectorPermutation = new int[] { 20, 11, 7, 24, 0, 31, 5, 29, 4, 30, 25, 21, 8, 14, 19, 1, 27,
                                23, 12, 28, 16, 17, 6, 26, 2, 9, 22, 18, 15, 13, 3, 10 };
                        break;
                    case 5:
                        sectorPermutation = new int[] { 20, 19, 7, 32, 0, 39, 2, 37, 22, 26, 14, 11, 23, 35, 10, 8, 1,
                                24, 16, 30, 4, 3, 29, 13, 34, 5, 38, 25, 27, 33, 12, 17, 36, 15, 6, 18, 21, 31, 9, 28 };
                        break;
                    case 6:
                        sectorPermutation = new int[] { 28, 19, 7, 40, 0, 47, 2, 24, 38, 10, 46, 4, 5, 9, 20, 43, 34,
                                32, 42, 8, 36, 44, 13, 27, 22, 11, 17, 29, 37, 31, 25, 39, 18, 26, 15, 33, 3, 35, 6, 23,
                                14, 45, 16, 41, 12, 30, 21, 1 };
                        break;
                    case 7:
                        sectorPermutation = new int[] { 28, 27, 7, 48, 0, 55, 35, 5, 44, 4, 11, 47, 30, 38, 43, 24, 42,
                                37, 13, 52, 15, 41, 26, 32, 10, 12, 36, 40, 19, 49, 51, 17, 33, 53, 20, 3, 29, 31, 6, 8,
                                1, 34, 9, 14, 18, 21, 54, 25, 39, 23, 2, 50, 45, 16, 22, 46 };
                        break;
                    case 8:
                        sectorPermutation = new int[] { 36, 27, 7, 56, 0, 63, 15, 51, 1, 50, 54, 18, 22, 48, 9, 47, 4,
                                35, 2, 43, 34, 11, 38, 20, 55, 29, 62, 26, 32, 52, 28, 57, 49, 41, 6, 25, 21, 60, 61,
                                13, 8, 10, 23, 24, 12, 16, 42, 45, 53, 17, 33, 5, 3, 14, 46, 31, 39, 58, 37, 40, 59, 19,
                                30, 44 };
                        break;
                    case 9:
                        sectorPermutation = new int[] { 36, 35, 7, 64, 0, 71, 3, 67, 6, 45, 17, 19, 1, 52, 44, 5, 41,
                                10, 4, 47, 46, 40, 12, 42, 32, 50, 31, 63, 48, 43, 62, 54, 58, 16, 9, 66, 60, 26, 30,
                                37, 65, 23, 15, 59, 51, 28, 25, 29, 68, 18, 34, 57, 14, 24, 11, 27, 33, 38, 55, 22, 70,
                                56, 39, 61, 20, 8, 21, 2, 53, 69, 13, 49 };
                        break;
                    case 10:
                        sectorPermutation = new int[] { 44, 35, 7, 72, 0, 79, 67, 45, 31, 65, 38, 21, 51, 27, 61, 13,
                                50, 59, 9, 68, 40, 56, 12, 48, 3, 63, 36, 24, 77, 60, 17, 66, 41, 1, 71, 18, 55, 64, 78,
                                6, 30, 2, 39, 5, 28, 57, 76, 42, 62, 73, 4, 14, 22, 53, 11, 70, 16, 52, 58, 10, 33, 46,
                                25, 49, 15, 8, 32, 74, 23, 26, 19, 29, 75, 34, 37, 43, 69, 54, 47, 20 };
                        break;
                }
                break;
            case 9:
                switch (sectorHeights.length) {
                    case 4:
                        sectorPermutation = new int[] { 22, 13, 8, 27, 0, 35, 7, 4, 19, 25, 16, 9, 17, 31, 34, 30, 32,
                                20, 3, 1, 14, 12, 6, 26, 23, 2, 28, 18, 33, 5, 21, 10, 24, 11, 29, 15 };
                        break;
                    case 5:
                        sectorPermutation = new int[] { 22, 8, 36, 0, 44, 3, 30, 13, 5, 7, 32, 25, 38, 39, 41, 18, 20,
                                21, 4, 19, 24, 14, 40, 37, 29, 9, 31, 6, 12, 10, 15, 11, 23, 33, 27, 16, 28, 34, 43, 1,
                                26, 42, 35, 2, 17 };
                        break;
                    case 6:
                        sectorPermutation = new int[] { 31, 22, 8, 45, 0, 53, 36, 12, 47, 25, 39, 30, 7, 16, 28, 5, 6,
                                41, 18, 11, 51, 37, 34, 49, 2, 40, 23, 20, 38, 17, 13, 35, 24, 52, 42, 4, 26, 46, 3, 14,
                                10, 33, 15, 50, 27, 43, 9, 44, 1, 29, 19, 21, 48, 32 };
                        break;
                    case 7:
                        sectorPermutation = new int[] { 31, 8, 54, 0, 62, 25, 49, 22, 32, 5, 12, 55, 46, 34, 28, 33,
                                14, 4, 39, 6, 40, 10, 60, 42, 13, 26, 1, 7, 17, 47, 2, 20, 15, 58, 56, 3, 61, 30, 41,
                                59, 57, 38, 37, 48, 50, 45, 44, 43, 52, 18, 23, 11, 29, 51, 9, 24, 36, 21, 53, 27, 35,
                                16, 19 };
                        break;
                    case 8:
                        sectorPermutation = new int[] { 40, 31, 8, 63, 0, 71, 10, 65, 50, 4, 2, 23, 6, 35, 1, 66, 70,
                                15, 59, 62, 29, 13, 20, 57, 38, 43, 45, 51, 5, 21, 36, 32, 12, 60, 41, 28, 52, 19, 9,
                                68, 49, 61, 69, 42, 30, 18, 3, 54, 47, 34, 24, 17, 25, 58, 33, 27, 64, 37, 55, 7, 11,
                                39, 67, 44, 22, 16, 26, 53, 46, 48, 14, 56 };
                        break;
                    case 9:
                        sectorPermutation = new int[] { 40, 8, 72, 0, 80, 41, 33, 28, 43, 24, 50, 56, 26, 37, 48, 71,
                                20, 53, 3, 12, 70, 61, 75, 74, 23, 1, 68, 13, 78, 76, 79, 67, 51, 17, 55, 32, 14, 18,
                                29, 69, 42, 49, 25, 77, 52, 57, 46, 5, 35, 4, 6, 60, 47, 63, 7, 27, 65, 11, 39, 58, 31,
                                36, 9, 22, 54, 45, 19, 34, 2, 73, 59, 62, 16, 10, 66, 64, 44, 21, 38, 15, 30 };
                        break;
                    case 10:
                        sectorPermutation = new int[] { 49, 40, 8, 81, 0, 89, 10, 72, 76, 69, 75, 34, 32, 47, 82, 38,
                                22, 86, 25, 31, 48, 56, 13, 59, 19, 51, 87, 44, 79, 23, 77, 9, 60, 26, 57, 55, 88, 65,
                                80, 4, 35, 14, 39, 3, 53, 27, 70, 66, 52, 12, 83, 28, 2, 36, 50, 43, 42, 30, 6, 61, 41,
                                85, 21, 63, 5, 15, 11, 58, 16, 62, 73, 29, 74, 7, 45, 54, 20, 64, 68, 46, 1, 78, 67, 71,
                                33, 17, 24, 18, 84, 37 };
                        break;
                }
                break;
            case 10:
                switch (sectorHeights.length) {
                    case 4:
                        sectorPermutation = new int[] { 25, 14, 9, 30, 0, 39, 26, 1, 35, 16, 17, 21, 32, 12, 7, 34, 38,
                                31, 8, 28, 11, 18, 22, 13, 6, 24, 20, 23, 10, 27, 36, 5, 15, 3, 33, 2, 29, 37, 19, 4 };
                        break;
                    case 5:
                        sectorPermutation = new int[] { 25, 24, 9, 40, 0, 49, 35, 15, 14, 39, 20, 27, 43, 18, 48, 30,
                                2, 22, 3, 11, 46, 10, 28, 4, 41, 26, 12, 5, 19, 16, 8, 29, 13, 7, 23, 33, 32, 42, 17,
                                37, 6, 21, 38, 1, 45, 31, 44, 34, 36, 47 };
                        break;
                    case 6:
                        sectorPermutation = new int[] { 35, 24, 9, 50, 0, 59, 39, 12, 1, 41, 29, 37, 52, 38, 46, 42,
                                58, 13, 25, 48, 57, 10, 34, 8, 30, 21, 23, 3, 18, 31, 26, 44, 54, 49, 27, 5, 17, 55, 45,
                                15, 20, 51, 6, 47, 36, 19, 4, 28, 56, 32, 53, 43, 33, 11, 14, 7, 2, 40, 16, 22 };
                        break;
                    case 7:
                        sectorPermutation = new int[] { 35, 34, 9, 60, 0, 69, 44, 2, 61, 37, 25, 5, 31, 41, 12, 40, 22,
                                28, 36, 3, 49, 42, 27, 62, 53, 21, 14, 32, 46, 20, 1, 18, 16, 24, 45, 59, 39, 38, 50,
                                33, 4, 55, 63, 23, 30, 67, 54, 19, 17, 13, 15, 68, 65, 10, 8, 26, 57, 51, 52, 47, 43,
                                56, 11, 29, 6, 48, 66, 64, 7, 58 };
                        break;
                    case 8:
                        sectorPermutation = new int[] { 45, 34, 9, 70, 0, 79, 58, 2, 59, 33, 5, 77, 3, 12, 23, 56, 63,
                                35, 46, 60, 11, 66, 32, 6, 1, 16, 21, 18, 65, 64, 31, 43, 27, 52, 73, 26, 54, 39, 67,
                                15, 53, 61, 29, 75, 71, 24, 13, 47, 22, 76, 69, 4, 17, 36, 20, 38, 7, 74, 48, 28, 49,
                                30, 19, 44, 78, 37, 57, 68, 8, 10, 72, 50, 51, 62, 42, 55, 40, 25, 41, 14 };
                        break;
                    case 9:
                        sectorPermutation = new int[] { 45, 44, 9, 80, 0, 89, 39, 56, 88, 20, 7, 71, 33, 22, 14, 34,
                                41, 51, 1, 38, 67, 30, 68, 28, 21, 72, 18, 52, 69, 32, 57, 77, 86, 53, 60, 11, 17, 31,
                                46, 43, 74, 35, 48, 3, 87, 8, 42, 82, 36, 25, 10, 19, 84, 85, 29, 61, 23, 78, 81, 63,
                                75, 40, 4, 58, 26, 66, 2, 13, 65, 49, 12, 37, 76, 54, 73, 70, 79, 27, 15, 24, 62, 16,
                                50, 5, 55, 83, 47, 6, 59, 64 };
                        break;
                    case 10:
                        sectorPermutation = new int[] { 55, 44, 9, 90, 0, 99, 15, 29, 58, 19, 34, 24, 7, 13, 8, 97, 45,
                                78, 46, 40, 36, 67, 64, 50, 81, 95, 56, 3, 11, 37, 25, 80, 33, 59, 91, 42, 61, 4, 35,
                                88, 70, 47, 86, 49, 87, 94, 82, 6, 32, 53, 26, 51, 65, 5, 77, 89, 71, 92, 21, 18, 31, 1,
                                54, 22, 75, 85, 16, 63, 2, 20, 74, 17, 14, 48, 72, 93, 69, 68, 84, 76, 66, 52, 27, 10,
                                96, 60, 30, 79, 38, 57, 73, 39, 62, 83, 28, 43, 23, 98, 41, 12 };
                        break;
                }
                break;
        }
    }

    public int[] computeSectorSizes(int dim) {
        switch (dim) {
            case 20:
                return new int[] { 5, 5, 5, 5 };
            case 21:
                return new int[] { 5, 5, 5, 6 };
            case 22:
                return new int[] { 5, 6, 5, 6 };
            case 23:
                return new int[] { 5, 6, 6, 6 };
            case 24:
                return new int[] { 6, 6, 6, 6 };
            case 25:
                return new int[] { 5, 5, 5, 5, 5 };
            case 26:
                return new int[] { 5, 5, 5, 5, 6 };
            case 27:
                return new int[] { 5, 5, 6, 5, 6 };
            case 28:
                return new int[] { 5, 6, 5, 6, 6 };
            case 29:
                return new int[] { 5, 6, 6, 6, 6 };
            case 30:
                return new int[] { 6, 6, 6, 6, 6 };
            case 31:
                return new int[] { 5, 5, 5, 5, 5, 6 };
            case 32:
                return new int[] { 5, 5, 6, 5, 5, 6 };
            case 33:
                return new int[] { 5, 6, 5, 6, 5, 6 };
            case 34:
                return new int[] { 5, 6, 6, 5, 6, 6 };
            case 35:
                return new int[] { 5, 6, 6, 6, 6, 6 };
            case 36:
                return new int[] { 6, 6, 6, 6, 6, 6 };
            case 37:
                return new int[] { 5, 5, 5, 6, 5, 5, 6 };
            case 38:
                return new int[] { 5, 5, 6, 5, 6, 5, 6 };
            case 39:
                return new int[] { 5, 6, 5, 6, 5, 6, 6 };
            case 40:
                return new int[] { 5, 6, 6, 5, 6, 6, 6 };
            case 41:
                return new int[] { 5, 6, 6, 6, 6, 6, 6 };
            case 42:
                return new int[] { 6, 6, 6, 6, 6, 6, 6 };
            case 43:
                return new int[] { 5, 5, 6, 5, 5, 6, 5, 6 };
            case 44:
                return new int[] { 5, 6, 5, 6, 5, 6, 5, 6 };
            case 45:
                return new int[] { 5, 6, 5, 6, 6, 5, 6, 6 };
            case 46:
                return new int[] { 5, 6, 6, 6, 5, 6, 6, 6 };
            case 47:
                return new int[] { 5, 6, 6, 6, 6, 6, 6, 6 };
            case 48:
                return new int[] { 6, 6, 6, 6, 6, 6, 6, 6 };
            case 49:
                return new int[] { 5, 5, 6, 5, 6, 5, 6, 5, 6 };
            case 50:
                return new int[] { 5, 6, 5, 6, 5, 6, 5, 6, 6 };
            case 51:
                return new int[] { 5, 6, 6, 5, 6, 6, 5, 6, 6 };
            case 52:
                return new int[] { 5, 6, 6, 6, 5, 6, 6, 6, 6 };
            case 53:
                return new int[] { 5, 6, 6, 6, 6, 6, 6, 6, 6 };
            case 54:
                return new int[] { 6, 6, 6, 6, 6, 6, 6, 6, 6 };
            case 55:
                return new int[] { 5, 6, 5, 6, 5, 6, 5, 6, 5, 6 };
            case 56:
                return new int[] { 5, 6, 5, 6, 6, 5, 6, 5, 6, 6 };
            case 57:
                return new int[] { 5, 6, 6, 5, 6, 6, 5, 6, 6, 6 };
            case 58:
                return new int[] { 5, 6, 6, 6, 6, 5, 6, 6, 6, 6 };
            case 59:
                return new int[] { 5, 6, 6, 6, 6, 6, 6, 6, 6, 6 };
            case 60:
                return new int[] { 6, 6, 6, 6, 6, 6, 6, 6, 6, 6 };
            default:
                return new int[] {};
        }
    }

    public int[] computeSectorCenters(int dim) {
        switch (dim) {
            case 20:
                return new int[] { 2, 7, 12, 17 };
            case 21:
                return new int[] { 2, 7, 12, 18 };
            case 22:
                return new int[] { 2, 8, 13, 19 };
            case 23:
                return new int[] { 2, 8, 14, 20 };
            case 24:
                return new int[] { 3, 9, 15, 21 };
            case 25:
                return new int[] { 2, 7, 12, 17, 22 };
            case 26:
                return new int[] { 2, 7, 12, 17, 23 };
            case 27:
                return new int[] { 2, 7, 13, 18, 24 };
            case 28:
                return new int[] { 2, 8, 13, 19, 25 };
            case 29:
                return new int[] { 2, 8, 14, 20, 26 };
            case 30:
                return new int[] { 3, 9, 15, 21, 27 };
            case 31:
                return new int[] { 2, 7, 12, 17, 22, 28 };
            case 32:
                return new int[] { 2, 7, 13, 18, 23, 29 };
            case 33:
                return new int[] { 2, 8, 13, 19, 24, 30 };
            case 34:
                return new int[] { 2, 8, 14, 19, 25, 31 };
            case 35:
                return new int[] { 2, 8, 14, 20, 26, 32 };
            case 36:
                return new int[] { 3, 9, 15, 21, 27, 33 };
            case 37:
                return new int[] { 2, 7, 12, 18, 23, 28, 34 };
            case 38:
                return new int[] { 2, 7, 13, 18, 24, 29, 35 };
            case 39:
                return new int[] { 2, 8, 13, 19, 24, 30, 36 };
            case 40:
                return new int[] { 2, 8, 14, 19, 25, 31, 37 };
            case 41:
                return new int[] { 2, 8, 14, 19, 26, 32, 38 };
            case 42:
                return new int[] { 3, 9, 15, 21, 27, 33, 39 };
            case 43:
                return new int[] { 2, 7, 13, 18, 23, 29, 34, 40 };
            case 44:
                return new int[] { 2, 8, 13, 19, 24, 30, 35, 41 };
            case 45:
                return new int[] { 2, 8, 13, 19, 25, 30, 36, 42 };
            case 46:
                return new int[] { 2, 8, 14, 20, 25, 31, 37, 43 };
            case 47:
                return new int[] { 2, 8, 14, 20, 26, 32, 38, 44 };
            case 48:
                return new int[] { 3, 9, 15, 21, 27, 33, 39, 45 };
            case 49:
                return new int[] { 2, 7, 13, 18, 24, 29, 35, 40, 46 };
            case 50:
                return new int[] { 2, 8, 13, 19, 24, 30, 35, 41, 47 };
            case 51:
                return new int[] { 2, 8, 14, 19, 25, 31, 35, 42, 48 };
            case 52:
                return new int[] { 2, 8, 14, 20, 25, 31, 37, 42, 49 };
            case 53:
                return new int[] { 2, 8, 14, 20, 26, 32, 38, 43, 50 };
            case 54:
                return new int[] { 3, 9, 15, 21, 27, 33, 39, 45, 51 };
            case 55:
                return new int[] { 2, 8, 13, 19, 24, 30, 35, 41, 46, 52 };
            case 56:
                return new int[] { 2, 8, 13, 19, 25, 30, 36, 41, 47, 53 };
            case 57:
                return new int[] { 2, 8, 14, 19, 25, 31, 36, 42, 48, 54 };
            case 58:
                return new int[] { 2, 8, 14, 20, 26, 31, 37, 43, 49, 55 };
            case 59:
                return new int[] { 2, 8, 14, 20, 26, 32, 38, 44, 50, 56 };
            case 60:
                return new int[] { 3, 9, 15, 21, 27, 33, 39, 45, 51, 57 };
            default:
                return new int[] {};
        }
    }

    /**
     * Updates sector information. Scans nearby tiles, enemy locations, and nearby
     * resources
     * and aggregates into sectorControls and sectorResoruces as buffers. Uses
     * markedSectorsBuffer to track which buffers have been modified each turn to
     * reset them.
     * Alternates whether control or resources are scanned each turn to conserve
     * bytecode.
     * 
     * Note: This is not set up until turn 3 to save compute on initialization.
     * 
     * @throws GameActionException
     */
    public void setSectorStates() throws GameActionException {
        // int bytecodeUsed = Clock.getBytecodeNum();

        // Not initialized until turn 3
        if (turnCount <= 2) {
            return;
        }

        setSectorControlStates();
    }

    public void recordEnemyFlag(MapLocation loc) throws GameActionException {
        if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS)
            return;

        int nextFlagIdx = getNextEnemyFlagIdx(loc);
        if (nextFlagIdx == -1) {
            return;
        }

        Comms.writeEnemyFlagLocation(nextFlagIdx, loc);
        // sectorDatabase.at(whichSector(loc)).addFlag();
    }

    /**
     * Updates sector information. Scans nearby enemy locations.
     * 
     * @throws GameActionException
     */
    public void setSectorControlStates() throws GameActionException {
        // Mark nearby sectors with enemies as hostile
        // Process at max 10 enemies
        int numEnemies = Math.min(enemies.length, 10);
        for (int i = 0; i < numEnemies; i++) {
            RobotInfo enemy = enemies[i];
            int sectorIdx = whichXLoc[enemy.location.x] + whichYLoc[enemy.location.y];
            int controlStatus = Comms.ControlStatus.ENEMY;
            sectorDatabase.at(sectorIdx).addEnemy(controlStatus);
        }

        FlagInfo[] flags = rc.senseNearbyFlags(-1, opponent);
        for (FlagInfo flag : flags) {
            if (flag.isPickedUp())
                continue;
            recordEnemyFlag(flag.getLocation());
        }
    }

    /**
     * Returns nearest combat sector or UNDEFINED_SECTOR_INDEX otherwise
     * 
     * @return
     * @throws GameActionException
     */
    public int getNearestCombatSector() throws GameActionException {
        return getNearestCombatSector(currLoc);
    }

    /**
     * Returns nearest combat sector or UNDEFINED_SECTOR_INDEX otherwise
     * 
     * @return
     * @throws GameActionException
     */
    public int getNearestCombatSector(MapLocation loc) throws GameActionException {
        int closestSector = Comms.UNDEFINED_SECTOR_INDEX;
        int closestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < Comms.COMBAT_SECTOR_SLOTS; i++) {
            int nearestSector = Comms.readCombatSectorIndex(i);
            if (nearestSector == Comms.UNDEFINED_SECTOR_INDEX) {
                continue;
            }
            int distance = loc.distanceSquaredTo(sectorCenters[nearestSector]);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestSector = nearestSector;
            }
        }
        return closestSector;
    }

    /**
     * Returns nearest combat sector or UNDEFINED_SECTOR_INDEX otherwise
     * Should only be called by amplifiers
     * 
     * @return
     * @throws GameActionException
     */
    public int getNearestUnclaimedCombatSector() throws GameActionException {
        int closestSector = Comms.UNDEFINED_SECTOR_INDEX;
        int closestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < Comms.COMBAT_SECTOR_SLOTS; i++) {
            int nearestSector = Comms.readCombatSectorIndex(i);
            if (nearestSector == Comms.UNDEFINED_SECTOR_INDEX) {
                continue;
            }
            if (Comms.readCombatSectorClaimStatus(nearestSector) == Comms.ClaimStatus.CLAIMED) {
                continue;
            }
            int distance = currLoc.distanceSquaredTo(sectorCenters[nearestSector]);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestSector = nearestSector;
            }
        }
        return closestSector;
    }

    /**
     * Only claims if it can currently write
     * WARNING: Combat sectors MUST be unclaimed at some point. Otherwise
     * they will never be removed from comms.
     * In particular, a sector should be unclaimed if the unit has any chance
     * of dying.
     */
    // public void claimSector(int sectorIdx) throws GameActionException {
    // if (rc.canWriteSharedArray(0, 0)) {
    // Comms.writeCombatSectorClaimStatus(sectorIdx, Comms.ClaimStatus.CLAIMED);
    // }
    // }

    /**
     * Only unclaims if it can currently write
     */
    // public void unclaimSector(int sectorIdx) throws GameActionException {
    // if (rc.canWriteSharedArray(0, 0)) {
    // Comms.writeCombatSectorClaimStatus(sectorIdx, Comms.ClaimStatus.UNCLAIMED);
    // }
    // }

    /**
     * Returns nearest combat sector outside of sectorToAvoid or
     * UNDEFINED_SECTOR_INDEX otherwise
     * 
     * @return
     * @throws GameActionException
     */
    public int getNextNearestCombatSector(int sectorToAvoid) throws GameActionException {
        int closestSector = Comms.UNDEFINED_SECTOR_INDEX;
        int closestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < Comms.COMBAT_SECTOR_SLOTS; i++) {
            int nearestSector = Comms.readCombatSectorIndex(i);
            if (nearestSector == Comms.UNDEFINED_SECTOR_INDEX) {
                continue;
            }
            // ignore the sector to avoid
            if (nearestSector == sectorToAvoid) {
                continue;
            }
            int distance = currLoc.distanceSquaredTo(sectorCenters[nearestSector]);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestSector = nearestSector;
            }
        }
        return closestSector;
    }

    /**
     * Returns nearest explore sector or UNDEFINED_SECTOR_INDEX otherwise
     * 
     * Note: Explore sectors are not necessarily removed from the explore list
     * once they are visited. They have to be overwritten by a new explore sector.
     * This has the effect of keeping the initial sectors (symmetry locs) in the
     * explore list until better sectors are chosen.
     * 
     * @return
     * @throws GameActionException
     */
    public int getNearestExploreSectorIdx() throws GameActionException {
        int closestSector = Comms.UNDEFINED_SECTOR_INDEX;
        int closestSectorIndex = Comms.UNDEFINED_SECTOR_INDEX;
        int closestDistance = Integer.MAX_VALUE;
        // Try to choose an explore sector that you have never visited first.
        // If you have visited all explore sectors,
        // choose one that you haven't visited recently
        for (int j = 0; j < 2 && closestSectorIndex == Comms.UNDEFINED_SECTOR_INDEX; j++) {
            closestSector = Comms.UNDEFINED_SECTOR_INDEX;
            closestSectorIndex = Comms.UNDEFINED_SECTOR_INDEX;
            closestDistance = Integer.MAX_VALUE;
            for (int i = 0; i < Comms.EXPLORE_SECTOR_SLOTS; i++) {
                int nearestSectorAll = Comms.readExploreSectorAll(i);
                int nearestSector = nearestSectorAll & 127; // 7 lowest order bits
                if (nearestSector == Comms.UNDEFINED_SECTOR_INDEX) {
                    continue;
                }
                // Skip sectors which are fully claimed
                int nearestSectorStatus = (nearestSectorAll & 128) >> 7; // 2^7
                if (nearestSectorStatus == Comms.ClaimStatus.CLAIMED)
                    continue;

                // Skip sectors that have been visited (recently)
                if (j == 0) {
                    if (sectorDatabase.at(nearestSector).hasVisited())
                        continue;
                } else {
                    if (sectorDatabase.at(nearestSector).hasVisitedRecently())
                        continue;
                }

                int distance = currLoc.distanceSquaredTo(sectorCenters[nearestSector]);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestSector = nearestSector;
                    closestSectorIndex = i;
                }
            }
        }
        // Claim sector
        if (closestSectorIndex != Comms.UNDEFINED_SECTOR_INDEX) {
            Comms.writeExploreSectorClaimStatus(closestSectorIndex, Comms.ClaimStatus.CLAIMED);
            exploreMode = true;
        }
        return closestSector;
    }

    /**
     * Returns nearest mine sector or UNDEFINED_SECTOR_INDEX otherwise
     * Ignores sectors that have already been visited.
     * null can be passed in for visited if you don't care about visited sectors.
     * 
     * @return
     * @throws GameActionException
     */
    // public int getNearestMineSectorIdx(ResourceType resource, FastLocSet visited)
    // throws GameActionException {
    // int closestSector = Comms.UNDEFINED_SECTOR_INDEX;
    // int closestDistance = Integer.MAX_VALUE;
    // for (int i = 0; i < Comms.MINE_SECTOR_SLOTS; i++) {
    // int nearestSector = Comms.readMineSectorIndex(i);
    // if (nearestSector == Comms.UNDEFINED_SECTOR_INDEX) {
    // continue;
    // }
    // // Only look at the resource type we are mining
    // switch (resource) {
    // case ADAMANTIUM:
    // if (Comms.readSectorAdamantiumFlag(nearestSector) == 0)
    // continue;
    // break;
    // case ELIXIR:
    // case MANA:
    // if (Comms.readSectorManaFlag(nearestSector) == 0)
    // continue;
    // break;
    // default:
    // break;
    // }
    // // Skip sectors that have been visited
    // if (visited != null && visited.contains(sectorCenters[nearestSector]))
    // continue;

    // int distance = currLoc.distanceSquaredTo(sectorCenters[nearestSector]);
    // if (distance < closestDistance) {
    // closestDistance = distance;
    // closestSector = nearestSector;
    // }
    // }
    // return closestSector;
    // }

    // Note: index is included in being available
    public int getNextEmptyExploreSectorIdx(int index) throws GameActionException {
        // Preserve explore sectors which still have not been claimed or visited
        while (index < Comms.EXPLORE_SECTOR_SLOTS) {
            int sector = Comms.readExploreSectorIndex(index);
            if (sector == Comms.UNDEFINED_SECTOR_INDEX)
                break;
            int claimStatus = Comms.readExploreSectorClaimStatus(sector);
            if (claimStatus == Comms.ClaimStatus.CLAIMED)
                break;
            int controlStatus = Comms.readSectorControlStatus(sector);
            if (controlStatus != Comms.ControlStatus.EXPLORING)
                break;
            index++;
        }
        return index;
    }

    // Note: index is included in being available
    public int getNextEmptyCombatSectorIdx(int index) throws GameActionException {
        // Preserve combat sectors which still have enemies or are claimed
        while (index < Comms.COMBAT_SECTOR_SLOTS) {
            int sector = Comms.readCombatSectorIndex(index);
            if (sector == Comms.UNDEFINED_SECTOR_INDEX) {
                break;
            }
            int controlStatus = Comms.readSectorControlStatus(sector);
            if (!Comms.isEnemyControlStatus(controlStatus) &&
                    Comms.readCombatSectorClaimStatus(sector) == Comms.ClaimStatus.UNCLAIMED) {
                break;
            }
            index++;
        }
        return index;
    }

    // Note: index is included in being available
    // public int getNextEmptyMineSectorIdx(int index) throws GameActionException {
    // // Preserve mine sectors which still have resources
    // while (index < Comms.MINE_SECTOR_SLOTS) {
    // int sector = Comms.readMineSectorIndex(index);
    // if (sector == Comms.UNDEFINED_SECTOR_INDEX) {
    // break;
    // }
    // index++;
    // }
    // return index;
    // }

    /**
     * If unit redirects from an exploration, remark the sector as unknown
     * 
     * @param destination
     * @throws GameActionException
     */
    public void resetControlStatus(MapLocation destination) throws GameActionException {
        if (exploreMode) {
            int sector = whichXLoc[destination.x] + whichYLoc[destination.y];
            sectorDatabase.at(sector).resetControlStatus();
        }
    }

    /**
     * Get the nearest sector that satisfies the given control status, encoded by
     * `Comms.ControlStatus`. Returns UNDEFINED_SECTOR_INDEX if no such sector
     */
    public int getNearestSectorByControlStatus(int status) throws GameActionException {
        int closestSector = Comms.UNDEFINED_SECTOR_INDEX;
        int closestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < numSectors; i++) {
            if (Comms.readSectorControlStatus(i) == status) {
                int distance = currLoc.distanceSquaredTo(sectorCenters[i]);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestSector = i;
                }
            }
        }
        return closestSector;
    }

    /**
     * Get the nearest combat sector that satisfies the given control status,
     * encoded by `Comms.ControlStatus`.
     * Returns UNDEFINED_SECTOR_INDEX if no such sector
     */
    public int getNearestCombatSectorByControlStatus(int status) throws GameActionException {
        int closestSector = Comms.UNDEFINED_SECTOR_INDEX;
        int closestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < Comms.COMBAT_SECTOR_SLOTS; i++) {
            int nearestSector = Comms.readCombatSectorIndex(i);
            if (nearestSector == Comms.UNDEFINED_SECTOR_INDEX) {
                continue;
            }
            if (Comms.readSectorControlStatus(nearestSector) != status) {
                continue;
            }
            int distance = currLoc.distanceSquaredTo(sectorCenters[nearestSector]);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestSector = nearestSector;
            }
        }
        return closestSector;
    }

    public void printEnemySectors() throws GameActionException {
        for (int i = 0; i < numSectors; i++) {
            if (Comms.isEnemyControlStatus(Comms.readSectorControlStatus(i))) {
                Debug.println("Enemy sector " + i + " at " + sectorCenters[i] + " with status "
                        + Comms.readSectorControlStatus(i));
            }
        }
    }

    public void printCombatSectors() throws GameActionException {
        if (rc.getRoundNum() == 1)
            return;

        for (int i = 0; i < Comms.COMBAT_SECTOR_SLOTS; i++) {
            int sectorIdx = Comms.readCombatSectorIndex(i);

            if (sectorIdx == Comms.UNDEFINED_SECTOR_INDEX)
                continue;

            int claimStatus = Comms.readCombatSectorClaimStatus(i);
            int controlStatus = Comms.readSectorControlStatus(sectorIdx);
            Debug.println("Combat sector " + i + " at " + sectorCenters[sectorIdx] + " with status "
                    + controlStatus + " and claim " + claimStatus);
        }
    }

    public void printEnemyCombatSectors() throws GameActionException {
        for (int i = 0; i < Comms.COMBAT_SECTOR_SLOTS; i++) {
            int idx = Comms.readCombatSectorIndex(i);
            if (idx != Comms.UNDEFINED_SECTOR_INDEX) {
                Debug.println("Enemy Combat sector " + i + " at " + sectorCenters[idx] + " with status "
                        + Comms.readSectorControlStatus(idx));
            }
        }
    }

    public void displayCombatSectors() throws GameActionException {
        for (int i = 0; i < Comms.COMBAT_SECTOR_SLOTS; i++) {
            int sector = Comms.readCombatSectorIndex(i);
            if (sector == Comms.UNDEFINED_SECTOR_INDEX)
                continue;
            MapLocation loc = sectorCenters[sector];
            rc.setIndicatorDot(loc, 255, 0, 0);
        }
    }

    public void displayExploreSectors() throws GameActionException {
        for (int i = 0; i < Comms.EXPLORE_SECTOR_SLOTS; i++) {
            int sector = Comms.readExploreSectorIndex(i);
            if (sector == Comms.UNDEFINED_SECTOR_INDEX)
                continue;
            MapLocation loc = sectorCenters[sector];
            rc.setIndicatorDot(loc, 0, 0, 255);
        }
    }

    public void updateSymmetryLocs() throws GameActionException {
        if (spawnLocations == null)
            return;
        symmetryAll = Util.getValidSymmetries();
        MapLocation[] symLocs = new MapLocation[12];
        int numSymLocs = 0;
        for (int i = 0; i < spawnLocations.length; i++) {
            MapLocation hqLoc = spawnLocations[i];
            if (!rc.onTheMap(hqLoc))
                continue;
            MapLocation[] possibleFlips = Util.getValidSymmetryLocs(hqLoc, symmetryAll);
            for (int j = possibleFlips.length; --j >= 0;) {
                symLocs[numSymLocs++] = possibleFlips[j];
            }
        }
        enemySpawnLocs = new MapLocation[numSymLocs];
        System.arraycopy(symLocs, 0, enemySpawnLocs, 0, numSymLocs);
    }

    // Gets the closest enemy hq guess based on all currently valid symmetries
    public MapLocation getClosestEnemyHQGuess() throws GameActionException {
        MapLocation bestLoc = null;
        int bestDist = Integer.MAX_VALUE;
        for (int i = enemySpawnLocs.length; --i >= 0;) {
            MapLocation loc = enemySpawnLocs[i];
            if (loc == null)
                continue;
            int dist = currLoc.distanceSquaredTo(loc);
            if (dist < bestDist) {
                bestDist = dist;
                bestLoc = loc;
            }
        }
        return bestLoc;
    }

    // Gets the closest hq guess based on a specific symmetry
    public MapLocation getClosestEnemyHQ(int symmetry) throws GameActionException {
        MapLocation bestLoc = null;
        int bestDist = Integer.MAX_VALUE;
        MapLocation loc;
        int dist;
        for (int i = spawnLocations.length; --i >= 0;) {
            loc = spawnLocations[i];
            if (loc == null)
                continue;
            loc = Util.getValidSymmetryLocs(loc, symmetry)[0];
            dist = currLoc.distanceSquaredTo(loc);
            if (dist < bestDist) {
                bestDist = dist;
                bestLoc = loc;
            }
        }
        return bestLoc;
    }

    public MapLocation getCombatSector() throws GameActionException {
        MapLocation enemySectorLoc = null;
        int sectorCenterIdx = getNearestCombatSectorByControlStatus(Comms.ControlStatus.ENEMY);
        if (sectorCenterIdx != Comms.UNDEFINED_SECTOR_INDEX) {
            Debug.printString("enemy");
            enemySectorLoc = sectorCenters[sectorCenterIdx];
        } else {
            sectorCenterIdx = getNearestCombatSectorByControlStatus(Comms.ControlStatus.ENEMY_FLAG);
            if (sectorCenterIdx != Comms.UNDEFINED_SECTOR_INDEX) {
                Debug.printString("e_bread");
                enemySectorLoc = sectorCenters[sectorCenterIdx];
            } else {
                sectorCenterIdx = getNearestCombatSectorByControlStatus(Comms.ControlStatus.ENEMY_SPAWN_LOC);
                if (sectorCenterIdx != Comms.UNDEFINED_SECTOR_INDEX) {
                    Debug.printString("e_spawn");
                    enemySectorLoc = sectorCenters[sectorCenterIdx];
                }
            }
        }
        return enemySectorLoc;
    }

    /**
     * Gets the nearest unvisited combat sector by prioritized control status
     */
    public int getPrioritizedCombatSectorIdx() throws GameActionException {
        int closestSector = Comms.UNDEFINED_SECTOR_INDEX;
        int closestDistance = Integer.MAX_VALUE;
        int closestStatus = -1;
        for (int i = 0; i < Comms.COMBAT_SECTOR_SLOTS; i++) {
            int sectorIdx = Comms.readCombatSectorIndex(i);
            if (sectorIdx == Comms.UNDEFINED_SECTOR_INDEX) {
                continue;
            }
            // Debug.println("Combat sector: " + sectorCenters[sectorIdx], 11749);
            int status = Comms.readSectorControlStatus(sectorIdx);
            // if (status == 2) {
            // // The enemy info for this combat sector was removed but the
            // // combat sector is still there, assume it is aggressive.
            // status = Comms.ControlStatus.ENEMY;
            // }
            if (status < closestStatus)
                continue;
            if (sectorDatabase.at(sectorIdx).hasVisitedRecently())
                continue;
            int distance = currLoc.distanceSquaredTo(sectorCenters[sectorIdx]);
            if (distance < closestDistance || status > closestStatus) {
                closestDistance = distance;
                closestSector = sectorIdx;
                closestStatus = status;
                // Debug.println("Best sector: " + sectorCenters[closestSector] + " status: " +
                // closestStatus, 11749);
            }
        }
        return closestSector;
    }

    // Rotates at distance rad due to BugNav
    boolean moveSafely(MapLocation loc, int rad) throws GameActionException {
        if (loc == null)
            return false;
        int d = rc.getLocation().distanceSquaredTo(loc);
        d = Math.min(d, rad);
        boolean[] imp = new boolean[Util.DIRS_CENTER.length];
        boolean greedy = false;
        for (int i = Util.DIRS_CENTER.length; i-- > 0;) {
            MapLocation newLoc = rc.getLocation().add(Util.DIRS_CENTER[i]);
            if (newLoc.distanceSquaredTo(loc) <= d) {
                imp[i] = true;
                greedy = true;
            }
        }
        Pathfinding.setImpassable(imp);
        Nav.move(loc, greedy, false);
        return true;
    }

    public MapLocation getClosestSpawnLocation(MapLocation loc) throws GameActionException {
        MapLocation bestLoc = null;
        MapLocation spawnLoc;
        int bestDist = Integer.MAX_VALUE;
        int dist;
        for (int i = 0; i < spawnLocations.length; i++) {
            spawnLoc = spawnLocations[i];
            if (!rc.onTheMap(spawnLoc))
                continue;
            dist = loc.distanceSquaredTo(spawnLoc);
            if (dist < bestDist) {
                bestDist = dist;
                bestLoc = spawnLoc;
            }
        }
        return bestLoc;
    }

    // TODO: Break this up over several turns if there are lots of sectors?
    // public MapLocation getClosestNeutralIsland() throws GameActionException {
    // MapLocation bestLoc = null;
    // int bestDist = Integer.MAX_VALUE;
    // int dist;
    // MapLocation loc;

    // for (int i = numSectors; --i >= 0;) {
    // int sectorAll = Comms.readSectorAll(i);
    // // Island flag
    // if ((sectorAll >> 5) == 0)
    // continue;
    // // Control status
    // if ((sectorAll & 0b111) != Comms.ControlStatus.NEUTRAL_ISLAND)
    // continue;
    // loc = sectorCenters[i];
    // dist = currLoc.distanceSquaredTo(loc);
    // if (dist < bestDist) {
    // bestDist = dist;
    // bestLoc = loc;
    // }
    // }

    // return bestLoc;
    // }

    // Keep loc in action radius if possible, then choose direction
    public Direction chooseForwardDirection(MapLocation loc) throws GameActionException {
        Direction Dir = currLoc.directionTo(loc);
        Direction[] dirsToConsider = Util.getInOrderDirections(Dir);
        int numDirs = 0;
        MapLocation newLoc;
        for (int i = dirsToConsider.length; --i >= 0;) {
            newLoc = currLoc.add(dirsToConsider[i]);
            if (newLoc.distanceSquaredTo(loc) <= actionRadiusSquared) {
                dirsToConsider[numDirs++] = dirsToConsider[i];
            }
        }

        // If no directions in action radius, just do normal chooseDirection
        if (numDirs == 0) {
            return chooseDirection(Util.getInOrderDirections(Dir));
        } else {
            Direction[] newDirs = new Direction[numDirs];
            System.arraycopy(dirsToConsider, 0, newDirs, 0, numDirs);
            return chooseDirection(newDirs);
        }
    }

    public Direction chooseBackupDirection(Direction Dir) throws GameActionException {
        Direction[] dirsToConsider = Util.getInOrderDirections(Dir);
        return chooseDirection(dirsToConsider);
    }

    // Direction priority breaks ties through
    // 1. Least number of enemies seen
    // 2. Least number of enemies in action radius
    // 3. Maximum average distance from enemies
    // 4. Minimum average distance to friends
    public Direction chooseDirection(Direction[] dirsToConsider) throws GameActionException {
        Direction bestDirSoFar = null;
        int bestEnemiesInVision = Integer.MAX_VALUE;
        int bestEnemiesInAction = Integer.MAX_VALUE;
        double bestAvgEnemyDist = Double.MAX_VALUE;
        double bestAvgFriendDist = Double.MAX_VALUE;
        RobotInfo enemy;
        MapLocation enemyLoc;

        Direction newDir;
        MapLocation targetLoc;
        boolean isPassible;
        int enemiesInAction;
        int enemiesInVision;
        double avgEnemyDist;
        double avgFriendDist;

        for (int x = 0; x < dirsToConsider.length; x++) {
            newDir = dirsToConsider[x];
            if (rc.canMove(newDir)) {
                targetLoc = currLoc.add(newDir);
                // Debug.printString("best dir " + newDir);
                if (rc.canSenseLocation(targetLoc)) {
                    isPassible = rc.sensePassability(targetLoc);
                } else {
                    // TODO
                    isPassible = true;
                }
                enemiesInAction = 0;
                enemiesInVision = 0;
                avgEnemyDist = 0;
                for (int i = attackableEnemies.length; --i >= 0;) {
                    enemy = attackableEnemies[i];
                    enemyLoc = enemy.getLocation();
                    if (enemyLoc.distanceSquaredTo(targetLoc) <= actionRadiusSquared) {
                        enemiesInAction++;
                        enemiesInVision++;
                    } else if (enemyLoc.distanceSquaredTo(targetLoc) <= visionRadiusSquared) {
                        enemiesInVision++;
                    }
                    avgEnemyDist += enemyLoc.distanceSquaredTo(targetLoc);
                }
                avgEnemyDist /= attackableEnemies.length;
                if (!isPassible) {
                    continue;
                }

                // Vision first
                if (enemiesInVision < bestEnemiesInVision) {
                    bestDirSoFar = newDir;
                    bestEnemiesInVision = enemiesInVision;
                    bestEnemiesInAction = enemiesInAction;
                    bestAvgEnemyDist = avgEnemyDist;
                    bestAvgFriendDist = Double.MAX_VALUE;
                    continue;
                } else if (enemiesInVision > bestEnemiesInVision) {
                    continue;
                }

                // Action radius second
                if (enemiesInAction < bestEnemiesInAction) {
                    bestDirSoFar = newDir;
                    bestEnemiesInAction = enemiesInAction;
                    bestAvgEnemyDist = avgEnemyDist;
                    bestAvgFriendDist = Double.MAX_VALUE;
                    continue;
                } else if (enemiesInAction > bestEnemiesInAction) {
                    continue;
                }

                // Distance third
                if (avgEnemyDist > bestAvgEnemyDist) {
                    bestDirSoFar = newDir;
                    bestAvgEnemyDist = avgEnemyDist;
                    bestAvgFriendDist = Double.MAX_VALUE;
                    continue;
                } else if (avgEnemyDist < bestAvgEnemyDist) {
                    continue;
                }

                // Min dist to friends fourth
                // Avg friend dist is only calced if we need this tiebreaker
                // Calc it for the bestLoc first if we haven't already,
                // and then calc it for the targetLoc
                if (bestAvgFriendDist == Double.MAX_VALUE) {
                    avgFriendDist = 0;
                    MapLocation bestLoc = currLoc.add(bestDirSoFar);
                    for (int i = allies.length; --i >= 0;) {
                        avgFriendDist += allies[i].getLocation().distanceSquaredTo(bestLoc);
                    }
                    avgFriendDist /= allies.length;
                    bestAvgFriendDist = avgFriendDist;
                }

                avgFriendDist = 0;
                for (int i = allies.length; --i >= 0;) {
                    avgFriendDist += allies[i].getLocation().distanceSquaredTo(targetLoc);
                }
                avgFriendDist /= allies.length;
                if (avgFriendDist < bestAvgFriendDist) {
                    bestDirSoFar = newDir;
                    bestAvgFriendDist = avgFriendDist;
                }
            }
        }

        return bestDirSoFar;
    }

    public FlagInfo findFlagInfo() throws GameActionException {
        FlagInfo[] enemyFlags = rc.senseNearbyFlags(visionRadiusSquared, opponent);
        FlagInfo bestFlag = null;
        int bestDist = Integer.MAX_VALUE;
        int dist;
        for (FlagInfo flag : enemyFlags) {
            if (flag.isPickedUp())
                continue;
            dist = flag.getLocation().distanceSquaredTo(rc.getLocation());
            if (dist < bestDist) {
                bestDist = dist;
                bestFlag = flag;
            }
        }
        return bestFlag;
    }

    public MapLocation findCrumbs() throws GameActionException {
        MapLocation[] crumbs = rc.senseNearbyCrumbs(visionRadiusSquared);
        MapLocation bestCrumb = null;
        int bestDist = Integer.MAX_VALUE;
        int dist;
        for (MapLocation crumb : crumbs) {
            dist = crumb.distanceSquaredTo(rc.getLocation());
            if (dist < bestDist) {
                bestDist = dist;
                bestCrumb = crumb;
            }
        }
        return bestCrumb;
    }

    public void loadSetupExploreTarget() throws GameActionException {
        MapLocation crumb = findCrumbs();
        if (crumb != null) {
            Debug.printString("Crumb");
            exploreTarget = crumb;
            turnsFollowedExploreTarget = 0;
            return;
        }

        exploreTarget = Explore.getExploreTarget();
    }

    public void loadExploreTarget2() throws GameActionException {
        MapLocation target;
        goingToSymLoc = false;
        MapLocation symLoc = chooseSymmetricLoc();
        MapLocation combatSector = null;

        int combatSectorIdx = getPrioritizedCombatSectorIdx();
        if (combatSectorIdx != Comms.UNDEFINED_SECTOR_INDEX) {
            combatSector = sectorCenters[combatSectorIdx];
        }

        MapLocation crumb = findCrumbs();
        if (crumb != null) {
            Debug.printString("Crumb");
            target = crumb;
        } else if (lastClosestEnemy != null
                && turnSawLastClosestAttackingEnemy + LAST_ATTACKING_ENEMY_TIMEOUT >= rc.getRoundNum()) {
            // && friendlyAttackingHealth >= lastEnemyAttackingHealth) {
            Debug.printString("LastEnemy");
            target = lastClosestEnemy;
        } else if (combatSector != null && symLoc != null) {
            int combSecDist = Util.manhattan(currLoc, combatSector);
            int symLocDist = Util.manhattan(currLoc, symLoc);
            int controlStatus = Comms.readSectorControlStatus(combatSectorIdx);
            boolean protect = combSecDist * Util.SYM_TO_COMB_DIST_RATIO < symLocDist;
            boolean closeToHQ = Util.manhattan(combatSector,
                    getClosestSpawnLocation(combatSector)) <= Util.COMB_TO_HOME_DIST;
            boolean protectFromEnemy = combSecDist * Util.SYM_TO_COMB_HOME_AGGRESSIVE_DIST_RATIO < symLocDist &&
                    controlStatus == Comms.ControlStatus.ENEMY;
            if (protect || (closeToHQ && protectFromEnemy)) {
                target = combatSector;
                Debug.printString("PrefCombSec");
            } else {
                target = symLoc;
                markSymmetricLocSeen(target);
                goingToSymLoc = true;
                Debug.printString("PrefSymLoc");
            }
        } else if (combatSector != null) {
            target = combatSector;
            Debug.printString("CombSec");
        } else if (symLoc != null) {
            target = symLoc;
            markSymmetricLocSeen(target);
            goingToSymLoc = true;
            Debug.printString("SymLoc");
        } else {
            int exploreSectorIdx = getNearestExploreSectorIdx();
            if (exploreSectorIdx != Comms.UNDEFINED_SECTOR_INDEX) {
                target = sectorCenters[exploreSectorIdx];
                Debug.printString("ExpSec");
            } else {
                target = Explore.getExploreTarget();
                Debug.printString("Exploring");
            }
        }

        if (goingToSymLoc) {
            // Time out sym locs after a while because we are not guaranteed it
            // is reachable.
            if (!target.equals(exploreTarget)) {
                turnsFollowedExploreTarget = 0;
                EXPLORE_TARGET_TIMEOUT = EXPLORE_TIMEOUT_MULT * Util.distance(currLoc, target);
            }
        } else {
            EXPLORE_TARGET_TIMEOUT = GameConstants.GAME_MAX_NUMBER_OF_ROUNDS;
        }

        exploreTarget = target;
    }

    public MapLocation chooseSymmetricLoc() throws GameActionException {
        MapLocation bestLoc = null;
        MapLocation possibleLoc;
        int bestDist = Integer.MAX_VALUE;
        int currDist;
        for (int i = enemySpawnLocs.length; --i >= 0;) {
            possibleLoc = enemySpawnLocs[i];
            currDist = currLoc.distanceSquaredTo(possibleLoc);
            // int controlStatus = Comms.readSectorControlStatus(whichSector(possibleLoc));
            // boolean notTraversed = controlStatus == Comms.ControlStatus.UNKNOWN ||
            // controlStatus == Comms.ControlStatus.EXPLORING;

            if (currDist < bestDist && !seenSymmetricLocs.contains(possibleLoc)) {
                bestLoc = possibleLoc;
                bestDist = currDist;
            }
        }

        // Consider the center of the map as a symmetric location
        if (isSemiSmallMap()) {
            possibleLoc = new MapLocation(Util.MAP_WIDTH / 2, Util.MAP_HEIGHT / 2);
            currDist = currLoc.distanceSquaredTo(possibleLoc);
            if (currDist < bestDist && !seenSymmetricLocs.contains(possibleLoc)) {
                bestLoc = possibleLoc;
                bestDist = currDist;
            }
        }

        return bestLoc;
    }

    public void resetLocalEnemyInformation() throws GameActionException {
        closestEnemy = null;

        int closestDist = Integer.MAX_VALUE;
        RobotInfo bot;
        MapLocation candidateLoc;
        int candidateDist;
        for (int i = 0; i < enemies.length; i++) {
            bot = enemies[i];
            candidateLoc = bot.getLocation();
            candidateDist = currLoc.distanceSquaredTo(candidateLoc);
            if (candidateDist < closestDist) {
                closestDist = candidateDist;
                closestEnemy = bot;
                lastClosestEnemy = bot.getLocation();
                turnSawLastClosestAttackingEnemy = rc.getRoundNum();
            }
        }
    }

    public void markSymmetricLocSeen(MapLocation target) throws GameActionException {
        if (rc.canSenseLocation(target)) {
            if (rc.getLocation().distanceSquaredTo(target) <= 13) {
                seenSymmetricLocs.add(target);
            } else if (!rc.sensePassability(target)) {
                // If the target is not passable, it is probably not reachable.
                // Check for at least 3 other adjacent tiles that are not passable
                // and mark it as seen if so.
                // Theoretically we might want to remove the symmetry that this target
                // is a part of. But that seems a little risky.
                int numNotPassable = 0;
                for (Direction dir : Util.directions) {
                    if (rc.canSenseLocation(target.add(dir)) &&
                            !rc.sensePassability(target.add(dir))) {
                        numNotPassable++;
                    }
                }

                if (numNotPassable >= 3) {
                    seenSymmetricLocs.add(target);
                }
            }
        }

        if (turnsFollowedExploreTarget > EXPLORE_TARGET_TIMEOUT) {
            // We have been trying to get to this target for a while and it is not
            // reachable. Mark it as seen.
            // Theoretically we might want to remove the symmetry that this target
            // is a part of. But that seems a little risky.
            seenSymmetricLocs.add(target);
        }
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

    public void digHole() throws GameActionException {
        if (!rc.isActionReady())
            return;

        for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(),
                GameConstants.INTERACT_RADIUS_SQUARED)) {
            if (rc.canDig(loc)) {
                rc.dig(loc);
                return;
            }
        }
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
}
