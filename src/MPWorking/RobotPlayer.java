package MPWorking;

import battlecode.common.*;
import MPWorking.Util.*;
import MPWorking.fast.*;
import MPWorking.Comms.*;
import MPWorking.Debug.*;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what
 * we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    static Robot bot;
    static RobotController rc;

    /**
     * run() is the method that is called when a robot is instantiated in the
     * Battlecode world.
     * It is like the main function for your robot. If this method returns, the
     * robot dies!
     *
     * @param rc The RobotController object. You use it to perform actions from this
     *           robot, and to get
     *           information on its current status. Essentially your portal to
     *           interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        FastMath.initRand(rc);
        Debug.init(rc);
        Util.init(rc);
        Comms.init(rc);
        MapTracker.init(rc);
        Pathfinding.init(rc);
        Nav.init(rc);
        FastSort.init(rc);
        MicroDuck.init(rc);
        RobotPlayer.rc = rc;

        int lastUnitNum = Comms.readUnitNum();
        Robot.unitNum = lastUnitNum + 1;
        Comms.writeUnitNum(Robot.unitNum);
        Debug.println("I am unit number: " + Robot.unitNum);

        switch (Robot.unitNum) {
            case 10:
            case 11:
            case 12:
                bot = new BuilderDuck(rc);
                MicroDuck.canBuildTraps = true;
                break;
            default:
                bot = new Duck(rc);
                MicroDuck.canBuildTraps = false;
                break;
        }

        while (true) {
            try {
                bot.initTurn();
                bot.takeTurn();
                bot.endTurn();
                Debug.flush();
                Clock.yield();
                localResign();
            } catch (Exception e) {
                System.out.println("Exception. Round num: " + rc.getRoundNum());
                e.printStackTrace();
                reset(rc);
            }
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction
        // imminent...
    }

    // Last resort if a bot errors out in deployed code
    // Certain static variables might need to be cleared to ensure
    // a successful return to execution.
    public static void reset(RobotController rc) throws GameActionException {
        bot = new Duck(rc);
        // End of loop: go back to the top. Clock.yield() has ended, so it's time for
        // another turn!
        rc.resign();
    }

    public static void localResign() throws GameActionException {
        if (rc.getRoundNum() > 800) {
            rc.resign();
        }
    }
}
