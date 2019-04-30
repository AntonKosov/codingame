using System;
using System.Collections.Generic;

namespace CodinGame.VeryHard
{
    // https://www.codingame.com/training/expert/cross-the-lines
    public class CrossTheLines
    {
        static void Main()
        {
            var crossTheLines = new CrossTheLines(new ConsoleInterface());
            crossTheLines.Run();
        }

        private readonly IIOInterface _iIOInterface;

        private readonly List<List<int>> _connections = new List<List<int>>();

        private readonly List<Coordinate> _coordinates = new List<Coordinate>();

        private int[] _groups;

        private Color[] _colors;

        private readonly Dictionary<int, Color> _segmentsColors = new Dictionary<int, Color>();

        private int[] _parents;

        private int _numberOfEdges;

        private readonly List<List<int>> _polygons = new List<List<int>>();

        public CrossTheLines(IIOInterface iIOInterface)
        {
            if (iIOInterface == null)
                throw new ArgumentNullException(nameof(iIOInterface));
            _iIOInterface = iIOInterface;
        }

        public void Run()
        {
            ReadData();

            FindCycles();

            int crosses = CountCrosses();
            _iIOInterface.WriteLine(crosses.ToString());
        }

        private void FindCycles()
        {
            for (int nodeIndex = 0; nodeIndex < _connections.Count; nodeIndex++)
            {
                if (_colors[nodeIndex] == Color.Black) continue;

                _colors[nodeIndex] = Color.Gray;
                List<int> nextConnections = _connections[nodeIndex];
                foreach (var nextConnection in nextConnections)
                {
                    while (true)
                    {
                        if (LookForCycles(nodeIndex, nextConnection) == 0)
                            break;
                    }
                }

                _colors[nodeIndex] = Color.Black;
            }
        }

        private int CountCrosses()
        {
            HashSet<int> countedEdges = new HashSet<int>();
            HashSet<int> countedAsTwo = new HashSet<int>();
            Dictionary<int, int> commonEdges = new Dictionary<int, int>();
            foreach (List<int> polygon in _polygons)
                foreach (int segmentId in polygon)
                    if (commonEdges.ContainsKey(segmentId))
                        commonEdges[segmentId] = 2;
                    else
                        commonEdges.Add(segmentId, 1);
            
            int crosses = 0;
            foreach (List<int> polygon in _polygons)
            {
                bool hasCommonEdges = false;
                foreach (int segmentId in polygon)
                {
                    hasCommonEdges |= commonEdges[segmentId] == 2;
                    if (countedEdges.Contains(segmentId)) continue;
                    
                    crosses++;
                    if (commonEdges[segmentId] == 2 && !countedAsTwo.Contains(segmentId))
                    {
                        crosses++;
                        countedAsTwo.Add(segmentId);
                    }
                    
                    countedEdges.Add(segmentId);
                }

                if (!hasCommonEdges && polygon.Count % 2 == 1)
                    crosses++;
            }

            crosses += _numberOfEdges - countedEdges.Count;

            return crosses;
        }

        private int LookForCycles(int previousNodeIndex, int currentNodeIndex)
        {
            if (_colors[currentNodeIndex] == Color.Black)
                return 0;

            if (_colors[currentNodeIndex] == Color.Gray)
            {
                List<int> polygon = BuildPolygon(currentNodeIndex, previousNodeIndex);
                int stepsBack = TryToFindSmallerCycle(polygon);
                if (stepsBack > 0) return stepsBack;
                SetSegmentColor(polygon[0], polygon[polygon.Count - 2],
                    Color.Black); //todo rename all: segment xor edge

                List<int> edges = new List<int>();
                for (int i = 0; i < polygon.Count - 1; i++)
                    edges.Add(GetSegmentId(polygon[i], polygon[i + 1]));
                _polygons.Add(edges);

                return 0;
            }

            _parents[currentNodeIndex] = previousNodeIndex;
            _colors[currentNodeIndex] = Color.Gray;
            List<int> nextConnections = _connections[currentNodeIndex];
            for (int i = 0; i < nextConnections.Count; i++)
            {
                int nextNodeIndex = nextConnections[i];
                if (_parents[currentNodeIndex] != nextNodeIndex &&
                    GetSegmentColor(currentNodeIndex, nextNodeIndex) != Color.Black)
                {
                    int stepsBack = LookForCycles(currentNodeIndex, nextNodeIndex);
                    if (stepsBack > 0)
                    {
                        _colors[currentNodeIndex] = Color.White;
                        return stepsBack - 1;
                    }
                }
            }

            _colors[currentNodeIndex] = Color.Black;
            return 0;
        }

        private List<int> BuildPolygon(int lastNodeIndex, int prevNodeIndex)
        {
            List<int> polygon = new List<int>();

            int current = prevNodeIndex;
            polygon.Add(lastNodeIndex);
            while (current != lastNodeIndex)
            {
                polygon.Add(current);
                current = _parents[current];
            }

            polygon.Add(lastNodeIndex);

            return polygon;
        }

        private int TryToFindSmallerCycle(List<int> polygon)
        {
            for (int i = 1; i < polygon.Count - 2; i++)
            {
                int currentNode = polygon[i];
                List<int> nextNodes = _connections[currentNode];
                for (int j = 0; j < nextNodes.Count; j++)
                {
                    int nextNode = nextNodes[j];
                    if (_colors[nextNode] == Color.Black ||
                        polygon[i - 1] == nextNode ||
                        polygon[i + 1] == nextNode ||
                        GetSegmentColor(currentNode, nextNode) == Color.Black)
                        continue;

                    Coordinate c0 = _coordinates[currentNode];
                    Coordinate c1 = _coordinates[nextNode];
                    float x = (c0.X + c1.X) / 2f;
                    float y = (c0.Y + c1.Y) / 2f;
                    if (!IsPointInside(polygon, x, y))
                        continue;

                    var localColors = new Color[_colors.Length];
                    localColors[currentNode] = Color.Gray;
                    if (IsConnectedInside(nextNode, localColors))
                    {
                        Swap(nextNodes, 0, j);
                        return polygon.Count - i;
                    }
                }
            }

            return 0;
        }

        private bool IsConnectedInside(int currentNodeIndex, Color[] localColors)
        {
            if (_colors[currentNodeIndex] == Color.Gray || localColors[currentNodeIndex] == Color.Gray)
                return true;

            localColors[currentNodeIndex] = Color.Gray;
            List<int> nextNodes = _connections[currentNodeIndex];
            for (int i = 0; i < nextNodes.Count; i++)
            {
                int nextNodeIndex = nextNodes[i];
                if (localColors[nextNodeIndex] == Color.Black) continue;
                if (IsConnectedInside(nextNodeIndex, localColors))
                {
                    Swap(nextNodes, 0, i);
                    return true;
                }
            }

            localColors[currentNodeIndex] = Color.Black;
            return false;
        }

        private static void Swap(List<int> list, int index0, int index1)
        {
            if (index0 == index1) return;
            int tmp = list[index0];
            list[index0] = list[index1];
            list[index1] = tmp;
        }

        /*private bool HasDirectConnectionInside(List<int> polygon)
        {
            HashSet<int> checkedNodes = new HashSet<int>();
            HashSet<int> polygonNodes = new HashSet<int>(polygon);
            for (int i = 0; i < polygon.Count - 2; i++)
            {
                int node = polygon[i];
                for (int j = 0; j < _connections[i].Count; j++)
                {
                    int connectedNode = _connections[i][j];
                    if (!polygonNodes.Contains(connectedNode) || checkedNodes.Contains(connectedNode))
                        continue;

                    Coordinate c0 = _coordinates[node];
                    Coordinate c1 = _coordinates[connectedNode];
                    float x = (c0.X + c1.X) / 2f;
                    float y = (c0.Y + c1.Y) / 2f;
                    if (IsPointInside(polygon, x, y))
                        return true;
                }
                    
                checkedNodes.Add(node);
            }

            return false;
        }*/

        private bool IsPointInside(List<int> polygon, float x, float y)
        {
            // Ray casting algorithm
            int crosses = 0;
            for (int i = 0; i < polygon.Count - 1; i++)
            {
                int node0 = polygon[i];
                int node1 = polygon[i + 1];
//                if (GetSegmentColor(node0, node1) == Color.Black) continue; //todo ???
                Coordinate c0 = _coordinates[node0];
                Coordinate c1 = _coordinates[node1];
                if (c0.X > x && c1.X > x || // on the right
                    c0.Y > y && c1.Y > y || // above
                    c0.Y < y && c1.Y < y) // below
                    continue;

                if (c0.X == c1.X) // vertical
                {
                    crosses++;
                    continue;
                }

                float crossPointX = 1f * (c0.X - c1.X) * (y - c1.Y) / (c0.Y - c1.Y) + c1.X;
                if (crossPointX < x)
                    crosses++;
            }

            return crosses % 2 == 1;
        }

        /*private bool IsAnyConnectedNodeInside(List<int> polygon)
        {
            int group = _groups[polygon[0]];
            for (int nodeIndex = 0; nodeIndex < _connections.Count; nodeIndex++)
            {
                if (_groups[nodeIndex] != group || polygon.Contains(nodeIndex))
                    continue;
                
                Coordinate nodeCoordinate = _coordinates[nodeIndex];
                if (IsPointInside(polygon, nodeCoordinate.X, nodeCoordinate.Y))
                    return true;
            }

            return false;
        }*/

        private Color GetSegmentColor(int nodeIndex1, int nodeIndex2)
        {
            int segmentId = GetSegmentId(nodeIndex1, nodeIndex2);
            Color color;
            return _segmentsColors.TryGetValue(segmentId, out color) ? color : Color.White;
        }

        private void SetSegmentColor(int nodeIndex1, int nodeIndex2, Color color)
        {
            int segmentId = GetSegmentId(nodeIndex1, nodeIndex2);
            if (_segmentsColors.ContainsKey(segmentId))
                _segmentsColors[segmentId] = color;
            else
                _segmentsColors.Add(segmentId, color);
        }

        private static int GetSegmentId(int nodeIndex1, int nodeIndex2)
        {
            int firstNodeIndex = Math.Min(nodeIndex1, nodeIndex2);
            int secondNodeIndex = Math.Max(nodeIndex1, nodeIndex2);
            return firstNodeIndex * 100 + secondNodeIndex;
        }

        private void ReadData()
        {
            _connections.Clear();
            _coordinates.Clear();
            _polygons.Clear();
            _segmentsColors.Clear();
            _numberOfEdges = int.Parse(_iIOInterface.ReadLine());
            Dictionary<int, int> nodesDictionary = new Dictionary<int, int>(); // key - coordinates, value - index
            int nodeIndex = -1;

            Func<int, int, int> getNodeIndex = (x, y) =>
            {
                int key = x * 1000 + y;
                int index;
                if (!nodesDictionary.TryGetValue(key, out index))
                {
                    index = ++nodeIndex;
                    nodesDictionary.Add(key, index);
                    _coordinates.Add(new Coordinate(x, y));
                }

                return index;
            };

            for (int i = 0; i < _numberOfEdges; i++)
            {
                string[] inputs = _iIOInterface.ReadLine().Split(' ');
                int x1 = int.Parse(inputs[0]);
                int y1 = int.Parse(inputs[1]);
                int x2 = int.Parse(inputs[2]);
                int y2 = int.Parse(inputs[3]);

                int nodeIndex1 = getNodeIndex(x1, y1);
                int nodeIndex2 = getNodeIndex(x2, y2);
                SetConnection(nodeIndex1, nodeIndex2);
            }

            int nodes = nodeIndex + 1;
            _colors = new Color[nodes];
            _parents = new int[nodes];
            _groups = new int[nodes];
            SetGroups();
        }

        private void SetGroups()
        {
            int groupsCounter = 0;
            for (int i = 0; i < _groups.Length; i++)
            {
                if (_groups[i] != 0)
                    continue;

                groupsCounter++;
                List<int> vertices = new List<int> {i};
                while (vertices.Count > 0)
                {
                    _groups[vertices[0]] = groupsCounter;
                    vertices.RemoveAt(0);
                    for (int j = 0; j < _connections[i].Count; j++)
                    {
                        int connectedNodeIndex = _connections[i][j];
                        if (_groups[connectedNodeIndex] == 0)
                            vertices.Add(connectedNodeIndex);
                    }
                }
            }
        }

        private void SetConnection(int i0, int i1)
        {
            int maxIndex = Math.Max(i0, i1);
            while (_connections.Count <= maxIndex)
                _connections.Add(new List<int>());
            _connections[i0].Add(i1);
            _connections[i1].Add(i0);
        }

        public interface IIOInterface
        {
            string ReadLine();
            void WriteLine(string value);
        }

        private class ConsoleInterface : IIOInterface
        {
            public string ReadLine()
            {
                string line = Console.ReadLine();
                Console.Error.WriteLine("Input: " + line);
                return line;
            }

            public void WriteLine(string value)
            {
                Console.WriteLine(value);
            }
        }

        private enum Color
        {
            White,
            Gray,
            Black
        }

        private class Coordinate
        {
            public int X { get; }
            public int Y { get; }

            public Coordinate(int x, int y)
            {
                X = x;
                Y = y;
            }

            public override string ToString()
            {
                return X + ", " + Y;
            }
        }
    }
}