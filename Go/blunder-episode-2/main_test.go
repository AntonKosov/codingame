package main

import (
	"strings"
	"testing"

	"github.com/stretchr/testify/require"
)

func testRooms(t *testing.T, in string, expectedSum int) {
	lines := strings.Split(in, "\n")[1:]
	input := NewInput(lines)
	actualSum := input.Solve()

	require.Equal(t, expectedSum, actualSum)
}

func Test4Rooms(t *testing.T) {
	testRooms(t, `
0 4 1 3
1 3 0 2
2 2 1 3
3 1 E E`,
		10)
}

func TestExample(t *testing.T) {
	testRooms(t, `
0 17 1 2
1 15 3 4
2 15 4 5
3 20 6 7
4 12 7 8
5 11 8 9
6 18 10 11
7 19 11 12
8 12 12 13
9 11 13 14
10 13 E E
11 14 E E
12 17 E E
13 19 E E
14 15 E E`,
		88)
}

func Test55rooms(t *testing.T) {
	testRooms(t, `
0 46 1 2
1 29 3 4
2 26 4 5
3 34 6 7
4 36 7 8
5 10 8 9
6 21 10 11
7 21 11 12
8 12 12 13
9 16 13 14
10 21 15 16
11 22 16 17
12 38 17 18
13 34 18 19
14 49 19 20
15 21 21 22
16 37 22 23
17 38 23 24
18 31 24 25
19 42 25 26
20 21 26 27
21 48 28 29
22 24 29 30
23 14 30 31
24 23 31 32
25 40 32 33
26 11 33 34
27 22 34 35
28 25 36 37
29 11 37 38
30 13 38 39
31 16 39 40
32 43 40 41
33 24 41 42
34 47 42 43
35 46 43 44
36 15 45 46
37 36 46 47
38 36 47 48
39 31 48 49
40 17 49 50
41 27 50 51
42 22 51 52
43 46 52 53
44 19 53 54
45 34 E E
46 21 E E
47 14 E E
48 16 E E
49 25 E E
50 47 E E
51 37 E E
52 39 E E
53 38 E E
54 32 E E`,
		358)
}
