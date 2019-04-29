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

        private NodeColor[] _colors;

        private int[] _parents;

        private int _segments;

        private int _crosses;

        private readonly HashSet<int> _countedSegments = new HashSet<int>();

        public CrossTheLines(IIOInterface iIOInterface)
        {
            if (iIOInterface == null)
                throw new ArgumentNullException(nameof(iIOInterface));
            _iIOInterface = iIOInterface;
        }

        public void Run()
        {
            ReadData();

            _crosses = 0;
            _countedSegments.Clear();
            Solve();

            _iIOInterface.WriteLine(_crosses.ToString());
        }

        private void Solve()
        {
            for (int nodeIndex = 0; nodeIndex < _connections.Count; nodeIndex++)
            {
                if (_colors[nodeIndex] != NodeColor.Black)
                {
                    _colors[nodeIndex] = NodeColor.Gray;
                    List<int> nextConnections = _connections[nodeIndex];
                    for (int i = 0; i < nextConnections.Count; i++)
                    {
                        LookForCycles(nodeIndex, nextConnections[i]);
                    }

                    _colors[nodeIndex] = NodeColor.Black;
                }
            }

            _crosses += _segments - _countedSegments.Count;
        }

        private void LookForCycles(int previousNodeIndex, int currentNodeIndex)
        {
            if (_colors[currentNodeIndex] == NodeColor.Black)
                return;

            if (_colors[currentNodeIndex] == NodeColor.Gray)
            {
                List<int> polygon = BuildPolygon(currentNodeIndex, previousNodeIndex);
                if (IsAnyConnectedNodeInside(polygon))
                    return;

                bool isNewCycle = false;
                for (int i = 0; i < polygon.Count - 1; i++)
                {
                    int node1 = polygon[i];
                    int node2 = polygon[i + 1];
                    isNewCycle |= CountSegment(node1, node2);

                    Console.Error.Write(node1 + " "); // todo remove
                }

                Console.Error.WriteLine(); // todo remove

                if (isNewCycle && (polygon.Count - 1) % 2 == 1)
                    _crosses++;

                return;
            }

            _parents[currentNodeIndex] = previousNodeIndex;
            _colors[currentNodeIndex] = NodeColor.Gray;
            List<int> nextConnections = _connections[currentNodeIndex];
            for (int i = 0; i < nextConnections.Count; i++)
            {
                int nextNodeIndex = nextConnections[i];
                if (_parents[currentNodeIndex] != nextNodeIndex)
                    LookForCycles(currentNodeIndex, nextNodeIndex);
            }

            _colors[currentNodeIndex] = NodeColor.Black;
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

        private bool IsAnyConnectedNodeInside(List<int> polygon)
        {
            // Ray casting algorithm
            int crosses = 0;
            for (int nodeIndex = 0; nodeIndex < _connections.Count; nodeIndex++)
            {
                if (polygon.Contains(nodeIndex))
                    continue;

                Coordinate nodeCoordinate = _coordinates[nodeIndex];
                for (int i = 0; i < polygon.Count - 2; i++)
                {
                    int node0 = polygon[i];
                    int node1 = polygon[i + 1];
                    if (_groups[node0] != _groups[nodeIndex])
                        continue;

                    Coordinate c0 = _coordinates[node0];
                    Coordinate c1 = _coordinates[node1];
                    if (c0.X > nodeCoordinate.X && c1.X > nodeCoordinate.X || // on the right
                        c0.Y > nodeCoordinate.Y && c1.Y > nodeCoordinate.Y || // above
                        c0.Y < nodeCoordinate.Y && c1.Y < nodeCoordinate.Y) // below
                        continue;

                    if (c0.X == c1.X) // vertical
                    {
                        crosses++;
                        continue;
                    }

                    float crossPointX = 1f * (c0.X - c1.X) * (nodeCoordinate.Y - c1.Y) / (c0.Y - c1.Y) + c1.X;
                    if (crossPointX < nodeCoordinate.X)
                        crosses++;
                }
            }

            return crosses % 2 == 1;
        }

        private bool CountSegment(int nodeIndex1, int nodeIndex2)
        {
            int firstNodeIndex = Math.Min(nodeIndex1, nodeIndex2);
            int secondNodeIndex = Math.Max(nodeIndex1, nodeIndex2);
            int segment = firstNodeIndex * 100 + secondNodeIndex;
            if (_countedSegments.Contains(segment))
                return false;

            _crosses++;
            _countedSegments.Add(segment);
            return true;
        }

        private void ReadData()
        {
            _connections.Clear();
            _coordinates.Clear();
            _crosses = 0;
            _segments = int.Parse(_iIOInterface.ReadLine());
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

            for (int i = 0; i < _segments; i++)
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
            _colors = new NodeColor[nodes];
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

        private enum NodeColor
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
        }
    }
}