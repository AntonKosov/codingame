package main

import (
	"context"
	"testing"

	"github.com/stretchr/testify/require"
)

func runSimulation(t *testing.T, startState *State, survivors, minScore int) {
	state := startState
	for len(state.zombies) > 0 {
		ctx, cancel := context.WithTimeout(context.Background(), Timeout)
		state = findNextState(ctx, state)
		require.True(t, len(state.humans) > 0)
		cancel()
	}

	require.Equal(t, survivors, len(state.humans))
	require.GreaterOrEqual(t, state.score, minScore)
}

func TestMoveTo1(t *testing.T) {
	z := NewVector(100, 100)
	p := moveTo(z, NewVector(600, 600), 400)
	require.Equal(t, NewVector(382, 382), p)
}

func TestMoveTo2(t *testing.T) {
	p := NewVector(2994, 3358)
	target := NewVector(3208, 2381)
	p = moveTo(p, target, PlayerMaxStep)
	require.Equal(t, NewVector(3207, 2382), p)
}

func TestMoveTo3(t *testing.T) {
	p := NewVector(8250, 8999)
	target := NewVector(8250, 4500)
	p = moveTo(p, target, ZombieStep)
	require.Equal(t, NewVector(8250, 8999-ZombieStep), p)
}

func TestEarningScoreZeroZombies(t *testing.T) {
	require.Equal(t, 0, Score(1, 0))
}

func TestEarningScoreOneZombieOneHuman(t *testing.T) {
	require.Equal(t, 10, Score(1, 1))
}

func TestEarningScoreTwoZombieOneHuman(t *testing.T) {
	require.Equal(t, 30, Score(1, 2))
}

// func TestShouldSurviveAfterOneStep(t *testing.T) {
// 	player := NewVector(0, 0)
// 	humans := Humans{NewVector(3000, 0): struct{}{}}
// 	zombies := Zombies{NewVector(3000+ZombieStep/2, 0): 1}
// 	state := NewState(player, humans, zombies)
// 	state.prepareMovements()
// 	require.Equal(t, 1, state.humansMaySurvive)
// }
//
// func TestShouldSurviveInTest1(t *testing.T) {
// 	player := NewVector(0, 0)
// 	humans := Humans{NewVector(8250, 4500): struct{}{}}
// 	zombies := Zombies{NewVector(8250, 8999): 1}
// 	state := NewState(player, humans, zombies)
// 	state.prepareMovements()
// 	require.Equal(t, 1, state.humansMaySurvive)
// }
//
// func TestShouldSurviveAfterThreeSteps(t *testing.T) {
// 	player := NewVector(0, 0)
// 	humans := Humans{NewVector(5000, 0): struct{}{}}
// 	zombies := Zombies{NewVector(5000-3*ZombieStep, 1): 1}
// 	state := NewState(player, humans, zombies)
// 	state.prepareMovements()
// 	require.Equal(t, 1, state.humansMaySurvive)
// }
//
// func TestShouldNotSurviveAfterThreeSteps(t *testing.T) {
// 	player := NewVector(0, 0)
// 	humans := Humans{NewVector(5000, 0): struct{}{}}
// 	zombies := Zombies{NewVector(5000, 2*ZombieStep-ZombieStep/2): 1}
// 	state := NewState(player, humans, zombies)
// 	state.prepareMovements()
// 	require.Equal(t, 0, state.humansMaySurvive)
// }

func TestCase1(t *testing.T) {
	humans := Humans{NewVector(8250, 4500): struct{}{}}
	zombies := Zombies{NewVector(8250, 8999): 1}
	player := NewVector(0, 0)
	state := NewState(player, humans, zombies)
	runSimulation(t, state, 1, 10)
}

func TestCase2(t *testing.T) {
	humans := Humans{
		NewVector(950, 6000):  struct{}{},
		NewVector(8000, 6100): struct{}{},
	}
	zombies := Zombies{
		NewVector(3100, 7000):  1,
		NewVector(11500, 7100): 1,
	}
	player := NewVector(0, 0)
	state := NewState(player, humans, zombies)
	runSimulation(t, state, 2, 80)
}

func TestCase4(t *testing.T) {
	humans := Humans{NewVector(8000, 4500): struct{}{}}
	zombies := Zombies{
		NewVector(2000, 6500):  1,
		NewVector(14000, 6500): 1,
	}
	player := NewVector(8000, 2000)
	state := NewState(player, humans, zombies)
	runSimulation(t, state, 1, 30)
}
