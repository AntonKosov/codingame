using System;
using CodinGame.VeryHard;
using Xunit;

namespace Tests.VeryHard
{
    public class CrossTheLinesTests
    {
        [Fact]
        public void Line()
        {
            var io = new IOInterface(
                "1\n" +
                "0 0 10 10");
            var crossTheLine = new CrossTheLines(io);
            
            crossTheLine.Run();
            
            Assert.Equal("1", io.Output);
        }

        [Fact]
        public void TwoIndependentLines()
        {
            var io = new IOInterface(
                "2\n" +
                "0 0 10 10\n" +
                "1 1 11 11");
            var crossTheLine = new CrossTheLines(io);
            
            crossTheLine.Run();
            
            Assert.Equal("2", io.Output);
        }

        [Fact]
        public void Triangle()
        {
            var io = new IOInterface(
                "3\n" +
                "0 0 10 10\n" +
                "10 10 10 0\n" +
                "0 0 10 0");
            var crossTheLine = new CrossTheLines(io);
            
            crossTheLine.Run();
            
            Assert.Equal("4", io.Output);
        }

        [Fact]
        public void TriangleAndTail()
        {
            var io = new IOInterface(
                "4\n" +
                "0 0 10 10\n" +
                "10 10 10 0\n" +
                "0 0 10 0\n" +
                "0 0 0 10");
            var crossTheLine = new CrossTheLines(io);
            
            crossTheLine.Run();
            
            Assert.Equal("5", io.Output);
        }

        [Fact]
        public void Square()
        {
            var io = new IOInterface(
                "4\n" +
                "0 0 0 10\n" +
                "0 10 10 10\n" +
                "10 10 10 0\n" +
                "10 0 0 0");
            var crossTheLine = new CrossTheLines(io);
            
            crossTheLine.Run();
            
            Assert.Equal("4", io.Output);
        }

        [Fact]
        public void Star()
        {
            var io = new IOInterface(
                "3\n" +
                "0 0 0 1\n" +
                "0 0 1 0\n" +
                "0 0 1 1");
            var crossTheLine = new CrossTheLines(io);
            
            crossTheLine.Run();
            
            Assert.Equal("3", io.Output);
        }

        [Fact]
        public void OneSquareOneTriangle()
        {
            var io = new IOInterface(
                "6\n" +
                "0 0 0 10\n" +
                "0 10 10 10\n" +
                "10 10 10 0\n" +
                "10 0 0 0\n" +
                "10 10 20 5\n" +
                "20 5 10 0");
            var crossTheLine = new CrossTheLines(io);
            
            crossTheLine.Run();
            
            Assert.Equal("7", io.Output);
        }

        private class IOInterface : CrossTheLines.IIOInterface
        {
            private readonly string[] _data;

            private int _index = 0;

            public string Output { get; private set; }

            public IOInterface(string data)
            {
                _data = data.Split("\n");
            }

            public string ReadLine()
            {
                if (_index > _data.Length - 1)
                    throw new IndexOutOfRangeException();
                return _data[_index++];
            }

            public void WriteLine(string value)
            {
                Output += value;
            }
        }
    }
}