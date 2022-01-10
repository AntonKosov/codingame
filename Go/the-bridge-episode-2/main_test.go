package main

import (
	"testing"

	"github.com/stretchr/testify/require"
)

func TestImmediateDown(t *testing.T) {
	r := newRoad([...]string{
		".............................0..0....",
		".0.0..................000....000.....",
		"....000.........0.0...000............",
		"............0.0......................",
	})
	b := [lanes]bike{}
	b[0] = bike{lane: 2, activated: true}
	s := newState(0, 4, "", b, 0, r.len())
	_, actual := nextCommand(r, s, 1)
	require.Equal(t, "DOWN", actual.command)
}

func TestImmediateUp(t *testing.T) {
	r := newRoad([...]string{
		".............................0..0....",
		"....000.........0.0...000............",
		".0.0..................000....000.....",
		"............0.0......................",
	})
	b := [lanes]bike{}
	b[0] = bike{lane: 1, activated: true}
	s := newState(0, 4, "", b, 0, r.len())
	_, actual := nextCommand(r, s, 1)
	require.Equal(t, "UP", actual.command)
}

func playAll(t *testing.T, s state, r road, mustSurvive int) {
	for i := 0; i < 50; i++ {
		_, s = nextCommand(r, s, mustSurvive)
		if s.isFinished {
			return
		}
	}
	t.Fail()
}

func TestWellWornRoad(t *testing.T) {
	r := newRoad([...]string{
		"................000000000........00000........000.............00.",
		".0.0..................000....000......0.0..................00000.",
		"....000.........0.0...000................000............000000.0.",
		"............0.000000...........0000...............0.0.....000000.",
	})
	b := [lanes]bike{}
	b[0] = bike{lane: 1, activated: true}
	b[1] = bike{lane: 2, activated: true}
	s := newState(0, 1, "", b, 0, r.len())
	playAll(t, s, r, 1)
}

func TestWellWornRoad2(t *testing.T) {
	r := newRoad([...]string{
		"............0.000000...........0000...............0.0.....000000.",
		"....000.........0.0...000................000............000000.0.",
		".0.0..................000....000......0.0..................00000.",
		"................000000000........00000........000.............00.",
	})
	b := [lanes]bike{}
	b[0] = bike{lane: 1, activated: true}
	b[1] = bike{lane: 2, activated: true}
	s := newState(0, 1, "", b, 0, r.len())
	playAll(t, s, r, 1)
}

func TestMandatorySacrifices(t *testing.T) {
	r := newRoad([...]string{
		"...0........0........0000.....",
		"....00......0.0...............",
		".....000.......00.............",
		".............0.0..............",
	})
	b := [lanes]bike{}
	for i := range b {
		b[i] = bike{lane: i, activated: true}
	}
	s := newState(0, 3, "", b, 0, r.len())
	playAll(t, s, r, 1)
}
