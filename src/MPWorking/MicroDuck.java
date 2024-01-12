package MPWorking;

import battlecode.common.*;
import MPWorking.fast.*;

public class MicroDuck {
    static final int INF = 1000000;
    static final float ACTION_RANGE = GameConstants.ATTACK_RADIUS_SQUARED;
    static final float VISION_RANGE = GameConstants.VISION_RADIUS_SQUARED;
    static final int ACTION_RANGE_EXTENDED = 10;
    static final int STUN_BUILD_RANGE_EXTENDED = 13;
    static final int EXPLOSIVE_BUILD_RANGE_EXTENDED = 8;
    static final int OVERWHELMING_RATIO = 4;

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
    static boolean canBuildStunTrap;
    static boolean canBuildExplosiveTrap;
    static boolean canGlobalMove;
    static MapLocation currentLoc;
    static RobotInfo currentUnit;
    static int numDucks;
    static boolean overwhelming;
    static boolean seesAllyFlagHolder;
    static boolean appliedAttack;

    static float DAMAGE;
    static float HEAL;

    static MicroInfo[] microInfo = null;

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
        // if (units.length == 0)
        // return false;

        calcConstants();

        canAttack = rc.isActionReady();
        canBuildStunTrap = rc.getCrumbs() >= TrapType.STUN.buildCost && canAttack;
        canBuildExplosiveTrap = rc.getCrumbs() >= TrapType.EXPLOSIVE.buildCost && canAttack;
        canGlobalMove = allowMovement && rc.isMovementReady();
        numDucks = 0;
        seesAllyFlagHolder = false;
        appliedAttack = false;

        microInfo = new MicroInfo[9];
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
            if (currentUnit.hasFlag)
                seesAllyFlagHolder = true;

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

        i = 8;
        for (; --i >= 0;) {
            microInfo[i].loadActionScore();
        }

        if (Robot.enemies.length == 0 && !seesAllyFlagHolder) {
            // No enemies. Just apply the zero micro's attack.
            applyAttack(microInfo[8]);
            return false;
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

    static void clearMicroInfo() {
        microInfo = null;
    }

    // Only to be called after a Nav.move
    static boolean apply() throws GameActionException {
        if (!rc.isActionReady())
            return false;
        if (microInfo == null) {
            assert false;
            return false;
        }

        // Apply the micro action of the current location.
        MapLocation currLoc = rc.getLocation();
        if (microInfo[8].location.equals(currLoc))
            return applyAttack(microInfo[8]);
        if (microInfo[7].location.equals(currLoc))
            return applyAttack(microInfo[7]);
        if (microInfo[6].location.equals(currLoc))
            return applyAttack(microInfo[6]);
        if (microInfo[5].location.equals(currLoc))
            return applyAttack(microInfo[5]);
        if (microInfo[4].location.equals(currLoc))
            return applyAttack(microInfo[4]);
        if (microInfo[3].location.equals(currLoc))
            return applyAttack(microInfo[3]);
        if (microInfo[2].location.equals(currLoc))
            return applyAttack(microInfo[2]);
        if (microInfo[1].location.equals(currLoc))
            return applyAttack(microInfo[1]);
        if (microInfo[0].location.equals(currLoc))
            return applyAttack(microInfo[0]);

        assert false;
        return false;
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

    static boolean doActionNumber(int i, MicroInfo bestMicro) throws GameActionException {
        switch (i) {
            case 0:
                return bestMicro.doAttack();
            case 1:
                return bestMicro.doHeal();
            case 2:
                return bestMicro.doBuildStunTrap();
            case 3:
                return bestMicro.doBuildExplosiveTrap();
        }

        assert false;
        return false;
    }

    static boolean applyAttack(MicroInfo bestMicro) throws GameActionException {
        // We probably don't want the same unit building 2 traps in a single turn.
        if (appliedAttack)
            return false;
        final int SCALAR = 1000;
        int[] scores = {
                (int) (bestMicro.enemyDamageScore * SCALAR),
                (int) (bestMicro.allyHealScore * SCALAR),
                (int) (bestMicro.enemyStunScore * SCALAR),
                (int) (bestMicro.enemyExplosiveScore * SCALAR),
        };

        appliedAttack = true;
        FastSort.sort(scores);
        for (int i = 0; i < FastSort.size; i++) {
            if (doActionNumber(FastSort.indices[i], bestMicro))
                return true;
        }
        return false;
    }

    static class MicroInfo {
        static final float STUN_SCORE_PER_ENEMY = 0.25f;

        Direction dir;
        MapLocation location;
        int minDistanceToEnemy = INF;

        // These are AOE and are summed over all enemies.
        float enemyStunScore = 0;
        float stunTrapEnemiesX = 0;
        float stunTrapEnemiesY = 0;
        int stunTrapEnemiesN = 0;
        float enemyExplosiveScore = 0;
        int explosiveTrapEnemiesX = 0;
        int explosiveTrapEnemiesY = 0;
        int explosiveTrapEnemiesN = 0;

        float enemyDamageScore = 0;
        float allyHealScore = 0;
        int ducksAttackRange = 0;
        int possibleEnemyDucks = 0;
        int minDistToAlly = INF;
        MapLocation enemyTarget = null;
        MapLocation allyTarget = null;
        boolean canMove = true;
        boolean isSupported = false;

        int distToAllyFlagHolder = INF;
        int distToEnemyFlagHolder = INF;

        float actionScore = 0;

        public MicroInfo(Direction dir) {
            this.dir = dir;
            this.location = rc.adjacentLocation(dir);
            if (dir != Direction.CENTER && !rc.canMove(dir))
                canMove = false;
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
            if (canBuildStunTrap && dist <= STUN_BUILD_RANGE_EXTENDED) {
                enemyStunScore += STUN_SCORE_PER_ENEMY;
                stunTrapEnemiesX = (stunTrapEnemiesX * stunTrapEnemiesN + currentLoc.x) / (stunTrapEnemiesN + 1);
                stunTrapEnemiesY = (stunTrapEnemiesY * stunTrapEnemiesN + currentLoc.y) / (stunTrapEnemiesN + 1);
                stunTrapEnemiesN++;
            }
            if (canBuildExplosiveTrap && dist <= EXPLOSIVE_BUILD_RANGE_EXTENDED) {
                // Calculate a score based on how much percent damage
                float damageScore = 0;

                int health = currentUnit.getHealth();
                // We give lower reward because it costs crumbs
                if (TrapType.EXPLOSIVE.enterDamage > health) {
                    // We want to reward killing units
                    // Reward dealing more damage
                    // Range 1 to 6
                    damageScore = 5 * health / DAMAGE + 1;
                } else {
                    // Reward dealing greater percent damage
                    // Range 0 to 1
                    damageScore = DAMAGE / health / 2;
                }

                enemyExplosiveScore += damageScore;
                explosiveTrapEnemiesX = (explosiveTrapEnemiesX * explosiveTrapEnemiesN + currentLoc.x)
                        / (explosiveTrapEnemiesN + 1);
                explosiveTrapEnemiesY = (explosiveTrapEnemiesY * explosiveTrapEnemiesN + currentLoc.y)
                        / (explosiveTrapEnemiesN + 1);
                explosiveTrapEnemiesN++;
            }
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
                    damageScore = DAMAGE / health / SkillType.ATTACK.cooldown;
                }

                // Kill flag holders
                if (currentUnit.hasFlag)
                    damageScore += 10;

                if (damageScore > enemyDamageScore) {
                    enemyDamageScore = damageScore;
                    enemyTarget = currentLoc;
                }
            }
            if (currentUnit.hasFlag)
                distToEnemyFlagHolder = Math.min(distToEnemyFlagHolder, dist);
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
                int currentUnitHealth = currentUnit.getHealth();
                if (currentUnitHealth < GameConstants.DEFAULT_HEALTH - HEAL) {
                    float healScore = HEAL / currentUnitHealth / SkillType.HEAL.cooldown;
                    // 80 / 500
                    // Reward saving a unit's life.
                    if (currentUnitHealth <= DAMAGE)
                        healScore += 2;
                    // Reward healing a unit with a flag.
                    if (currentUnit.hasFlag)
                        healScore += 1;
                    if (healScore > allyHealScore) {
                        allyHealScore = healScore;
                        allyTarget = currentLoc;
                    }
                }
            }
            if (currentUnit.hasFlag)
                distToAllyFlagHolder = Math.min(distToAllyFlagHolder, dist);
        }

        boolean inRange() {
            if (alwaysInRange)
                return true;
            return minDistanceToEnemy <= ACTION_RANGE;
        }

        void loadActionScore() {
            actionScore = Math.max(enemyDamageScore,
                    Math.max(allyHealScore, Math.max(enemyStunScore, enemyExplosiveScore)));
        }

        // equal => true
        boolean isBetter(MicroInfo M) {
            if (canMove && !M.canMove)
                return true;
            if (!canMove && M.canMove)
                return false;

            if (actionScore > M.actionScore)
                return true;
            if (actionScore < M.actionScore)
                return false;

            // Should we prioritize defending our flag holder or the enemy flag holder?
            // Choosing ours first right now.

            // Enforce not being adjacent to the flag holder so that it can pathfind
            // boolean bothInf = distToAllyFlagHolder == INF && M.distToAllyFlagHolder ==
            // INF;
            boolean neitherAllyInf = distToAllyFlagHolder != INF && M.distToAllyFlagHolder != INF;
            // assert bothInf || neitherAllyInf;
            if (neitherAllyInf) {
                boolean lessEqual2 = distToAllyFlagHolder <= 2;
                boolean otherLessEqual2 = M.distToAllyFlagHolder <= 2;
                // If both are lessEqual2, ignore this check.
                if (!(lessEqual2 && otherLessEqual2)) {
                    if (lessEqual2 && !otherLessEqual2)
                        return false;
                    if (!lessEqual2 && otherLessEqual2)
                        return true;
                    // Now both are greater than 2
                    if (distToAllyFlagHolder < M.distToAllyFlagHolder)
                        return true;
                    if (distToAllyFlagHolder > M.distToAllyFlagHolder)
                        return false;
                }
            }

            // boolean bothInf = distToEnemyFlagHolder == INF && M.distToEnemyFlagHolder ==
            // INF;
            // boolean neitherEnemyInf = distToEnemyFlagHolder != INF &&
            // M.distToEnemyFlagHolder != INF;
            // assert bothInf || neitherEnemyInf;
            if (distToEnemyFlagHolder < M.distToEnemyFlagHolder)
                return true;
            if (distToEnemyFlagHolder > M.distToEnemyFlagHolder)
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
            return actionScore > M.actionScore;
        }

        boolean doAttack() throws GameActionException {
            if (enemyTarget == null)
                return false;
            if (rc.canAttack(enemyTarget)) {
                rc.attack(enemyTarget);
                return true;
            }
            Debug.println("Couldn't attack: " + enemyTarget);
            return false;
        }

        boolean doHeal() throws GameActionException {
            if (allyTarget == null)
                return false;
            if (rc.canHeal(allyTarget)) {
                rc.heal(allyTarget);
                return true;
            }
            Debug.println("Couldn't heal: " + allyTarget);
            return false;
        }

        boolean doBuildStunTrap() throws GameActionException {
            if (!canBuildStunTrap)
                return false;
            MapLocation stunTrapEnemies = new MapLocation((int) (stunTrapEnemiesX), (int) (stunTrapEnemiesY));
            Direction centralDir = rc.getLocation().directionTo(stunTrapEnemies);
            MapLocation loc;
            for (Direction dirToTrap : Util.getInOrderDirectionsCenter(centralDir)) {
                loc = rc.adjacentLocation(dirToTrap);
                if (rc.canBuild(TrapType.STUN, loc)) {
                    rc.build(TrapType.STUN, loc);
                    Debug.setIndicatorDot(loc, 255, 100, 5);
                    Debug.println("built stun trap at: " + loc);
                    return true;
                }
            }
            Debug.println("Couldn't build stun trap towards: " + stunTrapEnemies + " " + centralDir);
            Debug.println("CurrLoc: " + rc.getLocation());
            return false;
        }

        boolean doBuildExplosiveTrap() throws GameActionException {
            if (!canBuildExplosiveTrap)
                return false;
            MapLocation explosiveTrapEnemies = new MapLocation((int) (explosiveTrapEnemiesX),
                    (int) (explosiveTrapEnemiesY));
            Direction centralDir = rc.getLocation().directionTo(explosiveTrapEnemies);
            MapLocation loc;
            for (Direction dirToTrap : Util.getInOrderDirectionsCenter(centralDir)) {
                loc = rc.adjacentLocation(dirToTrap);
                if (rc.canBuild(TrapType.EXPLOSIVE, loc)) {
                    rc.build(TrapType.EXPLOSIVE, loc);
                    Debug.setIndicatorDot(loc, 255, 100, 5);
                    Debug.println("built exp trap at: " + loc);
                    return true;
                }
            }
            Debug.println("Couldn't build explosive trap towards: " + explosiveTrapEnemies + " " + centralDir);
            return false;
        }
    }
}
