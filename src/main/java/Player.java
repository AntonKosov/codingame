import java.util.*;

/**
 * The machines are gaining ground. Time to show them what we're really made of...
 **/
class Player {

    public static final int UP = 0;
    public static final int DOWN = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;

    private static int width;
    private static int height;
    private static int countActiveNodes;

    private static Map map;

    private static Stack<State> stack;

    public static void main(String args[]) {
        countActiveNodes = 0;
        stack = new Stack<State>();
        Scanner in = new Scanner(System.in);
        width = in.nextInt(); // the number of cells on the X axis
        height = in.nextInt(); // the number of cells on the Y axis
        log("width=" + width + ", height=" + height);
//        countNodes = width * height;
        String[] lines = new String[height];
        in.nextLine();
        for (int i = 0; i < height; i++) {
            String line = in.nextLine(); // width characters, each either a number or a '.'
            lines[i] = line;
            log(line);
        }
        
        map = new Map(lines);
        lookingSolutions();
        if (map.isSolved()) {
            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            ArrayList<String> answers = map.getLinks();
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

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    isRepeat |= map.distributeIsPossible(x, y);
                }
            }
        }

        if (!map.isSolved()) {
            // brute force
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Node node = map.nodes[x][y];
                    if (node != null && node.value > 0) {
                        distribute(node, 0);
                        if (map.isSolved()) break;
                    }
                }
            }
        }

        if (!map.isSolved()) {
            while (stack.size() > startStackSize) {
                map.restore(stack.pop());
            }
        }
    }

    private static void distribute(Node node, int startPoint) {
        final int value = node.value;
        if (value == 0) {
            lookingSolutions();
        } else {
            final Link[] links = node.links;
            for (int i = startPoint; i < links.length; i++) {
                final Link link = links[i];
                if (link != null && link.countFreeLinks() > 0 && !link.isBlocked() && link.node1.value > 0 && link.node2.value > 0) {
                    final State state = map.distribute(link, 1);
                    if (state.isNoSolution) {
                        map.restore(state);
                        continue;
                    }
                    stack.push(state);
                    distribute(node, i);
                    if (map.isSolved()) break;
                    map.restore(stack.pop());
                }
            }
        }
    }

    private static void log(String message) {
        //System.err.println(message);
    }

    private static class Map {
        
        private final Node[][] nodes = new Node[width][height];

        private final int maxDoubleLinks;

        private int countNotEmptyNodes = 0;
        private int countDoubleLinks = 0;

        private int step = 0;

        public Map(String[] lines) {
            int sum = 0;
            for (int y = 0; y < height; y++) {
                String line = lines[y];
                for (int x = 0; x < width; x++) {
                    char ch = line.charAt(x);
                    if (ch == '.') {
                        continue;
                    }
                    countActiveNodes++;
                    int value = Character.getNumericValue(ch);
                    sum += value;
                    countNotEmptyNodes++;

                    Node node = new Node(x, y, value);
                    nodes[x][y] = node;

                    for (int i = x - 1; i >= 0; i--) {
                        final Node sibling = nodes[i][y];
                        if (sibling != null) {
                            node.siblings[LEFT] = sibling;
                            sibling.siblings[RIGHT] = node;
                            int possibleLink = Math.min(2, Math.min(node.value, sibling.value));
                            final Link link = new Link(possibleLink, node, sibling);
                            node.links[LEFT] = link;
                            sibling.links[RIGHT] = link;
                            break;
                        }
                    }
                    for (int i = y - 1; i >= 0; i--) {
                        final Node sibling = nodes[x][i];
                        if (sibling != null) {
                            node.siblings[UP] = sibling;
                            sibling.siblings[DOWN] = node;
                            int possibleLink = Math.min(2, Math.min(node.value, sibling.value));
                            final Link link = new Link(possibleLink, node, sibling);
                            node.links[UP] = link;
                            sibling.links[DOWN] = link;
                            break;
                        }
                    }
                }
            }

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Node node = nodes[x][y];
                    if (node != null) {
                        Link upLink = node.links[UP];
                        if (upLink != null) {
                            int yStart = Math.min(upLink.node1.y, upLink.node2.y) + 1;
                            int yEnd = Math.max(upLink.node1.y, upLink.node2.y) - 1;
                            for (int ys = yStart; ys <= yEnd; ys++) {
                                for (int xs = upLink.node1.x - 1; xs >= 0; xs--) {
                                    Node sn = nodes[xs][ys];
                                    if (sn != null) {
                                        final Link crossLink = sn.links[RIGHT];
                                        if (crossLink != null) {
                                            upLink.crosses.add(crossLink);
                                        }
                                        break;
                                    }
                                }
                            }
                        }

                        Link leftLink = node.links[LEFT];
                        if (leftLink != null) {
                            int xStart = Math.min(leftLink.node1.x, leftLink.node2.x) + 1;
                            int xEnd = Math.max(leftLink.node1.x, leftLink.node2.x) - 1;
                            for (int xs = xStart; xs <= xEnd; xs++) {
                                for (int ys = leftLink.node1.y - 1; ys >= 0; ys--) {
                                    Node sn = nodes[xs][ys];
                                    if (sn != null) {
                                        final Link crossLink = sn.links[DOWN];
                                        if (crossLink != null) {
                                            leftLink.crosses.add(crossLink);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            maxDoubleLinks = sum - 2 * (countActiveNodes - 1);
        }

        public boolean isSolved() {
            if (countNotEmptyNodes > 0) return false;
            final int c = countActiveLinks();
            if (c < countActiveNodes - 1) {
                log("isSolved: " + c + "/" + countActiveNodes);
                return false;
            }

            //c >= (countActiveNodes * countActiveNodes - 3 * countActiveNodes + 4) / 2;
            return true;
        }
        
        public ArrayList<String> getLinks() {
            ArrayList<String> result = new ArrayList<String>();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Node node = nodes[x][y];
                    if (node != null) {
                        final Link upLink = node.links[UP];
                        if (Link.isActive(upLink)) {
                            result.add(upLink.node2.x + " " + upLink.node2.y +" " +
                                    upLink.node1.x + " " + upLink.node1.y + " " + upLink.value);
                        }
                        final Link leftLink = node.links[LEFT];
                        if (Link.isActive(leftLink)) {
                            result.add(leftLink.node2.x + " " + leftLink.node2.y +" " +
                                    leftLink.node1.x + " " + leftLink.node1.y + " " + leftLink.value);
                        }
                    }
                }
            }

            return result;
        }

        //todo optimization
        private int countActiveLinks() {
            int result = 0;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    final Node node = nodes[x][y];
                    if (node != null) {
                        if (Link.isActive(node.links[UP])) {
                            result++;
                        }
                        if (Link.isActive(node.links[LEFT])) {
                            result++;
                        }
                    }
                }
            }

            return result;
        }

        public boolean distributeIsPossible(int x, int y) {
            final Node node = nodes[x][y];
            if (node == null || node.value == 0) {
                return false;
            }

            if (node.value != node.countRestLinks() && !(node.value == 1 && node.isSingleFreeSibling())) {
                return false;
            }

            final int startStackSize = stack.size();

            for (Link link : node.links) {
                if (link != null) {
                    final int countFreeLinks = link.countFreeLinks();
                    if (countFreeLinks > 0 && link.node1.value > 0 && link.node2.value > 0) {
                        int countLinks = Math.min(countFreeLinks, link.node1.value);
                        countLinks = Math.min(countLinks, link.node2.value);
                        final State state = distribute(link, countLinks);
                        if (state.isNoSolution) {
                            restore(state);
                        } else {
                            stack.push(state);
                            if (node.value == 0) {
                                break;
                            }
                        }
                    }
                }
            }

            return stack.size() > startStackSize;
        }

        public State distribute(Link link, int value) {
            step++;
            final int newValue = link.value + value;
            log(">>>before: step=" + step + ", l=" + link.node2 + "-" + link.node1 +
                    ", v=" + value + "\n" + toString());
            link.value = newValue;
            link.node1.value -= value;
            link.node2.value -= value;
            if (link.node1.value == 0) {
                countNotEmptyNodes--;
            }
            if (link.node2.value == 0) {
                countNotEmptyNodes--;
            }

            if (newValue == 2) {
                countDoubleLinks++;
            }

            for (Link lc : link.crosses) {
                lc.block();
            }

            boolean isNoSolution =
                    countDoubleLinks > maxDoubleLinks ||
                    !link.node1.isPossible(true) ||
                    !link.node2.isPossible(true);
            if (!isNoSolution) {
                for (Link lc : link.crosses) {
                    isNoSolution = !lc.node1.isPossible(false) || !lc.node2.isPossible(false);
                    if (isNoSolution) {
                        break;
                    }
                }
            }

            if (!isNoSolution) {
                log(
                        "\nstep=" + step +
                        ", stack=" + stack.size());
                log(toString());
//                try {
//                    Thread.sleep(50);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }

            return new State(link, value, isNoSolution);
        }

        private void restore(State state) {
            final int oldValue = state.link.value;
            if (oldValue == 2) {
                countDoubleLinks--;
            }
            state.link.value -= state.value;
            if (state.link.node1.value == 0) {
                countNotEmptyNodes++;
            }
            if (state.link.node2.value == 0) {
                countNotEmptyNodes++;
            }
            state.link.node1.value += state.value;
            state.link.node2.value += state.value;
            for (Link lc : state.link.crosses) {
                lc.unblock();
            }
            log("restore " + state);
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            Link[] vLinks = new Link[width];
            for (int y = 0; y < height; y++) {
                Link hLink = null;
                for (int x = 0; x < width; x++) {
                    Node node = nodes[x][y];
                    if (node != null) {
                        hLink = node.links[RIGHT];
                        vLinks[x] = node.links[DOWN];
                        result.append(node.value);
                    } else if (hLink != null && hLink.value > 0) {
                        result.append("-");
                    } else if (vLinks[x] != null && vLinks[x].value > 0) {
                        result.append("|");
                    } else {
                        result.append(".");
                    }
                }
                result.append("\n");
            }

            return result.toString();
        }
    }

    private static class State {

        public final Link link;
        public final int value;
        public final boolean isNoSolution;

        public State(Link link, int value, boolean isNoSolution) {
            this.link = link;
            this.value = value;
            this.isNoSolution = isNoSolution;
        }

        @Override
        public String toString() {
            return "l=" + link + ", v=" + value;
        }
    }


    private static class Node {
        public final Node[] siblings = new Node[4];
        public final Link[] links = new Link[4];

        public final int x;
        public final int y;

        public int value;

        public Node(int x, int y, int value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }

        public int countRestLinks() {
            int result = 0;

            for (Link link : links) {
                if (link != null) {
                    result += Math.min(link.node2.value, Math.min(link.node1.value, link.maxValue - link.value));
                }
            }

            return result;
        }

        public boolean isSingleFreeSibling() {
            int result = 0;

            for (Link link : links) {
                if (link != null) {
                    result += link.value < link.maxValue ? 1 : 0;
                }
            }

            return result == 1;
        }

        public boolean isPossible(boolean checkSiblings) {
            if (value > countRestLinks()) {
                return false;
            }

            if (checkSiblings) {
                for (Node node : siblings) {
                    if (node != null && !node.isPossible(false)) {
                        return false;
                    }
                }
            }

            return true;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    private static class Link {
        public final int maxValue;
        public int value = 0;

        public final Node node1;
        public final Node node2;

        public final ArrayList<Link> crosses = new ArrayList<Link>();

        public int blockCounter = 0;

        public Link(int maxValue, Node node1, Node node2) {
            this.maxValue = maxValue;
            this.node1 = node1;
            this.node2 = node2;
        }

        public int countFreeLinks() {
            return maxValue - value;
        }

        @Override
        public String toString() {
            return value + "/" + maxValue + " " + (isBlocked() ? "b" : "u") + ", " + node2 + "-" + node1;
        }

        public void block() {
            blockCounter++;
            value = maxValue;
        }

        public void unblock() {
            blockCounter--;
            if (!isBlocked()) {
                value = 0;
            }
        }

        public boolean isBlocked() {
            return blockCounter > 0;
        }

        public static boolean isActive(Link link) {
            return link != null && link.value > 0 && !link.isBlocked();
        }
    }
}
