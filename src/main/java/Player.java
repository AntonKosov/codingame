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

        String[] answers = new String[4];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                answers[0] = matrix.getNeighbor(x, y, x, y - 1);
                answers[1] = matrix.getNeighbor(x, y, x + 1, y);
                answers[2] = matrix.getNeighbor(x, y, x, y + 1);
                answers[3] = matrix.getNeighbor(x, y, x - 1, y);
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
        
        private final int[] freeInRows = new int[height];
        private final int[] freeInColumns = new int[width];
        
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
                    if (y > 0 && data[cn(x, y - 1)][cn(x, y - 1)] != null) {
                        data[cn(x, y)][cn(x, y - 1)] = 0;
                        freeInColumns[cn(x, y)]++;
                        freeInRows[cn(x, y - 1)]++;
                    }
                    if (x < width - 1 && data[cn(x + 1, y)][cn(x + 1, y)] != null) {
                        data[cn(x, y)][cn(x + 1, y)] = 0;
                        freeInColumns[cn(x, y)]++;
                        freeInRows[cn(x + 1, y)]++;
                    }
                    if (y < height - 1 && data[cn(x, y + 1)][cn(x, y + 1)] != null) {
                        data[cn(x, y)][cn(x, y + 1)] = 0;
                        freeInColumns[cn(x, y)]++;
                        freeInRows[cn(x, y + 1)]++;
                    }
                    if (x > 0 && data[cn(x - 1, y)][cn(x - 1, y)] != null) {
                        data[cn(x, y)][cn(x - 1, y)] = 0;
                        freeInColumns[cn(x, y)]++;
                        freeInRows[cn(x - 1, y)]++;
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

            Integer value = data[cn(sX, sY)][cn(tX, tY)];

            if (value == null || value == 0) {
                return null;
            }

            return sX + " " + sY + " " + tX + " " + tY + " 1";
        }

        public boolean distributeIsPossible(int node) {
            if (data[node][node] != freeInColumns[node] + freeInRows[node]) {
                return false;
            }

            for (int i = 0; i < width; i++) {
                if (data[i][node] != null && data[i][node] == 0) {
                    data[i][node] = 1;
                }
            }

            for (int i = 0; i < height; i++) {
                if (data[node][i] != null && data[node][i] == 0) {
                    data[node][i] = 1;
                }
            }

            data[node][node] = 0;

            freeInColumns[node] = 0;
            freeInRows[node] = 0;

            return true;
        }
    }
}
