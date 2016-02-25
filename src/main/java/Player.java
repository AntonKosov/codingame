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
            if (cell.obj instanceof Empty) {
                uselessCells.clear();
                log("------ " + cell.canDestroy);
                if (findSolution(map, rounds, bombs, cell, uselessCells, countNodes, 0, answers)) {
                    answers.push(cell.x + " " + cell.y);
                    return answers;
                }
            }
        }

        throw new IllegalStateException();
    }

    private static boolean findSolution(
            Cell[][] map, int rounds, int bombs,
            Cell cellForBomb,
            HashSet<Integer> uselessCells,
            int goodNodes,
            int destroyingNodes,
            LinkedList<String> answers) {
        if (rounds < 0 || (bombs == 0 && goodNodes > 0)) {
            log("no solution");
            return false;
        }

        if (cellForBomb != null) {
            log("Try next " + cellForBomb + ", rounds=" + rounds);
        } else {
            log("wait rounds=" + rounds + ", b=" + bombs);
        }

        ArrayList<Cell> usefulCells = null;
        if ((bombs > 1 && cellForBomb != null) || (bombs > 0 && cellForBomb == null)) {
            usefulCells = getUsefulCells(map);
        }

        destroyingNodes -= doAction(map);

        if (destroyingNodes == 0 && goodNodes == 0) {
            answers.push(ACTION_WAIT);
            answers.push(ACTION_WAIT);
            return true;
        }

        if (usefulCells != null) {
            int countNodes = 0;
            int iB = 0;
            for (Cell cell : usefulCells) {
                countNodes += cell.canDestroy;
                iB++;
                if (iB > bombs) {
                    break;
                }
            }
            if (goodNodes - countNodes > 0) {
                log("no solution " + cellForBomb + ", gn=" + goodNodes + ", cn=" + countNodes /*+ ", block" + cellForBomb*/);
                //uselessCells.add(getUselessCellId(cellForBomb));
                return false;
            }
        }

        Obj[][] copy = null;
        try {
            if (cellForBomb != null) {
                copy = createCopy(map);
                cellForBomb.obj = new Bomb();
                for (Cell n : cellForBomb.reachableCells) {
                    if (n.obj instanceof Node) {
                        n.obj = new DestroyingNode();
                    }
                }
                bombs--;
                goodNodes -= cellForBomb.canDestroy;
                destroyingNodes += cellForBomb.canDestroy;
                log("dn=" + destroyingNodes + ", gn=" + goodNodes);
            }

            if (bombs > 0) {
                usefulCells = getUsefulCells(map);
                for (Cell cell : usefulCells) {
                    if (!(cell.obj instanceof Empty) || uselessCells.contains(getUselessCellId(cell))) {
                        continue;
                    }
                    final boolean isFound = findSolution(map, rounds - 1, bombs, cell, uselessCells, goodNodes, destroyingNodes, answers);
                    if (isFound) {
                        final String answer = cell.x + " " + cell.y;
                        answers.push(answer);
                        log("BOMB " + answer);
                        return true;
                    }
                }
            }

            final boolean isFound = findSolution(map, rounds - 1, bombs, null, uselessCells, goodNodes, destroyingNodes, answers);
            if (isFound) {
//                log("end WAIT");
                answers.push(ACTION_WAIT);
            } else if (cellForBomb != null) {
//                uselessCells.add(getUselessCellId(cellForBomb));
//                log("block " + cellForBomb);
            }
            return isFound;
        } finally {
            if (copy != null) {
                restoreFromCopy(map, copy);
            }
        }
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
                map[x][y].canDestroy = 0;
            }
        }
        for (int y = 0; y < sHeight; y++) {
            for (int x = 0; x < sWidth; x++) {
                final Cell cell = map[x][y];
                if (!(cell.obj instanceof Node)) {
                    continue;
                }

                for (Cell n : cell.reachableCells) {
                    empties.add(n);
                    n.canDestroy++;
                }
            }
        }

        final ArrayList<Cell> result = new ArrayList<>();
        result.addAll(empties);
        result.sort((v1, v2) -> v2.canDestroy - v1.canDestroy);

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
        public int canDestroy;

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int doAction(Cell[][] map) {
            if (obj instanceof BusyNode) {
                obj = new Empty();
            }
            return obj.doAction(map, x, y);
        }

        public int destroy(Cell[][] map) {
            if (obj instanceof DestroyingNode) {
                obj = new BusyNode();
                return 1;
            } else if (obj instanceof Bomb) {
                final int countDestroyed = ((Bomb) obj).destroy(map, x, y);
                obj = new BusyNode();
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

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
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

        @Override
        public int doAction(Cell[][] map, int x, int y) {
            return 0;
        }
    }

    private static class Node extends Obj {

        @Override
        public int doAction(Cell[][] map, int x, int y) {
            return 0;
        }
    }

    private static class DestroyingNode extends Obj {

        @Override
        public int doAction(Cell[][] map, int x, int y) {
            return 0;
        }
    }

    private static class BusyNode extends Obj {

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