package hard;

import java.util.HashSet;
import java.util.Scanner;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
public class SkynetStrikesBack {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int N = in.nextInt(); // the total number of nodes in the level, including the gateways
        log("Nodes " + N);
        int L = in.nextInt(); // the number of links
        log("Links " + L);
        int E = in.nextInt(); // the number of exit gateways
        boolean[][] conn = new boolean[N][N];
        for (int i = 0; i < L; i++) {
            int N1 = in.nextInt(); // N1 and N2 defines a link between these nodes
            int N2 = in.nextInt();
            conn[N1][N2] = true;
            conn[N2][N1] = true;
            log("Linked " + N1 + "-" + N2);
        }

        int[] exitNodes = new int[E];
        for (int i = 0; i < E; i++) {
            int EI = in.nextInt(); // the index of a gateway node
            exitNodes[i] = EI;
            log("Exit " + EI);
        }

        int[] distance = new int[N];
        HashSet<Integer> visited = new HashSet<Integer>();

        int[] countConnectedExitNodes = new int[N];
        for (int en : exitNodes) {
            for (int i = 0; i < N; i++) {
                if (conn[en][i]) {
                    countConnectedExitNodes[i]++;
                }
            }
        }

        // game loop
        while (true) {
            int SI = in.nextInt(); // The index of the node on which the Skynet agent is positioned this turn

            visited.clear();
            for (int i = 0; i < N; i++) {
                distance[i] = i == SI ? 0 : Integer.MAX_VALUE;
            }

            distance[SI] -= countConnectedExitNodes[SI];

            Integer minDistanceIndex = null;
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
                    if (conn[currentNode][u] && !visited.contains(u) && distance[u] > distance[currentNode] + 1 - countConnectedExitNodes[u]) {
                        distance[u] = distance[currentNode] + 1 - countConnectedExitNodes[u];
                        if (countConnectedExitNodes[u] > 0 && (minDistanceIndex == null || distance[u] < distance[minDistanceIndex])) {
                            minDistanceIndex = u;
                        }
                    }
                }
            }

            Integer currentNode = null;
            Integer currentExitNode = null;
            for (int en : exitNodes) {
                log("dist " + en + " = " + distance[en]);
                if (distance[en] == 0 && conn[SI][en]) {
                    currentNode = SI;
                    currentExitNode = en;
                    log("direct");
                    break;
                }
            }

            if (currentNode == null) {
                currentNode = minDistanceIndex;
                log("mdi=" + minDistanceIndex + " (" + distance[minDistanceIndex] + ")");
                for (int i = 0; i < N; i++) {
                    if (conn[minDistanceIndex][i]) {
                        for (int en : exitNodes) {
                            if (i == en) {
                                currentExitNode = en;
                                break;
                            }
                        }
                        if (currentExitNode != null) {
                            break;
                        }
                    }
                }
            }

            System.out.println(currentNode + " " + currentExitNode);

            conn[currentExitNode][currentNode] = false;
            conn[currentNode][currentExitNode] = false;
            countConnectedExitNodes[currentNode]--;
        }
    }

    private static void log(String message) {
        System.err.println(message);
    }
}
