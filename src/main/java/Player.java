import java.util.*;

class Player {

    private static final boolean SHOW_LOGS = true;

    private static final String ACTION_WAIT = "WAIT";
    private static final String ACTION_NEED_NEW_PATH = "NEW_PATH";

    private static final String TOP = "TOP";
    private static final String LEFT = "LEFT";
    private static final String RIGHT = "RIGHT";
    private static final String BOTTOM = "BOTTOM";

    private static final HashMap<String, Direction> sRooms = new HashMap<>();
    private static final HashMap<String, Integer> sRotations = new HashMap<>();

    private static int sWidth;
    private static int sHeight;

    private static final HashSet<Cell> sBlackListOfCells = new HashSet<>();

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
        final Node[][] map = new Node[sWidth][sHeight];
        for (int i = 0; i < sHeight; i++) {
            final String line = in.nextLine(); // each line represents a line in the grid and contains W integers T. The absolute value of T specifies the type of the room. If T is negative, the room cannot be rotated.
            log(line);
            final String[] split = line.split(" ");
            for (int c = 0; c < sWidth; c++) {
                int cellType = Integer.parseInt(split[c]);
                if (cellType == 1) {
                    cellType = -1;
                }
                map[c][i] = new Node(c, i, -1, cellType, Direction.unknown, 0);
            }
        }
        final int exit = in.nextInt(); // the coordinate along the X axis of the exit.
        final ArrayList<Rock> rocks = new ArrayList<>();
        boolean isFirstStep = true;

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

            final Node currentNode = map[iX][iY];

            String action = ACTION_WAIT;
            do {
                if (action.equals(ACTION_NEED_NEW_PATH)) {
                    action = ACTION_WAIT;
                    isFirstStep = true;
                    for (int x = 0; x < sWidth; x++) {
                        for (int y = 0; y < sHeight; y++) {
                            final Node node = map[x][y];
                            node.targetCellType = node.cellType;
                            node.step = -1;
                            node.in = Direction.unknown;
                        }
                    }
                }
                if (isFirstStep) {
                    isFirstStep = false;
                    currentNode.in = getDirection(iPos);
                    currentNode.step = 0;
                    findPath(map, exit, currentNode);
                }

                if (rocks.size() > 0 && isCanStopStone(map, currentNode)) {
                    action = tryFindRotationForStopStone(map, currentNode, rocks, currentNode.step);
                    log("try stop stones, result=" + action);
                }
            } while (action.equals(ACTION_NEED_NEW_PATH));

            if (action.equals(ACTION_WAIT)) {
                action = tryFindRotationOnPath(map, currentNode);
            }

            System.out.println(action); // One line containing on of three commands: 'X Y LEFT', 'X Y RIGHT' or 'WAIT'
        }
    }

    private static String tryFindRotationForStopStone(Node[][] map, Node currentNode, ArrayList<Rock> rocks, int startStep) {
        final ArrayList<ArrayList<StonePath>> paths = new ArrayList<>();
        for (Rock rock : rocks) {
            final ArrayList<StonePath> stonePath = getStonePath(map, rock, startStep);
            log("path=" + stonePath.size());

            final StonePath last = stonePath.get(stonePath.size() - 1);
            if (last.step != last.node.step || stonePath.size() == 1) {
                continue;
            }

            paths.add(stonePath);
        }

        log("Stone paths: " + paths.size());

        final HashSet<Integer> safeStones = new HashSet<>();
        for (int i = 0; i < paths.size() - 1; i++) {
            if (safeStones.contains(i)) {
                continue;
            }
            final ArrayList<StonePath> firstPath = paths.get(i);
            for (int j = i + 1; j < paths.size(); j++) {
                final ArrayList<StonePath> secondPath = paths.get(j);
                if (safeStones.contains(j) || firstPath.size() != secondPath.size()) {
                    continue;
                }
                if (firstPath.get(firstPath.size() - 2).node == secondPath.get(secondPath.size() - 2).node) {
                    safeStones.add(i);
                    safeStones.add(j);
                }
            }
        }

        for (int i = 0; i < paths.size(); i++) {
            final ArrayList<StonePath> stonePath = paths.get(i);
            final String stoneId = stonePath.get(0).node.cell.toString();
            if (safeStones.contains(i)) {
                log("Safe: " + stoneId);
                continue;
            }

            int countRotatable = 0;
            for (int j = 1; j < stonePath.size(); j++) {
                final StonePath sp = stonePath.get(j);
                if (sp.node.cellType > 0) {
                    countRotatable++;
                }
            }
            log("Stone " + stoneId + ", cr=" + countRotatable);

            final StonePath last = stonePath.get(stonePath.size() - 1);
            final StonePath second = stonePath.get(1);
            if (countRotatable == 0) {
                for (StonePath sp : stonePath) {
                    if (sp.node.step > 0) {
                        log("Need new path: " + sp.node.cell);
                        sBlackListOfCells.add(sp.node.cell);
                        return ACTION_NEED_NEW_PATH;
                    }
                }
            } else if (countRotatable == 1 && last.node.cellType > 0) {
                final Direction in = last.node.in;
                final Direction out = getOut(last.node.cellType, in);
                final Direction blockIn = last.in;
                if (!isNeedCellType(last.node.targetCellType, in, out, blockIn)) {
                    final Integer left = rotateToLeft(last.node.targetCellType);
                    int newTargetCellType = last.node.targetCellType;
                    if (left != null) {
                        if (isNeedCellType(left, in, out, blockIn)) {
                            newTargetCellType = left;
                        } else {
                            final Integer dLeft = rotateToLeft(left);
                            if (dLeft != null && isNeedCellType(dLeft, in, out, blockIn)) {
                                newTargetCellType = dLeft;
                            } else {
                                final Integer right = rotateToRight(last.node.cellType);
                                if (right != null && isNeedCellType(right, in, out, blockIn)) {
                                    newTargetCellType = right;
                                }
                            }
                        }
                    }

                    if (last.node.targetCellType != newTargetCellType) {
                        last.node.targetCellType = newTargetCellType;
                        log("Target is changed " + last.node.cell + ", " + last.node.targetCellType);
                    } else {
                        //todo new path?
                    }
                }
            } else if (countRotatable == 1 && second.node.cellType > 0) {
                second.node.cellType = rotateToLeft(second.node.cellType);
                second.node.targetCellType = second.node.cellType;
                return second.node.cell.x + " " + second.node.cell.y + " " + LEFT;
            }
        }

        return ACTION_WAIT;
    }

    private static ArrayList<StonePath> getStonePath(Node[][] map, Rock rock, int step) {
        final Cell cell = new Cell(rock.cell);
        Direction in = rock.in;
        ArrayList<StonePath> stonePath = new ArrayList<>();
        while (isInsideOfMaze(cell)) {
            final Node node = map[cell.x][cell.y];
            log("Stone path: " + node.cell + ", in=" + in + ", ct=" + node.targetCellType);
            if (!isExistEntrance(node.targetCellType, in)) {
                log("Stone path: no entrance, c=" + node.cell + ", in=" + in);
                break;
            }
            stonePath.add(new StonePath(node, in, step));

            if (node.step >= 0) {
                log("Stone path: node.step >= 0, c=" + node.cell);
                break;
            }

            Direction out = getOut(node.targetCellType, in);
            out.getNextCell(cell);
            in = out.inverse();
            step++;
        }

        return stonePath;
    }

    private static boolean isNeedCellType(int cellType, Direction in, Direction out, Direction block) {
        return isExistEntrance(cellType, in) && !isExistEntrance(cellType, block) && out == getOut(cellType, in);
    }

    private static boolean isCanStopStone(Node[][] map, Node currentNode) {
        final Node nextNode = getNextNode(map, currentNode);
        if (nextNode == null) {
            return true;
        }
        if (nextNode.cellType != nextNode.targetCellType) {
            log("isCanStopStone nextNode");
            return false;
        }

        final Node afterNextNode = getNextNode(map, nextNode);
        if (afterNextNode == null) {
            return true;
        }

        if (afterNextNode.cellType != afterNextNode.targetCellType) {
            final Integer left = rotateToLeft(afterNextNode.cellType);
            if (left != null && left == afterNextNode.targetCellType) {
                return true;
            }

            final Integer right = rotateToRight(afterNextNode.cellType);
            if (right != null && right == afterNextNode.targetCellType) {
                return true;
            }

            return false;
        }

        return true;
    }

    private static Node getNextNode(Node[][] map, Node currentNode) {
        final Direction outFromCurrentNode = getOut(currentNode.cellType, currentNode.in);
        final Cell cell = new Cell(currentNode.cell);
        outFromCurrentNode.getNextCell(cell);
        if (!isInsideOfMaze(cell)) {
            return null;
        }
        return map[cell.x][cell.y];
    }

    private static String tryFindRotationOnPath(Node[][] map, Node currentNode) {
        final Cell cell = new Cell(currentNode.cell);
        while (isInsideOfMaze(cell)) {
            final Node node = map[cell.x][cell.y];
            if (node.cellType != node.targetCellType) {
                String action = cell.x + " " + cell.y + " ";
                final int newCellType;
                final Integer left = rotateToLeft(node.cellType);
                if (left != null && left == node.targetCellType) {
                    action += "LEFT";
                    newCellType = left;
                } else {
                    final Integer right = rotateToRight(node.cellType);
                    if (right != null && right == node.targetCellType) {
                        action += "RIGHT";
                        newCellType = right;
                    } else {
                        action += "LEFT";
                        newCellType = left;
                    }
                }
                node.cellType = newCellType;
                return action;
            }

            final Direction out = getOut(node.cellType, node.in);
            out.getNextCell(cell);
        }

        return ACTION_WAIT;
    }

    private static Direction getDirection(String position) {
        return position.equals("TOP") ? Direction.top : position.equals("LEFT") ? Direction.left : Direction.right;
    }

    private static void findPath(Node[][] map, int exit, Node currentNode) {
        final PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(n -> -n.cell.y));
        queue.add(currentNode);
        final Cell nextCell = new Cell();
        Node pathNode;
        while (true) {
            final Node node = queue.poll();
            log("Try " + node);
            if (node.cell.y == sHeight - 1 && node.cell.x == exit) {
                log("It is the exit");
                pathNode = node;
                break;
            }
            nextCell.set(node.cell);
            getNextCell(node.cellType, node.in, nextCell);
            if (!isInsideOfMaze(nextCell) || sBlackListOfCells.contains(nextCell)) {
                continue;
            }
            final Direction nextIn = getOut(node.cellType, node.in).inverse();
            final Node nextNode = map[nextCell.x][nextCell.y];
//            log("Think about: cell " + nextCell + ", in " + nextDirectionIn + ", type " + nextCellType);
            if (nextNode.cellType == 0 ||
                    (nextNode.cellType < 0 && !isExistEntrance(nextNode.cellType, nextIn))) {
//                log("impossible");
                continue;
            }
            Direction waitDirOut = null;
            if (isExistEntrance(nextNode.cellType, nextIn)) {
                final Node waitNode = new Node(nextCell, node.step + 1, nextNode.cellType, nextIn, node.countTurns + 1);
                waitNode.parent = node;
                waitDirOut = getOut(waitNode.cellType, nextIn);
                queue.add(waitNode);
//                log("Add to queue: nothing " + nextNode);
            }
            final Integer leftCellType = rotateToLeft(nextNode.cellType);
            Direction leftDirOut = null;
            if (leftCellType != null && isExistEntrance(leftCellType, nextIn)) {
                leftDirOut = getOut(leftCellType, nextIn);
                if (leftDirOut != waitDirOut) {
                    final Node leftRotationNode = new Node(nextCell, node.step + 1, leftCellType, nextIn, node.countTurns);
                    leftRotationNode.parent = node;
                    queue.add(leftRotationNode);
//                    log("Add to queue: left rotation " + leftRotationNode);
                }
            }
            final Integer rightCellType = rotateToRight(nextNode.cellType);
            Direction rightDirOut = null;
            if (rightCellType != null && isExistEntrance(rightCellType, nextIn) &&
                    getOut(nextNode.cellType, nextIn) != getOut(rightCellType, nextIn)) {
                rightDirOut = getOut(rightCellType, nextIn);
                if (rightDirOut != waitDirOut && rightDirOut != leftDirOut) {
                    final Node rightRotationNode = new Node(nextCell, node.step + 1, rightCellType, nextIn, node.countTurns);
                    rightRotationNode.parent = node;
                    queue.add(rightRotationNode);
//                    log("Add to queue: right rotation " + rightRotationNode);
                }
            }
            if (node.countTurns > 1 && leftCellType != null) {
                final Integer dLeftCellType = rotateToLeft(leftCellType);
                if (dLeftCellType != null && dLeftCellType != node.cellType && isExistEntrance(dLeftCellType, nextIn)) {
                    Direction dLeftDirOut = getOut(dLeftCellType, nextIn);
                    if (dLeftDirOut != waitDirOut && dLeftDirOut != leftDirOut && dLeftDirOut != rightDirOut) {
                        final Node doubleLeftRotationNode = new Node(nextCell, node.step + 1, dLeftCellType, nextIn, node.countTurns - 1);
                        doubleLeftRotationNode.parent = node;
                        queue.add(doubleLeftRotationNode);
//                        log("Add to queue: double left rotation " + doubleLeftRotationNode);
                    }
                }
            }
        }

        while (pathNode != null) {
            final Node nodeOfMap = map[pathNode.cell.x][pathNode.cell.y];
            nodeOfMap.targetCellType = pathNode.cellType;
            nodeOfMap.in = pathNode.in;
            nodeOfMap.step = pathNode.step;
            log("update " + nodeOfMap.cell + ", in=" + nodeOfMap.in + ", cellType=" + nodeOfMap.cellType + ", target ct=" + nodeOfMap.targetCellType);
            pathNode = pathNode.parent;
        }
    }

    private static boolean isInsideOfMaze(Cell cell) {
        return cell.y <= sHeight - 1 && cell.x >= 0 && cell.x <= sWidth - 1;
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
        final Direction direction = sRooms.get(Math.abs(cellType) + in.getText());
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

    private enum Direction {
        unknown {
            @Override
            void getNextCell(Cell cell) {
                throw new IllegalStateException();
            }

            @Override
            String getText() {
                throw new IllegalStateException();
            }

            @Override
            Direction inverse() {
                throw new IllegalStateException();
            }
        },

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

        public Cell(Cell cell) {
            this(cell.x, cell.y);
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
        public int hashCode() {
            return x * 1000 + y;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Cell) {
                final Cell cell = (Cell) obj;
                return x == cell.x && y == cell.y;
            }

            return false;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    private static class StonePath {
        public final Node node;
        public final Direction in;
        public final int step;

        private StonePath(Node node, Direction in, int step) {
            this.node = node;
            this.in = in;
            this.step = step;
        }
    }

    private static class Node {

        public final Cell cell = new Cell();
        public int step;
        public Direction in;
        public int cellType;
        public int targetCellType;
        public int countTurns;
        public Node parent;

        public Node(Cell cell, int step, int cellType, Direction in, int countTurns) {
            this(cell.x, cell.y, step, cellType, in, countTurns);
        }

        public Node(int x, int y, int step, int cellType, Direction in, int countTurns) {
            cell.x = x;
            cell.y = y;
            this.step = step;
            this.cellType = cellType;
            this.in = in;
            this.countTurns = countTurns;
            targetCellType = cellType;
        }

        @Override
        public String toString() {
            return cell + ", type=" + cellType;
        }
    }

    private static class Rock {

        public final Cell cell;
        public final Direction in;

        public Rock(int x, int y, Direction in) {
            cell = new Cell(x, y);
            this.in = in;
        }

        @Override
        public String toString() {
            return cell + ", " + in;
        }
    }
}