import java.util.*;

class Player {

    private static final boolean SHOW_LOGS = true;

    private static final int MAX_THRUST = 200;
    private static final int MAX_ANGLE = 18;
    private static final int POD_RADIUS = 400;
    private static final int CHECKPOINT_RADIUS = 600;

    private static int sCountLaps;
    private static Point[] sCheckpoints;
    private static Pod[] sMyPods = new Pod[2];;
    private static Pod[] sOppPods = new Pod[2];;

    public static void main(String args[]) {
        final Scanner in = new Scanner(System.in);
        sCountLaps = in.nextInt();
        final int checkpointCount = in.nextInt();
        sCheckpoints = new Point[checkpointCount];
        for (int i = 0; i < checkpointCount; i++) {
            final Point point = new Point();
            point.x = in.nextInt();
            point.y = in.nextInt();
            sCheckpoints[i] = point;
        }

        sMyPods[0] = new Pod();
        sMyPods[1] = new Pod();
        sOppPods[0] = new Pod();
        sOppPods[1] = new Pod();

        // game loop
        while (true) {
            sMyPods[0].readData(in);
            sMyPods[1].readData(in);
            sOppPods[0].readData(in);
            sOppPods[1].readData(in);

            final String answer = getAnswer();

            System.out.println(answer); //"8000 4500 100"
            System.out.println(answer);
        }
    }

    private static String getAnswer() {
        final Point nextCheckpoint = sCheckpoints[sMyPods[0].nextCheckPointId];
        return nextCheckpoint.x + " " + nextCheckpoint.y + " 200";
    }

    private static void log(String message) {
        if (SHOW_LOGS) {
            System.err.println(message);
        }
    }

    private static class Pod {
        public Point loc = new Point();
        public Point vel = new Point();
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

    private static class Point {
        int x;
        int y;
    }
}