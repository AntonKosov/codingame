package hard;

import java.util.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
public class Labyrinth {

    private static final boolean SHOW_LOGS = false;
    private static final boolean SLOW_MODE = false;

    private static final int COUNT_PATHS = 3;
    private static final int BEST_EXPLORE_PATH = 0;
    private static final int BACK_PATH = 1;
    private static final int TO_CONTROL_PATH = 2;

    private static final int UP = 0;
    private static final int RIGHT = 1;
    private static final int DOWN = 2;
    private static final int LEFT = 3;

    private static final LinkedList<Cell> sTempQueue = new LinkedList<Cell>();

    private static int sHeight;
    private static int sWidth;

    private static int sKX;
    private static int sKY;

    private static Cell[][] sMaze;

    private static Cell sTeleport;

    private static Cell sControl;

    private static State sState;

    public static void main(String args[]) {
        final Scanner in = new Scanner(System.in);

        sHeight = in.nextInt(); // number of rows.
        sWidth = in.nextInt(); // number of columns.
        final int timeOfAlarm = in.nextInt(); // number of rounds between the time the alarm countdown is activated and the time the alarm goes off.
        log("h=" + sHeight + ", w=" + sWidth + ", a=" + timeOfAlarm);
        sState = State.lookingControl;
        sTeleport = null;
        sControl = null;

        sMaze = new Cell[sWidth][sHeight];
        for (int y = 0; y < sHeight; y++) {
            final int countRows = y == 0 || y == sHeight - 1 ? 3 : y == 1 || y == sHeight - 2 ? 4 : 5;
            for (int x = 0; x < sWidth; x++) {
                int countColumns = x == 0 || x == sWidth - 1 ? 3 : x == 1 || x == sWidth - 2 ? 4 : 5;
                final Cell cell = new Cell(x, y);
                cell.countUnknownCells = countColumns * countRows;
                sMaze[x][y] = cell;
            }
        }
        int step = 0;

        // game loop
        final LinkedList<Cell> path = new LinkedList<Cell>();
        while (true) {
            step++;
            log("step=" + step);
            sKY = in.nextInt(); // row where Kirk is located.
            sKX = in.nextInt(); // column where Kirk is located.
            log("kx=" + sKX + ", ky=" + sKY);
            if (readMaze(in)) {
                if (sState == State.lookingControl && sControl != null) {
                    sState = State.lookingBackPath;
                    log("The control is found");
                }

                if (sState == State.lookingControl) {
                    lookForPaths(sKX, sKY, BEST_EXPLORE_PATH);
                    calculateBestExplorePath(BEST_EXPLORE_PATH, path);
                    log("Looking for the control");
                } else if (sState == State.lookingBackPath) {
                    lookForPaths(sControl.x, sControl.y, BACK_PATH);
                    if (sTeleport.paths[BACK_PATH] <= timeOfAlarm) {
                        sState = State.goToControl;
                        lookForPaths(sKX, sKY, TO_CONTROL_PATH);
                        calculatePath(TO_CONTROL_PATH, sControl, path);
                        log("Go to the control");
                    } else {
                        lookForPaths(sKX, sKY, BEST_EXPLORE_PATH);
                        calculateBestExplorePath(BEST_EXPLORE_PATH, path);
                        log("Looking for a better path to control");
                    }
                } else if (sState == State.goToControl) {
                    //nothing
                } else if (sState == State.goToTeleport) {
                    //nothing
                }
            }

            final Cell nextCell = path.pop();
            if (nextCell == sControl) {
                sState = State.goToTeleport;
                calculatePath(BACK_PATH, sTeleport, path);
                log("Go to the teleport");
            }
            String nextStep;
            if (nextCell.y < sKY) {
                nextStep = "UP";
            } else if (nextCell.x > sKX) {
                nextStep = "RIGHT";
            } else if (nextCell.y > sKY) {
                nextStep = "DOWN";
            } else {
                nextStep = "LEFT";
            }

            System.out.println(nextStep); // Kirk's next move (UP DOWN LEFT or RIGHT).
            if (SLOW_MODE) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void calculatePath(int indexOfPath, Cell finishCell, LinkedList<Cell> result) {
        result.clear();
        Cell currentCell = finishCell;
        while (currentCell != null) {
            result.add(0, currentCell);
            Cell nextCell = null;
            for (Cell neighbour : currentCell.neighbours) {
                if (neighbour != null && neighbour.paths[indexOfPath] + 1 == currentCell.paths[indexOfPath]) {
                    if (nextCell == null || nextCell.indexOfExplore < neighbour.indexOfExplore) {
                        nextCell = neighbour;
                    }
                }
            }
            currentCell = nextCell;
        }
        result.pop(); // current point
    }

    private static void calculateBestExplorePath(int indexOfPath, LinkedList<Cell> result) {
        sTempQueue.clear();
        final Cell firstCell = sMaze[sKX][sKY];
        firstCell.indexOfExplore = 0;
        sTempQueue.add(firstCell);
        Cell bestCell = null;
        while (!sTempQueue.isEmpty()) {
            final Cell currentCell = sTempQueue.pop();
            final int nextStep = currentCell.paths[indexOfPath] + 1;
            for (Cell neighbour : currentCell.neighbours) {
                if (neighbour != null && neighbour.paths[indexOfPath] == nextStep) {
                    neighbour.indexOfExplore = currentCell.indexOfExplore + ((float) neighbour.countUnknownCells) / nextStep / nextStep;
                    if (bestCell == null || bestCell.indexOfExplore < neighbour.indexOfExplore) {
                        bestCell = neighbour;
                    }
                    sTempQueue.add(neighbour);
                }
            }
            if (bestCell != null && bestCell.indexOfExplore > 0) {
                break;
            }
        }

        calculatePath(indexOfPath, bestCell, result);
    }

    private static void lookForPaths(int sX, int sY, int indexOfPath) {
        for (int x = 0; x < sWidth; x++) {
            for (int y = 0; y < sHeight; y++) {
                sMaze[x][y].paths[indexOfPath] = Integer.MAX_VALUE;
            }
        }
        sMaze[sX][sY].paths[indexOfPath] = 0;

        sTempQueue.clear();
        sTempQueue.add(sMaze[sX][sY]);
        while (!sTempQueue.isEmpty()) {
            final Cell cell = sTempQueue.pop();
            final int nextStep = cell.paths[indexOfPath] + 1;
            for (Cell neighbour : cell.neighbours) {
                if (neighbour != null && neighbour.paths[indexOfPath] > nextStep) {
                    if (neighbour.type == CellType.control &&
                            (sState == State.lookingControl || sState == State.lookingBackPath)) {
                        continue;
                    }
                    neighbour.paths[indexOfPath] = nextStep;
                    sTempQueue.add(neighbour);
                }
            }
        }
    }

    private static boolean readMaze(Scanner in) {
        boolean existsNewCells = false;
        for (int y = 0; y < sHeight; y++) {
            String row = in.next(); // C of the characters in '#.TC?' (i.e. one line of the ASCII maze).
            if (SHOW_LOGS) {
                if (y == sKY) {
                    String rowForLog = "";
                    if (sKX > 0) {
                        rowForLog = row.substring(0, sKX);
                    }
                    rowForLog += "X";
                    if (sKX < sWidth - 1) {
                        rowForLog += row.substring(sKX + 1, sWidth);
                    }
                    log(rowForLog);
                } else {
                    log(row);
                }
            }
            for (int x = 0; x < sWidth; x++) {
                final char ch = row.charAt(x);
                final Cell cell = sMaze[x][y];
                if (cell.type != CellType.unknown) {
                    continue;
                }
                switch (ch) {
                    case '?':
                        continue;
                    case '#':
                        openCell(cell, CellType.wall);
                        break;
                    case '.':
                        openCell(cell, CellType.empty);
                        break;
                    case 'T':
                        openCell(cell, CellType.teleport);
                        sTeleport = cell;
                        break;
                    case 'C':
                        openCell(cell, CellType.control);
                        sControl = cell;
                        break;
                }
                existsNewCells = true;
            }
        }

        log("---");

        return existsNewCells;
    }

    private static void openCell(Cell cell, CellType type) {
        if (cell.type != CellType.unknown) {
            return;
        }

        cell.type = type;
        final int xs = cell.x > 1 ? cell.x - 2 : 0;
        final int xe = cell.x < sWidth - 2 ? cell.x + 2 : sWidth - 1;
        final int ys = cell.y > 1 ? cell.y - 2 : 0;
        final int ye = cell.y < sHeight - 2 ? cell.y + 2 : sHeight - 1;
        for (int x = xs; x <= xe; x++) {
            for (int y = ys; y <= ye; y++) {
                sMaze[x][y].countUnknownCells--;
            }
        }
        if (type != CellType.wall) {
            if (cell.x > 0) {
                sMaze[cell.x - 1][cell.y].neighbours[RIGHT] = cell;
            }
            if (cell.x < sWidth - 1) {
                sMaze[cell.x + 1][cell.y].neighbours[LEFT] = cell;
            }
            if (cell.y > 0) {
                sMaze[cell.x][cell.y - 1].neighbours[DOWN] = cell;
            }
            if (cell.y < sHeight - 1) {
                sMaze[cell.x][cell.y + 1].neighbours[UP] = cell;
            }
        }
    }

    private static void log(String message) {
        if (SHOW_LOGS) {
            System.err.println(message);
        }
    }

    private enum CellType {
        unknown, wall, empty, teleport, control
    }

    private enum State {
        lookingControl, lookingBackPath, goToControl, goToTeleport
    }

    private static class Cell {
        public final int x;
        public final int y;
        public int countUnknownCells = 0;
        public CellType type = CellType.unknown;
        public final int[] paths = new int[COUNT_PATHS];
        public final Cell[] neighbours = new Cell[4];
        public float indexOfExplore;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "x=" + x + ", y=" + y + ", t=" + type;
        }
    }
}