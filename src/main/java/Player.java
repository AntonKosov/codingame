import java.util.*;

class Player {

    private static Point sExitPoint;
    private static int sWidth;
    private static final HashSet<Point> sElevs = new HashSet<Point>();
    private static final HashSet<Point> sBlocked = new HashSet<Point>();

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int nbFloors = in.nextInt(); // number of floors
        sWidth = in.nextInt(); // width of the area
        int nbRounds = in.nextInt(); // maximum number of rounds
        int exitFloor = in.nextInt();
        int exitPos = in.nextInt();
        int nbTotalClones = in.nextInt(); // number of generated clones
        int nbAdditionalElevators = in.nextInt(); // number of additional elevators
        int nbElevators = in.nextInt(); // number of elevators

        sExitPoint = new Point(exitFloor, exitPos);

        for (int i = 0; i < nbElevators; i++) {
            int elevatorFloor = in.nextInt(); // floor on which this elevator is found
            int elevatorPos = in.nextInt(); // position of the elevator on its floor
            sElevs.add(new Point(elevatorFloor, elevatorPos));
        }

        final Point currentPoint = new Point(0, 0);

        // game loop
        while (true) {
            int cloneFloor = in.nextInt(); // floor of the leading clone
            int clonePos = in.nextInt(); // position of the leading clone on its floor
            String direction = in.next(); // direction of the leading clone: LEFT or RIGHT

            String action = "WAIT";
            currentPoint.floor = cloneFloor;
            currentPoint.pos = clonePos;
            if (!direction.equals("NONE") && !sElevs.contains(currentPoint)) {
                final int directionOffset = direction.equals("LEFT") ? -1 : 1;

                boolean isExitPresent = check(currentPoint, directionOffset);

                if (!isExitPresent) {
                    boolean isBackExist = check(currentPoint, -directionOffset);
                    if (!isBackExist) {
                        action = "ELEVATOR";
                        sElevs.add(currentPoint);
                        isExitPresent = true;
                    }
                }

                if (!isExitPresent) {
                    action = "BLOCK";
                    sBlocked.add(new Point(cloneFloor, clonePos));
                }
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            System.out.println(action); // action: WAIT or BLOCK or ELEVATOR
        }
    }

    /**
     * Find the exit point.
     * @param startPoint A start point
     * @param direction Direction +-1 (right/left)
     * @return The exit point or an elevator is present
     */
    private static boolean check(Point startPoint, int direction) {
        final Point p = new Point(startPoint);
        p.pos = p.pos + direction;
        while (p.pos >= 0 && p.pos < sWidth) {
            if (sExitPoint.equals(p) || sElevs.contains(p)) {
                return true;
            }
            if (sBlocked.contains(p)) {
                return false;
            }
            p.pos += direction;
        }

        return false;
    }

    private static class Point {
        public int floor;
        public int pos;

        public Point(Point point) {
            floor = point.floor;
            pos = point.pos;
        }

        public Point(int floor, int pos) {
            this.floor = floor;
            this.pos = pos;
        }

        @Override
        public boolean equals(Object obj) {
            Point another = (Point) obj;
            return floor == another.floor && pos == another.pos;
        }

        @Override
        public int hashCode() {
            return floor * 1000 + pos;
        }
    }
}