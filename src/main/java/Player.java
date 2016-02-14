import java.util.*;

class Player {

    private static final boolean SHOW_LOGS = true;

    private static Linear sTop;
    private static Linear sRight;
    private static Linear sBottom;
    private static Linear sLeft;

    private static int sWidth;
    private static int sHeight;

    private static boolean sIsSameMode;
    private static final ArrayList<Vertex> sSameVertexes = new ArrayList<>();
    private static final HashSet<Vertex> sVisitedVertexes = new HashSet<>();

    private static int sStep;

    public static void main(String args[]) {
        final Vertex oldVertex = new Vertex();
        final Vertex currVertex = new Vertex();
        sIsSameMode = false;
        sSameVertexes.clear();
        sVisitedVertexes.clear();
        sStep = 0;

        Scanner in = new Scanner(System.in);
        sWidth = in.nextInt(); // width of the building.
        sHeight = in.nextInt(); // height of the building.
        final int turns = in.nextInt();// maximum number of turns before game over.
        oldVertex.x = in.nextInt() + 0.5f;
        oldVertex.y = in.nextInt() + 0.5f;

        log("w=" + sWidth + ", h=" + sHeight + ", t=" + turns);

        oldVertex.copyTo(currVertex);

        sTop = Linear.createByVertexes(0, 0, sWidth, 0);
        sRight = Linear.createByVertexes(sWidth, 0, sWidth, sHeight);
        sBottom = Linear.createByVertexes(0, sHeight, sWidth, sHeight);
        sLeft = Linear.createByVertexes(0, 0, 0, sHeight);

        ArrayList<StartOfSegment> segments = new ArrayList<>();
        final Vertex testVertex = new Vertex(1, 1);
        segments.add(new StartOfSegment(0, 0, sTop, sTop.getSign(testVertex)));
        segments.add(new StartOfSegment(sWidth, 0, sRight, sRight.getSign(testVertex)));
        segments.add(new StartOfSegment(sWidth, sHeight, sBottom, sBottom.getSign(testVertex)));
        segments.add(new StartOfSegment(0, sHeight, sLeft, sLeft.getSign(testVertex)));

        final Vertex centerOfMass = new Vertex();

        // game loop
        while (true) {
            sStep++;
            String BOMBDIST = in.next(); // Current distance to the bomb compared to previous distance (COLDER, WARMER, SAME or UNKNOWN)

            log("BOMBDIST=" + BOMBDIST + ", step=" + sStep);

            if (BOMBDIST.equals("UNKNOWN")) {
                firstJump(oldVertex, currVertex);
            } else {
                updateMap(BOMBDIST, oldVertex, currVertex, segments);
                centerOfMass.copyFrom(findCenter(segments));
                final Vertex nextVertex = findNextVertex(segments, centerOfMass, currVertex);
                oldVertex.copyFrom(currVertex);
                currVertex.copyFrom(nextVertex);
            }

            final int x = (int) currVertex.x;
            final int y = (int) currVertex.y;
            System.out.println(x + " " + y);
            sVisitedVertexes.add(new Vertex(x, y));
        }
    }

    private static void updateMap(
            String answer,
            Vertex prevVertex,
            Vertex currVertex,
            ArrayList<StartOfSegment> outSegments) {
        final Distance dist = answer.equals("COLDER") ? Distance.colder : answer.equals("WARMER") ? Distance.warmer : Distance.same;
        if (sIsSameMode) {
            cutSame(prevVertex, currVertex, dist);
            return;
        }

        if (dist == Distance.same) {
            sIsSameMode = true;
            firstSearchSame(prevVertex, currVertex, outSegments);
            return;
        }

        final ArrayList<StartOfSegment> newShape = cut(dist, prevVertex, currVertex, outSegments);
        if (newShape.isEmpty()) {
            // no crosses
            return;
        }

        outSegments.clear();
        outSegments.addAll(newShape);
    }

    private static void cutSame(Vertex prevVertex, Vertex currVertex, Distance dist) {
        int i = 0;
        while (i < sSameVertexes.size()) {
            final Vertex vertex = sSameVertexes.get(i);
            boolean isRemove = vertex.equals(currVertex);
            if (!isRemove) {
                float dst2ToPrev = prevVertex.dst2(vertex);
                float dst2ToCurr = currVertex.dst2(vertex);
                switch (dist) {
                    case same:
                        isRemove = dst2ToPrev != dst2ToCurr;
                        break;
                    case warmer:
                        isRemove = dst2ToCurr > dst2ToPrev;
                        break;
                    case colder:
                        isRemove = dst2ToCurr < dst2ToPrev;
                }
            }

            if (isRemove) {
                sSameVertexes.remove(i);
            } else {
                i++;
            }
        }
    }

    private static void firstSearchSame(Vertex v1, Vertex v2, ArrayList<StartOfSegment> segments) {
        final Linear v1v2 = Linear.createByVertexes(v1, v2);
        final Vertex v1v2middle = v1.middle(v2);
        final Linear same = Linear.createByVertexAndOrthLine(v1v2middle, v1v2);

        final ArrayList<Vertex> unvisitedInLine = getUnvisitedInLine(same, segments);
        for (Vertex vertex : unvisitedInLine) {
            if (vertex.dst2(v1) == vertex.dst2(v2)) {
                sSameVertexes.add(vertex);
            }
        }
    }

    private static ArrayList<Vertex> getUnvisitedInLine(Linear line, ArrayList<StartOfSegment> segments) {
        final ArrayList<Vertex> result = new ArrayList<>();
        final float dx;
        final float dy;
        if (line.b == 0) {
            dx = 0;
            dy = 1;
        } else if (line.a == 0) {
            dx = 1;
            dy = 0;
        } else {
            if (Math.abs(line.a / line.b) > 1) {
                dx = line.b / line.a * Math.signum(line.a * line.b);
                dy = 1;
            } else {
                dx = 1;
                dy = line.a / line.b * Math.signum(line.a * line.b);
            }
        }

        final Vertex v = new Vertex();
        if (dx == 1) {
            v.x = 0;
            v.y = line.getY(v.x);
        } else {
            v.y = 0;
            v.x = line.getX(v.y);
        }

        final Vertex tv = new Vertex();
        final Vertex cv = new Vertex();
        while (v.x < sWidth && v.y < sHeight) {
            tv.copyFrom(v);
            round(tv);
            cv.x = (int) tv.x;
            cv.y = (int) tv.y;
            if (!sVisitedVertexes.contains(cv) && isInside(tv, segments)) {
                result.add(new Vertex(tv.x, tv.y));
            }

            v.x += dx;
            v.y += dy;
        }

        return result;
    }

    private static boolean isInside(Vertex v, ArrayList<StartOfSegment> segments) {
        for (StartOfSegment s : segments) {
            if (s.linear.getSign(v) != s.sign) {
                return false;
            }
        }

        return true;
    }

    private static Vertex findNextVertex(
            ArrayList<StartOfSegment> segments,
            Vertex center,
            Vertex lastVertex) {
        if (sIsSameMode) {
            return findNextSameVertex(lastVertex);
        }

        Vertex optimalVertex = tryFindByArea(segments);
        if (optimalVertex != null) {
            return optimalVertex;
        }
        float minDifference = 0;
        for (StartOfSegment segment : segments) {
            final Linear cutOverSegment = Linear.createByVertexes(segment.vertex, center);
            final Linear axis = Linear.createByVertexAndOrthLine(lastVertex, cutOverSegment);
            final Vertex intersect = axis.intersect(cutOverSegment);

            final Vertex newVertex = new Vertex(
                    intersect.x + intersect.x - lastVertex.x,
                    intersect.y + intersect.y - lastVertex.y);
            if (newVertex.y < 0) {
                fixVertex(sTop, axis, 0, 0.5f, newVertex);
            }
            if (newVertex.x >= sWidth) {
                fixVertex(sRight, axis, -0.5f, 0, newVertex);
            }
            if (newVertex.y >= sHeight) {
                fixVertex(sBottom, axis, 0, -0.5f, newVertex);
            }
            if (newVertex.x < 0) {
                fixVertex(sLeft, axis, 0.5f, 0, newVertex);
            }
            round(newVertex);
            if (newVertex.equals(lastVertex)) {
                continue;
            }

            final ArrayList<StartOfSegment> subSegment1 = cut(Distance.warmer, lastVertex, newVertex, segments);
            final ArrayList<StartOfSegment> subSegment2 = cut(Distance.colder, lastVertex, newVertex, segments);
            final float area1 = getArea(subSegment1);
            final float area2 = getArea(subSegment2);
            if (area1 != 0 && area2 != 0) {
                final float difference = Math.abs(area1 - area2);
                if (optimalVertex == null || difference < minDifference) {
                    optimalVertex = newVertex;
                    minDifference = difference;
                }
            }
        }

        final float area = getArea(segments);
        final float optimalK = minDifference / area * 100;
        final Vertex min = new Vertex();
        final Vertex max = new Vertex();
        getBound(segments, min, max);
        final float areaRatio = area / (max.x - min.x) / (max.y - min.y);
        log("ok=" + optimalK + ", ar=" + areaRatio);
        if (areaRatio < 0.1f && optimalK > 5) {
            log("Very thin, min=" + min + ", max=" + max);
            optimalVertex = findVertexFromThinShape(segments, lastVertex, min, max);
        } else if (optimalVertex == null) {
            optimalVertex = findVertexFromThinShape(segments, lastVertex, min, max);
        }

        log("Next vertex: " + optimalVertex);

        return optimalVertex;
    }

    private static Vertex findVertexFromThinShape(ArrayList<StartOfSegment> segments, Vertex lastVertex, Vertex min, Vertex max) {
        Vertex optimalVertex;
        Vertex farhest = segments.get(0).vertex;
        float maxDistance = 0;
        for (StartOfSegment segment : segments) {
            final float dst2 = lastVertex.dst2(segment.vertex);
            if (dst2 > maxDistance) {
                maxDistance = dst2;
                farhest = segment.vertex;
            }
        }

        final Vertex topRight = new Vertex(max.x, min.y);
        final Vertex bottomLeft = new Vertex(min.x, max.y);
        Vertex corner = min;
        int dx = 1;
        int dy = -1;
        if (farhest.dst2(corner) > farhest.dst2(topRight)) {
            corner = topRight;
            dx = -1;
            dy = -1;
        }
        if (farhest.dst2(corner) > farhest.dst2(max)) {
            corner = max;
            dx = -1;
            dy = 1;
        }
        if (farhest.dst2(corner) > farhest.dst2(bottomLeft)) {
            corner = bottomLeft;
            dx = 1;
            dy = 1;
        }

        round(corner);
        int s = 1;
        final Vertex v = new Vertex();
        final Vertex cv = new Vertex();
        optimalVertex = null;
        while (optimalVertex == null) {
            v.copyFrom(corner);
            v.y = v.y + (s - 1) * -dy;
            for (int k = 0; k < s; k++) {
                cv.set((int) v.x, (int) v.y);
                if (!sVisitedVertexes.contains(cv) && isInside(v, segments)) {
                    optimalVertex = v;
                    break;
                }

                v.x += dx;
                v.y += dy;
            }
            s++;
        }
        return optimalVertex;
    }

    private static void getBound(ArrayList<StartOfSegment> segments, Vertex min, Vertex max) {
        float minX = sWidth;
        float maxX = 0;
        float minY = sHeight;
        float maxY = 0;
        for (StartOfSegment segment : segments) {
            final Vertex v = segment.vertex;
            minX = Math.min(minX, v.x);
            maxX = Math.max(maxX, v.x);
            minY = Math.min(minY, v.y);
            maxY = Math.max(maxY, v.y);
        }

        min.x = minX;
        min.y = minY;
        max.x = maxX;
        max.y = maxY;
    }

    private static Vertex tryFindByArea(ArrayList<StartOfSegment> segments) {
        final float area = getArea(segments);
        if (area > 20) {
            return null;
        }

        final Vertex min = new Vertex();
        final Vertex max = new Vertex();
        getBound(segments, min, max);

        final ArrayList<Vertex> vertices = new ArrayList<>();
        final Vertex v = new Vertex();
        final Vertex cv = new Vertex();
        for (float y = min.y; y <= max.y; y++) {
            for (float x = min.x; x <= max.x; x++) {
                v.x = x;
                v.y = y;
                round(v);
                cv.x = (int) x;
                cv.y = (int) y;
                if (!sVisitedVertexes.contains(cv) && isInside(v, segments)) {
                    if (vertices.size() > 1) {
                        return null;
                    }
                    vertices.add(new Vertex(v.x, v.y));
                }
            }
        }

        return vertices.get(0);
    }

    private static Vertex findNextSameVertex(Vertex lastVertex) {
        //todo optimize cut half
        final Vertex firstSame = sSameVertexes.get(0);
        if (sSameVertexes.size() <= 2) {
            return firstSame;
        }

        final float dst2ToFirst = firstSame.dst2(lastVertex);
        final Vertex lastSame = sSameVertexes.get(sSameVertexes.size() - 1);
        final float dst2ToLast = lastSame.dst2(lastVertex);
        return dst2ToFirst > dst2ToLast ? firstSame : lastSame;
    }

    private static void fixVertex(Linear border, Linear axis, float dX, float dY, Vertex out) {
        final Vertex intersect = border.intersect(axis);
        intersect.x += dX;
        intersect.y += dY;
        out.copyFrom(intersect);
    }

    private static void round(Vertex vertex) {
        vertex.x = 0.5f + (int) vertex.x;
        vertex.y = 0.5f + (int) vertex.y;
    }

    private static float getArea(ArrayList<StartOfSegment> segments) {
        float area = 0;
        if (segments.size() > 0) {
            final Vertex vertex1 = segments.get(0).vertex;
            for (int v = 1; v < segments.size() - 1; v++) {
                final Vertex vertex2 = segments.get(v).vertex;
                final Vertex vertex3 = segments.get(v + 1).vertex;

                final float a = vertex1.dst(vertex2);
                final float b = vertex1.dst(vertex3);
                final float c = vertex2.dst(vertex3);
                final float areaTriangle = (float) Math.sqrt((a + b + c) * (b + c - a) * (a + c - b) * (a + b - c)) / 4;
                area += areaTriangle;
            }
        }

        return area;
    }

    private static Vertex findCenter(ArrayList<StartOfSegment> segments) {
        final Vertex center = new Vertex();
        float area = 0;
        final Vertex vertex1 = segments.get(0).vertex;
        for (int v = 1; v < segments.size() - 1; v++) {
            final Vertex vertex2 = segments.get(v).vertex;
            final Vertex vertex3 = segments.get(v + 1).vertex;

            final Vertex half = vertex2.createVertexRatio(vertex3, 0.5f);
            final Vertex median = vertex1.createVertexRatio(half, 2f / 3f);

            final float a = vertex1.dst(vertex2);
            final float b = vertex1.dst(vertex3);
            final float c = vertex2.dst(vertex3);
            final float areaTriangle = (float) Math.sqrt((a + b +c) * (b + c - a) * (a + c - b) * (a + b - c)) / 4;

            if (v == 1) {
                //first step
                median.copyTo(center);
                area = areaTriangle;
            } else {
                final Vertex newCenter = center.createVertexRatio(median, areaTriangle / (areaTriangle + area));
                newCenter.copyTo(center);

                area += areaTriangle;
            }
        }

        return center;
    }

    private static ArrayList<StartOfSegment> cut(
            Distance dist,
            Vertex prevVertex,
            Vertex currVertex,
            ArrayList<StartOfSegment> segments) {
        final Vertex middle = prevVertex.middle(currVertex);
        final Linear cut = Linear.createByVertexAndOrthLine(middle, Linear.createByVertexes(prevVertex, currVertex));
        final Vertex closestVertex = dist == Distance.warmer ? currVertex : prevVertex;
        final Sign side = cut.getSign(closestVertex);
        Integer startIndex = null;
        Integer endIndex = null;
        int current = 0;
        int prev = prevSegment(segments, current);
        int next = nextSegment(segments, current);
        int circle = 0;
        while (startIndex == null || endIndex == null) {
            final Sign currentSign = cut.getSign(segments.get(current).vertex);
            if (startIndex == null && currentSign == side) {
                if (cut.getSign(segments.get(prev).vertex) != side) {
                    startIndex = prev;
                }
            }
            if (endIndex == null && currentSign == side) {
                if (cut.getSign(segments.get(next).vertex) != side) {
                    endIndex = next;
                }
            }

            current = nextSegment(segments, current);
            prev = nextSegment(segments, prev);
            next = nextSegment(segments, next);

            if (current == 0) {
                circle++;
                if (circle == 3) {
                    // no crosses
                    return new ArrayList<>();
                }
            }
        }

        ArrayList<StartOfSegment> newShape = new ArrayList<>();
        int v = prevSegment(segments, startIndex);
        while (v != endIndex || newShape.size() < 3) {
            v = nextSegment(segments, v);
            newShape.add(segments.get(v));
        }

        // the first and the last must be replaced
        final Linear firstLine = Linear.createByVertexes(newShape.get(0).vertex, newShape.get(1).vertex);
        final Linear lastLine = Linear.createByVertexes(newShape.get(newShape.size() - 1).vertex, newShape.get(newShape.size() - 2).vertex);
        newShape.remove(0);
        newShape.add(0, new StartOfSegment(cut.intersect(firstLine), firstLine, firstLine.getSign(newShape.get(1).vertex)));

        final StartOfSegment l2 = newShape.get(newShape.size() - 2);
        final StartOfSegment l3 = newShape.get(newShape.size() - 3);
        newShape.remove(newShape.size() - 1);
        newShape.remove(newShape.size() - 1);
        final Vertex lastVertex = cut.intersect(lastLine);
        final Linear l2new = Linear.createByVertexes(l2.vertex, lastVertex);
        newShape.add(new StartOfSegment(
                l2.vertex,
                Linear.createByVertexes(l2.vertex, lastVertex),
                l2new.getSign(l3.vertex)));
        final Linear lastLineNew = Linear.createByVertexes(lastVertex, newShape.get(0).vertex);
        newShape.add(new StartOfSegment(lastVertex, lastLineNew, lastLineNew.getSign(newShape.get(1).vertex)));

        return newShape;
    }

    private static int nextSegment(ArrayList<StartOfSegment> segments, int currentSegment) {
        return currentSegment == segments.size() - 1 ? 0 : currentSegment + 1;
    }

    private static int prevSegment(ArrayList<StartOfSegment> segments, int currentSegment) {
        return currentSegment == 0 ? segments.size() - 1 : currentSegment - 1;
    }

    private static void firstJump(Vertex oldVertex, Vertex outNewVertex) {
        int middleX = sWidth / 2;
        int middleY = sHeight / 2;
        if (sWidth % 2 == 1 && sHeight % 2 == 1 && middleX == oldVertex.x && middleY == oldVertex.y) {
            outNewVertex.x += 0.5f;
            outNewVertex.y += 0.5f;
        } else {
            outNewVertex.x = sWidth - outNewVertex.x;
            outNewVertex.y = sHeight - outNewVertex.y;
        }
    }

    private static void log(String message) {
        if (SHOW_LOGS) {
            System.err.println(message);
        }
    }

    private enum Distance {
        same, colder, warmer
    }

    private static class Vertex {
        public float x;
        public float y;

        public Vertex() {
            this(0, 0);
        }

        public Vertex(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public Vertex(Vertex v) {
            this(v.x, v.y);
        }

        public void set(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Vertex createVertexRatio(Vertex v2, float ratio) {
            final float rx = x + (v2.x - x) * ratio;
            final float ry = y + (v2.y - y) * ratio;
            return new Vertex(rx, ry);
        }

        public float dst(Vertex v2) {
            return (float) Math.sqrt(dst2(v2));
        }

        public void copyTo(Vertex v) {
            v.x = x;
            v.y = y;
        }

        public void copyFrom(Vertex v) {
            x = v.x;
            y = v.y;
        }

        public Vertex middle(Vertex v) {
            return new Vertex((x + v.x) / 2, (y + v.y) / 2);
        }

        public float dst2(Vertex v2) {
            return (x - v2.x) * (x - v2.x) + (y - v2.y) * (y - v2.y);
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Vertex)) {
                return false;
            }

            final Vertex another = (Vertex) obj;
            return x == another.x && y == another.y;
        }

        @Override
        public int hashCode() {
            return (int) (x * 10000 + y);
        }
    }

    private static class StartOfSegment {
        public final Vertex vertex;
        public final Sign sign;
        public final Linear linear;

        public StartOfSegment(int x, int y, Linear linear, Sign sign) {
            this(new Vertex(x, y), linear, sign);
        }

        public StartOfSegment(Vertex vertex, Linear linear, Sign sign) {
            this.vertex = vertex;
            this.linear = linear;
            this.sign = sign;
        }

        @Override
        public String toString() {
            return vertex + ", " + sign;
        }
    }

    /**
     * ax+by+c=0
     */
    private static class Linear {
        public final float a;
        public final float b;
        public final float c;

        private Linear(float a, float b, float c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public Sign getSign(Vertex v) {
            final float result = a * v.x + b * v.y + c;
            return result > 0 ? Sign.positive : result < 0 ? Sign.negative : Sign.zero;
        }

        public static Linear createByVertexes(Vertex v1, Vertex v2) {
            return createByVertexes(v1.x, v1.y, v2.x, v2.y);
        }

        public static Linear createByVertexes(float x1, float y1, float x2, float y2) {
            final float a = y1 - y2;
            final float b = x2 - x1;
            final float c = x1 * y2  - x2 * y1;
            return new Linear(a, b, c);
        }

        public static Linear createByVertexAndOrthLine(Vertex vertex, Linear orth) {
            final float a = orth.b;
            final float b = -orth.a;
            return new Linear(a, b, -a * vertex.x - b * vertex.y);
        }

        public Vertex intersect(Linear l2) {
            if (a == 0) {
                return l2.intersect(this);
            } else {
                final float y = (l2.a * c / a - l2.c) / (l2.b - l2.a * b / a);
                final float x = (-b * y - c) / a;
                return new Vertex(x, y);
            }
        }

        public float getX(float y) {
            return (-b * y - c) / a;
        }

        public float getY(float x) {
            return (-a * x - c) / b;
        }

        @Override
        public String toString() {
            return
                    (a == 0 ? "" : a + "x") +
                    (b == 0 ? "" : (a != 0 && b > 0 ? "+" : "") + b + "y") +
                    (c == 0 ? "" : (c > 0 ? "+" : "") + c) +
                    "=0";
        }
    }

    private enum Sign {
        zero, positive, negative
    }
}