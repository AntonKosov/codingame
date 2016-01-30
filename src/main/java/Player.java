import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int N = in.nextInt(); // the total number of nodes in the level, including the gateways
        System.err.println("Nodes " + N);
        int L = in.nextInt(); // the number of links
        System.err.println("Links " + L);
        int E = in.nextInt(); // the number of exit gateways
        boolean[][] conn = new boolean[N][N];
        for (int i = 0; i < L; i++) {
            int N1 = in.nextInt(); // N1 and N2 defines a link between these nodes
            int N2 = in.nextInt();
            conn[N1][N2] = true;
            conn[N2][N1] = true;
            System.err.println("Linked " + N1 + "-" + N2);
        }

        int[] exitNodes = new int[E];
        for (int i = 0; i < E; i++) {
            int EI = in.nextInt(); // the index of a gateway node
            exitNodes[i] = EI;
            System.err.println("Exit " + EI);
        }

        int[] distance = new int[N];
        HashSet<Integer> visited = new HashSet<Integer>();

        // game loop
        while (true) {
            int SI = in.nextInt(); // The index of the node on which the Skynet agent is positioned this turn

            visited.clear();
            for (int i = 0; i < N; i++) {
                distance[i] = i == SI ? 0 : Integer.MAX_VALUE;
            }

            while (true) {
                Integer currentNode = null;
                int minimalDistance = 0;
                for (int i = 0; i < N; i++) {
                    if (!visited.contains(i) && (currentNode == null || minimalDistance > distance[i])) {
                        minimalDistance = distance[i];
                        currentNode = i;
                    }
                }

                if (currentNode == null) {
                    break;
                }

                visited.add(currentNode);
                for (int u = 0; u < N; u++) {
                    if (conn[currentNode][u] && !visited.contains(u) && distance[u] > distance[currentNode] + 1) {
                        distance[u] = distance[currentNode] + 1;
                    }
                }
            }

            ArrayList<Integer> nearestExitNodes = new ArrayList<Integer>();
            int exitNode = exitNodes[0];
            for (int en = 0; en < E; en++) {
                System.err.println("distance to " + exitNodes[en] + " = " + distance[exitNodes[en]]);
                if (distance[exitNode] < distance[exitNodes[en]]) {
                    continue;
                }
                if (distance[exitNode] > distance[exitNodes[en]]) {
                    nearestExitNodes.clear();
                }
                exitNode = exitNodes[en];
                nearestExitNodes.add(exitNode);
            }

            System.err.println("nearestExitNodes.s=" + nearestExitNodes.size());

            Integer nearestNode = null;
            Integer nearestExitNode = null;
            Integer firstNode = null;
            Integer firstExitNode = null;
            HashSet<Integer> nearestNodes = new HashSet<Integer>();
            for (int nen : nearestExitNodes) {
                for (int i = 0; i < N; i++) {
                    if (conn[nen][i] && distance[i] < distance[nen]) {
                        if (firstNode == null) {
                            firstNode = i;
                            firstExitNode = nen;
                            System.err.println("firstN=" + firstNode + ", firstEN=" + firstExitNode);
                        }
                        if (nearestNodes.contains(i)) {
                            nearestNode = i;
                            nearestExitNode = nen;
                            System.err.println("nearestN=" + nearestNode + ", nearestEN=" + nearestExitNode);
                            break;
                        }
                        nearestNodes.add(i);
                    }
                }
                if (nearestNode != null) {
                    break;
                }
            }

            if (nearestNode == null) {
                nearestNode = firstNode;
                nearestExitNode = firstExitNode;
            }

            System.out.println(nearestExitNode + " " + nearestNode);

            conn[nearestExitNode][nearestNode] = false;
            conn[nearestNode][nearestExitNode] = false;

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
        }
    }
}
