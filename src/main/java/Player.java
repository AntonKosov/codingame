import java.util.*;

class Player {

    private static final boolean DEBUG_MODE = true;

    private static final boolean RACER_SHIELD_ENABLED = true;

    private static final float RESISTANCE = 0.85f;
//    private static final float RIGHT_CIRCLE_K = 0.3f;
//    private static final float CIRCLE_MUL_K = 3f;
//    private static final float ANGLE_SPEED = 1;
    private static final float MAX_SPEED = 200;
    private static final float MIN_SPEED = 100;

    private static final float MAX_THRUST = 200;
    private static final float MAX_THRUST_2 = MAX_THRUST * MAX_THRUST;
    private static final float MAX_ANGLE = 18;
    private static final int POD_RADIUS = 400;
    private static final int POD_RADIUS_2 = POD_RADIUS * POD_RADIUS;
    private static final int CHECKPOINT_RADIUS = 600;

    private static int sCountLaps;
    private static int sTotalCheckpoints;
    private static Vector[] sCheckpoints;
    private static Pod[] sOppPods = new Pod[2];

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

        sOppPods[0] = sEnemy1;
        sOppPods[1] = sEnemy2;

        final Answer answerPod1 = new Answer();
        final Answer answerPod2 = new Answer();

        boolean isFirstStep = true;

        // game loop
        while (true) {
            sMyPod1.readData(in);
            sMyPod2.readData(in);
            sOppPods[0].readData(in);
            sOppPods[1].readData(in);

            if (isFirstStep) {
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

//    private static final Vector shv = new Vector(100, 100);

    private static void calculateHelper(final Pod pod, Answer answer) {
        // todo shield, protection
        answer.shieldIsActivated = true;
//        if (pod.vel.len2() == 0) {
//            final Vector v = new Vector(shv).add(pod.loc);
//            answer.x = (int) shv.x;
//            answer.y = (int) shv.y;
//            answer.thrust = (int) MAX_THRUST;
//            shv.rotate(120);
//        } else {
//            final Vector back = new Vector(pod.vel).scl(-1).add(pod.loc);
//            answer.x = (int) back.x;
//            answer.y = (int) back.y;
//            final float thrust = pod.vel.len() * RESISTANCE;
//            answer.thrust = (int) (thrust > MAX_THRUST ? MAX_THRUST : thrust);
//        }
//
//        log("ps=" + pod.vel.len() + ", pv=" + (new Vector(pod.vel).nor()) + ", a=" + answer + ", shv=" + shv + ", back=" + (new Vector(pod.vel).scl(-1).nor()));
    }

    private static void calculateRacer(final Pod pod, Answer answer) {
        answer.reset();

        final Vector checkpoint = sCheckpoints[pod.nextCheckPointId];
        final Vector nextCheckpoint = getAfterNextCheckpoint(pod);

        final Vector checkpointVelocity = new Vector();
        final Vector desiredSpeed = getDesiredVelocity(pod, checkpoint, checkpointVelocity);
        final Vector desiredThrust = getDesiredThrust(pod, desiredSpeed);
        log("Desired speed " + desiredSpeed.len() + ", desired thrust " + desiredThrust);

        final Vector podOrientation = new Vector(1, 0).rotate(pod.angle);
        log("angle " + pod.vel.angle(desiredSpeed) + ", cpv=" + checkpointVelocity.len() + ", cv=" + pod.vel.len());

        if (isRightVelocityAndDirection(pod, desiredSpeed)) {
            //todo fix
            answer.x = (int) nextCheckpoint.x;
            answer.y = (int) nextCheckpoint.y;
            answer.thrust = 0;

            log("cool");
            if (RACER_SHIELD_ENABLED && (isCollision(pod, answer, sEnemy1, null) || isCollision(pod, answer, sEnemy2, null))) {
                answer.shieldIsActivated = true;
            }
            return;
        }

//        if (desiredThrust) {
//            final Vector toNextPoint = new Vector(nextCheckpoint).sub(checkpoint);
//            final float angle = checkpointVelocity.angle(toNextPoint);
//            final Vector targetThrust = new Vector(toNextPoint).rotate(angle).add(pod.loc);
//            answer.x = (int) targetThrust.x;
//            answer.y = (int) targetThrust.y;
//            log("Velocity is right, rotation to the next point, a=" + angle);
//            return;
//        }

        final float desiredPodOrientationAngle = Math.abs(desiredThrust.angle(podOrientation));
        if (desiredPodOrientationAngle <= MAX_ANGLE) {
            final float len2 = desiredThrust.len2();
            answer.thrust = (int) (len2 > MAX_THRUST_2 ? MAX_THRUST : Math.sqrt(len2));
            sTmpVector.set(pod.loc).add(desiredThrust);
            answer.x = (int) sTmpVector.x;
            answer.y = (int) sTmpVector.y;
            log("Thrust is right");
            if (RACER_SHIELD_ENABLED && (isCollision(pod, answer, sEnemy1, null) || isCollision(pod, answer, sEnemy2, null))) {
                answer.shieldIsActivated = true;
            }
            return;
        }

        answer.thrust = 0;
        //todo fix - optimal rotation
        answer.x = (int) checkpoint.x;
        answer.y = (int) checkpoint.y;
    }

    private static boolean isCollision(Pod pod1, Answer pod1A, Pod pod2, Answer pod2A) {
        final Vector pod1Thrust = new Vector(pod1A.x, pod1A.y).nor().scl(pod1A.thrust);
        final Vector pod1NextPosition = new Vector(pod1.vel).add(pod1Thrust).add(pod1.loc);

        final Vector pod2NextPosition;
        if (pod2A != null) {
            final Vector pod2Thrust = new Vector(pod2A.x, pod2A.y).nor().scl(pod2A.thrust);
            pod2NextPosition = new Vector(pod2.vel).add(pod2Thrust).add(pod2.loc);
        } else {
            pod2NextPosition = new Vector(pod2.loc).add(pod2.vel);
        }

        final float dst = pod1NextPosition.dst(pod2NextPosition);
        final boolean isCollision = dst < POD_RADIUS * 2;

        if (isCollision) {
            log(">< Collision " + pod1 + "-" + pod2);
        } else {
            log("No collision: " + pod1 + "-" + pod2 + ": " + dst + ", " + pod1NextPosition + ", " + pod2NextPosition);
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
            log("velDirAngle " + velDirAngle + ", " + dirRadAngle);
            return false;
        }

        final float mustVelocity = desiredSpeed.len() * RESISTANCE;
        final float currentVelocity = pod.vel.len();
        log("mustVelocity " + mustVelocity + ", " + currentVelocity);

        return currentVelocity >= mustVelocity;
    }

    private static Vector getDesiredThrust(Pod pod, Vector desiredSpeed) {
        final Vector nextVelocityWithoutThrust = new Vector(pod.vel).scl(RESISTANCE);
        return new Vector(desiredSpeed).sub(nextVelocityWithoutThrust);
    }

    private static Vector getDesiredVelocity(Pod pod, Vector checkpoint, Vector outCheckpointVelocity) {
        final Vector afterNextCheckpoint = getAfterNextCheckpoint(pod);
        final Vector currentDir = new Vector(checkpoint).sub(pod.loc);
        final Vector nextDir = new Vector(afterNextCheckpoint).sub(checkpoint);

        final float speedOnCheckpoint = MIN_SPEED + (MAX_SPEED - MIN_SPEED) * Math.abs(currentDir.angle(nextDir)) / 180f;
        log("speedOnCheckpoint "+ speedOnCheckpoint);

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

//    private static void calculateRacer(final Pod pod, Answer answer) {
//        final Vector p1 = new Vector(pod.loc);
//        final Vector p2 = new Vector(sCheckpoints[pod.nextCheckPointId]);
//        final Vector p3 = new Vector(getAfterNextCheckpoint(pod));
//
//        final float p1p2Length = new Vector(p2).sub(p1).len2();
//        final float p2p3Length = new Vector(p3).sub(p2).len2();
//        final float ratio = p1p2Length / p2p3Length;
//        log("ratio=" + ratio);
//        if (ratio > RIGHT_CIRCLE_K) {
//            log("fix p3");
//            final Vector rightCenterOfCircle = getCenterOfCircle(p1, p2, p3);
//            final Vector newP3 = new Vector(p3).sub(rightCenterOfCircle).scl(ratio * CIRCLE_MUL_K).add(rightCenterOfCircle);
//            p3.set(newP3);
//        }
//        final Vector centerOfCircle = getCenterOfCircle(p1, p2, p3);
//
//        final Vector p1p3 = new Vector(p3).sub(p1);
//        final Vector p1p2 = new Vector(p2).sub(p1);
//        final float sign = p1p3.crs(p1p2) > 0 ? -1 : +1;
//
//        final Vector optimalOffset = new Vector(p1).sub(centerOfCircle).rotate(MAX_ANGLE * sign);
//        final Vector wantPoint = new Vector(centerOfCircle).add(optimalOffset);
//        final Vector wantVelocity = new Vector(wantPoint).sub(p1).scl(1.0f / RESISTANCE);
//        final Vector thrust = new Vector(wantVelocity).sub(pod.vel);
//
//        final Vector thrustPod = new Vector(p1).add(thrust);
//        answer.x = (int) thrustPod.x;
//        answer.y = (int) thrustPod.y;
//        final float len2 = thrust.len2();
//        answer.thrust = (int) (len2 > MAX_THRUST_2 ? MAX_THRUST : Math.sqrt(len2));
//
//        log(pod + ", a=" + answer + ", center=" + centerOfCircle + ", wantPoint=" + wantPoint + ", wantVelocity=" + wantVelocity);
//    }

//    private static void calculateRacer(final Pod pod, Answer answer) {
//        final Vector p1 = pod.loc;
//        final Vector p2 = sCheckpoints[pod.nextCheckPointId];
//        final Vector p3 = getAfterNextCheckpoint(pod);
//        final Vector centerOfCircle = getCenterOfCircle(p1, p2, p3);
//
//        final Vector p1p3 = new Vector(p3).sub(p1);
//        final Vector p1p2 = new Vector(p2).sub(p1);
//        final float sign = p1p3.crs(p1p2) > 0 ? -1 : +1;
//
//        final Vector optimalOffset = new Vector(p1).sub(centerOfCircle).rotate(MAX_ANGLE * sign);
//        final Vector wantPoint = new Vector(centerOfCircle).add(optimalOffset);
//        final Vector wantVelocity = new Vector(wantPoint).sub(p1).scl(1.0f / RESISTANCE);
//        final Vector thrust = new Vector(wantVelocity).sub(pod.vel);
//
//        final Vector thrustPod = new Vector(p1).add(thrust);
//        answer.x = (int) thrustPod.x;
//        answer.y = (int) thrustPod.y;
//        final float len2 = thrust.len2();
//        answer.thrust = (int) (len2 > MAX_THRUST_2 ? MAX_THRUST : Math.sqrt(len2));
//
//        //log(pod + ", a=" + answer + ", center=" + centerOfCircle + ", thrust=" + thrust + ", wantVelocity=" + wantVelocity);
//    }

//    private static Vector getCenterOfCircle(Vector p1, Vector p2, Vector p3) {
//        if (p1.equals(p3)) {
//            return new Vector(p1).add(p2).scl(0.5f);
//        }
//
//        final Vector point1;
//        final Vector point2;
//        final Vector point3;
//        if (p2.x - p1.x == 0) {
//            point1 = p1;
//            point2 = p3;
//            point3 = p2;
//        } else if (p3.x - p2.x == 0) {
//            point1 = p2;
//            point2 = p1;
//            point3 = p3;
//        } else {
//            point1 = p1;
//            point2 = p2;
//            point3 = p3;
//        }
//
//        final float ma = (point2.y - point1.y) / (point2.x - point1.x);
//        final float mb = (point3.y - point2.y) / (point3.x - point2.x);
//
////        log("ma=" + ma + ", mb=" + mb + ", point2=" + point2 + ", point3=" + point3 + "(point3.x - point2.x)=" + (point3.x - point2.x));
//
//        //todo div 0
//        final float x = (ma * mb * (point1.y - point3.y) + mb * (point1.x + point2.x) - ma * (point2.x + point3.x)) / (2 * (mb - ma));
//        //todo div 0
//        final float y = -1.0f / ma * (x - (point1.x + point2.x) / 2) + (point1.y + point2.y) / 2;
//
////        log("center: " + point1 + ", " + point2 + ", " + point3);
//
//        return new Vector(x, y);
//    }

//    private static void calculateAnswer(Answer answerAttacker, Answer answerProtector) {
//        final Pod attacker = sMyPods[0];
//        final Pod protector = sMyPods[1];
//
//        log("targeta=" + attacker.nextCheckPointId + ", t=" + sCheckpoints[attacker.nextCheckPointId]);
//
//        calculateBezier(
//                attacker.vel,
//                attacker.loc,
//                sCheckpoints[attacker.nextCheckPointId],
//                getAfterNextCheckpoint(attacker),
//                answerAttacker);
//
//        calculateBezier(
//                protector.vel,
//                protector.loc,
//                sCheckpoints[protector.nextCheckPointId],
//                getAfterNextCheckpoint(protector),
//                answerProtector);
//    }

//    private static void calculateBezier(
//            Vector currentVelocity,
//            Vector p0,
//            Vector p1,
//            Vector p2,
//            Answer answer) {
//        float tMin = 0.0f;
//        float tMax = 1.0f;
//        float thrustLen2 = 0;
//        float minDst2 = 0;
//        boolean isFirstStep = true;
//        //todo value?
//        for (int i = 0; i < 20; i++) {
//            final float t = (tMin + tMax) / 2;
//            sBP0.set(p0).scl((1 - t) * (1 - t));
//            sBP1.set(p1).scl(2 * t * (1 - t));
//            sBP2.set(p2).scl(t * t);
//            sBPResult.set(sBP0).add(sBP1).add(sBP2);
//
//            sBPThrust.set(sBPResult).sub(p0);
//            sBPNextLocation.set(p0).add(currentVelocity).add(sBPThrust);
//
//            final float dst2 = sBPNextLocation.dst2(p1);
//
//            thrustLen2 = sBPThrust.len2();
////            log("t=" + t + ", tl2=" + thrustLen2);
//            if (isFirstStep || minDst2 > dst2) {
//                isFirstStep = false;
//                minDst2 = dst2;
//                sBPTheBestThrust.set(sBPThrust);
//                log("t=" + t + ", od=" + minDst2);
//            }
//            if (thrustLen2 > MAX_THRUST_2) {
//                tMax /= 2;
//            } else {
//                tMin = tMax;
//                tMax = (1 + tMax) / 2;
//            }
//        }
//
//        sBPThrust.set(p0).add(sBPTheBestThrust);
//        answer.x = (int) sBPThrust.x;
//        answer.y = (int) sBPThrust.y;
//        final float thrust2 = sBPTheBestThrust.len2();
//        answer.thrust = thrust2 > MAX_THRUST_2 ? MAX_THRUST : (int) Math.sqrt(thrust2);
//    }

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

        public int passedCheckpoints = 0;

        public Pod(String name) {
            this.name = name;
        }

        public void readData(Scanner in) {
            loc.x = in.nextInt();
            loc.y = in.nextInt();
            vel.x = in.nextInt();
            vel.y = in.nextInt();
            vel.scl(RESISTANCE);
            angle = in.nextInt();
            final int oldTargetCheckpoint = nextCheckPointId;
            nextCheckPointId = in.nextInt();
            if (nextCheckPointId != oldTargetCheckpoint) {
                passedCheckpoints++;
                log(name + " passed " + oldTargetCheckpoint + ", left " + (sTotalCheckpoints - passedCheckpoints));
            }
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
            final String m = message.isEmpty() ? "" : " " + message;
            if (shieldIsActivated) {
                return x + " " + y + " SHIELD" + " Shield" + m;
            }

            return x + " " + y + " " + thrust + m;
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