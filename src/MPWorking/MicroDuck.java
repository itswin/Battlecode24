package MPWorking;

import battlecode.common.*;
import battlecode.schema.Action;

public class MicroDuck {
    static final int INF = 1000000;
    static final float ACTION_RANGE = GameConstants.ATTACK_RADIUS_SQUARED;
    static final float VISION_RANGE = GameConstants.VISION_RADIUS_SQUARED;
    static final int ACTION_RANGE_EXTENDED = 10;
    static final int OVERWHELMING_RATIO = 3;

    static final Direction[] dirs = {
            Direction.NORTH,
            Direction.NORTHWEST,
            Direction.WEST,
            Direction.SOUTHWEST,
            Direction.SOUTH,
            Direction.SOUTHEAST,
            Direction.EAST,
            Direction.NORTHEAST,
            Direction.CENTER,
    };

    static final int MAX_MICRO_BYTECODE_REMAINING = 2000;

    static RobotController rc;

    static int id = 14026;

    static void init(RobotController r) {
        rc = r;
    }

    static boolean alwaysInRange;
    static double currentActionRadius;
    static double currentExtendedActionRadius;
    static boolean canAttack;
    static boolean canGlobalMove;
    static MapLocation currentLoc;
    static RobotInfo currentUnit;
    static int numDucks;
    static boolean overwhelming;

    static float DAMAGE;
    static float HEAL;

    static int getDamage() {
        int damage = Math.round(SkillType.ATTACK.skillEffect
                * ((float) SkillType.ATTACK.getSkillEffect(rc.getLevel(SkillType.ATTACK)) / 100 + 1));
        return damage;
    }

    static int getHeal() {
        int base_heal = SkillType.HEAL.skillEffect;
        // check for upgrade
        if (rc.getRoundNum() >= 1500) {
            base_heal += GlobalUpgrade.HEALING.baseHealChange;
        }
        return Math.round(base_heal * ((float) SkillType.HEAL.getSkillEffect(rc.getLevel(SkillType.HEAL)) / 100 + 1));
    }

    static void calcConstants() {
        DAMAGE = getDamage();
        HEAL = getHeal();
    }

    static boolean doMicro() throws GameActionException {
        return doMicro(true);
    }

    static boolean doMicro(boolean allowMovement) throws GameActionException {
        RobotInfo[] units = Robot.enemies;
        if (units.length == 0)
            return false;

        calcConstants();

        canAttack = rc.isActionReady();
        canGlobalMove = allowMovement && rc.isMovementReady();
        numDucks = 0;

        MicroInfo[] microInfo = new MicroInfo[9];
        int i = 9;
        for (; --i >= 0;)
            microInfo[i] = new MicroInfo(dirs[i]);

        currentActionRadius = ACTION_RANGE;
        currentExtendedActionRadius = ACTION_RANGE_EXTENDED;

        overwhelming = Robot.enemies.length * OVERWHELMING_RATIO <= Robot.allies.length;
        if (overwhelming) {
            Debug.printString("OVERWHELMING");
        }

        boolean isThreatened = true;
        i = units.length;

        for (; --i >= 0;) {
            if (Clock.getBytecodesLeft() < MAX_MICRO_BYTECODE_REMAINING)
                break;
            currentUnit = units[i];
            currentLoc = currentUnit.getLocation();

            // if (Bfs.existsPathTo(currentLoc))
            // isThreatened = true;
            // else
            // continue;

            microInfo[0].updateEnemy();
            microInfo[1].updateEnemy();
            microInfo[2].updateEnemy();
            microInfo[3].updateEnemy();
            microInfo[4].updateEnemy();
            microInfo[5].updateEnemy();
            microInfo[6].updateEnemy();
            microInfo[7].updateEnemy();
            microInfo[8].updateEnemy();
        }

        if (!isThreatened)
            return false;

        units = Robot.allies;
        i = units.length;
        for (; --i >= 0;) {
            if (Clock.getBytecodesLeft() < MAX_MICRO_BYTECODE_REMAINING)
                break;
            currentUnit = units[i];
            currentLoc = currentUnit.getLocation();

            microInfo[0].updateAlly();
            microInfo[1].updateAlly();
            microInfo[2].updateAlly();
            microInfo[3].updateAlly();
            microInfo[4].updateAlly();
            microInfo[5].updateAlly();
            microInfo[6].updateAlly();
            microInfo[7].updateAlly();
            microInfo[8].updateAlly();
        }

        // If you can't move, we have to choose the zero micro.
        if (!canGlobalMove) {
            // If the zero micro has an attack, apply it.
            if (applyAttack(microInfo[8])) {
                return true;
            }

            return true;
        }

        // If no movement is the best damage, attack first.
        boolean isZeroBestAttack = true;
        i = 8;
        MicroInfo bestMicro = microInfo[8];
        if (canAttack) {
            for (; --i >= 0;) {
                if (microInfo[i].isBetterAttack(bestMicro)) {
                    isZeroBestAttack = false;
                    break;
                }
            }

            if (isZeroBestAttack) {
                applyAttack(bestMicro);
                // Ignore the zero direction's attack scores
                bestMicro.enemyDamageScore = 0;
            }
        }

        // alwaysInRange = !canAttack || numDucks >= 2;

        i = 8;
        for (; --i >= 0;) {
            if (microInfo[i].isBetter(bestMicro))
                bestMicro = microInfo[i];
        }

        apply(bestMicro);
        return true;
    }

    // @returns true if we moved
    static boolean apply(MicroInfo bestMicro) throws GameActionException {
        // Debug.println("BestMicro Dir: " + bestMicro.dir + " " + bestMicro.location +
        // " " + bestMicro.minDistanceToEnemy
        // + " " + bestMicro.enemyDamageScore + " " + bestMicro.allyHealScore + " " +
        // bestMicro.ducksAttackRange
        // + " " + bestMicro.possibleEnemyDucks + " " + bestMicro.minDistToAlly + " " +
        // bestMicro.canMove + " "
        // + bestMicro.isSupported, id);
        if (bestMicro.dir == Direction.CENTER) {
            // Attacking should have actually happened on the previous check,
            // but I put it here just in case.
            applyAttack(bestMicro);
            return true;
        }

        if (rc.canMove(bestMicro.dir)) {
            rc.move(bestMicro.dir);
            applyAttack(bestMicro);
            return true;
        }

        return false;
    }

    static boolean applyAttack(MicroInfo bestMicro) throws GameActionException {
        if (bestMicro.enemyTarget != null && bestMicro.enemyDamageScore >= bestMicro.allyHealScore) {
            if (rc.canAttack(bestMicro.enemyTarget)) {
                rc.attack(bestMicro.enemyTarget);
                return true;
            }
        } else if (bestMicro.allyTarget != null) {
            if (rc.canHeal(bestMicro.allyTarget)) {
                rc.heal(bestMicro.allyTarget);
                return true;
            }
        }

        return false;
    }

    static class MicroInfo {
        Direction dir;
        MapLocation location;
        int minDistanceToEnemy = INF;

        float enemyDamageScore = 0;
        float allyHealScore = 0;
        int ducksAttackRange = 0;
        int possibleEnemyDucks = 0;
        int minDistToAlly = INF;
        MapLocation enemyTarget = null;
        MapLocation allyTarget = null;
        boolean canMove = true;
        boolean isSupported = false;

        public MicroInfo(Direction dir) {
            this.dir = dir;
            this.location = rc.adjacentLocation(dir);
            if (dir != Direction.CENTER && !rc.canMove(dir))
                canMove = false;
            minDistanceToEnemy = INF;
        }

        void updateEnemy() {
            if (!canMove)
                return;
            int dist = currentLoc.distanceSquaredTo(location);
            if (dist < minDistanceToEnemy)
                minDistanceToEnemy = dist;
            if (dist <= currentActionRadius)
                ducksAttackRange++;
            if (dist <= currentExtendedActionRadius)
                possibleEnemyDucks++;
            if (dist <= ACTION_RANGE && canAttack) {
                // Calculate a score based on how much percent damage
                float damageScore = 0;

                int health = currentUnit.getHealth();
                if (DAMAGE > health) {
                    // We want to reward killing units
                    // Reward dealing more damage
                    // Range 1 to 11
                    damageScore = 10 * health / DAMAGE + 1;
                } else {
                    // Reward dealing greater percent damage
                    // Range 0 to 1
                    damageScore = DAMAGE / health;
                }

                if (damageScore > enemyDamageScore) {
                    enemyDamageScore = damageScore;
                    enemyTarget = currentLoc;
                }
            }
        }

        void updateAlly() {
            if (!canMove)
                return;
            int dist = currentLoc.distanceSquaredTo(location);
            if (dist < minDistToAlly)
                minDistToAlly = dist;
            isSupported = true;
            if (dist <= ACTION_RANGE && canAttack) {
                // Calculate score based on percent healed over current health
                float healScore = HEAL / currentUnit.getHealth();
                if (currentUnit.getHealth() <= DAMAGE)
                    healScore += 2;
                if (healScore > allyHealScore) {
                    allyHealScore = healScore;
                    allyTarget = currentLoc;
                }
            }
        }

        boolean inRange() {
            if (alwaysInRange)
                return true;
            return minDistanceToEnemy <= ACTION_RANGE;
        }

        // equal => true
        boolean isBetter(MicroInfo M) {
            if (canMove && !M.canMove)
                return true;
            if (!canMove && M.canMove)
                return false;

            if (enemyDamageScore > M.enemyDamageScore)
                return true;
            if (enemyDamageScore < M.enemyDamageScore)
                return false;

            if (allyHealScore > M.allyHealScore)
                return true;
            if (allyHealScore < M.allyHealScore)
                return false;

            if (!overwhelming) {
                if (ducksAttackRange < M.ducksAttackRange)
                    return true;
                if (ducksAttackRange > M.ducksAttackRange)
                    return false;

                if (possibleEnemyDucks < M.possibleEnemyDucks)
                    return true;
                if (possibleEnemyDucks > M.possibleEnemyDucks)
                    return false;

                if (minDistToAlly < M.minDistToAlly)
                    return true;
                if (minDistToAlly > M.minDistToAlly)
                    return false;

                if (inRange())
                    return minDistanceToEnemy >= M.minDistanceToEnemy;
                else
                    return minDistanceToEnemy <= M.minDistanceToEnemy;
            } else {
                // Go closer if we have a unit majority
                return minDistanceToEnemy <= M.minDistanceToEnemy;
            }
        }

        boolean isBetterAttack(MicroInfo M) {
            if (enemyDamageScore > M.enemyDamageScore)
                return true;
            if (enemyDamageScore < M.enemyDamageScore)
                return false;

            return allyHealScore > M.allyHealScore;
        }
    }
}
