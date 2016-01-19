import java.util.*;

/**
 * The machines are gaining ground. Time to show them what we're really made of...
 **/
class Player {
    
    private static int width;
    private static int height;
    private static int countNodes;

    private static Matrix matrix;

    private static final Stack<State> stack = new Stack<State>();

    public static void main(String args[]) {
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
        final int startStakSize = stack.size();

        boolean isRepeat = true;
        while (isRepeat) {
            isRepeat = false;
            for (int i = 0; i < countNodes; i++) {
                isRepeat |= matrix.distributeIsPossible(i);
            }
        }

        if (!matrix.isSolved()) {
            while (stack.size() > startStakSize) {
                matrix.restore(stack.pop());
            }
        }
    }
    
    private static class Matrix {
        
        private final Integer[][] data = new Integer[countNodes][countNodes];
        
        private final int[] freeInRows = new int[countNodes];
        private final int[] freeInColumns = new int[countNodes];

        private int countFullNodes = 0;
        
        public Matrix(String[] lines) {
            boolean[][] field = new boolean[width][height];

            for (int l = 0; l < height; l++) {
                String line = lines[l];
                for (int c = 0; c < width; c++) {
                    char ch = line.charAt(c);
                    if (ch == '.') {
                        continue;
                    }
                    int nodeIndex = cn(c, l);
                    int val = Character.getNumericValue(ch);
                    data[nodeIndex][nodeIndex] = val;
                    field[c][l] = true;
                    countFullNodes++;
                }
            }

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
            return countFullNodes == 0;
        }
        
        private static int cn(int x, int y) {
            return y * height + x;
        }

        public ArrayList<String> getLinks() {
            ArrayList<String> result = new ArrayList<String>();

            for (int y = 0; y < countNodes; y++) {
                for (int x = y + 1; x < countNodes; x++) {
                    Integer weight = data[x][y];
                    if (weight != null) {
                        int n1y = y / height;
                        int n1x = y % width;
                        int n2y = x / height;
                        int n2x = x % width;
                        result.add(n1x + " " + n1y + " " + n2x + " " + n2y + " " + weight);
                    }
                }
            }

            return result;
        }

        private Integer getWeight(int x1, int y1, int x2, int y2) {
            int n1 = cn(x1, y1);
            int n2 = cn(x2, y2);
            return getWeight(n1, n2);
        }

        private Integer getWeight(int node1, int node2) {
            return node1 > node2 ? data[node1][node2] : data[node2][node1];
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
                            while (stack.size() > startStackSize) {
                                restore(stack.pop());
                            }
                        }
                        stack.push(state);
                        if (data[node][node] == 0) {
                            break;
                        }
                    }
                }
            }

            return stack.size() > startStackSize;
        }

        private State distribute(int x, int y, int value) {
            setWeight(x, y, getWeight(x, y) + value);
            freeInRows[y] -= value;
            freeInColumns[x] -= value;
            data[x][x] -= value;
            data[y][y] -= value;
            if (data[x][x] == 0) {
                countFullNodes--;
            }
            if (data[y][y] == 0) {
                countFullNodes--;
            }

            boolean isNoSolution = data[x][x] > freeInColumns[x] + freeInRows[x] || data[y][y] > freeInColumns[y] + freeInRows[y];
            return new State(x, y, value, isNoSolution);
        }

        private void restore(State state) {
            setWeight(state.x, state.y, getWeight(state.x, state.y) - state.value);
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

        private void setWeight(int node1, int node2, int weight) {
            if (node1 > node2) {
                data[node1][node2] = weight;
            } else {
                data[node2][node1] = weight;
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
    }
}
