package main

import (
	"context"
	"testing"

	"github.com/stretchr/testify/require"
)

func runEmulation(t *testing.T, startState State, survivors, minScore int) {
	state := startState
	score := 0
	for len(state.zombies) > 0 {
		ctx, cancel := context.WithTimeout(context.Background(), Timeout)
		nextState := findOptimalDestination(ctx, state)
		score += nextState.score
		require.True(t, len(state.humans) > 0)
		state = NewState(nextState.player, nextState.humans, nextState.zombies)
		cancel()
	}

	require.Equal(t, survivors, len(state.humans))
	require.GreaterOrEqual(t, score, minScore)
}

func TestMoveTo(t *testing.T) {
	z := NewVector(100, 100)
	p := moveTo(z, NewVector(600, 600), 400)
	require.Equal(t, NewVector(382, 382), p)
}

func TestEarningScoreZeroZombies(t *testing.T) {
	require.Equal(t, 0, earnedScore(1, 0))
}

func TestEarningScoreOneZombieOneHuman(t *testing.T) {
	require.Equal(t, 10, earnedScore(1, 1))
}

func TestEarningScoreTwoZombieOneHuman(t *testing.T) {
	require.Equal(t, 30, earnedScore(1, 2))
}

func TestShouldSurviveAfterOneStep(t *testing.T) {
	player := NewVector(0, 0)
	humans := Humans{NewVector(3000, 0): struct{}{}}
	zombies := Zombies{NewVector(3000+ZombieStep/2, 0): 1}
	state := NewState(player, humans, zombies)
	state.prepareMovements()
	require.Equal(t, 1, state.humansMaySurvive)
}

func TestShouldSurviveInTest1(t *testing.T) {
	player := NewVector(0, 0)
	humans := Humans{NewVector(8250, 4500): struct{}{}}
	zombies := Zombies{NewVector(8250, 8999): 1}
	state := NewState(player, humans, zombies)
	state.prepareMovements()
	require.Equal(t, 1, state.humansMaySurvive)
}

func TestShouldSurviveAfterThreeSteps(t *testing.T) {
	player := NewVector(0, 0)
	humans := Humans{NewVector(5000, 0): struct{}{}}
	zombies := Zombies{NewVector(5000-3*ZombieStep, 1): 1}
	state := NewState(player, humans, zombies)
	state.prepareMovements()
	require.Equal(t, 1, state.humansMaySurvive)
}

func TestShouldNotSurviveAfterThreeSteps(t *testing.T) {
	player := NewVector(0, 0)
	humans := Humans{NewVector(5000, 0): struct{}{}}
	zombies := Zombies{NewVector(5000, 2*ZombieStep-ZombieStep/2): 1}
	state := NewState(player, humans, zombies)
	state.prepareMovements()
	require.Equal(t, 0, state.humansMaySurvive)
}

func TestEmulateTest1(t *testing.T) {
	humans := Humans{NewVector(8250, 4500): struct{}{}}
	zombies := Zombies{NewVector(8250, 8999): 1}
	player := NewVector(0, 0)
	state := NewState(player, humans, zombies)
	runEmulation(t, state, 1, 10)
}

func TestEmulateTest2(t *testing.T) {
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
	runEmulation(t, state, 2, 50)
}

func TestEmulateTest4(t *testing.T) {
	humans := Humans{NewVector(8000, 4500): struct{}{}}
	zombies := Zombies{
		NewVector(2000, 6500):  1,
		NewVector(14000, 6500): 1,
	}
	player := NewVector(8000, 2000)
	state := NewState(player, humans, zombies)
	runEmulation(t, state, 1, 30)
}
