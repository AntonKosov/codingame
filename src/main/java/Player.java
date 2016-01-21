import java.util.*;

/**
 * The machines are gaining ground. Time to show them what we're really made of...
 **/
class Player {
    
    private static int width;
    private static int height;
    private static int countNodes;
    private static int countActiveNodes;

    private static Matrix matrix;

    private static Stack<State> stack;

    public static void main(String args[]) {
        countActiveNodes = 0;
        stack = new Stack<State>();
        Scanner in = new Scanner(System.in);
        width = in.nextInt(); // the number of cells on the X axis
        height = in.nextInt(); // the number of cells on the Y axis
        System.err.println("width=" + width + ", height=" + height);
        countNodes = width * height;
        String[] lines = new String[height];
        in.nextLine();
        for (int i = 0; i < height; i++) {
            String line = in.nextLine(); // width characters, each either a number or a '.'
            lines[i] = line;
            System.err.println(line);
        }
        
        matrix = new Matrix(lines);
        lookingSolutions();
        if (matrix.isSolved()) {
            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            ArrayList<String> answers = matrix.getLinks();
            for (String answer : answers) {
                System.out.println(answer); // Two coordinates and one integer: a node, one of its neighbors, the number of links connecting them.
            }
        } else {
            System.out.println("No solution");
        }
    }

    private static void lookingSolutions() {
        final int startStackSize = stack.size();

        // exact solutions
        boolean isRepeat = true;
        while (isRepeat) {
            isRepeat = false;
            for (int i = 0; i < countNodes; i++) {
                isRepeat |= matrix.distributeIsPossible(i);
            }
        }

        if (!matrix.isSolved()) {
            // brute force
            //todo sort by free points?
            for (int i = 0; i < countNodes; i++) {
                Integer weight = matrix.getWeightOfNode(i);
                if (weight != null && weight > 0) {
                    final ArrayList<Point> possiblePoints = matrix.getPossiblePoints(i);
                    distribute(i, possiblePoints, 0);
                    if (matrix.isSolved()) break;
                }
            }
        }

        if (!matrix.isSolved()) {
            while (stack.size() > startStackSize) {
                matrix.restore(stack.pop());
            }
        }
    }

    private static void distribute(int node, ArrayList<Point> possiblePoints, int startPoint) {
        Integer weight = matrix.getWeightOfNode(node);
        if (weight == 0) {
            lookingSolutions();
        } else {
            for (int i = startPoint; i < possiblePoints.size(); i++) {
                final Point point = possiblePoints.get(i);
                Integer v = matrix.getWeightOfPoint(point);
                if (v != null && v < 2) {
                    final State state = matrix.distribute(point);
                    if (state.isNoSolution) {
                        matrix.restore(state);
                        continue;
                    }
                    stack.push(state);
                    distribute(node, possiblePoints, i);
                    if (matrix.isSolved()) break;
                    matrix.restore(stack.pop());
                }
            }
        }
    }
    
    private static class Matrix {
        
        private final Integer[][] data = new Integer[countNodes][countNodes];
        
        private final int[] freeInRows = new int[countNodes];
        private final int[] freeInColumns = new int[countNodes];

        private final int maxDoubleLinks;

        private int countFullNodes = 0;
        private int countDoubleLinks = 0;

        public Matrix(String[] lines) {
            boolean[][] field = new boolean[width][height];
            int summ = 0;
            for (int l = 0; l < height; l++) {
                String line = lines[l];
                for (int c = 0; c < width; c++) {
                    char ch = line.charAt(c);
                    if (ch == '.') {
                        continue;
                    }
                    countActiveNodes++;
                    int nodeIndex = cn(c, l);
                    int val = Character.getNumericValue(ch);
                    data[nodeIndex][nodeIndex] = val;
                    field[c][l] = true;
                    countFullNodes++;
                    summ += val;
                }
            }

            maxDoubleLinks = summ - 2 * (countActiveNodes - 1);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (field[x][y]) {
                        for (int i = x - 1; i >= 0; i--) {
                            if (field[i][y]) {
                                setWeight(cn(x, y), cn(i, y), 0);
                                freeInColumns[cn(x, y)] += 2;
                                freeInRows[cn(i, y)] += 2;
                                break;
                            }
                        }
                        for (int i = y - 1; i >= 0; i--) {
                            if (field[x][i]) {
                                setWeight(cn(x, y), cn(x, i), 0);
                                freeInColumns[cn(x, y)] += 2;
                                freeInRows[cn(x, i)] += 2;
                                break;
                            }
                        }
                    }
                }
            }
        }

        public boolean isSolved() {
            if (countFullNodes > 0) return false;
            final int c = countLinks();
            if (c < countActiveNodes - 1) {
                System.err.println("isSolved: " + c + "/" + countActiveNodes);
                return false;
            }

            //c >= (countActiveNodes * countActiveNodes - 3 * countActiveNodes + 4) / 2;
            return true;
        }
        
        private static int cn(int x, int y) {
            return y * width + x;
        }

        public ArrayList<String> getLinks() {
            ArrayList<String> result = new ArrayList<String>();

            for (int y = 0; y < countNodes; y++) {
                for (int x = y + 1; x < countNodes; x++) {
                    Integer weight = data[x][y];
                    if (weight != null && weight > 0) {
                        int n1y = y / width;
                        int n1x = y % width;
                        int n2y = x / width;
                        int n2x = x % width;
                        result.add(n1x + " " + n1y + " " + n2x + " " + n2y + " " + weight);
                    }
                }
            }

            return result;
        }

        //todo optimization
        private int countLinks() {
            int result = 0;

            for (int y = 0; y < countNodes; y++) {
                for (int x = y + 1; x < countNodes; x++) {
                    Integer weight = data[x][y];
                    if (weight != null && weight > 0) {
                        result++;
                    }
                }
            }

            return result;
        }

/*
        private Integer getWeight(int x1, int y1, int x2, int y2) {
            int n1 = cn(x1, y1);
            int n2 = cn(x2, y2);
            return getWeight(n1, n2);
        }
*/

        private Integer getWeight(int node1, int node2) {
            return node1 > node2 ? data[node1][node2] : data[node2][node1];
        }

        public ArrayList<Point> getPossiblePoints(int node) {
            ArrayList<Point> result = new ArrayList<Point>();
            for (int y = 0; y < node; y++) {
                Integer v = data[node][y];
                if (v != null && v > 0) {
                    result.add(new Point(node, y));
                }
            }
            for (int x = node + 1; x < countNodes; x++) {
                Integer v = data[x][node];
                if (v != null && v < 2) {
                    result.add(new Point(x, node));
                }
            }
            return result;
        }

        public Integer getWeightOfNode(int node) {
            return data[node][node];
        }

        public Integer getWeightOfPoint(Point point) {
            return data[point.x][point.y];
        }

        public boolean distributeIsPossible(int node) {
            final Integer valueOfNode = data[node][node];
            if (valueOfNode == null || valueOfNode == 0) {
                return false;
            }

            if (valueOfNode != freeInColumns[node] + freeInRows[node]) {
                //single point?
                if (valueOfNode == 1) {
                    int count = 0;
                    for (int i = 0; i < countNodes; i++) {
                        if (i != node) {
                            Integer weight = getWeight(i, node);
                            if (weight != null && weight < 2) {
                                count++;
                                if (count > 1) {
                                    return false;
                                }
                            }
                        }
                    }
                } else {
                    return false;
                }
            }

            final int startStackSize = stack.size();

            for (int i = 0; i < countNodes; i++) {
                if (i != node) {
                    Integer weight = getWeight(i, node);
                    if (weight != null && weight < 2) {
                        final State state = distribute(i, node, Math.min(data[node][node], 2 - weight));
                        if (state.isNoSolution) {
                            restore(state);
                        } else {
                            stack.push(state);
                            if (data[node][node] == 0) {
                                break;
                            }
                        }
                    }
                }
            }

            return stack.size() > startStackSize;
        }

        public State distribute(Point point) {
            return distribute(point.x, point.y, 1);
        }

        private State distribute(int x, int y, int value) {
            final int newValue = getWeight(x, y) + value;
            setWeight(x, y, newValue);
            int realX = x;
            int realY = y;
            if (x < y) {
                realX = y;
                realY = x;
            }
            freeInRows[realY] -= value;
            freeInColumns[realX] -= value;
            data[x][x] -= value;
            data[y][y] -= value;
            if (data[x][x] == 0) {
                countFullNodes--;
            }
            if (data[y][y] == 0) {
                countFullNodes--;
            }

            if (newValue == 2) {
                countDoubleLinks++;
            }

            boolean isNoSolution =
                    countDoubleLinks > maxDoubleLinks ||
                    data[x][x] < 0 ||
                    data[y][y] < 0 ||
                    data[x][x] > freeInColumns[x] + freeInRows[x] ||
                    data[y][y] > freeInColumns[y] + freeInRows[y];
            return new State(x, y, value, isNoSolution);
        }

        private void restore(State state) {
            final Integer oldValue = getWeight(state.x, state.y);
            if (oldValue == 2) {
                countDoubleLinks--;
            }
            setWeight(state.x, state.y, oldValue - state.value);
            freeInRows[state.y] += state.value;
            freeInColumns[state.x] += state.value;
            if (data[state.x][state.x] == 0) {
                countFullNodes++;
            }
            if (data[state.y][state.y] == 0) {
                countFullNodes++;
            }
            data[state.x][state.x] += state.value;
            data[state.y][state.y] += state.value;
        }

        private void setWeight(int x, int y, int weight) {
            if (x > y) {
                data[x][y] = weight;
            } else {
                data[y][x] = weight;
            }
        }
    }

    private static class State {

        public final int x;
        public final int y;
        public final int value;
        public final boolean isNoSolution;

        public State(int x, int y, int value, boolean isNoSolution) {
            this.x = x;
            this.y = y;
            this.value = value;
            this.isNoSolution = isNoSolution;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + "), v=" + value + ", ins=" + isNoSolution;
        }
    }

    private static class Point {
        public final int x;
        public final int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }
}
