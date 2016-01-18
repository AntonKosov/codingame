import java.util.*;

/**
 * The machines are gaining ground. Time to show them what we're really made of...
 **/
class Player {
    
    private static int width;
    private static int height;
    private static int countNodes;

    private static Matrix matrix;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        width = in.nextInt(); // the number of cells on the X axis
        height = in.nextInt(); // the number of cells on the Y axis
        countNodes = width * height;
        String[] lines = new String[height];
        in.nextLine();
        for (int i = 0; i < height; i++) {
            String line = in.nextLine(); // width characters, each either a number or a '.'
            lines[i] = line;
        }
        
        matrix = new Matrix(lines);
        lookingExactSolutions();

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        String[] answers = new String[2];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                answers[0] = matrix.getNeighbor(x, y, x + 1, y);
                answers[1] = matrix.getNeighbor(x, y, x, y + 1);
                for (String answer : answers) {
                    if (answer != null) {
                        System.out.println(answer); // Two coordinates and one integer: a node, one of its neighbors, the number of links connecting them.
                    }
                }
            }
        }
    }

    private static void lookingExactSolutions() {
        boolean isRepeat = true;
        while (isRepeat) {
            isRepeat = false;
            for (int i = 0; i < countNodes; i++) {
                isRepeat |= matrix.distributeIsPossible(i);
            }
        }
    }
    
    private static class Matrix {
        
        private final Integer[][] data = new Integer[countNodes][countNodes];
        
        private final int[] freeInRows = new int[countNodes];
        private final int[] freeInColumns = new int[countNodes];
        
        public Matrix(String[] lines) {
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
                }
            }

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (x < width - 1 && data[cn(x + 1, y)][cn(x + 1, y)] != null) {
                        setWeight(cn(x, y), cn(x + 1, y), 0);
                        freeInColumns[cn(x, y)] += 2;
                        freeInRows[cn(x + 1, y)] += 2;
                    }
                    if (y < height - 1 && data[cn(x, y + 1)][cn(x, y + 1)] != null) {
                        setWeight(cn(x, y), cn(x, y + 1), 0);
                        freeInColumns[cn(x, y)] += 2;
                        freeInRows[cn(x, y + 1)] += 2;
                    }
                }
            }
        }
        
        private static int cn(int x, int y) {
            return y * height + x;
        }

        public String getNeighbor(int sX, int sY, int tX, int tY) {
            if (tX < 0 || tX > width - 1 || tY < 0 || tY > height - 1) {
                return null;
            }

            Integer value = getWeight(sX, sY, tX, tY);

            if (value == null || value == 0) {
                return null;
            }

            return sX + " " + sY + " " + tX + " " + tY + " " + value;
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
                    for (int i = node + 1; i < countNodes; i++) {
                        Integer weight = getWeight(i, node);
                        if (weight != null && weight < 2) {
                            count++;
                            if (count > 1) {
                                return false;
                            }
                        }
                    }
                } else {
                    return false;
                }
            }

            for (int i = 0; i < countNodes; i++) {
                if (i != node) {
                    Integer weight = getWeight(i, node);
                    if (weight != null && weight < 2) {
                        distribute(i, node, Math.min(data[node][node], 2 - weight));
                        if (data[node][node] == 0) {
                            break;
                        }
                    }
                }
            }

            return true;
        }

        private void distribute(int tX, int tY, int weight) {
            setWeight(tX, tY, getWeight(tX, tY) + weight);
            freeInRows[tY] -= weight;
            freeInColumns[tX] -= weight;
            data[tX][tX] -= weight;
            data[tY][tY] -=weight;
        }

        private void setWeight(int node1, int node2, int weight) {
            if (node1 > node2) {
                data[node1][node2] = weight;
            } else {
                data[node2][node1] = weight;
            }
        }
    }
}
