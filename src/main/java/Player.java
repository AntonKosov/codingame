import java.util.*;

class Player {

    private static Point sExitPoint;
    private static int sWidth;
    private static int sAdditionalElevators;
    private static final HashSet<Point> sElevs = new HashSet<Point>();
    private static final HashSet<Point> sBlocked = new HashSet<Point>();
    private static final HashSet<Integer> sFloorsWithoutElevators = new HashSet<Integer>();

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int nbFloors = in.nextInt(); // number of floors
        sWidth = in.nextInt(); // width of the area
        int nbRounds = in.nextInt(); // maximum number of rounds
        int exitFloor = in.nextInt();
        int exitPos = in.nextInt();
        int nbTotalClones = in.nextInt(); // number of generated clones
        sAdditionalElevators = in.nextInt(); // number of additional elevators
        int nbElevators = in.nextInt(); // number of elevators

        log("nbFloors=" + nbFloors + ", nbRounds=" + nbRounds + ", sAdditionalElevators=" + sAdditionalElevators +
                ", nbTotalClones=" + nbTotalClones);

        sExitPoint = new Point(exitFloor, exitPos);

        for (int i = 0; i < exitFloor; i++) {
            sFloorsWithoutElevators.add(i);
        }
        for (int i = 0; i < nbElevators; i++) {
            int elevatorFloor = in.nextInt(); // floor on which this elevator is found
            int elevatorPos = in.nextInt(); // position of the elevator on its floor
            sElevs.add(new Point(elevatorFloor, elevatorPos));
            sFloorsWithoutElevators.remove(elevatorFloor);
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
            if (!direction.equals("NONE")) {
                BestTurn bestTurn = getBestTurn(currentPoint);
                final int directionOffset = direction.equals("LEFT") ? -1 : 1;
                switch (bestTurn) {
                    case left:
                        if (directionOffset > 0) {
                            action = "BLOCK";
                            sBlocked.add(new Point(cloneFloor, clonePos));
                        }
                        break;
                    case right:
                        if (directionOffset < 0) {
                            action = "BLOCK";
                            sBlocked.add(new Point(cloneFloor, clonePos));
                        }
                        break;
                    case elevator:
                        action = "ELEVATOR";
                        sElevs.add(currentPoint);
                        sFloorsWithoutElevators.remove(cloneFloor);
                        sAdditionalElevators--;
                        break;
                    case wait:
                        break;
                }
            }

            System.out.println(action); // action: WAIT or BLOCK or ELEVATOR
            nbRounds--;
            log(nbRounds + " rounds left");
        }
    }

    private static BestTurn getBestTurn(Point currentPoint) {
        if (sElevs.contains(currentPoint)) {
            return BestTurn.wait;
        }
        CheckResult toRight = check(currentPoint, 1);
        CheckResult toLeft = check(currentPoint, -1);
        CheckResult toElevator;
        if (sAdditionalElevators - sFloorsWithoutElevators.size() > 0) {
            log("sae=" + sAdditionalElevators + ", ef=" + sExitPoint.floor + ", cf=" + currentPoint.floor + ", cfwe=" + sFloorsWithoutElevators.size());
            toElevator = new CheckResult(false, true, 0);
        } else {
            toElevator = new CheckResult(false, false, 0);
        }
        log("r: " + toRight);
        log("l: " + toLeft);
        log("e: " + toElevator);

        if (toRight.isExit) {
            return BestTurn.right;
        } else if (toLeft.isExit) {
            return BestTurn.left;
        } else if (toElevator.isPathExist || (!toRight.isPathExist && !toLeft.isPathExist)) {
            return BestTurn.elevator;
        } else if (toLeft.isPathExist && toRight.isPathExist) {
            return toLeft.countTurnsToTheNextFloor > toRight.countTurnsToTheNextFloor ? BestTurn.right : BestTurn.left;
        } else if (toLeft.isPathExist) {
            return BestTurn.left;
        } else {
            return BestTurn.right;
        }
    }

    private static void log(String message) {
        System.err.println(message);
    }

    private static CheckResult check(Point startPoint, int direction) {
        final Point p = new Point(startPoint);
        p.pos = p.pos + direction;
        int countTurns = 0;
        while (p.pos >= 0 && p.pos < sWidth) {
            countTurns++;
            if (sExitPoint.equals(p)) {
                return new CheckResult(true, true, countTurns);
            }
            if (sElevs.contains(p)) {
                return new CheckResult(false, true, countTurns);
            }
            if (sBlocked.contains(p)) {
                break;
            }
            p.pos += direction;
        }

        return new CheckResult(false, false, 0);
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

    private enum BestTurn {
        wait, left, right, elevator
    }

    private static class CheckResult {
        public final int countTurnsToTheNextFloor;
        public final boolean isPathExist;
        public final boolean isExit;

        public CheckResult(boolean isExit, boolean isPathExist, int countTurnsToTheNextFloor) {
            this.isExit = isExit;
            this.isPathExist = isPathExist;
            this.countTurnsToTheNextFloor = countTurnsToTheNextFloor;
        }

        @Override
        public String toString() {
            return "c=" + countTurnsToTheNextFloor + ", ipe=" + isPathExist + ", ie=" + isExit;
        }
    }
}