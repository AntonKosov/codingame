import java.util.*;
import java.io.*;
import java.math.*;

/**
 * The machines are gaining ground. Time to show them what we're really made of...
 **/
class Player {
    
    private static int width;
    private static int height;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        width = in.nextInt(); // the number of cells on the X axis
        height = in.nextInt(); // the number of cells on the Y axis
        String[] lines = new String[height];
        in.nextLine();
        for (int i = 0; i < height; i++) {
            String line = in.nextLine(); // width characters, each either a number or a '.'
            lines[i] = line;
        }
        
        Matrix m = new Matrix(lines);

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        System.out.println("0 0 2 0 1"); // Two coordinates and one integer: a node, one of its neighbors, the number of links connecting them.
    }
    
    private static class Matrix {
        
        private final Integer[][] data = new Integer[width * height][width * height];
        
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
                    int val = Integer.parseInt(ch);
                    data[nodeIndex][nodeIndex] = val;
                }
            }

            for (int y = 0; y < height; y+) {
                for (int x = 0; x < width; x++) {
                    if (y > 0 && data[cn(x, y - 1)][cn(x, y - 1)] != null) {
                        data[cn(x, y)][cn(x, y - 1)] = 0;
                        freeInColumns[cn(x, y)]++;
                        freeInRows[cn(x, y - 1)]++;
                    }
                    if (x < width - 1 && data[cn(x + 1, y)][x + 1, y] != null) {
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
    }
}
