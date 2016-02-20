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
        final Node[][] map = new Node[sWidth][sHeight];
        for (int i = 0; i < sHeight; i++) {
            final String line = in.nextLine(); // each line represents a line in the grid and contains W integers T. The absolute value of T specifies the type of the room. If T is negative, the room cannot be rotated.
            log(line);
            final String[] split = line.split(" ");
            for (int c = 0; c < sWidth; c++) {
                final int cellType = Integer.parseInt(split[c]);
                map[c][i] = new Node(c, i, cellType, Direction.unknown, 0);
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
            if (isFirstStep) {
                isFirstStep = false;
                currentNode.in = getDirection(iPos);
                findPath(map, exit, currentNode);
            }

            String action = ACTION_WAIT;

            if (rocks.size() > 0 && isCanStopStone(map, currentNode)) {
                log("try stop stones");
                action = tryFindRotationForStopStone(map, currentNode, rocks);
            }

            if (action.equals(ACTION_WAIT)) {
                action = tryFindRotationOnPath(map, currentNode);
            }

            System.out.println(action); // One line containing on of three commands: 'X Y LEFT', 'X Y RIGHT' or 'WAIT'
        }
    }

    private static String tryFindRotationForStopStone(Node[][] map, Node currentNode, ArrayList<Rock> rocks) {
        final Cell cell = new Cell();
        for (Rock rock : rocks) {
            cell.set(rock.cell);
            Direction dirIn = rock.direction;
//            log("Check " + rock);
            while (true) {
                final Node node = map[cell.x][cell.y];
                final Direction out = getOut(node.cellType, dirIn);
                out.getNextCell(cell); // get the next cell
//                log("Next cell " + cell + ", current out " + out);
                if (!isInsideOfMaze(cell)) {
                    break;
                }
                final Node nextNode = map[cell.x][cell.y];
                if (nextNode == currentNode || !isExistEntrance(nextNode.targetCellType, out.inverse())) {
                    break;
                }
                if (nextNode.in != Direction.unknown) {
                    final Integer left = rotateToLeft(nextNode.cellType);
                    int newTargetCellType = nextNode.cellType;
                    if (left != null) {
                        Direction mustBeOut = getOut(nextNode.cellType, nextNode.in);
                        Direction block = out.inverse();
                        if (isNeedCellType(left, nextNode.in, mustBeOut, block)) {
                            newTargetCellType = left;
                        } else {
                            final Integer dLeft = rotateToLeft(left);
                            if (dLeft != null && isNeedCellType(dLeft, nextNode.in, mustBeOut, block)) {
                                newTargetCellType = dLeft;
                            } else {
                                newTargetCellType = rotateToRight(nextNode.cellType);
                            }
                        }
                    }

                    nextNode.targetCellType = newTargetCellType;
                    log("Target is changed " + nextNode.cell + ", " + nextNode.targetCellType);

                    return ACTION_WAIT;
                } else if (nextNode.cellType > 0) {
                    nextNode.cellType = rotateToLeft(nextNode.cellType);
                    nextNode.targetCellType = nextNode.cellType;
                    return cell.x + " " + cell.y + " LEFT";
                }
                dirIn = out.inverse();
            }
        }

        return ACTION_WAIT;
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
            if (!isInsideOfMaze(nextCell)) {
//                log("impossible: out of range");
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
                final Node waitNode = new Node(nextCell, nextNode.cellType, nextIn, node.countTurns + 1);
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
                    final Node leftRotationNode = new Node(nextCell, leftCellType, nextIn, node.countTurns);
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
                    final Node rightRotationNode = new Node(nextCell, rightCellType, nextIn, node.countTurns);
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
                        final Node doubleLeftRotationNode = new Node(nextCell, dLeftCellType, nextIn, node.countTurns - 1);
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
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    private static class Node {

        public final Cell cell = new Cell();
        public Direction in;
        public int cellType;
        public int targetCellType;
        public int countTurns;
        public Node parent;

        public Node(Cell cell, int cellType, Direction in, int countTurns) {
            this(cell.x, cell.y, cellType, in, countTurns);
        }

        public Node(int x, int y, int cellType, Direction in, int countTurns) {
            cell.x = x;
            cell.y = y;
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