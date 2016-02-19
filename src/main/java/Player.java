import java.util.*;

class Player {

    private static final boolean SHOW_LOGS = true;

    private static final String ACTION_WAIT = "WAIT";

    private static final String TOP = "TOP";
    private static final String LEFT = "LEFT";
    private static final String RIGHT = "RIGHT";
    private static final String BOTTOM = "BOTTOM";

    private static final HashMap<String, Direction> sRooms = new HashMap<>();
    private static final HashMap<String, Integer> sRotations = new HashMap<>();

    private static int sWidth;
    private static int sHeight;

    static {
        sRooms.put("1LEFT", Direction.bottom);
        sRooms.put("1TOP", Direction.bottom);
        sRooms.put("1RIGHT", Direction.bottom);
        sRooms.put("2LEFT", Direction.right);
        sRooms.put("2RIGHT", Direction.left);
        sRooms.put("3TOP", Direction.bottom);
        sRooms.put("4TOP", Direction.left);
        sRooms.put("4RIGHT", Direction.bottom);
        sRooms.put("5LEFT", Direction.bottom);
        sRooms.put("5TOP", Direction.right);
        sRooms.put("6LEFT", Direction.right);
        sRooms.put("6RIGHT", Direction.left);
        sRooms.put("7TOP", Direction.bottom);
        sRooms.put("7RIGHT", Direction.bottom);
        sRooms.put("8LEFT", Direction.bottom);
        sRooms.put("8RIGHT", Direction.bottom);
        sRooms.put("9LEFT", Direction.bottom);
        sRooms.put("9TOP", Direction.bottom);
        sRooms.put("10TOP", Direction.left);
        sRooms.put("11TOP", Direction.right);
        sRooms.put("12RIGHT", Direction.bottom);
        sRooms.put("13LEFT", Direction.bottom);

        sRotations.put("2L", 3);
        sRotations.put("3L", 2);
        sRotations.put("4L", 5);
        sRotations.put("5L", 4);
        sRotations.put("6L", 9);
        sRotations.put("6R", 7);
        sRotations.put("7L", 6);
        sRotations.put("7R", 8);
        sRotations.put("8L", 7);
        sRotations.put("8R", 9);
        sRotations.put("9L", 8);
        sRotations.put("9R", 6);
        sRotations.put("10L", 13);
        sRotations.put("10R", 11);
        sRotations.put("11L", 10);
        sRotations.put("11R", 12);
        sRotations.put("12L", 11);
        sRotations.put("12R", 13);
        sRotations.put("13L", 12);
        sRotations.put("13R", 10);
    }

    public static void main(String args[]) {
        final Scanner in = new Scanner(System.in);
        sWidth = in.nextInt(); // number of columns.
        sHeight = in.nextInt(); // number of rows.
        in.nextLine();
        final int[][] map = new int[sWidth][sHeight];
        for (int i = 0; i < sHeight; i++) {
            final String line = in.nextLine(); // each line represents a line in the grid and contains W integers T. The absolute value of T specifies the type of the room. If T is negative, the room cannot be rotated.
            log(line);
            final String[] split = line.split(" ");
            for (int c = 0; c < sWidth; c++) {
                map[c][i] = Integer.parseInt(split[c]);
            }
        }
        final int exit = in.nextInt(); // the coordinate along the X axis of the exit.
        LinkedList<Node> path = null;
        final ArrayList<Rock> rocks = new ArrayList<>();

        // game loop
        while (true) {
            final int iX = in.nextInt();
            final int iY = in.nextInt();
            final String iPos = in.next();
            rocks.clear();
            final int countOfRocks = in.nextInt(); // the number of rocks currently in the grid.
            for (int i = 0; i < countOfRocks; i++) {
                final int rX = in.nextInt();
                final int rY = in.nextInt();
                final Direction dir = getDirection(in.next());
                rocks.add(new Rock(rX, rY, dir));
            }
            if (path == null) {
                final Direction direction = getDirection(iPos);
                path = findPath(map, exit, new Node(iX, iY, map[iX][iY], direction, 0, Rotation.none, null));
            }

            String action = ACTION_WAIT;

            if (rocks.size() > 0 && isCanStopStone(iX, iY, path)) {
                action = tryFindRotationForStopStone(map, rocks);
            }

            if (action.equals(ACTION_WAIT)) {
                action = tryFindRotationOnPath(map, path);
            }

            System.out.println(action); // One line containing on of three commands: 'X Y LEFT', 'X Y RIGHT' or 'WAIT'
        }
    }

    private static String tryFindRotationForStopStone(int[][] map, ArrayList<Rock> rocks) {
        final Cell cell = new Cell();
        for (Rock rock : rocks) {
            cell.set(rock.cell);
            Direction dirIn = rock.direction;
            log("Check " + rock);
            while (true) {
                final Direction out = getOut(map[cell.x][cell.y], dirIn);
                out.getNextCell(cell); // get the next cell
                log("Next cell " + cell + ", current out " + out);
                if (isNotInsideOfMaze(cell)) {
                    break;
                }
                final int cellType = map[cell.x][cell.y];
                if (!isExistEntrance(cellType, out.inverse())) {
                    break;
                }
                if (cellType > 0) {
                    map[cell.x][cell.y] = rotateToLeft(cellType);
                    return cell.x + " " + cell.y + " LEFT";
                }
                dirIn = out.inverse();
            }
        }

        return ACTION_WAIT;
    }

    private static boolean isCanStopStone(int iX, int iY, LinkedList<Node> path) {
        for (int i = 0; i < path.size(); i++) {
            final Node node = path.get(i);
            if (node.cell.x == iX && node.cell.y == iY) {
                if (i < path.size() - 3) {
                    if (path.get(i + 1).rotation != Rotation.none) {
                        return false;
                    }
                }
                if (i < path.size() - 4) {
                    if (path.get(i + 2).rotation == Rotation.doubleLeft) {
                        return false;
                    }
                }
                return true;
            }
        }

        throw new IllegalStateException();
    }

    private static String tryFindRotationOnPath(int[][] map, LinkedList<Node> path) {
        for (int i = 0; i < path.size(); i++) {
            final Node node = path.get(i);
            if (node.rotation != Rotation.none) {
                String action = node.cell.x + " " + node.cell.y + " ";
                final int oldCellType = map[node.cell.x][node.cell.y];
                final Node newNode;
                final int newCellType;
                if (node.rotation == Rotation.left) {
                    action += "LEFT";
                    newCellType = rotateToLeft(oldCellType);
                    newNode = new Node(node.cell, newCellType, node.in, 0, Rotation.none, node.parent);
                } else if (node.rotation == Rotation.right) {
                    action += "RIGHT";
                    newCellType = rotateToRight(oldCellType);
                    newNode = new Node(node.cell, newCellType, node.in, 0, Rotation.none, node.parent);
                } else {
                    action += "LEFT";
                    newCellType = rotateToLeft(oldCellType);
                    newNode = new Node(node.cell, newCellType, node.in, 0, Rotation.left, node.parent);
                }
                map[node.cell.x][node.cell.y] = newCellType;
                path.remove(i);
                path.add(i, newNode);
                return action;
            }
        }
        return ACTION_WAIT;
    }

    private static Direction getDirection(String position) {
        return position.equals("TOP") ? Direction.top : position.equals("LEFT") ? Direction.left : Direction.right;
    }

    private static LinkedList<Node> findPath(int[][] map, int exit, Node currentNode) {
        final PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(n -> -n.cell.y));
        queue.add(currentNode);
        final Cell nextCell = new Cell();
        Node pathNode;
        while (true) {
            final Node node = queue.poll();
//            log("Try " + node);
            if (node.cell.y == sHeight - 1 && node.cell.x == exit) {
//                log("It is the exit");
                pathNode = node;
                break;
            }
            nextCell.set(node.cell);
            final int currentPositiveCellType = Math.abs(node.cellType);
            getNextCell(currentPositiveCellType, node.in, nextCell);
            if (isNotInsideOfMaze(nextCell)) {
//                log("impossible: out of range");
                continue;
            }
            final Direction nextDirectionIn = getOut(currentPositiveCellType, node.in).inverse();
            final int nextCellType = map[nextCell.x][nextCell.y];
//            log("Think about: cell " + nextCell + ", in " + nextDirectionIn + ", type " + nextCellType);
            if (nextCellType == 0 ||
                    (nextCellType < 0 && !isExistEntrance(nextCellType, nextDirectionIn))) {
//                log("impossible");
                continue;
            }
            if (isExistEntrance(nextCellType, nextDirectionIn)) {
                final Node nextNode = new Node(nextCell, nextCellType, nextDirectionIn, node.countTurns + 1, Rotation.none, node);
                queue.add(nextNode);
//                log("Add to queue: nothing " + nextNode);
            }
            final Integer leftCellType = rotateToLeft(nextCellType);
            if (leftCellType != null && isExistEntrance(leftCellType, nextDirectionIn)) {
                final Node leftRotationNode = new Node(nextCell, leftCellType, nextDirectionIn, node.countTurns, Rotation.left, node);
                queue.add(leftRotationNode);
//                log("Add to queue: left rotation " + leftRotationNode);
            }
            final Integer rightCellType = rotateToRight(nextCellType);
            if (rightCellType != null && isExistEntrance(rightCellType, nextDirectionIn)) {
                final Node rightRotationNode = new Node(nextCell, rightCellType, nextDirectionIn, node.countTurns, Rotation.right, node);
                queue.add(rightRotationNode);
//                log("Add to queue: right rotation " + rightRotationNode);
            }
            if (node.countTurns > 0 && leftCellType != null) {
                final Integer dLeftCellType = rotateToLeft(leftCellType);
                if (dLeftCellType != null && dLeftCellType != node.cellType && isExistEntrance(dLeftCellType, nextDirectionIn)) {
                    final Node doubleLeftRotationNode = new Node(nextCell, dLeftCellType, nextDirectionIn, node.countTurns - 1, Rotation.doubleLeft, node);
                    queue.add(doubleLeftRotationNode);
//                    log("Add to queue: double left rotation " + doubleLeftRotationNode);
                }
            }
        }

        final LinkedList<Node> path = new LinkedList<>();
        while (pathNode != null) {
            path.add(0, pathNode);
            pathNode = pathNode.parent;
        }
        return path;
    }

    private static boolean isNotInsideOfMaze(Cell nextCell) {
        return nextCell.y > sHeight - 1 || nextCell.x < 0 || nextCell.x > sWidth - 1;
    }

    private static Integer rotateToLeft(int cellType) {
        return sRotations.getOrDefault(cellType + "L", null);
    }

    private static Integer rotateToRight(int cellType) {
        return sRotations.getOrDefault(cellType + "R", null);
    }

    private static boolean isExistEntrance(int cellType, Direction direction) {
        return sRooms.containsKey(Math.abs(cellType) + direction.getText());
    }

    private static void getNextCell(int cellType, Direction in, Cell out) {
        final Direction direction = sRooms.get(cellType + in.getText());
        direction.getNextCell(out);
    }

    private static Direction getOut(int cellType, Direction in) {
        return sRooms.get(Math.abs(cellType) + in.getText());
    }

    private static void log(String message) {
        if (SHOW_LOGS) {
            System.err.println(message);
        }
    }

    private enum Rotation {
        none,
        left,
        right,
        doubleLeft
    }

    private enum Direction {
        top {
            @Override
            void getNextCell(Cell cell) {
                cell.y++;
            }

            @Override
            String getText() {
                return TOP;
            }

            @Override
            Direction inverse() {
                return bottom;
            }
        },

        left {
            @Override
            void getNextCell(Cell cell) {
                cell.x--;
            }

            @Override
            String getText() {
                return LEFT;
            }

            @Override
            Direction inverse() {
                return right;
            }
        },

        bottom {
            @Override
            void getNextCell(Cell cell) {
                cell.y++;
            }

            @Override
            String getText() {
                return BOTTOM;
            }

            @Override
            Direction inverse() {
                return top;
            }
        },

        right {
            @Override
            void getNextCell(Cell cell) {
                cell.x++;
            }

            @Override
            String getText() {
                return RIGHT;
            }

            @Override
            Direction inverse() {
                return left;
            }
        };

        abstract void getNextCell(Cell cell);
        abstract String getText();
        abstract Direction inverse();
    }

    private static class Cell {
        public int x;
        public int y;

        public Cell() {
            this(0, 0);
        }

        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void set(Cell cell) {
            x = cell.x;
            y = cell.y;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    private static class Node {

        public final Cell cell = new Cell();
        public final Direction in;
        public final Node parent;
        public final int cellType;
        public final Rotation rotation;
        public int countTurns;

        public Node(Cell cell, int cellType, Direction in, int countTurns, Rotation rotation, Node parent) {
            this(cell.x, cell.y, cellType, in, countTurns, rotation, parent);
        }

        public Node(int x, int y, int cellType, Direction in, int countTurns, Rotation rotation, Node parent) {
            cell.x = x;
            cell.y = y;
            this.cellType = cellType;
            this.in = in;
            this.countTurns = countTurns;
            this.rotation = rotation;
            this.parent = parent;
        }

        @Override
        public String toString() {
            return cell + ", type=" + cellType;
        }
    }

    private static class Rock {

        public final Cell cell;
        public final Direction direction;

        public Rock(int x, int y, Direction direction) {
            cell = new Cell(x, y);
            this.direction = direction;
        }

        @Override
        public String toString() {
            return cell + ", " + direction;
        }
    }
}