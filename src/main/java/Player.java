import java.util.*;

class Player {

    private static final boolean SHOW_LOGS = true;

    private static final float RESISTANCE = 1 - 0.15384615384615384615f;
    private static final float MAX_THRUST = 200;
    private static final float MAX_THRUST_2 = MAX_THRUST * MAX_THRUST;
    private static final int MAX_ANGLE = 18;
    private static final int POD_RADIUS = 400;
    private static final int CHECKPOINT_RADIUS = 600;

    private static int sCountLaps;
    private static Vector[] sCheckpoints;
    private static Pod[] sOppPods = new Pod[2];

    private static final Pod sRacer = new Pod("Racer");
    private static final Pod sHelper = new Pod("Helper");

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
        sCheckpoints = new Vector[checkpointCount];
        for (int i = 0; i < checkpointCount; i++) {
            final Vector point = new Vector();
            point.x = in.nextInt();
            point.y = in.nextInt();
            sCheckpoints[i] = point;
        }

        sOppPods[0] = new Pod("Enemy0");
        sOppPods[1] = new Pod("Enemy1");

        final Answer answerRacer = new Answer();
        final Answer answerHelper = new Answer();

//        int step = 0;
//        String[] a = new String[] {
//                "10000 10000 0",
//                "10000 10000 0",
//                "10000 10000 0",
//                "10000 10000 0",
//                "10000 10000 0",
//                "10000 10000 200",
//                "10000 10000 0",
//                "10000 10000 0",
//                "10000 10000 0",
//                "10000 10000 0",
//                "10000 10000 200",
//                "10000 10000 200",
//                "10000 10000 200",
//                "10000 10000 200",
//                "10000 10000 200",
//                "10000 10000 200",
//                "10000 10000 200",
//                "0 0 200",
//                "0 0 200",
//                "0 0 200",
//                "0 0 200",
//                "0 0 200",
//        };


        // game loop
        while (true) {
            sHelper.readData(in);
            sRacer.readData(in);
            sOppPods[0].readData(in);
            sOppPods[1].readData(in);

            calculateRacer(sRacer, answerRacer);
            calculateRacer(sHelper, answerHelper);

            System.out.println(answerRacer);
            System.out.println(answerHelper);

//            step++;
//            if (step == a.length) step = 0;
//
//            log("vel=" + sMyPods[0].vel + ", sp=" + sMyPods[0].vel.len());
//
//            System.out.println(a[step]); //"8000 4500 100"
//            System.out.println("0 0 0");
        }
    }

    private static void calculateRacer(final Pod pod, Answer answer) {
        final Vector p1 = pod.loc;
        final Vector p2 = sCheckpoints[pod.nextCheckPointId];
        final Vector p3 = getAfterNextCheckpoint(pod);
        final Vector centerOfCircle = getCenterOfCircle(p1, p2, p3);

        final Vector p1p3 = new Vector(p3).sub(p1);
        final Vector p1p2 = new Vector(p2).sub(p1);
        final float sign = p1p3.crs(p1p2) > 0 ? -1 : +1;

        final Vector optimalOffset = new Vector(p1).sub(centerOfCircle).rotate(MAX_ANGLE * sign);
        final Vector wantPoint = new Vector(centerOfCircle).add(optimalOffset);
        final Vector wantVelocity = new Vector(wantPoint).sub(p1).scl(1.0f / RESISTANCE);
        final Vector thrust = new Vector(wantVelocity).sub(pod.vel);

        //todo ?
//        thrust.scl(1.0f / RESISTANCE);
        final Vector thrustPod = new Vector(p1).add(thrust);
        answer.x = (int) thrustPod.x;
        answer.y = (int) thrustPod.y;
        final float len2 = thrust.len2();
        answer.thrust = (int) (len2 > MAX_THRUST_2 ? MAX_THRUST : Math.sqrt(len2));

        //log(pod + ", a=" + answer + ", center=" + centerOfCircle + ", thrust=" + thrust + ", wantVelocity=" + wantVelocity);
    }

    private static Vector getCenterOfCircle(Vector p1, Vector p2, Vector p3) {
        if (p1.equals(p3)) {
            return new Vector(p1).add(p2).scl(0.5f);
        }

        final Vector point1;
        final Vector point2;
        final Vector point3;
        if (p2.x - p1.x == 0) {
            point1 = p1;
            point2 = p3;
            point3 = p2;
        } else if (p3.x - p2.x == 0) {
            point1 = p2;
            point2 = p1;
            point3 = p3;
        } else {
            point1 = p1;
            point2 = p2;
            point3 = p3;
        }

        final float ma = (point2.y - point1.y) / (point2.x - point1.x);
        final float mb = (point3.y - point2.y) / (point3.x - point2.x);

//        log("ma=" + ma + ", mb=" + mb + ", point2=" + point2 + ", point3=" + point3 + "(point3.x - point2.x)=" + (point3.x - point2.x));

        //todo div 0
        final float x = (ma * mb * (point1.y - point3.y) + mb * (point1.x + point2.x) - ma * (point2.x + point3.x)) / (2 * (mb - ma));
        //todo div 0
        final float y = -1.0f / ma * (x - (point1.x + point2.x) / 2) + (point1.y + point2.y) / 2;

//        log("center: " + point1 + ", " + point2 + ", " + point3);

        return new Vector(x, y);
    }

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
        if (SHOW_LOGS) {
            System.err.println(message);
        }
    }

    private static class Pod {

        public final String name;

        public final Vector loc = new Vector();
        public final Vector vel = new Vector();
        public int angle;
        public int nextCheckPointId;

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
            nextCheckPointId = in.nextInt();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class Answer {

        public int x;
        public int y;
        public int thrust;
        public boolean shieldIsActivated;

        @Override
        public String toString() {
            if (shieldIsActivated) {
                return "SHIELD";
            }

            return x + " " + y + " " + thrust;
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
            return (float)Math.sqrt(len2());
        }

        public float len2() {
            return x * x + y * y;
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
            return "(" + ((int) x) + ", " + ((int) y) + ")";
        }
    }
}