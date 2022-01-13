package main

import (
	"strings"
	"testing"

	"github.com/stretchr/testify/require"
)

func testField(t *testing.T, source string, expected string) {
	f := newField(strings.Split(source, "\n")[1:])
	if !f.arrange(0) {
		t.Fail()
	}
	actual := f.grid.buildResult()
	// for _, row := range actual {
	// 	t.Logf("%v\n", row)
	// }
	require.Equal(t, strings.Split(expected, "\n")[1:], actual)
}

func TestExample(t *testing.T) {
	testField(t, `
2.X
X.H
.H1`, `
v..
v..
>.^`,
	)
}

func Test3(t *testing.T) {
	testField(t, `
4..XX
.H.H.
...H.
.2..2
.....`, `
v....
v...<
v^..^
v^.^^
>>>^.`,
	)
}

func Test4(t *testing.T) {
	testField(t, `
3..H.2
.2..H.
..H..H
.X.2.X
......
3..H..`, `
>>>..v
.>>>.v
>>....
^..v..
^..v..
^.....`,
	)
}

func Test9(t *testing.T) {
	testField(t, `
.XXX.5XX4H5............4H..3XXH.2.HX3...
XX4.X..X......3.....HH.2X.....5.....4XX.
X4..X3.X......H...5.....XXXXXXX2.HX2..H.
X..XXXXX.....H3.H.X..22X3XXH.X2X...2HHXH
.X.X.H.X........X3XH.HXX.XXXXX.H..HX..2.
X.HX.X.X....HH....X3.H.X.....H..XXXX3...
X..X.H.X.43......XXH....HXX3..H.X2.HX2..
.XHXXXXX..H3H...H2X.H..3X2..HXX3H.2XXXXH`, `
v<<<<<..v.>>>>>vv<<<<<<<.<<<...<>>..>>>v
v.>>>>v.v^<<<<<vv>>>..<<.v<<<<<^v<<<<..v
vvv<<<v.v>>>>>.vv^>>>>>v.v.....^v..>>v.<
vvv...v.v^>>>.vv.^.v<<vvvv..<.v.v^<<....
vvv.>.v.v^^>>vvv.^..>.vvvv..^.v.>>..^<<^
vv..^.v.v^^^..vvv<<<^.<vv>>>^.<^....>>>^
v>>>^.<.v^^^^<<vv...^<<<...>>>.^.>>..>>v
>>......>>.^.<<<.>>^.<<<.>>>...^.<<.....`,
	)
}

func Test18(t *testing.T) {
	testField(t, `
............
............
............
...3........
........3...
............
.H...HH.....
............
............
...3........
............
............
............`, `
............
............
............
...v........
...v.v<<<...
...v.v......
..<<........
......^.....
......^.....
...>>>^.....
............
............
............`,
	)
}

func Test19(t *testing.T) {
	testField(t, `
5.....X..3...H................HX.....4XH
......X....XXXXX..............XX..2..HXX
......4H........X..4H.H...3..H....4.....
.HH.........H5XX.....H................5.
X............XXXX....X.244.2.X..H.5.....
X.H..........XXXX.......44...X.........5
..............XX4.......3...H.........3.
...3......3..X........X....H.H..........
.......HH.....XXXXX.H.X.......XX....H.XX
3........5....H.H.....X.......HX......XH`, `
>>>>>v...v..>........v<<<>>>v..<<<<<<<..
.....v...v..^........v..^^..v.....>>>..^
..v<<vv..v..^<<<<<<<.v.<^^v.>.v<<<<....^
....^vv^<<...>>>>>v.^..^^^v...v..v<<<<<^
.^<<^vv...>>^.....v.^..^^^vv..v..v>>>>>^
...^^vv...^.......v.^<<<<vvv..>>^vv<<<<<
>>^^^v>>>v^.v<<<<.v..v<<<vv>.....vvv<<<.
^..^^>>v.v^.v..v<<<..v...v>.>.v<<<vv....
^...^....<..v..v.....<...v..^.v...v>....
^...^<<<<<..>>.>.........>>>^.....>>>>>.`,
	)
}
