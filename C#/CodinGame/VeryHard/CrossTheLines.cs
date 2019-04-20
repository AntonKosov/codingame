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

        private bool[,] _graph;

        private Node[] _nodes;

        private int _numberOfNodes;

        private int _numberOfCrosses;

        private Path _path;

        private readonly Queue<Segment> _uncountedSegments = new Queue<Segment>();

        private readonly HashSet<Segment> _countedSegments = new HashSet<Segment>();

        public CrossTheLines(IIOInterface iIOInterface)
        {
            if (iIOInterface == null)
                throw new ArgumentNullException(nameof(iIOInterface));
            _iIOInterface = iIOInterface;
        }

        public void Run()
        {
            ReadData();

            _numberOfCrosses = 0;
            _countedSegments.Clear();
            Solve();

            _iIOInterface.WriteLine(_numberOfCrosses.ToString());
        }

        private void Solve()
        {
            while (_uncountedSegments.Count > 0)
            {
                Segment segment = _uncountedSegments.Dequeue();
                if (_countedSegments.Contains(segment))
                    continue;
                if (LookForCompoundShapeWithStartSegment(segment))
                    LookForSmallerShapes();
            }
        }

        private void LookForSmallerShapes()
        {
            // Count tail's nodes
            while (_path.First != _path.Last)
            {
                int first = _path.RemoveFirst();
                int second = _path.First;
                CountSegment(first, second);
            }

            for (int pathIndex = _path.CountNodes - 2; pathIndex >= 0; pathIndex--)
            {
                HashSet<Segment> visitedSegments = new HashSet<Segment>();
                int startNodeIndex = _path[pathIndex + 1];
                int nextNodeIndex = _path[pathIndex];
                Segment startSegment = new Segment(_nodes[startNodeIndex], _nodes[nextNodeIndex]);
                if (_countedSegments.Contains(startSegment))
                    continue;

                int segments = LookForSmallSegment(startNodeIndex, startNodeIndex, nextNodeIndex, visitedSegments);

                if (segments % 2 == 1)
                    _numberOfCrosses++;
            }

            _path.Clear();
        }

        private int LookForSmallSegment(int startNodeIndex, int prevNodeIndex, int currentNodeIndex,
            HashSet<Segment> visitedSegments)
        {
            CountSegment(prevNodeIndex, currentNodeIndex);
            visitedSegments.Add(new Segment(_nodes[prevNodeIndex], _nodes[currentNodeIndex]));

            while (true)
            {
                int nextStepNodeIndex = GetNextNode(prevNodeIndex, currentNodeIndex, Direction.Left, true, visitedSegments);
                if (nextStepNodeIndex < 0) // Deadlock
                    return -1;

                CountSegment(currentNodeIndex, nextStepNodeIndex);
                if (nextStepNodeIndex == startNodeIndex)
                    return 2;

                int segments = LookForSmallSegment(startNodeIndex, currentNodeIndex, nextStepNodeIndex, visitedSegments);
                if (segments > 0)
                {
                    return segments + 1;
                }
            }
        }
        
        private bool LookForCompoundShapeWithStartSegment(Segment segment)
        {
            _path.AddLast(segment.Node1.Index);
            _path.AddLast(segment.Node2.Index);

            HashSet<int> visitedNodes = new HashSet<int>();
            visitedNodes.Add(segment.Node1.Index);
            visitedNodes.Add(segment.Node2.Index);
            
            HashSet<Segment> ignoredSegments = new HashSet<Segment>();

            while (_path.CountNodes > 1)
            {
                int nextNodeIndex = GetNextNode(_path.Previous, _path.Last, Direction.Right, false, ignoredSegments);
                if (nextNodeIndex < 0) // Deadlock?
                {
                    nextNodeIndex = GetNextNode(_path.Previous, _path.Last, Direction.Left, true, ignoredSegments);
                    if (nextNodeIndex < 0)
                    {
                        int lastNodeIndex = _path.RemoveLast(); // Step back
                        int prevNodeIndex = _path.Last;
                        CountSegment(prevNodeIndex, lastNodeIndex);
                        ignoredSegments.Add(segment);
                        continue;
                    }
                }

                _path.AddLast(nextNodeIndex);
                if (visitedNodes.Contains(nextNodeIndex))
                    return true; // A compound shape was found
                visitedNodes.Add(nextNodeIndex);
            }

            _path.Clear();
            return false;
        }

        private void CountSegment(int nodeIndex1, int nodeIndex2)
        {
            Segment countedSegment = new Segment(_nodes[nodeIndex1], _nodes[nodeIndex2]);
            if (!_countedSegments.Contains(countedSegment))
            {
                _numberOfCrosses++;
                _countedSegments.Add(countedSegment);
            }
        }

        private int GetNextNode(int prevNodeIndex, int lastNodeIndex, Direction direction, bool allowCountedSegments,
            HashSet<Segment> ignoredSegments = null)
        {
            int nextNode = -1;
            float? currentAngle = null;

            Node lastNode = _nodes[lastNodeIndex];
            Vector currentVector = CreateVector(prevNodeIndex, lastNodeIndex);

            for (int nodeIndex = 0; nodeIndex < _numberOfNodes; nodeIndex++)
            {
                Node node = _nodes[nodeIndex];
                if (nodeIndex == prevNodeIndex || !IsConnected(lastNodeIndex, nodeIndex))
                    continue;

                Segment segment = new Segment(lastNode, node);
                if (ignoredSegments != null && ignoredSegments.Contains(segment))
                    continue;

                if (!allowCountedSegments && _countedSegments.Contains(segment))
                    continue;

                Vector nextVector = CreateVector(lastNodeIndex, nodeIndex);
                float angle = currentVector.AngleRad(nextVector);

                if (currentAngle == null ||
                    (direction == Direction.Right ? angle < currentAngle : angle > currentAngle))
                {
                    currentAngle = angle;
                    nextNode = nodeIndex;
                }
            }

            return nextNode;
        }

        private Vector CreateVector(int startNodeIndex, int endNodeIndex)
        {
            Node startNode = _nodes[startNodeIndex];
            Node endNode = _nodes[endNodeIndex];
            return new Vector(startNode.X - endNode.X, startNode.Y - endNode.Y);
        }

        private void ReadData()
        {
            _uncountedSegments.Clear();
            int numberOfSegments = int.Parse(_iIOInterface.ReadLine());
            _graph = new bool[numberOfSegments * 2, numberOfSegments * 2];
            _nodes = new Node[numberOfSegments * 2];
            _path = new Path(_nodes);
            Dictionary<int, int> nodes = new Dictionary<int, int>(); // key - coordinates, value - index
            int nodeIndex = -1;

            Func<int, int, int> getNodeIndex = (x, y) =>
            {
                int key = x * 1000 + y;
                int index;
                if (!nodes.TryGetValue(key, out index))
                {
                    index = ++nodeIndex;
                    nodes.Add(key, index);
                    _nodes[index] = new Node(index, x, y);
                }

                return index;
            };

            for (int i = 0; i < numberOfSegments; i++)
            {
                string[] inputs = _iIOInterface.ReadLine().Split(' ');
                int x1 = int.Parse(inputs[0]);
                int y1 = int.Parse(inputs[1]);
                int x2 = int.Parse(inputs[2]);
                int y2 = int.Parse(inputs[3]);

                int nodeIndex1 = getNodeIndex(x1, y1);
                int nodeIndex2 = getNodeIndex(x2, y2);
                SetConnection(nodeIndex1, nodeIndex2);
                _uncountedSegments.Enqueue(new Segment(_nodes[nodeIndex1], _nodes[nodeIndex2]));
            }

            _numberOfNodes = nodeIndex + 1;
        }

        private void SetConnection(int i1, int i2)
        {
            _graph[i1, i2] = true;
            _graph[i2, i1] = true;
        }

        private bool IsConnected(int i1, int i2)
        {
            return _graph[i1, i2];
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

        private class Segment
        {
            private readonly string _key;

            public Node Node1 { get; }
            public Node Node2 { get; }

            public Segment(Node node1, Node node2)
            {
                Node1 = node1;
                Node2 = node2;

                Func<Node, Node, string> getKey = (n1, n2) => n1.X + "," + n1.Y + "_" + n2.X + "," + n2.Y;
                if (Node1.X < Node2.X)
                    _key = getKey(Node1, Node2);
                else if (Node1.X > Node2.X)
                    _key = getKey(Node2, Node1);
                else
                    _key = Node1.Y < Node2.Y ? getKey(Node1, Node2) : getKey(Node2, Node1);
            }

            public override int GetHashCode()
            {
                return _key.GetHashCode();
            }

            public override bool Equals(object obj)
            {
                Segment anotherSegment = obj as Segment;
                if (anotherSegment == null)
                    return false;
                return _key.Equals(anotherSegment._key);
            }
        }

        private class Node
        {
            public int X { get; }
            public int Y { get; }
            public int Index { get; }

            public Node(int index, int x, int y)
            {
                Index = index;
                X = x;
                Y = y;
            }
        }

        private class Path //todo ?
        {
            private readonly Node[] _nodes; //todo ?

            private readonly List<int> _path = new List<int>();

            public Path(Node[] nodes)
            {
                _nodes = nodes;
            }

            public int this[int index] => _path[index];

            public int CountNodes => _path.Count;

//            public bool IsEmpty => _path.Count == 0;

            public int First => _path[0];

            public int Last => _path[_path.Count - 1];

            public int Previous => _path[_path.Count - 2];

/*
            public Node LastNode => _nodes[Last];

            public Node PreviousNode => _nodes[Previous];
*/

            public void AddLast(int nodeIndex)
            {
                _path.Add(nodeIndex);
            }

            public int RemoveFirst()
            {
                int nodeIndex = _path[0];
                _path.RemoveAt(0);
                return nodeIndex;
            }

            public int RemoveLast()
            {
                int nodeIndex = _path[_path.Count - 1];
                _path.RemoveAt(_path.Count - 1);
                return nodeIndex;
            }

            public void Clear()
            {
                _path.Clear();
            }

/*
            public bool Contains(int nodeIndex)
            {
                for (int i = 0; i < _path.Count - 1; i++)
                {
                    if (_path[i] == nodeIndex)
                        return true;
                }

                return false;
            }
*/
        }

        private enum Direction
        {
            Left,
            Right
        }

        private class Vector
        {
            public float X { get; }
            public float Y { get; }
            public float Z { get; }

            public Vector(float x = 0, float y = 0, float z = 0)
            {
                X = x;
                Y = y;
                Z = z;
            }

            public Vector CrossProduct(Vector vector)
            {
                float x = Y * vector.Z - Z * vector.Y;
                float y = Z * vector.X - X * vector.Z;
                float z = X * vector.Y - Y * vector.X;

                return new Vector(x, y, z);
            }

            public float DotProduct(Vector vector)
            {
                return X * vector.X + Y * vector.Y + Z * vector.Z;
            }

            public float Magnitude => MathF.Sqrt(X * X + Y * Y + Z * Z);

            public float AngleRad(Vector vector)
            {
                float angle = MathF.Acos(DotProduct(vector) / Magnitude / vector.Magnitude);
                if (CrossProduct(vector).Z < 0)
                    angle = -angle;
                return angle;
            }
        }
    }
}