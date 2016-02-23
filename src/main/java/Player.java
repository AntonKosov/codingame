import java.util.*;

class Player {

    private static final boolean SHOW_LOGS = true;
    
    private static int sWidth;
    private static int sHeight;

    public static void main(String args[]) {
        final Scanner in = new Scanner(System.in);
        sWidth = in.nextInt(); // width of the firewall grid
        sHeight = in.nextInt(); // height of the firewall grid
        in.nextLine();
        final Cell[][] map = new Cell[sWidth][sHeight];
        for (int i = 0; i < sHeight; i++) {
            final String mapRow = in.nextLine(); // one line of the firewall grid
            for (int j = 0; j < sWidth; j++) {
                final String s = mapRow.substring(j, j + 1);
                final Cell cell = s.equals(".") ? new Empty(j, i) : s.equals("@") ? new Node(j, i) : new Wall(j, i);
                map[j][i] = cell;
            }
        }
        initReachableCells(map);

        // game loop
        while (true) {
            final int rounds = in.nextInt(); // number of rounds left before the end of the game
            final int bombs = in.nextInt(); // number of bombs left

            final String answer = getAnswer(map);

            System.out.println(answer);
        }
    }

    private static void initReachableCells(Cell[][] map) {
        for (int y = 0; y < sHeight; y++) {
            for (int x = 0; x < sWidth; x++) {
                map[x][y].initReachableCells(map);
            }
        }
    }

    private static String getAnswer(Cell[][] map) {
        for (int y = 0; y < sHeight; y++) {
            for (int x = 0; x < sWidth; x++) {
                map[x][y].doAction(map);
            }
        }

        Empty optimalCell = null;

        for (int y = 0; y < sHeight; y++) {
            for (int x = 0; x < sWidth; x++) {
                final Cell cell = map[x][y];
                if (!(cell instanceof Node)) {
                    continue;
                }
                final Node node = (Node) cell;
                if (node.willDestroy) {
                    continue;
                }

                for (Cell n : cell.reachableCells) {
                    if (n instanceof Empty) {
                        final Empty emptyCell = (Empty) n;
                        emptyCell.countNodes++;
                        if (optimalCell == null || optimalCell.countNodes < emptyCell.countNodes) {
                            optimalCell = emptyCell;
                        }
                    }
                }
            }
        }

        if (optimalCell == null) {
            return "WAIT";
        }

        final Bomb bomb = new Bomb(optimalCell.x, optimalCell.y);
        bomb.initReachableCells(map);
        map[optimalCell.x][optimalCell.y] = bomb;
        for (Cell cell : bomb.reachableCells) {
            if (cell instanceof Node) {
                ((Node) cell).willDestroy = true;
            }
        }

        return bomb.x + " " + bomb.y;
    }

    private static void log(String message) {
        if (SHOW_LOGS) {
            System.err.println(message);
        }
    }

    private abstract static class Cell {
        public final int x;
        public final int y;
        public final ArrayList<Cell> reachableCells = new ArrayList<>();

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void doAction(Cell[][] map) {
        }

        public void destroy(Cell[][] map) {
        }

        public void initReachableCells(Cell[][] map) {
            for (int i = 1; i <= 3; i++) {
                if (y - i >= 0) {
                    final Cell cell = map[x][y - i];
                    if (cell instanceof Wall) {
                        break;
                    }
                    reachableCells.add(cell);
                }
            }
            for (int i = 1; i <= 3; i++) {
                if (y + i < sHeight) {
                    final Cell cell = map[x][y + i];
                    if (cell instanceof Wall) {
                        break;
                    }
                    reachableCells.add(cell);
                }
            }
            for (int i = 1; i <= 3; i++) {
                if (x - i >= 0) {
                    final Cell cell = map[x - i][y];
                    if (cell instanceof Wall) {
                        break;
                    }
                    reachableCells.add(cell);
                }
            }
            for (int i = 1; i <= 3; i++) {
                if (x + i < sWidth) {
                    final Cell cell = map[x + i][y];
                    if (cell instanceof Wall) {
                        break;
                    }
                    reachableCells.add(cell);
                }
            }
        }
    }

    private static class Empty extends Cell {
        public int countNodes;

        public Empty(int x, int y) {
            super(x, y);
        }

        @Override
        public void doAction(Cell[][] map) {
            super.doAction(map);
            countNodes = 0;
        }
    }

    private static class Node extends Cell {

        public boolean willDestroy = false;

        public Node(int x, int y) {
            super(x, y);
        }

        @Override
        public void destroy(Cell[][] map) {
            super.destroy(map);
            map[x][y] = new Empty(x, y);
        }
    }

    private static class Wall extends Cell {
        public Wall(int x, int y) {
            super(x, y);
        }

        @Override
        public void initReachableCells(Cell[][] map) {
            //Nothing
        }
    }

    private static class Bomb extends Cell {

        private int mLeftTurns = 2;

        private boolean mCanExplode = true;

        public Bomb(int x, int y) {
            super(x, y);
        }

        @Override
        public void doAction(Cell[][] map) {
            super.doAction(map);
            mLeftTurns--;
            if (mLeftTurns == 0) {
                destroy(map);
            }
        }

        @Override
        public void destroy(Cell[][] map) {
            super.destroy(map);
            if (!mCanExplode) {
                return;
            }
            mCanExplode = false;
            for (Cell cell : reachableCells) {
                cell.destroy(map);
            }

            map[x][y] = new Empty(x, y);
        }
    }
}