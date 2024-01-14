package MPDodo;

import battlecode.common.*;

public class Comms {

    private static RobotController rc;

    private static int[] bufferPool;
    private static boolean[] dirtyFlags;

    static final int OFF_THE_MAP_LOC = 0xFFF;


    public final static int OUR_FLAG_SLOTS = 3;
    public final static int OUR_SPAWN_SLOTS = 3;
    public final static int ENEMY_FLAG_SLOTS = 3;
    public final static int UNIT_SLOTS = 1;
    public final static int SECTOR_SLOTS = 100;
    public final static int COMBAT_SECTOR_SLOTS = 4;
    public final static int EXPLORE_SECTOR_SLOTS = 13;
    public final static int SYMMETRY_SLOTS = 1;
    public final static int NEXT_BUILDER_SLOTS = 1;

    // ControlStatus priorities are in increasing priority.
    public class ControlStatus {
        public static final int UNKNOWN = 0;
        public static final int EXPLORING = 1;
        public static final int EMPTY = 2;

        public static final int ENEMY_SPAWN_LOC = 3;
        public static final int ENEMY = 4;
        public static final int ENEMY_FLAG = 5;

        // public static final int FRIENDLY_SPAWN_LOC = 6;
        // public static final int FRIENDLY_FLAG = 7;

        public static final int NUM_CONTROL_STATUS = 6;
    }

    public class ClaimStatus {
        public static final int UNCLAIMED = 0;
        public static final int CLAIMED = 1;
    }

    public class HQFlag {
        public static final int PLACEHOLDER = 0;
    }

    public final static int UNDEFINED_SECTOR_INDEX = 127;

    public static void init(RobotController r) throws GameActionException {
        rc = r;

        bufferPool = new int[64];
        dirtyFlags = new boolean[64];
    }

    public static void initComms() throws GameActionException {
        if (rc.getRoundNum() == 1) {
            initPrioritySectors();
            initSymmetry();
            initLocations();
        }
    }

    public static MapLocation readOurFlagLocation(int idx) throws GameActionException {
        return new MapLocation(readOurFlagXCoord(idx), readOurFlagYCoord(idx));
    }

    public static void writeOurFlagLocation(int idx, MapLocation loc) throws GameActionException {
        writeOurFlagXCoord(idx, loc.x);
        writeOurFlagYCoord(idx, loc.y);
    }

    public static MapLocation readEnemyFlagLocation(int idx) throws GameActionException {
        return new MapLocation(readEnemyFlagXCoord(idx), readEnemyFlagYCoord(idx));
    }

    public static void writeEnemyFlagLocation(int idx, MapLocation loc) throws GameActionException {
        writeEnemyFlagXCoord(idx, loc.x);
        writeEnemyFlagYCoord(idx, loc.y);
    }

    public static MapLocation readOurSpawnLocation(int idx) throws GameActionException {
        return new MapLocation(readOurSpawnXCoord(idx), readOurSpawnYCoord(idx));
    }

    public static void writeOurSpawnLocation(int idx, MapLocation loc) throws GameActionException {
        writeOurSpawnXCoord(idx, loc.x);
        writeOurSpawnYCoord(idx, loc.y);
    }

    public static void initSymmetry() throws GameActionException {
        writeSymmetryAll(7);
    }

    public static void initLocations() throws GameActionException {
        writeEnemyFlagAll(0, OFF_THE_MAP_LOC);
        writeEnemyFlagAll(1, OFF_THE_MAP_LOC);
        writeEnemyFlagAll(2, OFF_THE_MAP_LOC);

        writeOurFlagAll(0, OFF_THE_MAP_LOC);
        writeOurFlagAll(1, OFF_THE_MAP_LOC);
        writeOurFlagAll(2, OFF_THE_MAP_LOC);

        writeOurSpawnAll(0, OFF_THE_MAP_LOC);
        writeOurSpawnAll(1, OFF_THE_MAP_LOC);
        writeOurSpawnAll(2, OFF_THE_MAP_LOC);
    }

    public static int pickControlStatus(int newStatus, int currStatus) {
        return Math.max(newStatus, currStatus);
    }

    public static boolean isEnemyControlStatus(int controlStatus) {
        switch (controlStatus) {
            case ControlStatus.ENEMY:
            case ControlStatus.ENEMY_FLAG:
            case ControlStatus.ENEMY_SPAWN_LOC:
                return true;
            default:
                return false;
        }
    }

    public static void writeToBufferPool(int idx, int value) throws GameActionException {
        bufferPool[idx] = value;
        dirtyFlags[idx] = true;
    }

    public static void initBufferPool() throws GameActionException {
        dirtyFlags = new boolean[64];

        bufferPool[0] = rc.readSharedArray(0);
        bufferPool[1] = rc.readSharedArray(1);
        bufferPool[2] = rc.readSharedArray(2);
        bufferPool[3] = rc.readSharedArray(3);
        bufferPool[4] = rc.readSharedArray(4);
        bufferPool[5] = rc.readSharedArray(5);
        bufferPool[6] = rc.readSharedArray(6);
        bufferPool[7] = rc.readSharedArray(7);
        bufferPool[8] = rc.readSharedArray(8);
        bufferPool[9] = rc.readSharedArray(9);
        bufferPool[10] = rc.readSharedArray(10);
        bufferPool[11] = rc.readSharedArray(11);
        bufferPool[12] = rc.readSharedArray(12);
        bufferPool[13] = rc.readSharedArray(13);
        bufferPool[14] = rc.readSharedArray(14);
        bufferPool[15] = rc.readSharedArray(15);
        bufferPool[16] = rc.readSharedArray(16);
        bufferPool[17] = rc.readSharedArray(17);
        bufferPool[18] = rc.readSharedArray(18);
        bufferPool[19] = rc.readSharedArray(19);
        bufferPool[20] = rc.readSharedArray(20);
        bufferPool[21] = rc.readSharedArray(21);
        bufferPool[22] = rc.readSharedArray(22);
        bufferPool[23] = rc.readSharedArray(23);
        bufferPool[24] = rc.readSharedArray(24);
        bufferPool[25] = rc.readSharedArray(25);
        bufferPool[26] = rc.readSharedArray(26);
        bufferPool[27] = rc.readSharedArray(27);
        bufferPool[28] = rc.readSharedArray(28);
        bufferPool[29] = rc.readSharedArray(29);
        bufferPool[30] = rc.readSharedArray(30);
        bufferPool[31] = rc.readSharedArray(31);
        bufferPool[32] = rc.readSharedArray(32);
        bufferPool[33] = rc.readSharedArray(33);
        bufferPool[34] = rc.readSharedArray(34);
        bufferPool[35] = rc.readSharedArray(35);
        bufferPool[36] = rc.readSharedArray(36);
        bufferPool[37] = rc.readSharedArray(37);
        bufferPool[38] = rc.readSharedArray(38);
        bufferPool[39] = rc.readSharedArray(39);
        bufferPool[40] = rc.readSharedArray(40);
        bufferPool[41] = rc.readSharedArray(41);
        bufferPool[42] = rc.readSharedArray(42);
        bufferPool[43] = rc.readSharedArray(43);
        bufferPool[44] = rc.readSharedArray(44);
        bufferPool[45] = rc.readSharedArray(45);
        bufferPool[46] = rc.readSharedArray(46);
        bufferPool[47] = rc.readSharedArray(47);
        bufferPool[48] = rc.readSharedArray(48);
        bufferPool[49] = rc.readSharedArray(49);
        bufferPool[50] = rc.readSharedArray(50);
        bufferPool[51] = rc.readSharedArray(51);
        bufferPool[52] = rc.readSharedArray(52);
        bufferPool[53] = rc.readSharedArray(53);
        bufferPool[54] = rc.readSharedArray(54);
        bufferPool[55] = rc.readSharedArray(55);
        bufferPool[56] = rc.readSharedArray(56);
        bufferPool[57] = rc.readSharedArray(57);
        bufferPool[58] = rc.readSharedArray(58);
        bufferPool[59] = rc.readSharedArray(59);
        bufferPool[60] = rc.readSharedArray(60);
        bufferPool[61] = rc.readSharedArray(61);
        bufferPool[62] = rc.readSharedArray(62);
        bufferPool[63] = rc.readSharedArray(63);
    }

    public static void flushBufferPool() throws GameActionException {
        if (dirtyFlags[0])
            rc.writeSharedArray(0, bufferPool[0]);
        if (dirtyFlags[1])
            rc.writeSharedArray(1, bufferPool[1]);
        if (dirtyFlags[2])
            rc.writeSharedArray(2, bufferPool[2]);
        if (dirtyFlags[3])
            rc.writeSharedArray(3, bufferPool[3]);
        if (dirtyFlags[4])
            rc.writeSharedArray(4, bufferPool[4]);
        if (dirtyFlags[5])
            rc.writeSharedArray(5, bufferPool[5]);
        if (dirtyFlags[6])
            rc.writeSharedArray(6, bufferPool[6]);
        if (dirtyFlags[7])
            rc.writeSharedArray(7, bufferPool[7]);
        if (dirtyFlags[8])
            rc.writeSharedArray(8, bufferPool[8]);
        if (dirtyFlags[9])
            rc.writeSharedArray(9, bufferPool[9]);
        if (dirtyFlags[10])
            rc.writeSharedArray(10, bufferPool[10]);
        if (dirtyFlags[11])
            rc.writeSharedArray(11, bufferPool[11]);
        if (dirtyFlags[12])
            rc.writeSharedArray(12, bufferPool[12]);
        if (dirtyFlags[13])
            rc.writeSharedArray(13, bufferPool[13]);
        if (dirtyFlags[14])
            rc.writeSharedArray(14, bufferPool[14]);
        if (dirtyFlags[15])
            rc.writeSharedArray(15, bufferPool[15]);
        if (dirtyFlags[16])
            rc.writeSharedArray(16, bufferPool[16]);
        if (dirtyFlags[17])
            rc.writeSharedArray(17, bufferPool[17]);
        if (dirtyFlags[18])
            rc.writeSharedArray(18, bufferPool[18]);
        if (dirtyFlags[19])
            rc.writeSharedArray(19, bufferPool[19]);
        if (dirtyFlags[20])
            rc.writeSharedArray(20, bufferPool[20]);
        if (dirtyFlags[21])
            rc.writeSharedArray(21, bufferPool[21]);
        if (dirtyFlags[22])
            rc.writeSharedArray(22, bufferPool[22]);
        if (dirtyFlags[23])
            rc.writeSharedArray(23, bufferPool[23]);
        if (dirtyFlags[24])
            rc.writeSharedArray(24, bufferPool[24]);
        if (dirtyFlags[25])
            rc.writeSharedArray(25, bufferPool[25]);
        if (dirtyFlags[26])
            rc.writeSharedArray(26, bufferPool[26]);
        if (dirtyFlags[27])
            rc.writeSharedArray(27, bufferPool[27]);
        if (dirtyFlags[28])
            rc.writeSharedArray(28, bufferPool[28]);
        if (dirtyFlags[29])
            rc.writeSharedArray(29, bufferPool[29]);
        if (dirtyFlags[30])
            rc.writeSharedArray(30, bufferPool[30]);
        if (dirtyFlags[31])
            rc.writeSharedArray(31, bufferPool[31]);
        if (dirtyFlags[32])
            rc.writeSharedArray(32, bufferPool[32]);
        if (dirtyFlags[33])
            rc.writeSharedArray(33, bufferPool[33]);
        if (dirtyFlags[34])
            rc.writeSharedArray(34, bufferPool[34]);
        if (dirtyFlags[35])
            rc.writeSharedArray(35, bufferPool[35]);
        if (dirtyFlags[36])
            rc.writeSharedArray(36, bufferPool[36]);
        if (dirtyFlags[37])
            rc.writeSharedArray(37, bufferPool[37]);
        if (dirtyFlags[38])
            rc.writeSharedArray(38, bufferPool[38]);
        if (dirtyFlags[39])
            rc.writeSharedArray(39, bufferPool[39]);
        if (dirtyFlags[40])
            rc.writeSharedArray(40, bufferPool[40]);
        if (dirtyFlags[41])
            rc.writeSharedArray(41, bufferPool[41]);
        if (dirtyFlags[42])
            rc.writeSharedArray(42, bufferPool[42]);
        if (dirtyFlags[43])
            rc.writeSharedArray(43, bufferPool[43]);
        if (dirtyFlags[44])
            rc.writeSharedArray(44, bufferPool[44]);
        if (dirtyFlags[45])
            rc.writeSharedArray(45, bufferPool[45]);
        if (dirtyFlags[46])
            rc.writeSharedArray(46, bufferPool[46]);
        if (dirtyFlags[47])
            rc.writeSharedArray(47, bufferPool[47]);
        if (dirtyFlags[48])
            rc.writeSharedArray(48, bufferPool[48]);
        if (dirtyFlags[49])
            rc.writeSharedArray(49, bufferPool[49]);
        if (dirtyFlags[50])
            rc.writeSharedArray(50, bufferPool[50]);
        if (dirtyFlags[51])
            rc.writeSharedArray(51, bufferPool[51]);
        if (dirtyFlags[52])
            rc.writeSharedArray(52, bufferPool[52]);
        if (dirtyFlags[53])
            rc.writeSharedArray(53, bufferPool[53]);
        if (dirtyFlags[54])
            rc.writeSharedArray(54, bufferPool[54]);
        if (dirtyFlags[55])
            rc.writeSharedArray(55, bufferPool[55]);
        if (dirtyFlags[56])
            rc.writeSharedArray(56, bufferPool[56]);
        if (dirtyFlags[57])
            rc.writeSharedArray(57, bufferPool[57]);
        if (dirtyFlags[58])
            rc.writeSharedArray(58, bufferPool[58]);
        if (dirtyFlags[59])
            rc.writeSharedArray(59, bufferPool[59]);
        if (dirtyFlags[60])
            rc.writeSharedArray(60, bufferPool[60]);
        if (dirtyFlags[61])
            rc.writeSharedArray(61, bufferPool[61]);
        if (dirtyFlags[62])
            rc.writeSharedArray(62, bufferPool[62]);
        if (dirtyFlags[63])
            rc.writeSharedArray(63, bufferPool[63]);
    }

    public static void initPrioritySectors() throws GameActionException {
        rc.writeSharedArray(25, 1);
        rc.writeSharedArray(26, 65021);
        rc.writeSharedArray(27, 65021);
        rc.writeSharedArray(28, 65021);
        rc.writeSharedArray(29, 65021);
        rc.writeSharedArray(30, 65021);
        rc.writeSharedArray(31, 65021);
        rc.writeSharedArray(32, 65021);
        rc.writeSharedArray(33, 65021);
        rc.writeSharedArray(34, 64512);
    }


    public static void resetAllSectorControlStatus() throws GameActionException {
        rc.writeSharedArray(7, rc.readSharedArray(7) & 49152);
        rc.writeSharedArray(8, rc.readSharedArray(8) & 0);
        rc.writeSharedArray(9, rc.readSharedArray(9) & 0);
        rc.writeSharedArray(10, rc.readSharedArray(10) & 0);
        rc.writeSharedArray(11, rc.readSharedArray(11) & 0);
        rc.writeSharedArray(12, rc.readSharedArray(12) & 0);
        rc.writeSharedArray(13, rc.readSharedArray(13) & 0);
        rc.writeSharedArray(14, rc.readSharedArray(14) & 0);
        rc.writeSharedArray(15, rc.readSharedArray(15) & 0);
        rc.writeSharedArray(16, rc.readSharedArray(16) & 0);
        rc.writeSharedArray(17, rc.readSharedArray(17) & 0);
        rc.writeSharedArray(18, rc.readSharedArray(18) & 0);
        rc.writeSharedArray(19, rc.readSharedArray(19) & 0);
        rc.writeSharedArray(20, rc.readSharedArray(20) & 0);
        rc.writeSharedArray(21, rc.readSharedArray(21) & 0);
        rc.writeSharedArray(22, rc.readSharedArray(22) & 0);
        rc.writeSharedArray(23, rc.readSharedArray(23) & 0);
        rc.writeSharedArray(24, rc.readSharedArray(24) & 0);
        rc.writeSharedArray(25, rc.readSharedArray(25) & 3);
    }

    public static int readOurFlagXCoord(int idx) throws GameActionException {
        switch (idx) {
            case 0:
                return (rc.readSharedArray(0) & 64512) >>> 10;
            case 1:
                return ((rc.readSharedArray(0) & 15) << 2) + ((rc.readSharedArray(1) & 49152) >>> 14);
            case 2:
                return (rc.readSharedArray(1) & 252) >>> 2;
            default:
                return -1;
        }
    }

    public static void writeOurFlagXCoord(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                rc.writeSharedArray(0, (rc.readSharedArray(0) & 1023) | (value << 10));
                break;
            case 1:
                rc.writeSharedArray(0, (rc.readSharedArray(0) & 65520) | ((value & 60) >>> 2));
                rc.writeSharedArray(1, (rc.readSharedArray(1) & 16383) | ((value & 3) << 14));
                break;
            case 2:
                rc.writeSharedArray(1, (rc.readSharedArray(1) & 65283) | (value << 2));
                break;
        }
    }

    public static void writeBPOurFlagXCoord(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                writeToBufferPool(0, (bufferPool[0] & 1023) | (value << 10));
                break;
            case 1:
                writeToBufferPool(0, (bufferPool[0] & 65520) | ((value & 60) >>> 2));
                writeToBufferPool(1, (bufferPool[1] & 16383) | ((value & 3) << 14));
                break;
            case 2:
                writeToBufferPool(1, (bufferPool[1] & 65283) | (value << 2));
                break;
        }
    }

    public static int readOurFlagYCoord(int idx) throws GameActionException {
        switch (idx) {
            case 0:
                return (rc.readSharedArray(0) & 1008) >>> 4;
            case 1:
                return (rc.readSharedArray(1) & 16128) >>> 8;
            case 2:
                return ((rc.readSharedArray(1) & 3) << 4) + ((rc.readSharedArray(2) & 61440) >>> 12);
            default:
                return -1;
        }
    }

    public static void writeOurFlagYCoord(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                rc.writeSharedArray(0, (rc.readSharedArray(0) & 64527) | (value << 4));
                break;
            case 1:
                rc.writeSharedArray(1, (rc.readSharedArray(1) & 49407) | (value << 8));
                break;
            case 2:
                rc.writeSharedArray(1, (rc.readSharedArray(1) & 65532) | ((value & 48) >>> 4));
                rc.writeSharedArray(2, (rc.readSharedArray(2) & 4095) | ((value & 15) << 12));
                break;
        }
    }

    public static void writeBPOurFlagYCoord(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                writeToBufferPool(0, (bufferPool[0] & 64527) | (value << 4));
                break;
            case 1:
                writeToBufferPool(1, (bufferPool[1] & 49407) | (value << 8));
                break;
            case 2:
                writeToBufferPool(1, (bufferPool[1] & 65532) | ((value & 48) >>> 4));
                writeToBufferPool(2, (bufferPool[2] & 4095) | ((value & 15) << 12));
                break;
        }
    }

    public static int readOurFlagAll(int idx) throws GameActionException {
        switch (idx) {
            case 0:
                return (rc.readSharedArray(0) & 65520) >>> 4;
            case 1:
                return ((rc.readSharedArray(0) & 15) << 8) + ((rc.readSharedArray(1) & 65280) >>> 8);
            case 2:
                return ((rc.readSharedArray(1) & 255) << 4) + ((rc.readSharedArray(2) & 61440) >>> 12);
            default:
                return -1;
        }
    }

    public static void writeOurFlagAll(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                rc.writeSharedArray(0, (rc.readSharedArray(0) & 15) | (value << 4));
                break;
            case 1:
                rc.writeSharedArray(0, (rc.readSharedArray(0) & 65520) | ((value & 3840) >>> 8));
                rc.writeSharedArray(1, (rc.readSharedArray(1) & 255) | ((value & 255) << 8));
                break;
            case 2:
                rc.writeSharedArray(1, (rc.readSharedArray(1) & 65280) | ((value & 4080) >>> 4));
                rc.writeSharedArray(2, (rc.readSharedArray(2) & 4095) | ((value & 15) << 12));
                break;
        }
    }

    public static void writeBPOurFlagAll(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                writeToBufferPool(0, (bufferPool[0] & 15) | (value << 4));
                break;
            case 1:
                writeToBufferPool(0, (bufferPool[0] & 65520) | ((value & 3840) >>> 8));
                writeToBufferPool(1, (bufferPool[1] & 255) | ((value & 255) << 8));
                break;
            case 2:
                writeToBufferPool(1, (bufferPool[1] & 65280) | ((value & 4080) >>> 4));
                writeToBufferPool(2, (bufferPool[2] & 4095) | ((value & 15) << 12));
                break;
        }
    }

    public static int readOurSpawnXCoord(int idx) throws GameActionException {
        switch (idx) {
            case 0:
                return (rc.readSharedArray(2) & 4032) >>> 6;
            case 1:
                return (rc.readSharedArray(3) & 64512) >>> 10;
            case 2:
                return ((rc.readSharedArray(3) & 15) << 2) + ((rc.readSharedArray(4) & 49152) >>> 14);
            default:
                return -1;
        }
    }

    public static void writeOurSpawnXCoord(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                rc.writeSharedArray(2, (rc.readSharedArray(2) & 61503) | (value << 6));
                break;
            case 1:
                rc.writeSharedArray(3, (rc.readSharedArray(3) & 1023) | (value << 10));
                break;
            case 2:
                rc.writeSharedArray(3, (rc.readSharedArray(3) & 65520) | ((value & 60) >>> 2));
                rc.writeSharedArray(4, (rc.readSharedArray(4) & 16383) | ((value & 3) << 14));
                break;
        }
    }

    public static void writeBPOurSpawnXCoord(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                writeToBufferPool(2, (bufferPool[2] & 61503) | (value << 6));
                break;
            case 1:
                writeToBufferPool(3, (bufferPool[3] & 1023) | (value << 10));
                break;
            case 2:
                writeToBufferPool(3, (bufferPool[3] & 65520) | ((value & 60) >>> 2));
                writeToBufferPool(4, (bufferPool[4] & 16383) | ((value & 3) << 14));
                break;
        }
    }

    public static int readOurSpawnYCoord(int idx) throws GameActionException {
        switch (idx) {
            case 0:
                return (rc.readSharedArray(2) & 63);
            case 1:
                return (rc.readSharedArray(3) & 1008) >>> 4;
            case 2:
                return (rc.readSharedArray(4) & 16128) >>> 8;
            default:
                return -1;
        }
    }

    public static void writeOurSpawnYCoord(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                rc.writeSharedArray(2, (rc.readSharedArray(2) & 65472) | (value));
                break;
            case 1:
                rc.writeSharedArray(3, (rc.readSharedArray(3) & 64527) | (value << 4));
                break;
            case 2:
                rc.writeSharedArray(4, (rc.readSharedArray(4) & 49407) | (value << 8));
                break;
        }
    }

    public static void writeBPOurSpawnYCoord(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                writeToBufferPool(2, (bufferPool[2] & 65472) | (value));
                break;
            case 1:
                writeToBufferPool(3, (bufferPool[3] & 64527) | (value << 4));
                break;
            case 2:
                writeToBufferPool(4, (bufferPool[4] & 49407) | (value << 8));
                break;
        }
    }

    public static int readOurSpawnAll(int idx) throws GameActionException {
        switch (idx) {
            case 0:
                return (rc.readSharedArray(2) & 4095);
            case 1:
                return (rc.readSharedArray(3) & 65520) >>> 4;
            case 2:
                return ((rc.readSharedArray(3) & 15) << 8) + ((rc.readSharedArray(4) & 65280) >>> 8);
            default:
                return -1;
        }
    }

    public static void writeOurSpawnAll(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                rc.writeSharedArray(2, (rc.readSharedArray(2) & 61440) | (value));
                break;
            case 1:
                rc.writeSharedArray(3, (rc.readSharedArray(3) & 15) | (value << 4));
                break;
            case 2:
                rc.writeSharedArray(3, (rc.readSharedArray(3) & 65520) | ((value & 3840) >>> 8));
                rc.writeSharedArray(4, (rc.readSharedArray(4) & 255) | ((value & 255) << 8));
                break;
        }
    }

    public static void writeBPOurSpawnAll(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                writeToBufferPool(2, (bufferPool[2] & 61440) | (value));
                break;
            case 1:
                writeToBufferPool(3, (bufferPool[3] & 15) | (value << 4));
                break;
            case 2:
                writeToBufferPool(3, (bufferPool[3] & 65520) | ((value & 3840) >>> 8));
                writeToBufferPool(4, (bufferPool[4] & 255) | ((value & 255) << 8));
                break;
        }
    }

    public static int readEnemyFlagXCoord(int idx) throws GameActionException {
        switch (idx) {
            case 0:
                return (rc.readSharedArray(4) & 252) >>> 2;
            case 1:
                return (rc.readSharedArray(5) & 4032) >>> 6;
            case 2:
                return (rc.readSharedArray(6) & 64512) >>> 10;
            default:
                return -1;
        }
    }

    public static void writeEnemyFlagXCoord(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                rc.writeSharedArray(4, (rc.readSharedArray(4) & 65283) | (value << 2));
                break;
            case 1:
                rc.writeSharedArray(5, (rc.readSharedArray(5) & 61503) | (value << 6));
                break;
            case 2:
                rc.writeSharedArray(6, (rc.readSharedArray(6) & 1023) | (value << 10));
                break;
        }
    }

    public static void writeBPEnemyFlagXCoord(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                writeToBufferPool(4, (bufferPool[4] & 65283) | (value << 2));
                break;
            case 1:
                writeToBufferPool(5, (bufferPool[5] & 61503) | (value << 6));
                break;
            case 2:
                writeToBufferPool(6, (bufferPool[6] & 1023) | (value << 10));
                break;
        }
    }

    public static int readEnemyFlagYCoord(int idx) throws GameActionException {
        switch (idx) {
            case 0:
                return ((rc.readSharedArray(4) & 3) << 4) + ((rc.readSharedArray(5) & 61440) >>> 12);
            case 1:
                return (rc.readSharedArray(5) & 63);
            case 2:
                return (rc.readSharedArray(6) & 1008) >>> 4;
            default:
                return -1;
        }
    }

    public static void writeEnemyFlagYCoord(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                rc.writeSharedArray(4, (rc.readSharedArray(4) & 65532) | ((value & 48) >>> 4));
                rc.writeSharedArray(5, (rc.readSharedArray(5) & 4095) | ((value & 15) << 12));
                break;
            case 1:
                rc.writeSharedArray(5, (rc.readSharedArray(5) & 65472) | (value));
                break;
            case 2:
                rc.writeSharedArray(6, (rc.readSharedArray(6) & 64527) | (value << 4));
                break;
        }
    }

    public static void writeBPEnemyFlagYCoord(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                writeToBufferPool(4, (bufferPool[4] & 65532) | ((value & 48) >>> 4));
                writeToBufferPool(5, (bufferPool[5] & 4095) | ((value & 15) << 12));
                break;
            case 1:
                writeToBufferPool(5, (bufferPool[5] & 65472) | (value));
                break;
            case 2:
                writeToBufferPool(6, (bufferPool[6] & 64527) | (value << 4));
                break;
        }
    }

    public static int readEnemyFlagAll(int idx) throws GameActionException {
        switch (idx) {
            case 0:
                return ((rc.readSharedArray(4) & 255) << 4) + ((rc.readSharedArray(5) & 61440) >>> 12);
            case 1:
                return (rc.readSharedArray(5) & 4095);
            case 2:
                return (rc.readSharedArray(6) & 65520) >>> 4;
            default:
                return -1;
        }
    }

    public static void writeEnemyFlagAll(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                rc.writeSharedArray(4, (rc.readSharedArray(4) & 65280) | ((value & 4080) >>> 4));
                rc.writeSharedArray(5, (rc.readSharedArray(5) & 4095) | ((value & 15) << 12));
                break;
            case 1:
                rc.writeSharedArray(5, (rc.readSharedArray(5) & 61440) | (value));
                break;
            case 2:
                rc.writeSharedArray(6, (rc.readSharedArray(6) & 15) | (value << 4));
                break;
        }
    }

    public static void writeBPEnemyFlagAll(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                writeToBufferPool(4, (bufferPool[4] & 65280) | ((value & 4080) >>> 4));
                writeToBufferPool(5, (bufferPool[5] & 4095) | ((value & 15) << 12));
                break;
            case 1:
                writeToBufferPool(5, (bufferPool[5] & 61440) | (value));
                break;
            case 2:
                writeToBufferPool(6, (bufferPool[6] & 15) | (value << 4));
                break;
        }
    }

    public static int readUnitNum() throws GameActionException {
        return ((rc.readSharedArray(6) & 15) << 2) + ((rc.readSharedArray(7) & 49152) >>> 14);
    }

    public static void writeUnitNum(int value) throws GameActionException {
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 16383) | ((value & 3) << 14));
    }

    public static void writeBPUnitNum(int value) throws GameActionException {
        writeToBufferPool(6, (bufferPool[6] & 65520) | ((value & 60) >>> 2));
        writeToBufferPool(7, (bufferPool[7] & 16383) | ((value & 3) << 14));
    }

    public static int readUnitAll() throws GameActionException {
        return ((rc.readSharedArray(6) & 15) << 2) + ((rc.readSharedArray(7) & 49152) >>> 14);
    }

    public static void writeUnitAll(int value) throws GameActionException {
        rc.writeSharedArray(6, (rc.readSharedArray(6) & 65520) | ((value & 60) >>> 2));
        rc.writeSharedArray(7, (rc.readSharedArray(7) & 16383) | ((value & 3) << 14));
    }

    public static void writeBPUnitAll(int value) throws GameActionException {
        writeToBufferPool(6, (bufferPool[6] & 65520) | ((value & 60) >>> 2));
        writeToBufferPool(7, (bufferPool[7] & 16383) | ((value & 3) << 14));
    }

    public static int readSectorControlStatus(int idx) throws GameActionException {
        switch (idx) {
            case 0:
                return (rc.readSharedArray(7) & 14336) >>> 11;
            case 1:
                return (rc.readSharedArray(7) & 1792) >>> 8;
            case 2:
                return (rc.readSharedArray(7) & 224) >>> 5;
            case 3:
                return (rc.readSharedArray(7) & 28) >>> 2;
            case 4:
                return ((rc.readSharedArray(7) & 3) << 1) + ((rc.readSharedArray(8) & 32768) >>> 15);
            case 5:
                return (rc.readSharedArray(8) & 28672) >>> 12;
            case 6:
                return (rc.readSharedArray(8) & 3584) >>> 9;
            case 7:
                return (rc.readSharedArray(8) & 448) >>> 6;
            case 8:
                return (rc.readSharedArray(8) & 56) >>> 3;
            case 9:
                return (rc.readSharedArray(8) & 7);
            case 10:
                return (rc.readSharedArray(9) & 57344) >>> 13;
            case 11:
                return (rc.readSharedArray(9) & 7168) >>> 10;
            case 12:
                return (rc.readSharedArray(9) & 896) >>> 7;
            case 13:
                return (rc.readSharedArray(9) & 112) >>> 4;
            case 14:
                return (rc.readSharedArray(9) & 14) >>> 1;
            case 15:
                return ((rc.readSharedArray(9) & 1) << 2) + ((rc.readSharedArray(10) & 49152) >>> 14);
            case 16:
                return (rc.readSharedArray(10) & 14336) >>> 11;
            case 17:
                return (rc.readSharedArray(10) & 1792) >>> 8;
            case 18:
                return (rc.readSharedArray(10) & 224) >>> 5;
            case 19:
                return (rc.readSharedArray(10) & 28) >>> 2;
            case 20:
                return ((rc.readSharedArray(10) & 3) << 1) + ((rc.readSharedArray(11) & 32768) >>> 15);
            case 21:
                return (rc.readSharedArray(11) & 28672) >>> 12;
            case 22:
                return (rc.readSharedArray(11) & 3584) >>> 9;
            case 23:
                return (rc.readSharedArray(11) & 448) >>> 6;
            case 24:
                return (rc.readSharedArray(11) & 56) >>> 3;
            case 25:
                return (rc.readSharedArray(11) & 7);
            case 26:
                return (rc.readSharedArray(12) & 57344) >>> 13;
            case 27:
                return (rc.readSharedArray(12) & 7168) >>> 10;
            case 28:
                return (rc.readSharedArray(12) & 896) >>> 7;
            case 29:
                return (rc.readSharedArray(12) & 112) >>> 4;
            case 30:
                return (rc.readSharedArray(12) & 14) >>> 1;
            case 31:
                return ((rc.readSharedArray(12) & 1) << 2) + ((rc.readSharedArray(13) & 49152) >>> 14);
            case 32:
                return (rc.readSharedArray(13) & 14336) >>> 11;
            case 33:
                return (rc.readSharedArray(13) & 1792) >>> 8;
            case 34:
                return (rc.readSharedArray(13) & 224) >>> 5;
            case 35:
                return (rc.readSharedArray(13) & 28) >>> 2;
            case 36:
                return ((rc.readSharedArray(13) & 3) << 1) + ((rc.readSharedArray(14) & 32768) >>> 15);
            case 37:
                return (rc.readSharedArray(14) & 28672) >>> 12;
            case 38:
                return (rc.readSharedArray(14) & 3584) >>> 9;
            case 39:
                return (rc.readSharedArray(14) & 448) >>> 6;
            case 40:
                return (rc.readSharedArray(14) & 56) >>> 3;
            case 41:
                return (rc.readSharedArray(14) & 7);
            case 42:
                return (rc.readSharedArray(15) & 57344) >>> 13;
            case 43:
                return (rc.readSharedArray(15) & 7168) >>> 10;
            case 44:
                return (rc.readSharedArray(15) & 896) >>> 7;
            case 45:
                return (rc.readSharedArray(15) & 112) >>> 4;
            case 46:
                return (rc.readSharedArray(15) & 14) >>> 1;
            case 47:
                return ((rc.readSharedArray(15) & 1) << 2) + ((rc.readSharedArray(16) & 49152) >>> 14);
            case 48:
                return (rc.readSharedArray(16) & 14336) >>> 11;
            case 49:
                return (rc.readSharedArray(16) & 1792) >>> 8;
            case 50:
                return (rc.readSharedArray(16) & 224) >>> 5;
            case 51:
                return (rc.readSharedArray(16) & 28) >>> 2;
            case 52:
                return ((rc.readSharedArray(16) & 3) << 1) + ((rc.readSharedArray(17) & 32768) >>> 15);
            case 53:
                return (rc.readSharedArray(17) & 28672) >>> 12;
            case 54:
                return (rc.readSharedArray(17) & 3584) >>> 9;
            case 55:
                return (rc.readSharedArray(17) & 448) >>> 6;
            case 56:
                return (rc.readSharedArray(17) & 56) >>> 3;
            case 57:
                return (rc.readSharedArray(17) & 7);
            case 58:
                return (rc.readSharedArray(18) & 57344) >>> 13;
            case 59:
                return (rc.readSharedArray(18) & 7168) >>> 10;
            case 60:
                return (rc.readSharedArray(18) & 896) >>> 7;
            case 61:
                return (rc.readSharedArray(18) & 112) >>> 4;
            case 62:
                return (rc.readSharedArray(18) & 14) >>> 1;
            case 63:
                return ((rc.readSharedArray(18) & 1) << 2) + ((rc.readSharedArray(19) & 49152) >>> 14);
            case 64:
                return (rc.readSharedArray(19) & 14336) >>> 11;
            case 65:
                return (rc.readSharedArray(19) & 1792) >>> 8;
            case 66:
                return (rc.readSharedArray(19) & 224) >>> 5;
            case 67:
                return (rc.readSharedArray(19) & 28) >>> 2;
            case 68:
                return ((rc.readSharedArray(19) & 3) << 1) + ((rc.readSharedArray(20) & 32768) >>> 15);
            case 69:
                return (rc.readSharedArray(20) & 28672) >>> 12;
            case 70:
                return (rc.readSharedArray(20) & 3584) >>> 9;
            case 71:
                return (rc.readSharedArray(20) & 448) >>> 6;
            case 72:
                return (rc.readSharedArray(20) & 56) >>> 3;
            case 73:
                return (rc.readSharedArray(20) & 7);
            case 74:
                return (rc.readSharedArray(21) & 57344) >>> 13;
            case 75:
                return (rc.readSharedArray(21) & 7168) >>> 10;
            case 76:
                return (rc.readSharedArray(21) & 896) >>> 7;
            case 77:
                return (rc.readSharedArray(21) & 112) >>> 4;
            case 78:
                return (rc.readSharedArray(21) & 14) >>> 1;
            case 79:
                return ((rc.readSharedArray(21) & 1) << 2) + ((rc.readSharedArray(22) & 49152) >>> 14);
            case 80:
                return (rc.readSharedArray(22) & 14336) >>> 11;
            case 81:
                return (rc.readSharedArray(22) & 1792) >>> 8;
            case 82:
                return (rc.readSharedArray(22) & 224) >>> 5;
            case 83:
                return (rc.readSharedArray(22) & 28) >>> 2;
            case 84:
                return ((rc.readSharedArray(22) & 3) << 1) + ((rc.readSharedArray(23) & 32768) >>> 15);
            case 85:
                return (rc.readSharedArray(23) & 28672) >>> 12;
            case 86:
                return (rc.readSharedArray(23) & 3584) >>> 9;
            case 87:
                return (rc.readSharedArray(23) & 448) >>> 6;
            case 88:
                return (rc.readSharedArray(23) & 56) >>> 3;
            case 89:
                return (rc.readSharedArray(23) & 7);
            case 90:
                return (rc.readSharedArray(24) & 57344) >>> 13;
            case 91:
                return (rc.readSharedArray(24) & 7168) >>> 10;
            case 92:
                return (rc.readSharedArray(24) & 896) >>> 7;
            case 93:
                return (rc.readSharedArray(24) & 112) >>> 4;
            case 94:
                return (rc.readSharedArray(24) & 14) >>> 1;
            case 95:
                return ((rc.readSharedArray(24) & 1) << 2) + ((rc.readSharedArray(25) & 49152) >>> 14);
            case 96:
                return (rc.readSharedArray(25) & 14336) >>> 11;
            case 97:
                return (rc.readSharedArray(25) & 1792) >>> 8;
            case 98:
                return (rc.readSharedArray(25) & 224) >>> 5;
            case 99:
                return (rc.readSharedArray(25) & 28) >>> 2;
            default:
                return -1;
        }
    }

    public static void writeSectorControlStatus(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                rc.writeSharedArray(7, (rc.readSharedArray(7) & 51199) | (value << 11));
                break;
            case 1:
                rc.writeSharedArray(7, (rc.readSharedArray(7) & 63743) | (value << 8));
                break;
            case 2:
                rc.writeSharedArray(7, (rc.readSharedArray(7) & 65311) | (value << 5));
                break;
            case 3:
                rc.writeSharedArray(7, (rc.readSharedArray(7) & 65507) | (value << 2));
                break;
            case 4:
                rc.writeSharedArray(7, (rc.readSharedArray(7) & 65532) | ((value & 6) >>> 1));
                rc.writeSharedArray(8, (rc.readSharedArray(8) & 32767) | ((value & 1) << 15));
                break;
            case 5:
                rc.writeSharedArray(8, (rc.readSharedArray(8) & 36863) | (value << 12));
                break;
            case 6:
                rc.writeSharedArray(8, (rc.readSharedArray(8) & 61951) | (value << 9));
                break;
            case 7:
                rc.writeSharedArray(8, (rc.readSharedArray(8) & 65087) | (value << 6));
                break;
            case 8:
                rc.writeSharedArray(8, (rc.readSharedArray(8) & 65479) | (value << 3));
                break;
            case 9:
                rc.writeSharedArray(8, (rc.readSharedArray(8) & 65528) | (value));
                break;
            case 10:
                rc.writeSharedArray(9, (rc.readSharedArray(9) & 8191) | (value << 13));
                break;
            case 11:
                rc.writeSharedArray(9, (rc.readSharedArray(9) & 58367) | (value << 10));
                break;
            case 12:
                rc.writeSharedArray(9, (rc.readSharedArray(9) & 64639) | (value << 7));
                break;
            case 13:
                rc.writeSharedArray(9, (rc.readSharedArray(9) & 65423) | (value << 4));
                break;
            case 14:
                rc.writeSharedArray(9, (rc.readSharedArray(9) & 65521) | (value << 1));
                break;
            case 15:
                rc.writeSharedArray(9, (rc.readSharedArray(9) & 65534) | ((value & 4) >>> 2));
                rc.writeSharedArray(10, (rc.readSharedArray(10) & 16383) | ((value & 3) << 14));
                break;
            case 16:
                rc.writeSharedArray(10, (rc.readSharedArray(10) & 51199) | (value << 11));
                break;
            case 17:
                rc.writeSharedArray(10, (rc.readSharedArray(10) & 63743) | (value << 8));
                break;
            case 18:
                rc.writeSharedArray(10, (rc.readSharedArray(10) & 65311) | (value << 5));
                break;
            case 19:
                rc.writeSharedArray(10, (rc.readSharedArray(10) & 65507) | (value << 2));
                break;
            case 20:
                rc.writeSharedArray(10, (rc.readSharedArray(10) & 65532) | ((value & 6) >>> 1));
                rc.writeSharedArray(11, (rc.readSharedArray(11) & 32767) | ((value & 1) << 15));
                break;
            case 21:
                rc.writeSharedArray(11, (rc.readSharedArray(11) & 36863) | (value << 12));
                break;
            case 22:
                rc.writeSharedArray(11, (rc.readSharedArray(11) & 61951) | (value << 9));
                break;
            case 23:
                rc.writeSharedArray(11, (rc.readSharedArray(11) & 65087) | (value << 6));
                break;
            case 24:
                rc.writeSharedArray(11, (rc.readSharedArray(11) & 65479) | (value << 3));
                break;
            case 25:
                rc.writeSharedArray(11, (rc.readSharedArray(11) & 65528) | (value));
                break;
            case 26:
                rc.writeSharedArray(12, (rc.readSharedArray(12) & 8191) | (value << 13));
                break;
            case 27:
                rc.writeSharedArray(12, (rc.readSharedArray(12) & 58367) | (value << 10));
                break;
            case 28:
                rc.writeSharedArray(12, (rc.readSharedArray(12) & 64639) | (value << 7));
                break;
            case 29:
                rc.writeSharedArray(12, (rc.readSharedArray(12) & 65423) | (value << 4));
                break;
            case 30:
                rc.writeSharedArray(12, (rc.readSharedArray(12) & 65521) | (value << 1));
                break;
            case 31:
                rc.writeSharedArray(12, (rc.readSharedArray(12) & 65534) | ((value & 4) >>> 2));
                rc.writeSharedArray(13, (rc.readSharedArray(13) & 16383) | ((value & 3) << 14));
                break;
            case 32:
                rc.writeSharedArray(13, (rc.readSharedArray(13) & 51199) | (value << 11));
                break;
            case 33:
                rc.writeSharedArray(13, (rc.readSharedArray(13) & 63743) | (value << 8));
                break;
            case 34:
                rc.writeSharedArray(13, (rc.readSharedArray(13) & 65311) | (value << 5));
                break;
            case 35:
                rc.writeSharedArray(13, (rc.readSharedArray(13) & 65507) | (value << 2));
                break;
            case 36:
                rc.writeSharedArray(13, (rc.readSharedArray(13) & 65532) | ((value & 6) >>> 1));
                rc.writeSharedArray(14, (rc.readSharedArray(14) & 32767) | ((value & 1) << 15));
                break;
            case 37:
                rc.writeSharedArray(14, (rc.readSharedArray(14) & 36863) | (value << 12));
                break;
            case 38:
                rc.writeSharedArray(14, (rc.readSharedArray(14) & 61951) | (value << 9));
                break;
            case 39:
                rc.writeSharedArray(14, (rc.readSharedArray(14) & 65087) | (value << 6));
                break;
            case 40:
                rc.writeSharedArray(14, (rc.readSharedArray(14) & 65479) | (value << 3));
                break;
            case 41:
                rc.writeSharedArray(14, (rc.readSharedArray(14) & 65528) | (value));
                break;
            case 42:
                rc.writeSharedArray(15, (rc.readSharedArray(15) & 8191) | (value << 13));
                break;
            case 43:
                rc.writeSharedArray(15, (rc.readSharedArray(15) & 58367) | (value << 10));
                break;
            case 44:
                rc.writeSharedArray(15, (rc.readSharedArray(15) & 64639) | (value << 7));
                break;
            case 45:
                rc.writeSharedArray(15, (rc.readSharedArray(15) & 65423) | (value << 4));
                break;
            case 46:
                rc.writeSharedArray(15, (rc.readSharedArray(15) & 65521) | (value << 1));
                break;
            case 47:
                rc.writeSharedArray(15, (rc.readSharedArray(15) & 65534) | ((value & 4) >>> 2));
                rc.writeSharedArray(16, (rc.readSharedArray(16) & 16383) | ((value & 3) << 14));
                break;
            case 48:
                rc.writeSharedArray(16, (rc.readSharedArray(16) & 51199) | (value << 11));
                break;
            case 49:
                rc.writeSharedArray(16, (rc.readSharedArray(16) & 63743) | (value << 8));
                break;
            case 50:
                rc.writeSharedArray(16, (rc.readSharedArray(16) & 65311) | (value << 5));
                break;
            case 51:
                rc.writeSharedArray(16, (rc.readSharedArray(16) & 65507) | (value << 2));
                break;
            case 52:
                rc.writeSharedArray(16, (rc.readSharedArray(16) & 65532) | ((value & 6) >>> 1));
                rc.writeSharedArray(17, (rc.readSharedArray(17) & 32767) | ((value & 1) << 15));
                break;
            case 53:
                rc.writeSharedArray(17, (rc.readSharedArray(17) & 36863) | (value << 12));
                break;
            case 54:
                rc.writeSharedArray(17, (rc.readSharedArray(17) & 61951) | (value << 9));
                break;
            case 55:
                rc.writeSharedArray(17, (rc.readSharedArray(17) & 65087) | (value << 6));
                break;
            case 56:
                rc.writeSharedArray(17, (rc.readSharedArray(17) & 65479) | (value << 3));
                break;
            case 57:
                rc.writeSharedArray(17, (rc.readSharedArray(17) & 65528) | (value));
                break;
            case 58:
                rc.writeSharedArray(18, (rc.readSharedArray(18) & 8191) | (value << 13));
                break;
            case 59:
                rc.writeSharedArray(18, (rc.readSharedArray(18) & 58367) | (value << 10));
                break;
            case 60:
                rc.writeSharedArray(18, (rc.readSharedArray(18) & 64639) | (value << 7));
                break;
            case 61:
                rc.writeSharedArray(18, (rc.readSharedArray(18) & 65423) | (value << 4));
                break;
            case 62:
                rc.writeSharedArray(18, (rc.readSharedArray(18) & 65521) | (value << 1));
                break;
            case 63:
                rc.writeSharedArray(18, (rc.readSharedArray(18) & 65534) | ((value & 4) >>> 2));
                rc.writeSharedArray(19, (rc.readSharedArray(19) & 16383) | ((value & 3) << 14));
                break;
            case 64:
                rc.writeSharedArray(19, (rc.readSharedArray(19) & 51199) | (value << 11));
                break;
            case 65:
                rc.writeSharedArray(19, (rc.readSharedArray(19) & 63743) | (value << 8));
                break;
            case 66:
                rc.writeSharedArray(19, (rc.readSharedArray(19) & 65311) | (value << 5));
                break;
            case 67:
                rc.writeSharedArray(19, (rc.readSharedArray(19) & 65507) | (value << 2));
                break;
            case 68:
                rc.writeSharedArray(19, (rc.readSharedArray(19) & 65532) | ((value & 6) >>> 1));
                rc.writeSharedArray(20, (rc.readSharedArray(20) & 32767) | ((value & 1) << 15));
                break;
            case 69:
                rc.writeSharedArray(20, (rc.readSharedArray(20) & 36863) | (value << 12));
                break;
            case 70:
                rc.writeSharedArray(20, (rc.readSharedArray(20) & 61951) | (value << 9));
                break;
            case 71:
                rc.writeSharedArray(20, (rc.readSharedArray(20) & 65087) | (value << 6));
                break;
            case 72:
                rc.writeSharedArray(20, (rc.readSharedArray(20) & 65479) | (value << 3));
                break;
            case 73:
                rc.writeSharedArray(20, (rc.readSharedArray(20) & 65528) | (value));
                break;
            case 74:
                rc.writeSharedArray(21, (rc.readSharedArray(21) & 8191) | (value << 13));
                break;
            case 75:
                rc.writeSharedArray(21, (rc.readSharedArray(21) & 58367) | (value << 10));
                break;
            case 76:
                rc.writeSharedArray(21, (rc.readSharedArray(21) & 64639) | (value << 7));
                break;
            case 77:
                rc.writeSharedArray(21, (rc.readSharedArray(21) & 65423) | (value << 4));
                break;
            case 78:
                rc.writeSharedArray(21, (rc.readSharedArray(21) & 65521) | (value << 1));
                break;
            case 79:
                rc.writeSharedArray(21, (rc.readSharedArray(21) & 65534) | ((value & 4) >>> 2));
                rc.writeSharedArray(22, (rc.readSharedArray(22) & 16383) | ((value & 3) << 14));
                break;
            case 80:
                rc.writeSharedArray(22, (rc.readSharedArray(22) & 51199) | (value << 11));
                break;
            case 81:
                rc.writeSharedArray(22, (rc.readSharedArray(22) & 63743) | (value << 8));
                break;
            case 82:
                rc.writeSharedArray(22, (rc.readSharedArray(22) & 65311) | (value << 5));
                break;
            case 83:
                rc.writeSharedArray(22, (rc.readSharedArray(22) & 65507) | (value << 2));
                break;
            case 84:
                rc.writeSharedArray(22, (rc.readSharedArray(22) & 65532) | ((value & 6) >>> 1));
                rc.writeSharedArray(23, (rc.readSharedArray(23) & 32767) | ((value & 1) << 15));
                break;
            case 85:
                rc.writeSharedArray(23, (rc.readSharedArray(23) & 36863) | (value << 12));
                break;
            case 86:
                rc.writeSharedArray(23, (rc.readSharedArray(23) & 61951) | (value << 9));
                break;
            case 87:
                rc.writeSharedArray(23, (rc.readSharedArray(23) & 65087) | (value << 6));
                break;
            case 88:
                rc.writeSharedArray(23, (rc.readSharedArray(23) & 65479) | (value << 3));
                break;
            case 89:
                rc.writeSharedArray(23, (rc.readSharedArray(23) & 65528) | (value));
                break;
            case 90:
                rc.writeSharedArray(24, (rc.readSharedArray(24) & 8191) | (value << 13));
                break;
            case 91:
                rc.writeSharedArray(24, (rc.readSharedArray(24) & 58367) | (value << 10));
                break;
            case 92:
                rc.writeSharedArray(24, (rc.readSharedArray(24) & 64639) | (value << 7));
                break;
            case 93:
                rc.writeSharedArray(24, (rc.readSharedArray(24) & 65423) | (value << 4));
                break;
            case 94:
                rc.writeSharedArray(24, (rc.readSharedArray(24) & 65521) | (value << 1));
                break;
            case 95:
                rc.writeSharedArray(24, (rc.readSharedArray(24) & 65534) | ((value & 4) >>> 2));
                rc.writeSharedArray(25, (rc.readSharedArray(25) & 16383) | ((value & 3) << 14));
                break;
            case 96:
                rc.writeSharedArray(25, (rc.readSharedArray(25) & 51199) | (value << 11));
                break;
            case 97:
                rc.writeSharedArray(25, (rc.readSharedArray(25) & 63743) | (value << 8));
                break;
            case 98:
                rc.writeSharedArray(25, (rc.readSharedArray(25) & 65311) | (value << 5));
                break;
            case 99:
                rc.writeSharedArray(25, (rc.readSharedArray(25) & 65507) | (value << 2));
                break;
        }
    }

    public static void writeBPSectorControlStatus(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                writeToBufferPool(7, (bufferPool[7] & 51199) | (value << 11));
                break;
            case 1:
                writeToBufferPool(7, (bufferPool[7] & 63743) | (value << 8));
                break;
            case 2:
                writeToBufferPool(7, (bufferPool[7] & 65311) | (value << 5));
                break;
            case 3:
                writeToBufferPool(7, (bufferPool[7] & 65507) | (value << 2));
                break;
            case 4:
                writeToBufferPool(7, (bufferPool[7] & 65532) | ((value & 6) >>> 1));
                writeToBufferPool(8, (bufferPool[8] & 32767) | ((value & 1) << 15));
                break;
            case 5:
                writeToBufferPool(8, (bufferPool[8] & 36863) | (value << 12));
                break;
            case 6:
                writeToBufferPool(8, (bufferPool[8] & 61951) | (value << 9));
                break;
            case 7:
                writeToBufferPool(8, (bufferPool[8] & 65087) | (value << 6));
                break;
            case 8:
                writeToBufferPool(8, (bufferPool[8] & 65479) | (value << 3));
                break;
            case 9:
                writeToBufferPool(8, (bufferPool[8] & 65528) | (value));
                break;
            case 10:
                writeToBufferPool(9, (bufferPool[9] & 8191) | (value << 13));
                break;
            case 11:
                writeToBufferPool(9, (bufferPool[9] & 58367) | (value << 10));
                break;
            case 12:
                writeToBufferPool(9, (bufferPool[9] & 64639) | (value << 7));
                break;
            case 13:
                writeToBufferPool(9, (bufferPool[9] & 65423) | (value << 4));
                break;
            case 14:
                writeToBufferPool(9, (bufferPool[9] & 65521) | (value << 1));
                break;
            case 15:
                writeToBufferPool(9, (bufferPool[9] & 65534) | ((value & 4) >>> 2));
                writeToBufferPool(10, (bufferPool[10] & 16383) | ((value & 3) << 14));
                break;
            case 16:
                writeToBufferPool(10, (bufferPool[10] & 51199) | (value << 11));
                break;
            case 17:
                writeToBufferPool(10, (bufferPool[10] & 63743) | (value << 8));
                break;
            case 18:
                writeToBufferPool(10, (bufferPool[10] & 65311) | (value << 5));
                break;
            case 19:
                writeToBufferPool(10, (bufferPool[10] & 65507) | (value << 2));
                break;
            case 20:
                writeToBufferPool(10, (bufferPool[10] & 65532) | ((value & 6) >>> 1));
                writeToBufferPool(11, (bufferPool[11] & 32767) | ((value & 1) << 15));
                break;
            case 21:
                writeToBufferPool(11, (bufferPool[11] & 36863) | (value << 12));
                break;
            case 22:
                writeToBufferPool(11, (bufferPool[11] & 61951) | (value << 9));
                break;
            case 23:
                writeToBufferPool(11, (bufferPool[11] & 65087) | (value << 6));
                break;
            case 24:
                writeToBufferPool(11, (bufferPool[11] & 65479) | (value << 3));
                break;
            case 25:
                writeToBufferPool(11, (bufferPool[11] & 65528) | (value));
                break;
            case 26:
                writeToBufferPool(12, (bufferPool[12] & 8191) | (value << 13));
                break;
            case 27:
                writeToBufferPool(12, (bufferPool[12] & 58367) | (value << 10));
                break;
            case 28:
                writeToBufferPool(12, (bufferPool[12] & 64639) | (value << 7));
                break;
            case 29:
                writeToBufferPool(12, (bufferPool[12] & 65423) | (value << 4));
                break;
            case 30:
                writeToBufferPool(12, (bufferPool[12] & 65521) | (value << 1));
                break;
            case 31:
                writeToBufferPool(12, (bufferPool[12] & 65534) | ((value & 4) >>> 2));
                writeToBufferPool(13, (bufferPool[13] & 16383) | ((value & 3) << 14));
                break;
            case 32:
                writeToBufferPool(13, (bufferPool[13] & 51199) | (value << 11));
                break;
            case 33:
                writeToBufferPool(13, (bufferPool[13] & 63743) | (value << 8));
                break;
            case 34:
                writeToBufferPool(13, (bufferPool[13] & 65311) | (value << 5));
                break;
            case 35:
                writeToBufferPool(13, (bufferPool[13] & 65507) | (value << 2));
                break;
            case 36:
                writeToBufferPool(13, (bufferPool[13] & 65532) | ((value & 6) >>> 1));
                writeToBufferPool(14, (bufferPool[14] & 32767) | ((value & 1) << 15));
                break;
            case 37:
                writeToBufferPool(14, (bufferPool[14] & 36863) | (value << 12));
                break;
            case 38:
                writeToBufferPool(14, (bufferPool[14] & 61951) | (value << 9));
                break;
            case 39:
                writeToBufferPool(14, (bufferPool[14] & 65087) | (value << 6));
                break;
            case 40:
                writeToBufferPool(14, (bufferPool[14] & 65479) | (value << 3));
                break;
            case 41:
                writeToBufferPool(14, (bufferPool[14] & 65528) | (value));
                break;
            case 42:
                writeToBufferPool(15, (bufferPool[15] & 8191) | (value << 13));
                break;
            case 43:
                writeToBufferPool(15, (bufferPool[15] & 58367) | (value << 10));
                break;
            case 44:
                writeToBufferPool(15, (bufferPool[15] & 64639) | (value << 7));
                break;
            case 45:
                writeToBufferPool(15, (bufferPool[15] & 65423) | (value << 4));
                break;
            case 46:
                writeToBufferPool(15, (bufferPool[15] & 65521) | (value << 1));
                break;
            case 47:
                writeToBufferPool(15, (bufferPool[15] & 65534) | ((value & 4) >>> 2));
                writeToBufferPool(16, (bufferPool[16] & 16383) | ((value & 3) << 14));
                break;
            case 48:
                writeToBufferPool(16, (bufferPool[16] & 51199) | (value << 11));
                break;
            case 49:
                writeToBufferPool(16, (bufferPool[16] & 63743) | (value << 8));
                break;
            case 50:
                writeToBufferPool(16, (bufferPool[16] & 65311) | (value << 5));
                break;
            case 51:
                writeToBufferPool(16, (bufferPool[16] & 65507) | (value << 2));
                break;
            case 52:
                writeToBufferPool(16, (bufferPool[16] & 65532) | ((value & 6) >>> 1));
                writeToBufferPool(17, (bufferPool[17] & 32767) | ((value & 1) << 15));
                break;
            case 53:
                writeToBufferPool(17, (bufferPool[17] & 36863) | (value << 12));
                break;
            case 54:
                writeToBufferPool(17, (bufferPool[17] & 61951) | (value << 9));
                break;
            case 55:
                writeToBufferPool(17, (bufferPool[17] & 65087) | (value << 6));
                break;
            case 56:
                writeToBufferPool(17, (bufferPool[17] & 65479) | (value << 3));
                break;
            case 57:
                writeToBufferPool(17, (bufferPool[17] & 65528) | (value));
                break;
            case 58:
                writeToBufferPool(18, (bufferPool[18] & 8191) | (value << 13));
                break;
            case 59:
                writeToBufferPool(18, (bufferPool[18] & 58367) | (value << 10));
                break;
            case 60:
                writeToBufferPool(18, (bufferPool[18] & 64639) | (value << 7));
                break;
            case 61:
                writeToBufferPool(18, (bufferPool[18] & 65423) | (value << 4));
                break;
            case 62:
                writeToBufferPool(18, (bufferPool[18] & 65521) | (value << 1));
                break;
            case 63:
                writeToBufferPool(18, (bufferPool[18] & 65534) | ((value & 4) >>> 2));
                writeToBufferPool(19, (bufferPool[19] & 16383) | ((value & 3) << 14));
                break;
            case 64:
                writeToBufferPool(19, (bufferPool[19] & 51199) | (value << 11));
                break;
            case 65:
                writeToBufferPool(19, (bufferPool[19] & 63743) | (value << 8));
                break;
            case 66:
                writeToBufferPool(19, (bufferPool[19] & 65311) | (value << 5));
                break;
            case 67:
                writeToBufferPool(19, (bufferPool[19] & 65507) | (value << 2));
                break;
            case 68:
                writeToBufferPool(19, (bufferPool[19] & 65532) | ((value & 6) >>> 1));
                writeToBufferPool(20, (bufferPool[20] & 32767) | ((value & 1) << 15));
                break;
            case 69:
                writeToBufferPool(20, (bufferPool[20] & 36863) | (value << 12));
                break;
            case 70:
                writeToBufferPool(20, (bufferPool[20] & 61951) | (value << 9));
                break;
            case 71:
                writeToBufferPool(20, (bufferPool[20] & 65087) | (value << 6));
                break;
            case 72:
                writeToBufferPool(20, (bufferPool[20] & 65479) | (value << 3));
                break;
            case 73:
                writeToBufferPool(20, (bufferPool[20] & 65528) | (value));
                break;
            case 74:
                writeToBufferPool(21, (bufferPool[21] & 8191) | (value << 13));
                break;
            case 75:
                writeToBufferPool(21, (bufferPool[21] & 58367) | (value << 10));
                break;
            case 76:
                writeToBufferPool(21, (bufferPool[21] & 64639) | (value << 7));
                break;
            case 77:
                writeToBufferPool(21, (bufferPool[21] & 65423) | (value << 4));
                break;
            case 78:
                writeToBufferPool(21, (bufferPool[21] & 65521) | (value << 1));
                break;
            case 79:
                writeToBufferPool(21, (bufferPool[21] & 65534) | ((value & 4) >>> 2));
                writeToBufferPool(22, (bufferPool[22] & 16383) | ((value & 3) << 14));
                break;
            case 80:
                writeToBufferPool(22, (bufferPool[22] & 51199) | (value << 11));
                break;
            case 81:
                writeToBufferPool(22, (bufferPool[22] & 63743) | (value << 8));
                break;
            case 82:
                writeToBufferPool(22, (bufferPool[22] & 65311) | (value << 5));
                break;
            case 83:
                writeToBufferPool(22, (bufferPool[22] & 65507) | (value << 2));
                break;
            case 84:
                writeToBufferPool(22, (bufferPool[22] & 65532) | ((value & 6) >>> 1));
                writeToBufferPool(23, (bufferPool[23] & 32767) | ((value & 1) << 15));
                break;
            case 85:
                writeToBufferPool(23, (bufferPool[23] & 36863) | (value << 12));
                break;
            case 86:
                writeToBufferPool(23, (bufferPool[23] & 61951) | (value << 9));
                break;
            case 87:
                writeToBufferPool(23, (bufferPool[23] & 65087) | (value << 6));
                break;
            case 88:
                writeToBufferPool(23, (bufferPool[23] & 65479) | (value << 3));
                break;
            case 89:
                writeToBufferPool(23, (bufferPool[23] & 65528) | (value));
                break;
            case 90:
                writeToBufferPool(24, (bufferPool[24] & 8191) | (value << 13));
                break;
            case 91:
                writeToBufferPool(24, (bufferPool[24] & 58367) | (value << 10));
                break;
            case 92:
                writeToBufferPool(24, (bufferPool[24] & 64639) | (value << 7));
                break;
            case 93:
                writeToBufferPool(24, (bufferPool[24] & 65423) | (value << 4));
                break;
            case 94:
                writeToBufferPool(24, (bufferPool[24] & 65521) | (value << 1));
                break;
            case 95:
                writeToBufferPool(24, (bufferPool[24] & 65534) | ((value & 4) >>> 2));
                writeToBufferPool(25, (bufferPool[25] & 16383) | ((value & 3) << 14));
                break;
            case 96:
                writeToBufferPool(25, (bufferPool[25] & 51199) | (value << 11));
                break;
            case 97:
                writeToBufferPool(25, (bufferPool[25] & 63743) | (value << 8));
                break;
            case 98:
                writeToBufferPool(25, (bufferPool[25] & 65311) | (value << 5));
                break;
            case 99:
                writeToBufferPool(25, (bufferPool[25] & 65507) | (value << 2));
                break;
        }
    }

    public static int readSectorAll(int idx) throws GameActionException {
        switch (idx) {
            case 0:
                return (rc.readSharedArray(7) & 14336) >>> 11;
            case 1:
                return (rc.readSharedArray(7) & 1792) >>> 8;
            case 2:
                return (rc.readSharedArray(7) & 224) >>> 5;
            case 3:
                return (rc.readSharedArray(7) & 28) >>> 2;
            case 4:
                return ((rc.readSharedArray(7) & 3) << 1) + ((rc.readSharedArray(8) & 32768) >>> 15);
            case 5:
                return (rc.readSharedArray(8) & 28672) >>> 12;
            case 6:
                return (rc.readSharedArray(8) & 3584) >>> 9;
            case 7:
                return (rc.readSharedArray(8) & 448) >>> 6;
            case 8:
                return (rc.readSharedArray(8) & 56) >>> 3;
            case 9:
                return (rc.readSharedArray(8) & 7);
            case 10:
                return (rc.readSharedArray(9) & 57344) >>> 13;
            case 11:
                return (rc.readSharedArray(9) & 7168) >>> 10;
            case 12:
                return (rc.readSharedArray(9) & 896) >>> 7;
            case 13:
                return (rc.readSharedArray(9) & 112) >>> 4;
            case 14:
                return (rc.readSharedArray(9) & 14) >>> 1;
            case 15:
                return ((rc.readSharedArray(9) & 1) << 2) + ((rc.readSharedArray(10) & 49152) >>> 14);
            case 16:
                return (rc.readSharedArray(10) & 14336) >>> 11;
            case 17:
                return (rc.readSharedArray(10) & 1792) >>> 8;
            case 18:
                return (rc.readSharedArray(10) & 224) >>> 5;
            case 19:
                return (rc.readSharedArray(10) & 28) >>> 2;
            case 20:
                return ((rc.readSharedArray(10) & 3) << 1) + ((rc.readSharedArray(11) & 32768) >>> 15);
            case 21:
                return (rc.readSharedArray(11) & 28672) >>> 12;
            case 22:
                return (rc.readSharedArray(11) & 3584) >>> 9;
            case 23:
                return (rc.readSharedArray(11) & 448) >>> 6;
            case 24:
                return (rc.readSharedArray(11) & 56) >>> 3;
            case 25:
                return (rc.readSharedArray(11) & 7);
            case 26:
                return (rc.readSharedArray(12) & 57344) >>> 13;
            case 27:
                return (rc.readSharedArray(12) & 7168) >>> 10;
            case 28:
                return (rc.readSharedArray(12) & 896) >>> 7;
            case 29:
                return (rc.readSharedArray(12) & 112) >>> 4;
            case 30:
                return (rc.readSharedArray(12) & 14) >>> 1;
            case 31:
                return ((rc.readSharedArray(12) & 1) << 2) + ((rc.readSharedArray(13) & 49152) >>> 14);
            case 32:
                return (rc.readSharedArray(13) & 14336) >>> 11;
            case 33:
                return (rc.readSharedArray(13) & 1792) >>> 8;
            case 34:
                return (rc.readSharedArray(13) & 224) >>> 5;
            case 35:
                return (rc.readSharedArray(13) & 28) >>> 2;
            case 36:
                return ((rc.readSharedArray(13) & 3) << 1) + ((rc.readSharedArray(14) & 32768) >>> 15);
            case 37:
                return (rc.readSharedArray(14) & 28672) >>> 12;
            case 38:
                return (rc.readSharedArray(14) & 3584) >>> 9;
            case 39:
                return (rc.readSharedArray(14) & 448) >>> 6;
            case 40:
                return (rc.readSharedArray(14) & 56) >>> 3;
            case 41:
                return (rc.readSharedArray(14) & 7);
            case 42:
                return (rc.readSharedArray(15) & 57344) >>> 13;
            case 43:
                return (rc.readSharedArray(15) & 7168) >>> 10;
            case 44:
                return (rc.readSharedArray(15) & 896) >>> 7;
            case 45:
                return (rc.readSharedArray(15) & 112) >>> 4;
            case 46:
                return (rc.readSharedArray(15) & 14) >>> 1;
            case 47:
                return ((rc.readSharedArray(15) & 1) << 2) + ((rc.readSharedArray(16) & 49152) >>> 14);
            case 48:
                return (rc.readSharedArray(16) & 14336) >>> 11;
            case 49:
                return (rc.readSharedArray(16) & 1792) >>> 8;
            case 50:
                return (rc.readSharedArray(16) & 224) >>> 5;
            case 51:
                return (rc.readSharedArray(16) & 28) >>> 2;
            case 52:
                return ((rc.readSharedArray(16) & 3) << 1) + ((rc.readSharedArray(17) & 32768) >>> 15);
            case 53:
                return (rc.readSharedArray(17) & 28672) >>> 12;
            case 54:
                return (rc.readSharedArray(17) & 3584) >>> 9;
            case 55:
                return (rc.readSharedArray(17) & 448) >>> 6;
            case 56:
                return (rc.readSharedArray(17) & 56) >>> 3;
            case 57:
                return (rc.readSharedArray(17) & 7);
            case 58:
                return (rc.readSharedArray(18) & 57344) >>> 13;
            case 59:
                return (rc.readSharedArray(18) & 7168) >>> 10;
            case 60:
                return (rc.readSharedArray(18) & 896) >>> 7;
            case 61:
                return (rc.readSharedArray(18) & 112) >>> 4;
            case 62:
                return (rc.readSharedArray(18) & 14) >>> 1;
            case 63:
                return ((rc.readSharedArray(18) & 1) << 2) + ((rc.readSharedArray(19) & 49152) >>> 14);
            case 64:
                return (rc.readSharedArray(19) & 14336) >>> 11;
            case 65:
                return (rc.readSharedArray(19) & 1792) >>> 8;
            case 66:
                return (rc.readSharedArray(19) & 224) >>> 5;
            case 67:
                return (rc.readSharedArray(19) & 28) >>> 2;
            case 68:
                return ((rc.readSharedArray(19) & 3) << 1) + ((rc.readSharedArray(20) & 32768) >>> 15);
            case 69:
                return (rc.readSharedArray(20) & 28672) >>> 12;
            case 70:
                return (rc.readSharedArray(20) & 3584) >>> 9;
            case 71:
                return (rc.readSharedArray(20) & 448) >>> 6;
            case 72:
                return (rc.readSharedArray(20) & 56) >>> 3;
            case 73:
                return (rc.readSharedArray(20) & 7);
            case 74:
                return (rc.readSharedArray(21) & 57344) >>> 13;
            case 75:
                return (rc.readSharedArray(21) & 7168) >>> 10;
            case 76:
                return (rc.readSharedArray(21) & 896) >>> 7;
            case 77:
                return (rc.readSharedArray(21) & 112) >>> 4;
            case 78:
                return (rc.readSharedArray(21) & 14) >>> 1;
            case 79:
                return ((rc.readSharedArray(21) & 1) << 2) + ((rc.readSharedArray(22) & 49152) >>> 14);
            case 80:
                return (rc.readSharedArray(22) & 14336) >>> 11;
            case 81:
                return (rc.readSharedArray(22) & 1792) >>> 8;
            case 82:
                return (rc.readSharedArray(22) & 224) >>> 5;
            case 83:
                return (rc.readSharedArray(22) & 28) >>> 2;
            case 84:
                return ((rc.readSharedArray(22) & 3) << 1) + ((rc.readSharedArray(23) & 32768) >>> 15);
            case 85:
                return (rc.readSharedArray(23) & 28672) >>> 12;
            case 86:
                return (rc.readSharedArray(23) & 3584) >>> 9;
            case 87:
                return (rc.readSharedArray(23) & 448) >>> 6;
            case 88:
                return (rc.readSharedArray(23) & 56) >>> 3;
            case 89:
                return (rc.readSharedArray(23) & 7);
            case 90:
                return (rc.readSharedArray(24) & 57344) >>> 13;
            case 91:
                return (rc.readSharedArray(24) & 7168) >>> 10;
            case 92:
                return (rc.readSharedArray(24) & 896) >>> 7;
            case 93:
                return (rc.readSharedArray(24) & 112) >>> 4;
            case 94:
                return (rc.readSharedArray(24) & 14) >>> 1;
            case 95:
                return ((rc.readSharedArray(24) & 1) << 2) + ((rc.readSharedArray(25) & 49152) >>> 14);
            case 96:
                return (rc.readSharedArray(25) & 14336) >>> 11;
            case 97:
                return (rc.readSharedArray(25) & 1792) >>> 8;
            case 98:
                return (rc.readSharedArray(25) & 224) >>> 5;
            case 99:
                return (rc.readSharedArray(25) & 28) >>> 2;
            default:
                return -1;
        }
    }

    public static void writeSectorAll(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                rc.writeSharedArray(7, (rc.readSharedArray(7) & 51199) | (value << 11));
                break;
            case 1:
                rc.writeSharedArray(7, (rc.readSharedArray(7) & 63743) | (value << 8));
                break;
            case 2:
                rc.writeSharedArray(7, (rc.readSharedArray(7) & 65311) | (value << 5));
                break;
            case 3:
                rc.writeSharedArray(7, (rc.readSharedArray(7) & 65507) | (value << 2));
                break;
            case 4:
                rc.writeSharedArray(7, (rc.readSharedArray(7) & 65532) | ((value & 6) >>> 1));
                rc.writeSharedArray(8, (rc.readSharedArray(8) & 32767) | ((value & 1) << 15));
                break;
            case 5:
                rc.writeSharedArray(8, (rc.readSharedArray(8) & 36863) | (value << 12));
                break;
            case 6:
                rc.writeSharedArray(8, (rc.readSharedArray(8) & 61951) | (value << 9));
                break;
            case 7:
                rc.writeSharedArray(8, (rc.readSharedArray(8) & 65087) | (value << 6));
                break;
            case 8:
                rc.writeSharedArray(8, (rc.readSharedArray(8) & 65479) | (value << 3));
                break;
            case 9:
                rc.writeSharedArray(8, (rc.readSharedArray(8) & 65528) | (value));
                break;
            case 10:
                rc.writeSharedArray(9, (rc.readSharedArray(9) & 8191) | (value << 13));
                break;
            case 11:
                rc.writeSharedArray(9, (rc.readSharedArray(9) & 58367) | (value << 10));
                break;
            case 12:
                rc.writeSharedArray(9, (rc.readSharedArray(9) & 64639) | (value << 7));
                break;
            case 13:
                rc.writeSharedArray(9, (rc.readSharedArray(9) & 65423) | (value << 4));
                break;
            case 14:
                rc.writeSharedArray(9, (rc.readSharedArray(9) & 65521) | (value << 1));
                break;
            case 15:
                rc.writeSharedArray(9, (rc.readSharedArray(9) & 65534) | ((value & 4) >>> 2));
                rc.writeSharedArray(10, (rc.readSharedArray(10) & 16383) | ((value & 3) << 14));
                break;
            case 16:
                rc.writeSharedArray(10, (rc.readSharedArray(10) & 51199) | (value << 11));
                break;
            case 17:
                rc.writeSharedArray(10, (rc.readSharedArray(10) & 63743) | (value << 8));
                break;
            case 18:
                rc.writeSharedArray(10, (rc.readSharedArray(10) & 65311) | (value << 5));
                break;
            case 19:
                rc.writeSharedArray(10, (rc.readSharedArray(10) & 65507) | (value << 2));
                break;
            case 20:
                rc.writeSharedArray(10, (rc.readSharedArray(10) & 65532) | ((value & 6) >>> 1));
                rc.writeSharedArray(11, (rc.readSharedArray(11) & 32767) | ((value & 1) << 15));
                break;
            case 21:
                rc.writeSharedArray(11, (rc.readSharedArray(11) & 36863) | (value << 12));
                break;
            case 22:
                rc.writeSharedArray(11, (rc.readSharedArray(11) & 61951) | (value << 9));
                break;
            case 23:
                rc.writeSharedArray(11, (rc.readSharedArray(11) & 65087) | (value << 6));
                break;
            case 24:
                rc.writeSharedArray(11, (rc.readSharedArray(11) & 65479) | (value << 3));
                break;
            case 25:
                rc.writeSharedArray(11, (rc.readSharedArray(11) & 65528) | (value));
                break;
            case 26:
                rc.writeSharedArray(12, (rc.readSharedArray(12) & 8191) | (value << 13));
                break;
            case 27:
                rc.writeSharedArray(12, (rc.readSharedArray(12) & 58367) | (value << 10));
                break;
            case 28:
                rc.writeSharedArray(12, (rc.readSharedArray(12) & 64639) | (value << 7));
                break;
            case 29:
                rc.writeSharedArray(12, (rc.readSharedArray(12) & 65423) | (value << 4));
                break;
            case 30:
                rc.writeSharedArray(12, (rc.readSharedArray(12) & 65521) | (value << 1));
                break;
            case 31:
                rc.writeSharedArray(12, (rc.readSharedArray(12) & 65534) | ((value & 4) >>> 2));
                rc.writeSharedArray(13, (rc.readSharedArray(13) & 16383) | ((value & 3) << 14));
                break;
            case 32:
                rc.writeSharedArray(13, (rc.readSharedArray(13) & 51199) | (value << 11));
                break;
            case 33:
                rc.writeSharedArray(13, (rc.readSharedArray(13) & 63743) | (value << 8));
                break;
            case 34:
                rc.writeSharedArray(13, (rc.readSharedArray(13) & 65311) | (value << 5));
                break;
            case 35:
                rc.writeSharedArray(13, (rc.readSharedArray(13) & 65507) | (value << 2));
                break;
            case 36:
                rc.writeSharedArray(13, (rc.readSharedArray(13) & 65532) | ((value & 6) >>> 1));
                rc.writeSharedArray(14, (rc.readSharedArray(14) & 32767) | ((value & 1) << 15));
                break;
            case 37:
                rc.writeSharedArray(14, (rc.readSharedArray(14) & 36863) | (value << 12));
                break;
            case 38:
                rc.writeSharedArray(14, (rc.readSharedArray(14) & 61951) | (value << 9));
                break;
            case 39:
                rc.writeSharedArray(14, (rc.readSharedArray(14) & 65087) | (value << 6));
                break;
            case 40:
                rc.writeSharedArray(14, (rc.readSharedArray(14) & 65479) | (value << 3));
                break;
            case 41:
                rc.writeSharedArray(14, (rc.readSharedArray(14) & 65528) | (value));
                break;
            case 42:
                rc.writeSharedArray(15, (rc.readSharedArray(15) & 8191) | (value << 13));
                break;
            case 43:
                rc.writeSharedArray(15, (rc.readSharedArray(15) & 58367) | (value << 10));
                break;
            case 44:
                rc.writeSharedArray(15, (rc.readSharedArray(15) & 64639) | (value << 7));
                break;
            case 45:
                rc.writeSharedArray(15, (rc.readSharedArray(15) & 65423) | (value << 4));
                break;
            case 46:
                rc.writeSharedArray(15, (rc.readSharedArray(15) & 65521) | (value << 1));
                break;
            case 47:
                rc.writeSharedArray(15, (rc.readSharedArray(15) & 65534) | ((value & 4) >>> 2));
                rc.writeSharedArray(16, (rc.readSharedArray(16) & 16383) | ((value & 3) << 14));
                break;
            case 48:
                rc.writeSharedArray(16, (rc.readSharedArray(16) & 51199) | (value << 11));
                break;
            case 49:
                rc.writeSharedArray(16, (rc.readSharedArray(16) & 63743) | (value << 8));
                break;
            case 50:
                rc.writeSharedArray(16, (rc.readSharedArray(16) & 65311) | (value << 5));
                break;
            case 51:
                rc.writeSharedArray(16, (rc.readSharedArray(16) & 65507) | (value << 2));
                break;
            case 52:
                rc.writeSharedArray(16, (rc.readSharedArray(16) & 65532) | ((value & 6) >>> 1));
                rc.writeSharedArray(17, (rc.readSharedArray(17) & 32767) | ((value & 1) << 15));
                break;
            case 53:
                rc.writeSharedArray(17, (rc.readSharedArray(17) & 36863) | (value << 12));
                break;
            case 54:
                rc.writeSharedArray(17, (rc.readSharedArray(17) & 61951) | (value << 9));
                break;
            case 55:
                rc.writeSharedArray(17, (rc.readSharedArray(17) & 65087) | (value << 6));
                break;
            case 56:
                rc.writeSharedArray(17, (rc.readSharedArray(17) & 65479) | (value << 3));
                break;
            case 57:
                rc.writeSharedArray(17, (rc.readSharedArray(17) & 65528) | (value));
                break;
            case 58:
                rc.writeSharedArray(18, (rc.readSharedArray(18) & 8191) | (value << 13));
                break;
            case 59:
                rc.writeSharedArray(18, (rc.readSharedArray(18) & 58367) | (value << 10));
                break;
            case 60:
                rc.writeSharedArray(18, (rc.readSharedArray(18) & 64639) | (value << 7));
                break;
            case 61:
                rc.writeSharedArray(18, (rc.readSharedArray(18) & 65423) | (value << 4));
                break;
            case 62:
                rc.writeSharedArray(18, (rc.readSharedArray(18) & 65521) | (value << 1));
                break;
            case 63:
                rc.writeSharedArray(18, (rc.readSharedArray(18) & 65534) | ((value & 4) >>> 2));
                rc.writeSharedArray(19, (rc.readSharedArray(19) & 16383) | ((value & 3) << 14));
                break;
            case 64:
                rc.writeSharedArray(19, (rc.readSharedArray(19) & 51199) | (value << 11));
                break;
            case 65:
                rc.writeSharedArray(19, (rc.readSharedArray(19) & 63743) | (value << 8));
                break;
            case 66:
                rc.writeSharedArray(19, (rc.readSharedArray(19) & 65311) | (value << 5));
                break;
            case 67:
                rc.writeSharedArray(19, (rc.readSharedArray(19) & 65507) | (value << 2));
                break;
            case 68:
                rc.writeSharedArray(19, (rc.readSharedArray(19) & 65532) | ((value & 6) >>> 1));
                rc.writeSharedArray(20, (rc.readSharedArray(20) & 32767) | ((value & 1) << 15));
                break;
            case 69:
                rc.writeSharedArray(20, (rc.readSharedArray(20) & 36863) | (value << 12));
                break;
            case 70:
                rc.writeSharedArray(20, (rc.readSharedArray(20) & 61951) | (value << 9));
                break;
            case 71:
                rc.writeSharedArray(20, (rc.readSharedArray(20) & 65087) | (value << 6));
                break;
            case 72:
                rc.writeSharedArray(20, (rc.readSharedArray(20) & 65479) | (value << 3));
                break;
            case 73:
                rc.writeSharedArray(20, (rc.readSharedArray(20) & 65528) | (value));
                break;
            case 74:
                rc.writeSharedArray(21, (rc.readSharedArray(21) & 8191) | (value << 13));
                break;
            case 75:
                rc.writeSharedArray(21, (rc.readSharedArray(21) & 58367) | (value << 10));
                break;
            case 76:
                rc.writeSharedArray(21, (rc.readSharedArray(21) & 64639) | (value << 7));
                break;
            case 77:
                rc.writeSharedArray(21, (rc.readSharedArray(21) & 65423) | (value << 4));
                break;
            case 78:
                rc.writeSharedArray(21, (rc.readSharedArray(21) & 65521) | (value << 1));
                break;
            case 79:
                rc.writeSharedArray(21, (rc.readSharedArray(21) & 65534) | ((value & 4) >>> 2));
                rc.writeSharedArray(22, (rc.readSharedArray(22) & 16383) | ((value & 3) << 14));
                break;
            case 80:
                rc.writeSharedArray(22, (rc.readSharedArray(22) & 51199) | (value << 11));
                break;
            case 81:
                rc.writeSharedArray(22, (rc.readSharedArray(22) & 63743) | (value << 8));
                break;
            case 82:
                rc.writeSharedArray(22, (rc.readSharedArray(22) & 65311) | (value << 5));
                break;
            case 83:
                rc.writeSharedArray(22, (rc.readSharedArray(22) & 65507) | (value << 2));
                break;
            case 84:
                rc.writeSharedArray(22, (rc.readSharedArray(22) & 65532) | ((value & 6) >>> 1));
                rc.writeSharedArray(23, (rc.readSharedArray(23) & 32767) | ((value & 1) << 15));
                break;
            case 85:
                rc.writeSharedArray(23, (rc.readSharedArray(23) & 36863) | (value << 12));
                break;
            case 86:
                rc.writeSharedArray(23, (rc.readSharedArray(23) & 61951) | (value << 9));
                break;
            case 87:
                rc.writeSharedArray(23, (rc.readSharedArray(23) & 65087) | (value << 6));
                break;
            case 88:
                rc.writeSharedArray(23, (rc.readSharedArray(23) & 65479) | (value << 3));
                break;
            case 89:
                rc.writeSharedArray(23, (rc.readSharedArray(23) & 65528) | (value));
                break;
            case 90:
                rc.writeSharedArray(24, (rc.readSharedArray(24) & 8191) | (value << 13));
                break;
            case 91:
                rc.writeSharedArray(24, (rc.readSharedArray(24) & 58367) | (value << 10));
                break;
            case 92:
                rc.writeSharedArray(24, (rc.readSharedArray(24) & 64639) | (value << 7));
                break;
            case 93:
                rc.writeSharedArray(24, (rc.readSharedArray(24) & 65423) | (value << 4));
                break;
            case 94:
                rc.writeSharedArray(24, (rc.readSharedArray(24) & 65521) | (value << 1));
                break;
            case 95:
                rc.writeSharedArray(24, (rc.readSharedArray(24) & 65534) | ((value & 4) >>> 2));
                rc.writeSharedArray(25, (rc.readSharedArray(25) & 16383) | ((value & 3) << 14));
                break;
            case 96:
                rc.writeSharedArray(25, (rc.readSharedArray(25) & 51199) | (value << 11));
                break;
            case 97:
                rc.writeSharedArray(25, (rc.readSharedArray(25) & 63743) | (value << 8));
                break;
            case 98:
                rc.writeSharedArray(25, (rc.readSharedArray(25) & 65311) | (value << 5));
                break;
            case 99:
                rc.writeSharedArray(25, (rc.readSharedArray(25) & 65507) | (value << 2));
                break;
        }
    }

    public static void writeBPSectorAll(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                writeToBufferPool(7, (bufferPool[7] & 51199) | (value << 11));
                break;
            case 1:
                writeToBufferPool(7, (bufferPool[7] & 63743) | (value << 8));
                break;
            case 2:
                writeToBufferPool(7, (bufferPool[7] & 65311) | (value << 5));
                break;
            case 3:
                writeToBufferPool(7, (bufferPool[7] & 65507) | (value << 2));
                break;
            case 4:
                writeToBufferPool(7, (bufferPool[7] & 65532) | ((value & 6) >>> 1));
                writeToBufferPool(8, (bufferPool[8] & 32767) | ((value & 1) << 15));
                break;
            case 5:
                writeToBufferPool(8, (bufferPool[8] & 36863) | (value << 12));
                break;
            case 6:
                writeToBufferPool(8, (bufferPool[8] & 61951) | (value << 9));
                break;
            case 7:
                writeToBufferPool(8, (bufferPool[8] & 65087) | (value << 6));
                break;
            case 8:
                writeToBufferPool(8, (bufferPool[8] & 65479) | (value << 3));
                break;
            case 9:
                writeToBufferPool(8, (bufferPool[8] & 65528) | (value));
                break;
            case 10:
                writeToBufferPool(9, (bufferPool[9] & 8191) | (value << 13));
                break;
            case 11:
                writeToBufferPool(9, (bufferPool[9] & 58367) | (value << 10));
                break;
            case 12:
                writeToBufferPool(9, (bufferPool[9] & 64639) | (value << 7));
                break;
            case 13:
                writeToBufferPool(9, (bufferPool[9] & 65423) | (value << 4));
                break;
            case 14:
                writeToBufferPool(9, (bufferPool[9] & 65521) | (value << 1));
                break;
            case 15:
                writeToBufferPool(9, (bufferPool[9] & 65534) | ((value & 4) >>> 2));
                writeToBufferPool(10, (bufferPool[10] & 16383) | ((value & 3) << 14));
                break;
            case 16:
                writeToBufferPool(10, (bufferPool[10] & 51199) | (value << 11));
                break;
            case 17:
                writeToBufferPool(10, (bufferPool[10] & 63743) | (value << 8));
                break;
            case 18:
                writeToBufferPool(10, (bufferPool[10] & 65311) | (value << 5));
                break;
            case 19:
                writeToBufferPool(10, (bufferPool[10] & 65507) | (value << 2));
                break;
            case 20:
                writeToBufferPool(10, (bufferPool[10] & 65532) | ((value & 6) >>> 1));
                writeToBufferPool(11, (bufferPool[11] & 32767) | ((value & 1) << 15));
                break;
            case 21:
                writeToBufferPool(11, (bufferPool[11] & 36863) | (value << 12));
                break;
            case 22:
                writeToBufferPool(11, (bufferPool[11] & 61951) | (value << 9));
                break;
            case 23:
                writeToBufferPool(11, (bufferPool[11] & 65087) | (value << 6));
                break;
            case 24:
                writeToBufferPool(11, (bufferPool[11] & 65479) | (value << 3));
                break;
            case 25:
                writeToBufferPool(11, (bufferPool[11] & 65528) | (value));
                break;
            case 26:
                writeToBufferPool(12, (bufferPool[12] & 8191) | (value << 13));
                break;
            case 27:
                writeToBufferPool(12, (bufferPool[12] & 58367) | (value << 10));
                break;
            case 28:
                writeToBufferPool(12, (bufferPool[12] & 64639) | (value << 7));
                break;
            case 29:
                writeToBufferPool(12, (bufferPool[12] & 65423) | (value << 4));
                break;
            case 30:
                writeToBufferPool(12, (bufferPool[12] & 65521) | (value << 1));
                break;
            case 31:
                writeToBufferPool(12, (bufferPool[12] & 65534) | ((value & 4) >>> 2));
                writeToBufferPool(13, (bufferPool[13] & 16383) | ((value & 3) << 14));
                break;
            case 32:
                writeToBufferPool(13, (bufferPool[13] & 51199) | (value << 11));
                break;
            case 33:
                writeToBufferPool(13, (bufferPool[13] & 63743) | (value << 8));
                break;
            case 34:
                writeToBufferPool(13, (bufferPool[13] & 65311) | (value << 5));
                break;
            case 35:
                writeToBufferPool(13, (bufferPool[13] & 65507) | (value << 2));
                break;
            case 36:
                writeToBufferPool(13, (bufferPool[13] & 65532) | ((value & 6) >>> 1));
                writeToBufferPool(14, (bufferPool[14] & 32767) | ((value & 1) << 15));
                break;
            case 37:
                writeToBufferPool(14, (bufferPool[14] & 36863) | (value << 12));
                break;
            case 38:
                writeToBufferPool(14, (bufferPool[14] & 61951) | (value << 9));
                break;
            case 39:
                writeToBufferPool(14, (bufferPool[14] & 65087) | (value << 6));
                break;
            case 40:
                writeToBufferPool(14, (bufferPool[14] & 65479) | (value << 3));
                break;
            case 41:
                writeToBufferPool(14, (bufferPool[14] & 65528) | (value));
                break;
            case 42:
                writeToBufferPool(15, (bufferPool[15] & 8191) | (value << 13));
                break;
            case 43:
                writeToBufferPool(15, (bufferPool[15] & 58367) | (value << 10));
                break;
            case 44:
                writeToBufferPool(15, (bufferPool[15] & 64639) | (value << 7));
                break;
            case 45:
                writeToBufferPool(15, (bufferPool[15] & 65423) | (value << 4));
                break;
            case 46:
                writeToBufferPool(15, (bufferPool[15] & 65521) | (value << 1));
                break;
            case 47:
                writeToBufferPool(15, (bufferPool[15] & 65534) | ((value & 4) >>> 2));
                writeToBufferPool(16, (bufferPool[16] & 16383) | ((value & 3) << 14));
                break;
            case 48:
                writeToBufferPool(16, (bufferPool[16] & 51199) | (value << 11));
                break;
            case 49:
                writeToBufferPool(16, (bufferPool[16] & 63743) | (value << 8));
                break;
            case 50:
                writeToBufferPool(16, (bufferPool[16] & 65311) | (value << 5));
                break;
            case 51:
                writeToBufferPool(16, (bufferPool[16] & 65507) | (value << 2));
                break;
            case 52:
                writeToBufferPool(16, (bufferPool[16] & 65532) | ((value & 6) >>> 1));
                writeToBufferPool(17, (bufferPool[17] & 32767) | ((value & 1) << 15));
                break;
            case 53:
                writeToBufferPool(17, (bufferPool[17] & 36863) | (value << 12));
                break;
            case 54:
                writeToBufferPool(17, (bufferPool[17] & 61951) | (value << 9));
                break;
            case 55:
                writeToBufferPool(17, (bufferPool[17] & 65087) | (value << 6));
                break;
            case 56:
                writeToBufferPool(17, (bufferPool[17] & 65479) | (value << 3));
                break;
            case 57:
                writeToBufferPool(17, (bufferPool[17] & 65528) | (value));
                break;
            case 58:
                writeToBufferPool(18, (bufferPool[18] & 8191) | (value << 13));
                break;
            case 59:
                writeToBufferPool(18, (bufferPool[18] & 58367) | (value << 10));
                break;
            case 60:
                writeToBufferPool(18, (bufferPool[18] & 64639) | (value << 7));
                break;
            case 61:
                writeToBufferPool(18, (bufferPool[18] & 65423) | (value << 4));
                break;
            case 62:
                writeToBufferPool(18, (bufferPool[18] & 65521) | (value << 1));
                break;
            case 63:
                writeToBufferPool(18, (bufferPool[18] & 65534) | ((value & 4) >>> 2));
                writeToBufferPool(19, (bufferPool[19] & 16383) | ((value & 3) << 14));
                break;
            case 64:
                writeToBufferPool(19, (bufferPool[19] & 51199) | (value << 11));
                break;
            case 65:
                writeToBufferPool(19, (bufferPool[19] & 63743) | (value << 8));
                break;
            case 66:
                writeToBufferPool(19, (bufferPool[19] & 65311) | (value << 5));
                break;
            case 67:
                writeToBufferPool(19, (bufferPool[19] & 65507) | (value << 2));
                break;
            case 68:
                writeToBufferPool(19, (bufferPool[19] & 65532) | ((value & 6) >>> 1));
                writeToBufferPool(20, (bufferPool[20] & 32767) | ((value & 1) << 15));
                break;
            case 69:
                writeToBufferPool(20, (bufferPool[20] & 36863) | (value << 12));
                break;
            case 70:
                writeToBufferPool(20, (bufferPool[20] & 61951) | (value << 9));
                break;
            case 71:
                writeToBufferPool(20, (bufferPool[20] & 65087) | (value << 6));
                break;
            case 72:
                writeToBufferPool(20, (bufferPool[20] & 65479) | (value << 3));
                break;
            case 73:
                writeToBufferPool(20, (bufferPool[20] & 65528) | (value));
                break;
            case 74:
                writeToBufferPool(21, (bufferPool[21] & 8191) | (value << 13));
                break;
            case 75:
                writeToBufferPool(21, (bufferPool[21] & 58367) | (value << 10));
                break;
            case 76:
                writeToBufferPool(21, (bufferPool[21] & 64639) | (value << 7));
                break;
            case 77:
                writeToBufferPool(21, (bufferPool[21] & 65423) | (value << 4));
                break;
            case 78:
                writeToBufferPool(21, (bufferPool[21] & 65521) | (value << 1));
                break;
            case 79:
                writeToBufferPool(21, (bufferPool[21] & 65534) | ((value & 4) >>> 2));
                writeToBufferPool(22, (bufferPool[22] & 16383) | ((value & 3) << 14));
                break;
            case 80:
                writeToBufferPool(22, (bufferPool[22] & 51199) | (value << 11));
                break;
            case 81:
                writeToBufferPool(22, (bufferPool[22] & 63743) | (value << 8));
                break;
            case 82:
                writeToBufferPool(22, (bufferPool[22] & 65311) | (value << 5));
                break;
            case 83:
                writeToBufferPool(22, (bufferPool[22] & 65507) | (value << 2));
                break;
            case 84:
                writeToBufferPool(22, (bufferPool[22] & 65532) | ((value & 6) >>> 1));
                writeToBufferPool(23, (bufferPool[23] & 32767) | ((value & 1) << 15));
                break;
            case 85:
                writeToBufferPool(23, (bufferPool[23] & 36863) | (value << 12));
                break;
            case 86:
                writeToBufferPool(23, (bufferPool[23] & 61951) | (value << 9));
                break;
            case 87:
                writeToBufferPool(23, (bufferPool[23] & 65087) | (value << 6));
                break;
            case 88:
                writeToBufferPool(23, (bufferPool[23] & 65479) | (value << 3));
                break;
            case 89:
                writeToBufferPool(23, (bufferPool[23] & 65528) | (value));
                break;
            case 90:
                writeToBufferPool(24, (bufferPool[24] & 8191) | (value << 13));
                break;
            case 91:
                writeToBufferPool(24, (bufferPool[24] & 58367) | (value << 10));
                break;
            case 92:
                writeToBufferPool(24, (bufferPool[24] & 64639) | (value << 7));
                break;
            case 93:
                writeToBufferPool(24, (bufferPool[24] & 65423) | (value << 4));
                break;
            case 94:
                writeToBufferPool(24, (bufferPool[24] & 65521) | (value << 1));
                break;
            case 95:
                writeToBufferPool(24, (bufferPool[24] & 65534) | ((value & 4) >>> 2));
                writeToBufferPool(25, (bufferPool[25] & 16383) | ((value & 3) << 14));
                break;
            case 96:
                writeToBufferPool(25, (bufferPool[25] & 51199) | (value << 11));
                break;
            case 97:
                writeToBufferPool(25, (bufferPool[25] & 63743) | (value << 8));
                break;
            case 98:
                writeToBufferPool(25, (bufferPool[25] & 65311) | (value << 5));
                break;
            case 99:
                writeToBufferPool(25, (bufferPool[25] & 65507) | (value << 2));
                break;
        }
    }

    public static int readCombatSectorClaimStatus(int idx) throws GameActionException {
        switch (idx) {
            case 0:
                return (rc.readSharedArray(25) & 2) >>> 1;
            case 1:
                return (rc.readSharedArray(26) & 512) >>> 9;
            case 2:
                return (rc.readSharedArray(26) & 2) >>> 1;
            case 3:
                return (rc.readSharedArray(27) & 512) >>> 9;
            default:
                return -1;
        }
    }

    public static void writeCombatSectorClaimStatus(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                rc.writeSharedArray(25, (rc.readSharedArray(25) & 65533) | (value << 1));
                break;
            case 1:
                rc.writeSharedArray(26, (rc.readSharedArray(26) & 65023) | (value << 9));
                break;
            case 2:
                rc.writeSharedArray(26, (rc.readSharedArray(26) & 65533) | (value << 1));
                break;
            case 3:
                rc.writeSharedArray(27, (rc.readSharedArray(27) & 65023) | (value << 9));
                break;
        }
    }

    public static void writeBPCombatSectorClaimStatus(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                writeToBufferPool(25, (bufferPool[25] & 65533) | (value << 1));
                break;
            case 1:
                writeToBufferPool(26, (bufferPool[26] & 65023) | (value << 9));
                break;
            case 2:
                writeToBufferPool(26, (bufferPool[26] & 65533) | (value << 1));
                break;
            case 3:
                writeToBufferPool(27, (bufferPool[27] & 65023) | (value << 9));
                break;
        }
    }

    public static int readCombatSectorIndex(int idx) throws GameActionException {
        switch (idx) {
            case 0:
                return ((rc.readSharedArray(25) & 1) << 6) + ((rc.readSharedArray(26) & 64512) >>> 10);
            case 1:
                return (rc.readSharedArray(26) & 508) >>> 2;
            case 2:
                return ((rc.readSharedArray(26) & 1) << 6) + ((rc.readSharedArray(27) & 64512) >>> 10);
            case 3:
                return (rc.readSharedArray(27) & 508) >>> 2;
            default:
                return -1;
        }
    }

    public static void writeCombatSectorIndex(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                rc.writeSharedArray(25, (rc.readSharedArray(25) & 65534) | ((value & 64) >>> 6));
                rc.writeSharedArray(26, (rc.readSharedArray(26) & 1023) | ((value & 63) << 10));
                break;
            case 1:
                rc.writeSharedArray(26, (rc.readSharedArray(26) & 65027) | (value << 2));
                break;
            case 2:
                rc.writeSharedArray(26, (rc.readSharedArray(26) & 65534) | ((value & 64) >>> 6));
                rc.writeSharedArray(27, (rc.readSharedArray(27) & 1023) | ((value & 63) << 10));
                break;
            case 3:
                rc.writeSharedArray(27, (rc.readSharedArray(27) & 65027) | (value << 2));
                break;
        }
    }

    public static void writeBPCombatSectorIndex(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                writeToBufferPool(25, (bufferPool[25] & 65534) | ((value & 64) >>> 6));
                writeToBufferPool(26, (bufferPool[26] & 1023) | ((value & 63) << 10));
                break;
            case 1:
                writeToBufferPool(26, (bufferPool[26] & 65027) | (value << 2));
                break;
            case 2:
                writeToBufferPool(26, (bufferPool[26] & 65534) | ((value & 64) >>> 6));
                writeToBufferPool(27, (bufferPool[27] & 1023) | ((value & 63) << 10));
                break;
            case 3:
                writeToBufferPool(27, (bufferPool[27] & 65027) | (value << 2));
                break;
        }
    }

    public static int readCombatSectorAll(int idx) throws GameActionException {
        switch (idx) {
            case 0:
                return ((rc.readSharedArray(25) & 3) << 6) + ((rc.readSharedArray(26) & 64512) >>> 10);
            case 1:
                return (rc.readSharedArray(26) & 1020) >>> 2;
            case 2:
                return ((rc.readSharedArray(26) & 3) << 6) + ((rc.readSharedArray(27) & 64512) >>> 10);
            case 3:
                return (rc.readSharedArray(27) & 1020) >>> 2;
            default:
                return -1;
        }
    }

    public static void writeCombatSectorAll(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                rc.writeSharedArray(25, (rc.readSharedArray(25) & 65532) | ((value & 192) >>> 6));
                rc.writeSharedArray(26, (rc.readSharedArray(26) & 1023) | ((value & 63) << 10));
                break;
            case 1:
                rc.writeSharedArray(26, (rc.readSharedArray(26) & 64515) | (value << 2));
                break;
            case 2:
                rc.writeSharedArray(26, (rc.readSharedArray(26) & 65532) | ((value & 192) >>> 6));
                rc.writeSharedArray(27, (rc.readSharedArray(27) & 1023) | ((value & 63) << 10));
                break;
            case 3:
                rc.writeSharedArray(27, (rc.readSharedArray(27) & 64515) | (value << 2));
                break;
        }
    }

    public static void writeBPCombatSectorAll(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                writeToBufferPool(25, (bufferPool[25] & 65532) | ((value & 192) >>> 6));
                writeToBufferPool(26, (bufferPool[26] & 1023) | ((value & 63) << 10));
                break;
            case 1:
                writeToBufferPool(26, (bufferPool[26] & 64515) | (value << 2));
                break;
            case 2:
                writeToBufferPool(26, (bufferPool[26] & 65532) | ((value & 192) >>> 6));
                writeToBufferPool(27, (bufferPool[27] & 1023) | ((value & 63) << 10));
                break;
            case 3:
                writeToBufferPool(27, (bufferPool[27] & 64515) | (value << 2));
                break;
        }
    }

    public static int readExploreSectorClaimStatus(int idx) throws GameActionException {
        switch (idx) {
            case 0:
                return (rc.readSharedArray(27) & 2) >>> 1;
            case 1:
                return (rc.readSharedArray(28) & 512) >>> 9;
            case 2:
                return (rc.readSharedArray(28) & 2) >>> 1;
            case 3:
                return (rc.readSharedArray(29) & 512) >>> 9;
            case 4:
                return (rc.readSharedArray(29) & 2) >>> 1;
            case 5:
                return (rc.readSharedArray(30) & 512) >>> 9;
            case 6:
                return (rc.readSharedArray(30) & 2) >>> 1;
            case 7:
                return (rc.readSharedArray(31) & 512) >>> 9;
            case 8:
                return (rc.readSharedArray(31) & 2) >>> 1;
            case 9:
                return (rc.readSharedArray(32) & 512) >>> 9;
            case 10:
                return (rc.readSharedArray(32) & 2) >>> 1;
            case 11:
                return (rc.readSharedArray(33) & 512) >>> 9;
            case 12:
                return (rc.readSharedArray(33) & 2) >>> 1;
            default:
                return -1;
        }
    }

    public static void writeExploreSectorClaimStatus(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                rc.writeSharedArray(27, (rc.readSharedArray(27) & 65533) | (value << 1));
                break;
            case 1:
                rc.writeSharedArray(28, (rc.readSharedArray(28) & 65023) | (value << 9));
                break;
            case 2:
                rc.writeSharedArray(28, (rc.readSharedArray(28) & 65533) | (value << 1));
                break;
            case 3:
                rc.writeSharedArray(29, (rc.readSharedArray(29) & 65023) | (value << 9));
                break;
            case 4:
                rc.writeSharedArray(29, (rc.readSharedArray(29) & 65533) | (value << 1));
                break;
            case 5:
                rc.writeSharedArray(30, (rc.readSharedArray(30) & 65023) | (value << 9));
                break;
            case 6:
                rc.writeSharedArray(30, (rc.readSharedArray(30) & 65533) | (value << 1));
                break;
            case 7:
                rc.writeSharedArray(31, (rc.readSharedArray(31) & 65023) | (value << 9));
                break;
            case 8:
                rc.writeSharedArray(31, (rc.readSharedArray(31) & 65533) | (value << 1));
                break;
            case 9:
                rc.writeSharedArray(32, (rc.readSharedArray(32) & 65023) | (value << 9));
                break;
            case 10:
                rc.writeSharedArray(32, (rc.readSharedArray(32) & 65533) | (value << 1));
                break;
            case 11:
                rc.writeSharedArray(33, (rc.readSharedArray(33) & 65023) | (value << 9));
                break;
            case 12:
                rc.writeSharedArray(33, (rc.readSharedArray(33) & 65533) | (value << 1));
                break;
        }
    }

    public static void writeBPExploreSectorClaimStatus(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                writeToBufferPool(27, (bufferPool[27] & 65533) | (value << 1));
                break;
            case 1:
                writeToBufferPool(28, (bufferPool[28] & 65023) | (value << 9));
                break;
            case 2:
                writeToBufferPool(28, (bufferPool[28] & 65533) | (value << 1));
                break;
            case 3:
                writeToBufferPool(29, (bufferPool[29] & 65023) | (value << 9));
                break;
            case 4:
                writeToBufferPool(29, (bufferPool[29] & 65533) | (value << 1));
                break;
            case 5:
                writeToBufferPool(30, (bufferPool[30] & 65023) | (value << 9));
                break;
            case 6:
                writeToBufferPool(30, (bufferPool[30] & 65533) | (value << 1));
                break;
            case 7:
                writeToBufferPool(31, (bufferPool[31] & 65023) | (value << 9));
                break;
            case 8:
                writeToBufferPool(31, (bufferPool[31] & 65533) | (value << 1));
                break;
            case 9:
                writeToBufferPool(32, (bufferPool[32] & 65023) | (value << 9));
                break;
            case 10:
                writeToBufferPool(32, (bufferPool[32] & 65533) | (value << 1));
                break;
            case 11:
                writeToBufferPool(33, (bufferPool[33] & 65023) | (value << 9));
                break;
            case 12:
                writeToBufferPool(33, (bufferPool[33] & 65533) | (value << 1));
                break;
        }
    }

    public static int readExploreSectorIndex(int idx) throws GameActionException {
        switch (idx) {
            case 0:
                return ((rc.readSharedArray(27) & 1) << 6) + ((rc.readSharedArray(28) & 64512) >>> 10);
            case 1:
                return (rc.readSharedArray(28) & 508) >>> 2;
            case 2:
                return ((rc.readSharedArray(28) & 1) << 6) + ((rc.readSharedArray(29) & 64512) >>> 10);
            case 3:
                return (rc.readSharedArray(29) & 508) >>> 2;
            case 4:
                return ((rc.readSharedArray(29) & 1) << 6) + ((rc.readSharedArray(30) & 64512) >>> 10);
            case 5:
                return (rc.readSharedArray(30) & 508) >>> 2;
            case 6:
                return ((rc.readSharedArray(30) & 1) << 6) + ((rc.readSharedArray(31) & 64512) >>> 10);
            case 7:
                return (rc.readSharedArray(31) & 508) >>> 2;
            case 8:
                return ((rc.readSharedArray(31) & 1) << 6) + ((rc.readSharedArray(32) & 64512) >>> 10);
            case 9:
                return (rc.readSharedArray(32) & 508) >>> 2;
            case 10:
                return ((rc.readSharedArray(32) & 1) << 6) + ((rc.readSharedArray(33) & 64512) >>> 10);
            case 11:
                return (rc.readSharedArray(33) & 508) >>> 2;
            case 12:
                return ((rc.readSharedArray(33) & 1) << 6) + ((rc.readSharedArray(34) & 64512) >>> 10);
            default:
                return -1;
        }
    }

    public static void writeExploreSectorIndex(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                rc.writeSharedArray(27, (rc.readSharedArray(27) & 65534) | ((value & 64) >>> 6));
                rc.writeSharedArray(28, (rc.readSharedArray(28) & 1023) | ((value & 63) << 10));
                break;
            case 1:
                rc.writeSharedArray(28, (rc.readSharedArray(28) & 65027) | (value << 2));
                break;
            case 2:
                rc.writeSharedArray(28, (rc.readSharedArray(28) & 65534) | ((value & 64) >>> 6));
                rc.writeSharedArray(29, (rc.readSharedArray(29) & 1023) | ((value & 63) << 10));
                break;
            case 3:
                rc.writeSharedArray(29, (rc.readSharedArray(29) & 65027) | (value << 2));
                break;
            case 4:
                rc.writeSharedArray(29, (rc.readSharedArray(29) & 65534) | ((value & 64) >>> 6));
                rc.writeSharedArray(30, (rc.readSharedArray(30) & 1023) | ((value & 63) << 10));
                break;
            case 5:
                rc.writeSharedArray(30, (rc.readSharedArray(30) & 65027) | (value << 2));
                break;
            case 6:
                rc.writeSharedArray(30, (rc.readSharedArray(30) & 65534) | ((value & 64) >>> 6));
                rc.writeSharedArray(31, (rc.readSharedArray(31) & 1023) | ((value & 63) << 10));
                break;
            case 7:
                rc.writeSharedArray(31, (rc.readSharedArray(31) & 65027) | (value << 2));
                break;
            case 8:
                rc.writeSharedArray(31, (rc.readSharedArray(31) & 65534) | ((value & 64) >>> 6));
                rc.writeSharedArray(32, (rc.readSharedArray(32) & 1023) | ((value & 63) << 10));
                break;
            case 9:
                rc.writeSharedArray(32, (rc.readSharedArray(32) & 65027) | (value << 2));
                break;
            case 10:
                rc.writeSharedArray(32, (rc.readSharedArray(32) & 65534) | ((value & 64) >>> 6));
                rc.writeSharedArray(33, (rc.readSharedArray(33) & 1023) | ((value & 63) << 10));
                break;
            case 11:
                rc.writeSharedArray(33, (rc.readSharedArray(33) & 65027) | (value << 2));
                break;
            case 12:
                rc.writeSharedArray(33, (rc.readSharedArray(33) & 65534) | ((value & 64) >>> 6));
                rc.writeSharedArray(34, (rc.readSharedArray(34) & 1023) | ((value & 63) << 10));
                break;
        }
    }

    public static void writeBPExploreSectorIndex(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                writeToBufferPool(27, (bufferPool[27] & 65534) | ((value & 64) >>> 6));
                writeToBufferPool(28, (bufferPool[28] & 1023) | ((value & 63) << 10));
                break;
            case 1:
                writeToBufferPool(28, (bufferPool[28] & 65027) | (value << 2));
                break;
            case 2:
                writeToBufferPool(28, (bufferPool[28] & 65534) | ((value & 64) >>> 6));
                writeToBufferPool(29, (bufferPool[29] & 1023) | ((value & 63) << 10));
                break;
            case 3:
                writeToBufferPool(29, (bufferPool[29] & 65027) | (value << 2));
                break;
            case 4:
                writeToBufferPool(29, (bufferPool[29] & 65534) | ((value & 64) >>> 6));
                writeToBufferPool(30, (bufferPool[30] & 1023) | ((value & 63) << 10));
                break;
            case 5:
                writeToBufferPool(30, (bufferPool[30] & 65027) | (value << 2));
                break;
            case 6:
                writeToBufferPool(30, (bufferPool[30] & 65534) | ((value & 64) >>> 6));
                writeToBufferPool(31, (bufferPool[31] & 1023) | ((value & 63) << 10));
                break;
            case 7:
                writeToBufferPool(31, (bufferPool[31] & 65027) | (value << 2));
                break;
            case 8:
                writeToBufferPool(31, (bufferPool[31] & 65534) | ((value & 64) >>> 6));
                writeToBufferPool(32, (bufferPool[32] & 1023) | ((value & 63) << 10));
                break;
            case 9:
                writeToBufferPool(32, (bufferPool[32] & 65027) | (value << 2));
                break;
            case 10:
                writeToBufferPool(32, (bufferPool[32] & 65534) | ((value & 64) >>> 6));
                writeToBufferPool(33, (bufferPool[33] & 1023) | ((value & 63) << 10));
                break;
            case 11:
                writeToBufferPool(33, (bufferPool[33] & 65027) | (value << 2));
                break;
            case 12:
                writeToBufferPool(33, (bufferPool[33] & 65534) | ((value & 64) >>> 6));
                writeToBufferPool(34, (bufferPool[34] & 1023) | ((value & 63) << 10));
                break;
        }
    }

    public static int readExploreSectorAll(int idx) throws GameActionException {
        switch (idx) {
            case 0:
                return ((rc.readSharedArray(27) & 3) << 6) + ((rc.readSharedArray(28) & 64512) >>> 10);
            case 1:
                return (rc.readSharedArray(28) & 1020) >>> 2;
            case 2:
                return ((rc.readSharedArray(28) & 3) << 6) + ((rc.readSharedArray(29) & 64512) >>> 10);
            case 3:
                return (rc.readSharedArray(29) & 1020) >>> 2;
            case 4:
                return ((rc.readSharedArray(29) & 3) << 6) + ((rc.readSharedArray(30) & 64512) >>> 10);
            case 5:
                return (rc.readSharedArray(30) & 1020) >>> 2;
            case 6:
                return ((rc.readSharedArray(30) & 3) << 6) + ((rc.readSharedArray(31) & 64512) >>> 10);
            case 7:
                return (rc.readSharedArray(31) & 1020) >>> 2;
            case 8:
                return ((rc.readSharedArray(31) & 3) << 6) + ((rc.readSharedArray(32) & 64512) >>> 10);
            case 9:
                return (rc.readSharedArray(32) & 1020) >>> 2;
            case 10:
                return ((rc.readSharedArray(32) & 3) << 6) + ((rc.readSharedArray(33) & 64512) >>> 10);
            case 11:
                return (rc.readSharedArray(33) & 1020) >>> 2;
            case 12:
                return ((rc.readSharedArray(33) & 3) << 6) + ((rc.readSharedArray(34) & 64512) >>> 10);
            default:
                return -1;
        }
    }

    public static void writeExploreSectorAll(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                rc.writeSharedArray(27, (rc.readSharedArray(27) & 65532) | ((value & 192) >>> 6));
                rc.writeSharedArray(28, (rc.readSharedArray(28) & 1023) | ((value & 63) << 10));
                break;
            case 1:
                rc.writeSharedArray(28, (rc.readSharedArray(28) & 64515) | (value << 2));
                break;
            case 2:
                rc.writeSharedArray(28, (rc.readSharedArray(28) & 65532) | ((value & 192) >>> 6));
                rc.writeSharedArray(29, (rc.readSharedArray(29) & 1023) | ((value & 63) << 10));
                break;
            case 3:
                rc.writeSharedArray(29, (rc.readSharedArray(29) & 64515) | (value << 2));
                break;
            case 4:
                rc.writeSharedArray(29, (rc.readSharedArray(29) & 65532) | ((value & 192) >>> 6));
                rc.writeSharedArray(30, (rc.readSharedArray(30) & 1023) | ((value & 63) << 10));
                break;
            case 5:
                rc.writeSharedArray(30, (rc.readSharedArray(30) & 64515) | (value << 2));
                break;
            case 6:
                rc.writeSharedArray(30, (rc.readSharedArray(30) & 65532) | ((value & 192) >>> 6));
                rc.writeSharedArray(31, (rc.readSharedArray(31) & 1023) | ((value & 63) << 10));
                break;
            case 7:
                rc.writeSharedArray(31, (rc.readSharedArray(31) & 64515) | (value << 2));
                break;
            case 8:
                rc.writeSharedArray(31, (rc.readSharedArray(31) & 65532) | ((value & 192) >>> 6));
                rc.writeSharedArray(32, (rc.readSharedArray(32) & 1023) | ((value & 63) << 10));
                break;
            case 9:
                rc.writeSharedArray(32, (rc.readSharedArray(32) & 64515) | (value << 2));
                break;
            case 10:
                rc.writeSharedArray(32, (rc.readSharedArray(32) & 65532) | ((value & 192) >>> 6));
                rc.writeSharedArray(33, (rc.readSharedArray(33) & 1023) | ((value & 63) << 10));
                break;
            case 11:
                rc.writeSharedArray(33, (rc.readSharedArray(33) & 64515) | (value << 2));
                break;
            case 12:
                rc.writeSharedArray(33, (rc.readSharedArray(33) & 65532) | ((value & 192) >>> 6));
                rc.writeSharedArray(34, (rc.readSharedArray(34) & 1023) | ((value & 63) << 10));
                break;
        }
    }

    public static void writeBPExploreSectorAll(int idx, int value) throws GameActionException {
        switch (idx) {
            case 0:
                writeToBufferPool(27, (bufferPool[27] & 65532) | ((value & 192) >>> 6));
                writeToBufferPool(28, (bufferPool[28] & 1023) | ((value & 63) << 10));
                break;
            case 1:
                writeToBufferPool(28, (bufferPool[28] & 64515) | (value << 2));
                break;
            case 2:
                writeToBufferPool(28, (bufferPool[28] & 65532) | ((value & 192) >>> 6));
                writeToBufferPool(29, (bufferPool[29] & 1023) | ((value & 63) << 10));
                break;
            case 3:
                writeToBufferPool(29, (bufferPool[29] & 64515) | (value << 2));
                break;
            case 4:
                writeToBufferPool(29, (bufferPool[29] & 65532) | ((value & 192) >>> 6));
                writeToBufferPool(30, (bufferPool[30] & 1023) | ((value & 63) << 10));
                break;
            case 5:
                writeToBufferPool(30, (bufferPool[30] & 64515) | (value << 2));
                break;
            case 6:
                writeToBufferPool(30, (bufferPool[30] & 65532) | ((value & 192) >>> 6));
                writeToBufferPool(31, (bufferPool[31] & 1023) | ((value & 63) << 10));
                break;
            case 7:
                writeToBufferPool(31, (bufferPool[31] & 64515) | (value << 2));
                break;
            case 8:
                writeToBufferPool(31, (bufferPool[31] & 65532) | ((value & 192) >>> 6));
                writeToBufferPool(32, (bufferPool[32] & 1023) | ((value & 63) << 10));
                break;
            case 9:
                writeToBufferPool(32, (bufferPool[32] & 64515) | (value << 2));
                break;
            case 10:
                writeToBufferPool(32, (bufferPool[32] & 65532) | ((value & 192) >>> 6));
                writeToBufferPool(33, (bufferPool[33] & 1023) | ((value & 63) << 10));
                break;
            case 11:
                writeToBufferPool(33, (bufferPool[33] & 64515) | (value << 2));
                break;
            case 12:
                writeToBufferPool(33, (bufferPool[33] & 65532) | ((value & 192) >>> 6));
                writeToBufferPool(34, (bufferPool[34] & 1023) | ((value & 63) << 10));
                break;
        }
    }

    public static int readSymmetryVertical() throws GameActionException {
        return (rc.readSharedArray(34) & 512) >>> 9;
    }

    public static void writeSymmetryVertical(int value) throws GameActionException {
        rc.writeSharedArray(34, (rc.readSharedArray(34) & 65023) | (value << 9));
    }

    public static void writeBPSymmetryVertical(int value) throws GameActionException {
        writeToBufferPool(34, (bufferPool[34] & 65023) | (value << 9));
    }

    public static int readSymmetryHorizontal() throws GameActionException {
        return (rc.readSharedArray(34) & 256) >>> 8;
    }

    public static void writeSymmetryHorizontal(int value) throws GameActionException {
        rc.writeSharedArray(34, (rc.readSharedArray(34) & 65279) | (value << 8));
    }

    public static void writeBPSymmetryHorizontal(int value) throws GameActionException {
        writeToBufferPool(34, (bufferPool[34] & 65279) | (value << 8));
    }

    public static int readSymmetryRotational() throws GameActionException {
        return (rc.readSharedArray(34) & 128) >>> 7;
    }

    public static void writeSymmetryRotational(int value) throws GameActionException {
        rc.writeSharedArray(34, (rc.readSharedArray(34) & 65407) | (value << 7));
    }

    public static void writeBPSymmetryRotational(int value) throws GameActionException {
        writeToBufferPool(34, (bufferPool[34] & 65407) | (value << 7));
    }

    public static int readSymmetryAll() throws GameActionException {
        return (rc.readSharedArray(34) & 896) >>> 7;
    }

    public static void writeSymmetryAll(int value) throws GameActionException {
        rc.writeSharedArray(34, (rc.readSharedArray(34) & 64639) | (value << 7));
    }

    public static void writeBPSymmetryAll(int value) throws GameActionException {
        writeToBufferPool(34, (bufferPool[34] & 64639) | (value << 7));
    }

    public static int readNextBuilderUnitNum() throws GameActionException {
        return (rc.readSharedArray(34) & 126) >>> 1;
    }

    public static void writeNextBuilderUnitNum(int value) throws GameActionException {
        rc.writeSharedArray(34, (rc.readSharedArray(34) & 65409) | (value << 1));
    }

    public static void writeBPNextBuilderUnitNum(int value) throws GameActionException {
        writeToBufferPool(34, (bufferPool[34] & 65409) | (value << 1));
    }

    public static int readNextBuilderMinRoundNum() throws GameActionException {
        return ((rc.readSharedArray(34) & 1) << 10) + ((rc.readSharedArray(35) & 65472) >>> 6);
    }

    public static void writeNextBuilderMinRoundNum(int value) throws GameActionException {
        rc.writeSharedArray(34, (rc.readSharedArray(34) & 65534) | ((value & 1024) >>> 10));
        rc.writeSharedArray(35, (rc.readSharedArray(35) & 63) | ((value & 1023) << 6));
    }

    public static void writeBPNextBuilderMinRoundNum(int value) throws GameActionException {
        writeToBufferPool(34, (bufferPool[34] & 65534) | ((value & 1024) >>> 10));
        writeToBufferPool(35, (bufferPool[35] & 63) | ((value & 1023) << 6));
    }

    public static int readNextBuilderAll() throws GameActionException {
        return ((rc.readSharedArray(34) & 127) << 10) + ((rc.readSharedArray(35) & 65472) >>> 6);
    }

    public static void writeNextBuilderAll(int value) throws GameActionException {
        rc.writeSharedArray(34, (rc.readSharedArray(34) & 65408) | ((value & 130048) >>> 10));
        rc.writeSharedArray(35, (rc.readSharedArray(35) & 63) | ((value & 1023) << 6));
    }

    public static void writeBPNextBuilderAll(int value) throws GameActionException {
        writeToBufferPool(34, (bufferPool[34] & 65408) | ((value & 130048) >>> 10));
        writeToBufferPool(35, (bufferPool[35] & 63) | ((value & 1023) << 6));
    }

    // BUFFER POOL READ AND WRITE METHODS

}
