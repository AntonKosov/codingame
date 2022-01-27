package main

import (
	"context"
	"testing"

	"github.com/stretchr/testify/require"
)

func TestMoveTo(t *testing.T) {
	z := NewVector(100, 100)
	p := moveTo(z, NewVector(600, 600), 400)
	require.Equal(t, NewVector(382, 382), p)
}

func TestSimpleFirstStep(t *testing.T) {
	humans := Humans{NewVector(8250, 4500): struct{}{}}
	zombies := Zombies{NewVector(8250, 8999): 1}
	state := NewState(NewVector(0, 0), humans, zombies)
	destination := findOptimalDestination(context.Background(), state)
	require.NotEqual(t, NewVector(0, 0), destination)
}
