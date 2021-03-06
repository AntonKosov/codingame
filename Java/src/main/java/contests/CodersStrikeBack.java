package contests;

import java.util.*;

class CodersStrikeBack {

    private static final boolean DEBUG_MODE = true;

    private static final boolean RACER_SHIELD_ENABLED = true;
    private static final boolean TWO_RACERS = true;

    private static final float RESISTANCE = 0.85f;
//    private static final float RIGHT_CIRCLE_K = 0.3f;
//    private static final float CIRCLE_MUL_K = 3f;
//    private static final float ANGLE_SPEED = 1;
    private static final float MIN_VELOCITY = 100;
    private static final float MAX_VELOCITY = 800;

    private static final float MAX_THRUST = 200;
    private static final float MAX_THRUST_2 = MAX_THRUST * MAX_THRUST;
    private static final float MAX_ANGLE = 18;
    private static final int POD_RADIUS = 400;
    private static final int POD_RADIUS_2 = POD_RADIUS * POD_RADIUS;
    private static final int CHECKPOINT_RADIUS = 600;

    private static int sCountLaps;
    private static int sTotalCheckpoints;
    private static Vector[] sCheckpoints;

    private static final Pod sMyPod1 = new Pod("MyPod1");
    private static final Pod sMyPod2 = new Pod("MyPod2");
    private static final Pod sEnemy1 = new Pod("Enemy1");
    private static final Pod sEnemy2 = new Pod("Enemy2");

    private static final Vector sTmpVector = new Vector();

//    private static Vector sBP0 = new Vector();
//    private static Vector sBP1 = new Vector();
//    private static Vector sBP2 = new Vector();
//    private static Vector sBPResult = new Vector();
//    private static Vector sBPThrust = new Vector();
//    private static Vector sBPTheBestThrust = new Vector();
//    private static Vector sBPNextLocation = new Vector();

    public static void main(String args[]) {
        final Scanner in = new Scanner(System.in);
        sCountLaps = in.nextInt();
        final int checkpointCount = in.nextInt();
        sTotalCheckpoints = sCountLaps * checkpointCount;
        sCheckpoints = new Vector[checkpointCount];
        for (int i = 0; i < checkpointCount; i++) {
            final Vector point = new Vector();
            point.x = in.nextInt();
            point.y = in.nextInt();
            sCheckpoints[i] = point;
        }

        final Answer answerPod1 = new Answer();
        final Answer answerPod2 = new Answer();

        boolean isFirstStep = true;

        // game loop
        while (true) {
            sMyPod1.readData(in);
            sMyPod2.readData(in);
            sEnemy1.readData(in);
            sEnemy2.readData(in);

            if (isFirstStep && !TWO_RACERS) {
                isFirstStep = false;
                final Vector secondCheckpoint = getAfterNextCheckpoint(sMyPod1);
                final float dst1 = secondCheckpoint.dst2(sMyPod1.loc);
                final float dst2 = secondCheckpoint.dst2(sMyPod2.loc);
                if (dst1 > dst2) {
                    sMyPod1.role = Role.Racer;
                    sMyPod2.role = Role.Helper;
                } else {
                    sMyPod1.role = Role.Helper;
                    sMyPod2.role = Role.Racer;
                }
            }

            if (sMyPod1.role == Role.Racer) {
                calculateRacer(sMyPod1, answerPod1);
            } else {
                calculateHelper(sMyPod1, answerPod1);
            }
            if (sMyPod2.role == Role.Racer) {
                calculateRacer(sMyPod2, answerPod2);
            } else {
                calculateHelper(sMyPod2, answerPod2);
            }

            System.out.println(answerPod1);
            System.out.println(answerPod2);
        }
    }

    private static void calculateHelper(final Pod pod, Answer answer) {
        answer.reset();

        final Pod enemy = getDangerEnemy();
        final Vector checkpoint = sCheckpoints[enemy.nextCheckPointId];
        final float dstMeToCP = checkpoint.dst(pod.loc);
        final float dstEnemyToCP = checkpoint.dst(enemy.loc);
        final boolean isProtectNext = dstMeToCP * 1.5f > dstEnemyToCP && dstMeToCP > 1000;
        final Vector protectedCP = isProtectNext ? getAfterNextCheckpoint(enemy) : checkpoint;

        if (isProtectNext) {
            final Vector protectedPoint = new Vector(enemy.loc).sub(protectedCP).nor().scl(CHECKPOINT_RADIUS).add(protectedCP);
            final Vector checkpointVelocity = new Vector();
            final Vector desiredSpeed = getDesiredVelocity(pod, protectedPoint, 1, 1, checkpointVelocity);

            flyTo(pod, answer, protectedPoint, new Vector(enemy.loc), desiredSpeed);
            answer.addMessage("Fly to point " + protectedPoint);
        } else {
            final float dstToEnemy = pod.loc.dst(enemy.loc);
            final Vector nextEnemyPosition = new Vector(enemy.vel).scl(dstToEnemy / enemy.vel.len() / 2).add(enemy.loc); //todo distance * turns

            final Vector checkpointVelocity = new Vector();
            final Vector desiredSpeed = getDesiredVelocity(pod, nextEnemyPosition, 200, 400, checkpointVelocity);
            flyTo(pod, answer, nextEnemyPosition, new Vector(enemy.loc), desiredSpeed);
            answer.addMessage("Head attack to point " + nextEnemyPosition);
        }
    }

    private static Pod getDangerEnemy() {
        final Vector nextCP1 = sCheckpoints[sEnemy1.nextCheckPointId];
        final Vector nextCP2 = sCheckpoints[sEnemy2.nextCheckPointId];
        Pod danger;
        if (sEnemy1.passedCheckpoints > sEnemy2.passedCheckpoints) {
            danger = sEnemy1;
        } else if (sEnemy1.passedCheckpoints < sEnemy2.passedCheckpoints) {
            danger = sEnemy2;
        } else {
            final float dst1 = sTmpVector.set(nextCP1).sub(sEnemy1.loc).len2();
            final float dst2 = sTmpVector.set(nextCP2).sub(sEnemy2.loc).len2();
            if (dst1 > dst2) {
                danger = sEnemy2;
            } else {
                danger = sEnemy1;
            }
        }

        return danger;
    }

    private static void calculateRacer(final Pod pod, Answer answer) {
        answer.reset();
        final Vector checkpoint = sCheckpoints[pod.nextCheckPointId];
        final Vector nextCheckpoint = getAfterNextCheckpoint(pod);

        final Vector checkpointVelocity = new Vector();
        final Vector desiredSpeed = getDesiredVelocity(pod, checkpoint, MIN_VELOCITY, MAX_VELOCITY, checkpointVelocity);
//        answer.addMessage("CPV=" + checkpointVelocity.len());

        flyTo(pod, answer, checkpoint, nextCheckpoint, desiredSpeed);
    }

    private static void flyTo(Pod pod, Answer answer, Vector target, Vector targetLook, Vector desiredSpeed) {
        final Vector desiredThrust = getDesiredThrust(pod, desiredSpeed);
        //log("Desired speed " + desiredSpeed.len() + ", desired thrust " + desiredThrust);

        final Vector podOrientation = new Vector(1, 0).rotate(pod.angle);
        //log("angle " + pod.vel.angle(desiredSpeed) + ", cpv=" + checkpointVelocity.len() + ", cv=" + pod.vel.len());

        if (isRightVelocityAndDirection(pod, desiredSpeed)) {
            //todo fix
            answer.x = (int) targetLook.x;
            answer.y = (int) targetLook.y;
            answer.thrust = 0;

            log("cool");
            activateShieldIfNeed(pod, answer);
            return;
        }

        final float desiredPodOrientationAngle = Math.abs(desiredThrust.angle(podOrientation));
        if (desiredPodOrientationAngle <= MAX_ANGLE) {
            final float len2 = desiredThrust.len2();
            answer.thrust = (int) (len2 > MAX_THRUST_2 ? MAX_THRUST : Math.sqrt(len2));
            sTmpVector.set(pod.loc).add(desiredThrust);
            answer.x = (int) sTmpVector.x;
            answer.y = (int) sTmpVector.y;
            log("Thrust is right");
            activateShieldIfNeed(pod, answer);
            return;
        }

        answer.thrust = 0;
        //todo fix - optimal rotation
        answer.x = (int) target.x;
        answer.y = (int) target.y;
        log("Rotation pod.a=" + pod.angle);
        activateShieldIfNeed(pod, answer);
    }

    private static void activateShieldIfNeed(Pod pod, Answer answer) {
        if (!RACER_SHIELD_ENABLED) {
            return;
        }
        final boolean enemy1Collision = isCollision(pod, answer, sEnemy1, null);
        final boolean enemy2Collision = isCollision(pod, answer, sEnemy2, null);
        final float angle1 = Math.abs(pod.vel.angle(sEnemy1.vel));
        final float angle2 = Math.abs(pod.vel.angle(sEnemy1.vel));
        final float minAngle = 30;
        final boolean isActivateEnemy1 = enemy1Collision && angle1 > minAngle;
        final boolean isActivateEnemy2 = enemy2Collision && angle2 > minAngle;
        if (isActivateEnemy1 || isActivateEnemy2) {
            answer.shieldIsActivated = true;
        }
    }

    private static boolean isCollision(Pod pod1, Answer pod1A, Pod pod2, Answer pod2A) {
        final Vector pod1Thrust = new Vector(pod1A.x, pod1A.y).nor().scl(pod1A.thrust);
        final Vector pod1NextPosition = new Vector(pod1.vel).add(pod1Thrust).add(pod1.loc);

        final Vector pod2NextPosition;
        if (pod2A != null) {
            final Vector pod2Thrust = new Vector(pod2A.x, pod2A.y).nor().scl(pod2A.thrust);
            pod2NextPosition = new Vector(pod2.vel).add(pod2Thrust).add(pod2.loc);
        } else {
            sTmpVector.set(pod2.vel).add(pod2.prevThrust);
            pod2NextPosition = new Vector(pod2.loc).add(sTmpVector);
        }

        final float dst = pod1NextPosition.dst(pod2NextPosition);
        final boolean isCollision = dst < POD_RADIUS * 2;

        if (isCollision) {
            log(">< Collision " + pod1 + "-" + pod2);
        } else {
            //log("No collision: " + pod1 + "-" + pod2 + ": " + dst + ", " + pod1NextPosition + ", " + pod2NextPosition);
        }

        return isCollision;
    }

    private static boolean isRightVelocityAndDirection(Pod pod, Vector desiredSpeed) {
        final Vector checkpoint = sCheckpoints[pod.nextCheckPointId];
        final Vector dir = new Vector(checkpoint).sub(pod.loc);
        final Vector crs = new Vector(dir).rotate(90).nor().scl(CHECKPOINT_RADIUS).add(dir);
        final float velDirAngle = Math.abs(pod.vel.angle(dir));
        final float dirRadAngle = Math.abs(dir.angle(crs));
        if (velDirAngle >= dirRadAngle) {
//            log("velDirAngle " + velDirAngle + ", " + dirRadAngle);
            return false;
        }

        final float mustVelocity = desiredSpeed.len() * RESISTANCE;
        final float currentVelocity = pod.vel.len();
//        log("mustVelocity " + mustVelocity + ", " + currentVelocity);

        return currentVelocity >= mustVelocity;
    }

    private static Vector getDesiredThrust(Pod pod, Vector desiredSpeed) {
        final Vector nextVelocityWithoutThrust = new Vector(pod.vel).scl(RESISTANCE);
        return new Vector(desiredSpeed).sub(nextVelocityWithoutThrust);
    }

    private static Vector getDesiredVelocity(Pod pod, Vector checkpoint, float minVelocity, float maxVelocity, Vector outCheckpointVelocity) {
        final Vector afterNextCheckpoint = getAfterNextCheckpoint(pod);
        final Vector currentDir = new Vector(checkpoint).sub(pod.loc);
        final Vector nextDir = new Vector(afterNextCheckpoint).sub(checkpoint);

        final float nextAngle = 180 - Math.abs(currentDir.angle(nextDir));
        final float speedOnCheckpoint = minVelocity + (maxVelocity - minVelocity) * nextAngle / 180f;
//        log("speedOnCheckpoint "+ speedOnCheckpoint + ", next angle=" + nextAngle);

        final Vector nextPositionWithoutThrust = new Vector(pod.vel).scl(RESISTANCE).add(pod.loc);
        final Vector dirNextPositionWithoutThrust = new Vector(checkpoint).sub(nextPositionWithoutThrust);
        outCheckpointVelocity.set(dirNextPositionWithoutThrust).nor().scl(speedOnCheckpoint);
        float rest = dirNextPositionWithoutThrust.len();
        float speed = speedOnCheckpoint;
        if (speed > rest) {
            return new Vector(dirNextPositionWithoutThrust).nor().scl(speedOnCheckpoint - speed); //negative
        }

        float speedAfter = 0;
        float speedBefore = 0;
        while (rest >= 0) {
            speedAfter = speed;
            speed /= RESISTANCE;
            speedBefore = speed;
            rest -= speed;
//            log("sa=" + speedAfter + ", sb=" + speedBefore + ", rest=" + rest);
        }

        return new Vector(dirNextPositionWithoutThrust).nor().scl(speedBefore + rest / (speedBefore - speedAfter));
    }

    private static Vector getAfterNextCheckpoint(final Pod pod) {
        if (pod.nextCheckPointId == sCheckpoints.length - 1) {
            return sCheckpoints[0];
        }

        return sCheckpoints[pod.nextCheckPointId + 1];
    }

    private static void log(String message) {
        if (DEBUG_MODE) {
            System.err.println(message);
        }
    }

    private static class Pod {

        public String name;
        public final Vector loc = new Vector();
        public final Vector vel = new Vector();
        public int angle;
        public int nextCheckPointId = 1;
        public Role role = Role.Racer;
        public final Vector prevThrust = new Vector();

        public int passedCheckpoints = 0;

        private boolean mIsFirstRead = true;

        public Pod(String name) {
            this.name = name;
        }

        public void readData(Scanner in) {
            final Vector prevVelocity = new Vector(vel);
            loc.x = in.nextInt();
            loc.y = in.nextInt();
            vel.x = in.nextInt();
            vel.y = in.nextInt();
            vel.scl(RESISTANCE);
            angle = in.nextInt();
            if (mIsFirstRead) {
                mIsFirstRead = false;
                angle = (int) new Vector(1, 0).angle(new Vector(sCheckpoints[1]).sub(loc));
            }
            final int oldTargetCheckpoint = nextCheckPointId;
            nextCheckPointId = in.nextInt();
            if (nextCheckPointId != oldTargetCheckpoint) {
                passedCheckpoints++;
                log(name + " passed " + oldTargetCheckpoint + ", left " + (sTotalCheckpoints - passedCheckpoints));
            }
            prevThrust.set(vel).sub(prevVelocity);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private enum Role {
        Racer, Helper
    }

    private static class Answer {

        public int x;
        public int y;
        public int thrust;
        public boolean shieldIsActivated;
        public String message = "";

        @Override
        public String toString() {
            final String forDebug = message.isEmpty() ? "" : " " + (shieldIsActivated ? "SHIELD " : "") + message;
            final String m = DEBUG_MODE ? forDebug : "";
            String a = x + " " + y + " ";
            if (shieldIsActivated) {
                a +="SHIELD";
            } else {
                a += thrust;
            }

            return a + m;
        }

        public void addMessage(String m) {
            if (!message.isEmpty()) {
                message += ", ";
            }
            message += m;
        }

        public void reset() {
            x = 0;
            y = 0;
            thrust = 0;
            shieldIsActivated = false;
            message = "";
        }
    }

    private static class Vector {

        public float x;
        public float y;

        public Vector() {
            this(0, 0);
        }

        public Vector(Vector v) {
            this(v.x, v.y);
        }

        public Vector(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public Vector sub(Vector v) {
            x -= v.x;
            y -= v.y;
            return this;
        }

        public Vector add(Vector v) {
            x += v.x;
            y += v.y;
            return this;
        }

        public Vector scl(float scalar) {
            x = scalar * x;
            y = scalar * y;
            return this;
        }

        public Vector set(Vector v) {
            x = v.x;
            y = v.y;
            return this;
        }

        public Vector set(float x, float y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public float dst2(Vector v) {
            final float dX = x - v.x;
            final float dY = y - v.y;
            return dX * dX + dY * dY;
        }

        public float len() {
            return (float) Math.sqrt(len2());
        }

        public float len2() {
            return x * x + y * y;
        }

        public float angle(Vector v) {
            return (float) Math.toDegrees(Math.atan2(crs(v), dot(v)));
        }

        public float dot(Vector v) {
            return x * v.x + y * v.y;
        }

        public Vector rotate(float degrees) {
            final float radians = (float) Math.toRadians(degrees);

            final float cos = (float) Math.cos(radians);
            final float sin = (float) Math.sin(radians);

            final float newX = x * cos - y * sin;
            final float newY = x * sin + y * cos;

            x = newX;
            y = newY;

            return this;
        }

        public float crs(Vector v) {
            return x * v.y - y * v.x;
        }

        public Vector nor() {
            final float len = len();
            if (len != 0) {
                x /= len;
                y /= len;
            }
            return this;
        }

        public float dst(float x, float y) {
            final float xD = x - this.x;
            final float yD = y - this.y;
            return (float) Math.sqrt(xD * xD + yD * yD);
        }

        public float dst(Vector v) {
            return dst(v.x, v.y);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Vector) {
                final Vector v = (Vector) obj;
                return v.x == x && v.y == y;
            }

            return false;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }
}