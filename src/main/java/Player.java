import java.util.*;

class Player {

    private static final boolean SHOW_LOGS = true;

    private static final int MAX_THRUST = 200;
    private static final int MAX_THRUST_2 = MAX_THRUST * MAX_THRUST;
    private static final int MAX_ANGLE = 18;
    private static final int POD_RADIUS = 400;
    private static final int CHECKPOINT_RADIUS = 600;

    private static int sCountLaps;
    private static Vector[] sCheckpoints;
    private static Pod[] sMyPods = new Pod[2];;
    private static Pod[] sOppPods = new Pod[2];

    private static Vector sBP0 = new Vector();
    private static Vector sBP1 = new Vector();
    private static Vector sBP2 = new Vector();
    private static Vector sBPResult = new Vector();
    private static Vector sBPThrust = new Vector();
    private static Vector sBPTheBestThrust = new Vector();
    private static Vector sBPNextLocation = new Vector();

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

        sMyPods[0] = new Pod();
        sMyPods[1] = new Pod();
        sOppPods[0] = new Pod();
        sOppPods[1] = new Pod();

        final Answer answerAttacker = new Answer();
        final Answer answerProtector = new Answer();
        // game loop
        while (true) {
            sMyPods[0].readData(in);
            sMyPods[1].readData(in);
            sOppPods[0].readData(in);
            sOppPods[1].readData(in);

            calculateAnswer(answerAttacker, answerProtector);

            System.out.println(answerAttacker); //"8000 4500 100"
            System.out.println(answerProtector);
        }
    }

    private static void calculateAnswer(Answer answerAttacker, Answer answerProtector) {
        final Pod attacker = sMyPods[0];
        final Pod protector = sMyPods[1];

        log("targeta=" + attacker.nextCheckPointId + ", t=" + sCheckpoints[attacker.nextCheckPointId]);

        calculateBezier(
                attacker.vel,
                attacker.loc,
                sCheckpoints[attacker.nextCheckPointId],
                getAfterNextCheckpoint(attacker),
                answerAttacker);

        calculateBezier(
                protector.vel,
                protector.loc,
                sCheckpoints[protector.nextCheckPointId],
                getAfterNextCheckpoint(protector),
                answerProtector);
    }

    private static void calculateBezier(
            Vector currentVelocity,
            Vector p0,
            Vector p1,
            Vector p2,
            Answer answer) {
        float tMin = 0.0f;
        float tMax = 1.0f;
        int thrustLen2 = 0;
        int minDst2 = 0;
        boolean isFirstStep = true;
        //todo value?
        for (int i = 0; i < 20; i++) {
            final float t = (tMin + tMax) / 2;
            sBP0.set(p0).scl((1 - t) * (1 - t));
            sBP1.set(p1).scl(2 * t * (1 - t));
            sBP2.set(p2).scl(t * t);
            sBPResult.set(sBP0).add(sBP1).add(sBP2);

            sBPThrust.set(sBPResult).sub(p0);
            sBPNextLocation.set(p0).add(currentVelocity).add(sBPThrust);

            final int dst2 = sBPNextLocation.dst2(p1);

            thrustLen2 = sBPThrust.len2();
//            log("t=" + t + ", tl2=" + thrustLen2);
            if (isFirstStep || minDst2 > dst2) {
                isFirstStep = false;
                minDst2 = dst2;
                sBPTheBestThrust.set(sBPThrust);
                log("t=" + t + ", od=" + minDst2);
            }
            if (thrustLen2 > MAX_THRUST_2) {
                tMax /= 2;
            } else {
                tMin = tMax;
                tMax = (1 + tMax) / 2;
            }
        }

        sBPThrust.set(p0).add(sBPTheBestThrust);
        answer.x = sBPThrust.x;
        answer.y = sBPThrust.y;
        final int thrust2 = sBPTheBestThrust.len2();
        answer.thrust = thrust2 > MAX_THRUST_2 ? MAX_THRUST : (int) Math.sqrt(thrust2);
    }

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
        public Vector loc = new Vector();
        public Vector vel = new Vector();
        public int angle;
        public int nextCheckPointId;

        public void readData(Scanner in) {
            loc.x = in.nextInt();
            loc.y = in.nextInt();
            vel.x = in.nextInt();
            vel.y = in.nextInt();
            angle = in.nextInt();
            nextCheckPointId = in.nextInt();
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

        public int x;
        public int y;

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
            x = Math.round(scalar * x);
            y = Math.round(scalar * y);
            return this;
        }

        public Vector set(Vector v) {
            x = v.x;
            y = v.y;
            return this;
        }

        public int dst2(Vector v) {
            final int dX = x - v.x;
            final int dY = y - v.y;
            return dX * dX + dY * dY;
        }

        public int len2() {
            return x * x + y * y;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }
}