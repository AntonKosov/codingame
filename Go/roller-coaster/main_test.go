package main

import (
	"testing"

	"github.com/stretchr/testify/require"
)

func Test01(t *testing.T) {
	actual := solve(3, 3, []int{3, 1, 1, 2})
	require.Equal(t, 7, actual)
}

func Test04(t *testing.T) {
	actual := solve(10000, 10, []int{100, 200, 300, 400, 500})
	require.Equal(t, 15000, actual)
}
