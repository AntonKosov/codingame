using System;
using System.Collections.Generic;

namespace CodinGame.VeryHard
{
    public class CrossTheLines
    {
        static void Main()
        {
            var crossTheLines = new CrossTheLines(new ConsoleInterface());
            crossTheLines.Run();
        }

        private readonly IIOInterface _iIOInterface;

        private bool[,] _graph;

        private int _numberOfNodes;

        public CrossTheLines(IIOInterface iIOInterface)
        {
            if (iIOInterface == null)
                throw new ArgumentNullException(nameof(iIOInterface));
            _iIOInterface = iIOInterface;
        }

        public void Run()
        {
            ReadData();

            int numberOfCrosses = Solve();

            _iIOInterface.WriteLine(numberOfCrosses.ToString());
        }

        private int Solve()
        {
            int result = 0;
            bool[] visitedNodes = new bool[_numberOfNodes];

            foreach (int node1 in Nodes())
            {
                foreach (int node2 in Nodes())
                    if (IsConnected(node1, node2) && !visitedNodes[node1])
                    {
                        int segmentsInCircle = 0;
                        int lastNodeIndex = -1;
                        result += GetNumberOfIntersections(node1, ref visitedNodes, ref segmentsInCircle,
                            ref lastNodeIndex);
                    }
            }

            return result;
        }

        private int GetNumberOfIntersections(int previousNode, ref bool[] visitedNodes, ref int segmentsInCircle,
            ref int lastNode)
        {
            int crosses = 0;
            visitedNodes[previousNode] = true;
            
            foreach (int node in Nodes())
            {
                if (!IsConnected(previousNode, node)) continue;

                crosses++;
                SetConnection(previousNode, node, false);

                if (visitedNodes[node]) // circle
                {
                    lastNode = node;
                    segmentsInCircle = 1;
                    break;
                }

                crosses += GetNumberOfIntersections(node, ref visitedNodes, ref segmentsInCircle, ref lastNode);
                if (lastNode < 0) continue;
                
                segmentsInCircle++;
                if (lastNode != previousNode) break; // come back in order to find the last node of circle
                
                // found the last node of circle
                if ((segmentsInCircle & 1) == 1) crosses++; // odd number of segments
                
                // reset counters
                segmentsInCircle = 0;
                lastNode = -1;
            }

            visitedNodes[previousNode] = false;
            
            return crosses;
        }

        private IEnumerable<int> Nodes()
        {
            for (int i = 0; i < _numberOfNodes; i++)
                yield return i;
        }

        private void ReadData()
        {
            int numberOfSegments = int.Parse(_iIOInterface.ReadLine());
            _graph = new bool[numberOfSegments * 2, numberOfSegments * 2];
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

                SetConnection(getNodeIndex(x1, y1), getNodeIndex(x2, y2), true);
            }

            _numberOfNodes = nodeIndex + 1;
        }

        private void SetConnection(int i1, int i2, bool isConnected)
        {
            _graph[i1, i2] = isConnected;
            _graph[i2, i1] = isConnected;
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

        public class ConsoleInterface : IIOInterface
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
    }
}