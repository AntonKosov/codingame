import java.util.*;

class Player {

    private static final boolean SHOW_LOGS = false;

    private static final int EMPTY = -1;

    private static int[][] sMap;
    private static int sWidth;
    private static int sHeight;
    private static int sOldX;
    private static int sOldY;
    private static int sNewX;
    private static int sNewY;
    private static int sCenterOfMassX;
    private static int sCenterOfMassY;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        sWidth = in.nextInt(); // width of the building.
        sHeight = in.nextInt(); // height of the building.
        in.nextInt(); // maximum number of turns before game over.
        sOldX = in.nextInt();
        sOldY = in.nextInt();

        sMap = new int[sWidth][sHeight];
        sNewX = sOldX;
        sNewY = sOldY;
        for (int y = 0; y < sHeight; y++) {
            for (int x = 0; x < sWidth; x++) {
                sMap[x][y] = dist2(sOldX, sOldY, x, y);
            }
        }

        // game loop
        while (true) {
            String BOMBDIST = in.next(); // Current distance to the bomb compared to previous distance (COLDER, WARMER, SAME or UNKNOWN)

            log("BOMBDIST=" + BOMBDIST);
            logMap();

            if (BOMBDIST.equals("UNKNOWN")) {
                firstJump();
            } else {
                updateMap(BOMBDIST);
                findNewCoord();
                log("vvv");
                logMap();
            }

            System.out.println(sNewX + " " + sNewY);
        }
    }

    private static void findNewCoord() {
        sNewX = sCenterOfMassX + (sCenterOfMassX - sOldX);
        sNewY = sCenterOfMassY + (sCenterOfMassY - sOldY);
        if (isPossible(sNewX, sNewY)) {
            return;
        }

        int radius = 1;
        int dx = 0;
        int dy = -1;
        while (true) {
            for (int s = 0; s < 2; s++) {
                for (int i = 0; i < radius; i++) {
                    sNewX += dx;
                    sNewY += dy;
                    if (isPossible(sNewX, sNewY)) {
                        return;
                    }
                }
                if (dy < 0) {
                    dx = 1;
                    dy = 0;
                } else if (dx > 0) {
                    dx = 0;
                    dy = 1;
                } else if (dy > 0) {
                    dx = -1;
                    dy = 0;
                } else {
                    dx = 0;
                    dy = -1;
                }
            }

            radius++;
        }
    }

    private static boolean isPossible(int x, int y) {
        return x >= 0 && x < sWidth && y >= 0 && y < sHeight && sMap[x][y] > 0;
    }

    private static void updateMap(String answer) {
        final Distance dist = answer.equals("COLDER") ? Distance.colder : answer.equals("WARMER") ? Distance.warmer : Distance.same;
        int countCells = 0;
        int weightX = 0;
        int weightY = 0;
        for (int y = 0; y < sHeight; y++) {
            for (int x = 0; x < sWidth; x++) {
                int oldDist2 = sMap[x][y];
                if (oldDist2 == EMPTY) {
                    continue;
                }

                int newDist2 = dist2(sNewX, sNewY, x, y);
                switch (dist) {
                    case same:
                        if (oldDist2 != newDist2) {
                            sMap[x][y] = EMPTY;
                        }
                        break;
                    case colder:
                        if (oldDist2 >= newDist2) {
                            sMap[x][y] = EMPTY;
                        } else {
                            sMap[x][y] = newDist2;
                        }
                        break;
                    case warmer:
                        if (oldDist2 <= newDist2) {
                            sMap[x][y] = EMPTY;
                        } else {
                            sMap[x][y] = newDist2;
                        }
                }
                if (sMap[x][y] != EMPTY) {
                    weightX += x;
                    weightY += y;
                    countCells++;
                }

            }
        }
        sOldX = sNewX;
        sOldY = sNewY;
        sCenterOfMassX = weightX / countCells;
        sCenterOfMassY = weightY / countCells;
    }

    private static void firstJump() {
        int middleX = sWidth / 2;
        int middleY = sHeight / 2;
        if (sWidth % 2 == 1 && sHeight % 2 == 1 && middleX == sOldX && middleY == sOldY) {
            sNewX++;
            sNewY++;
        } else {
            sNewX = sWidth - sNewX - 1;
            sNewY = sHeight - sNewY - 1;
        }
    }

    private static int dist2(int x0, int y0, int x1, int y1) {
        return (x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0);
    }

    private static void logMap() {
        if (!SHOW_LOGS) {
            return;
        }
        StringBuilder sb = new StringBuilder(sWidth);
        for (int y = 0; y <sHeight; y++) {
            sb.setLength(0);
            for (int x = 0; x < sWidth; x++) {
                if (x == sOldX && y == sOldY) {
                    sb.append("O");
                } else if (x == sNewX & y == sNewY) {
                    sb.append("N");
                } else {
                    sb.append(sMap[x][y] == EMPTY ? "." : "?");
                }
            }
            log(sb.toString());
        }
    }

    private static void log(String message) {
        if (SHOW_LOGS) {
            System.err.println(message);
        }
    }

    private enum Distance {
        same, colder, warmer
    }
}