import java.util.*;

class Player {

    private static final boolean SHOW_LOGS = true;

    private static final String ACTION_WAIT = "WAIT";

    private static int sWidth;
    private static int sHeight;

    public static void main(String args[]) {
        final Scanner in = new Scanner(System.in);
        sWidth = in.nextInt(); // width of the firewall grid
        sHeight = in.nextInt(); // height of the firewall grid
        in.nextLine();
        final Cell[][] map = new Cell[sWidth][sHeight];
        int countNodes = 0;
        for (int i = 0; i < sHeight; i++) {
            final String mapRow = in.nextLine(); // one line of the firewall grid
            for (int j = 0; j < sWidth; j++) {
                final String s = mapRow.substring(j, j + 1);
                final Cell cell = new Cell(j, i);
                cell.obj = s.equals(".") ? new Empty() : s.equals("@") ? new Node() : new Wall();
                map[j][i] = cell;
                if (cell.obj instanceof Node) {
                    countNodes++;
                }
            }
        }
        initReachableCells(map);

        // game loop
        LinkedList<String> answers = null;
        while (true) {
            final int rounds = in.nextInt(); // number of rounds left before the end of the game
            final int bombs = in.nextInt(); // number of bombs left

            if (answers == null) {
                answers = getAnswer(map, rounds, bombs, countNodes);
            }

            System.out.println(answers.pop());
        }
    }

    private static void initReachableCells(Cell[][] map) {
        for (int y = 0; y < sHeight; y++) {
            for (int x = 0; x < sWidth; x++) {
                map[x][y].initReachableCells(map);
            }
        }
    }

    private static LinkedList<String> getAnswer(Cell[][] map, int rounds, int bombs, int countNodes) {
        final ArrayList<Cell> usefulCells = getUsefulCells(map);
        final LinkedList<String> answers = new LinkedList<>();
        final HashSet<Integer> uselessCells = new HashSet<>();
        for (Cell cell : usefulCells) {
            uselessCells.clear();
            if (findSolution(map, rounds, bombs, cell, uselessCells, countNodes, answers)) {
                return answers;
            }
        }

        throw new IllegalStateException();
    }

    private static boolean findSolution(
            Cell[][] map, int rounds, int bombs,
            Cell cellForBomb,
            HashSet<Integer> uselessCells,
            int leftNodes,
            LinkedList<String> answers) {

        if (rounds < 0) {
            return false;
        }

        final int destroyedNodes = doAction(map);
        leftNodes -= destroyedNodes;

        if (leftNodes == 0) {
            answers.push(ACTION_WAIT);
            answers.push(ACTION_WAIT);
            log("leftNodes == 0 WAIT");
            return true;
        }

        if (cellForBomb != null) {
            final Obj[][] copy = createCopy(map);

            cellForBomb.obj = new Bomb();
            final boolean isFound = findSolution(map, rounds - 1, bombs - 1, null, uselessCells, leftNodes, answers);
            if (isFound) {
                final String answer = cellForBomb.x + " " + cellForBomb.y;
                answers.push(answer);
                log("BOMB " + answer);
                return true;
            }

            restoreFromCopy(map, copy);

            uselessCells.add(getUselessCellId(cellForBomb));
            return false;
        }

        if (bombs > 0) {
            final ArrayList<Cell> usefulCells = getUsefulCells(map);
            for (Cell cell : usefulCells) {
                if (uselessCells.contains(getUselessCellId(cell))) {
                    continue;
                }
                final boolean isFound = findSolution(map, rounds - 1, bombs, cell, uselessCells, leftNodes, answers);
                if (isFound) {
                    return true;
                }
            }
        }

        final boolean isFound = findSolution(map, rounds - 1, bombs, null, uselessCells, leftNodes, answers);
        if (isFound) {
            log("end WAIT");
            answers.push(ACTION_WAIT);
        }
        return isFound;
    }

    private static Obj[][] createCopy(Cell[][] map) {
        final Obj[][] copy = new Obj[sWidth][sHeight];
        for (int y = 0; y < sHeight; y++) {
            for (int x = 0; x < sWidth; x++) {
                copy[x][y] = map[x][y].obj;
            }
        }

        return copy;
    }

    private static void restoreFromCopy(Cell[][] map, Obj[][] copy) {
        for (int y = 0; y < sHeight; y++) {
            for (int x = 0; x < sWidth; x++) {
                map[x][y].obj = copy[x][y];
            }
        }
    }

    private static int getUselessCellId(Cell cell) {
        return cell.x * 1000 + cell.y;
    }

    private static int doAction(Cell[][] map) {
        int countDestroyedNodes = 0;
        for (int y = 0; y < sHeight; y++) {
            for (int x = 0; x < sWidth; x++) {
                countDestroyedNodes += map[x][y].doAction(map);
            }
        }

        return countDestroyedNodes;
    }

    private static ArrayList<Cell> getUsefulCells(Cell[][] map) {
        final HashSet<Cell> empties = new HashSet<>();

        for (int y = 0; y < sHeight; y++) {
            for (int x = 0; x < sWidth; x++) {
                final Cell cell = map[x][y];
                if (!(cell.obj instanceof Node)) {
                    continue;
                }

                for (Cell n : cell.reachableCells) {
                    if (n.obj instanceof Empty) {
                        final Empty emptyCell = (Empty) n.obj;
                        empties.add(n);
                        emptyCell.countNodes++;
                    }
                }
            }
        }

        final ArrayList<Cell> result = new ArrayList<>();
        result.addAll(empties);
        result.sort((v1, v2) -> ((Empty) v2.obj).countNodes - ((Empty) v1.obj).countNodes);

        return result;
    }

    private static void log(String message) {
        if (SHOW_LOGS) {
            System.err.println(message);
        }
    }

    private static class Cell {
        public final int x;
        public final int y;
        public final ArrayList<Cell> reachableCells = new ArrayList<>();
        public Obj obj;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int doAction(Cell[][] map) {
            return obj.doAction(map, x, y);
        }

        public int destroy(Cell[][] map) {
            if (obj instanceof Node) {
                obj = new Empty();
                return 1;
            } else if (obj instanceof Bomb) {
                final int countDestroyed = ((Bomb) obj).destroy(map, x, y);
                obj = new Empty();
                return countDestroyed;
            }

            return 0;
        }

        @Override
        public int hashCode() {
            return x * 1000 + y;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Cell) {
                final Cell c = (Cell) obj;
                return c.x == x && c.y == y;
            }
            return false;
        }

        public void initReachableCells(Cell[][] map) {
            if (obj instanceof Wall) {
                return;
            }

            for (int i = 1; i <= 3; i++) {
                if (y - i >= 0) {
                    final Cell cell = map[x][y - i];
                    if (cell.obj instanceof Wall) {
                        break;
                    }
                    reachableCells.add(cell);
                }
            }
            for (int i = 1; i <= 3; i++) {
                if (y + i < sHeight) {
                    final Cell cell = map[x][y + i];
                    if (cell.obj instanceof Wall) {
                        break;
                    }
                    reachableCells.add(cell);
                }
            }
            for (int i = 1; i <= 3; i++) {
                if (x - i >= 0) {
                    final Cell cell = map[x - i][y];
                    if (cell.obj instanceof Wall) {
                        break;
                    }
                    reachableCells.add(cell);
                }
            }
            for (int i = 1; i <= 3; i++) {
                if (x + i < sWidth) {
                    final Cell cell = map[x + i][y];
                    if (cell.obj instanceof Wall) {
                        break;
                    }
                    reachableCells.add(cell);
                }
            }
        }
    }

    private static abstract class Obj {
        public abstract int doAction(Cell[][] map, int x, int y);
    }

    private static class Empty extends Obj {

        public int countNodes;

        @Override
        public int doAction(Cell[][] map, int x, int y) {
            countNodes = 0;
            return 0;
        }
    }

    private static class Node extends Obj {

        @Override
        public int doAction(Cell[][] map, int x, int y) {
            return 0;
        }
    }

    private static class Wall extends Obj {

        @Override
        public int doAction(Cell[][] map, int x, int y) {
            return 0;
        }
    }

    private static class Bomb extends Obj {

        private int mLeftTurns = 3;

        private boolean mCanExplode = true;

        @Override
        public int doAction(Cell[][] map, int x, int y) {
            mLeftTurns--;
            if (mLeftTurns == 0) {
                return destroy(map, x, y);
            }
            return 0;
        }

        public int destroy(Cell[][] map, int x, int y) {
            if (!mCanExplode) {
                return 0;
            }
            mCanExplode = false;
            final Cell currentCell = map[x][y];
            int countDestroyed = 0;
            for (Cell cell : currentCell.reachableCells) {
                countDestroyed += cell.destroy(map);
            }

            return countDestroyed;
        }
    }
}