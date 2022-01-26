package main

import (
	"context"
	"fmt"
	"math"
	"os"
	"time"
)

// TODOs:
// 1. Hunt zombies even if the game is lose
// 2. Reduce the number of variants
// 3. Collect more score
// 4. If there are winners, leave only the best
func main() {
	ctx := context.Background()
	for {
		process(ctx)
	}
}

func process(ctx context.Context) {
	var x, y int
	fmt.Scan(&x, &y)
	player := NewVector(x, y)

	var humanCount int
	fmt.Scan(&humanCount)
	humans := make(VectorMap, humanCount)
	for i := 0; i < humanCount; i++ {
		var humanId, humanX, humanY int
		fmt.Scan(&humanId, &humanX, &humanY)
		humans[NewVector(humanX, humanY)] = struct{}{}
	}

	var zombiesCount int
	fmt.Scan(&zombiesCount)
	zombies := make(VectorMap, zombiesCount)
	for i := 0; i < zombiesCount; i++ {
		var zombieId, zombieX, zombieY, zombieXNext, zombieYNext int
		fmt.Scan(&zombieId, &zombieX, &zombieY, &zombieXNext, &zombieYNext)
		zombies[NewVector(zombieX, zombieY)] = struct{}{}
	}

	fmt.Fprintf(os.Stderr, "Player: %+v; humans: %v; zombies: %v\n", player, len(humans), len(zombies))
	state := NewState(player, humans, zombies)

	ctx, cancel := context.WithTimeout(ctx, Timeout)
	defer cancel()

	destination := findOptimalDestination(ctx, state)

	// fmt.Fprintln(os.Stderr, "Debug messages...")
	fmt.Printf("%v %v\n", destination.X, destination.Y) // Your destination coordinates
}

const (
	Timeout          = 100 * time.Millisecond
	PlayerMaxStep    = 1000
	ZombieStep       = 400
	DestroyDistance  = 2000
	DestroyDistance2 = DestroyDistance * DestroyDistance
	MaxZombies       = 99
)

type Vector struct {
	X, Y int
}

func NewVector(x, y int) Vector {
	return Vector{X: x, Y: y}
}

func (v Vector) Add(av Vector) Vector {
	return Vector{X: v.X + av.X, Y: v.Y + av.Y}
}

func (v Vector) Sub(av Vector) Vector {
	return Vector{X: v.X - av.X, Y: v.Y - av.Y}
}

func (v Vector) Scale(f float32) Vector {
	return Vector{
		X: int(float32(v.X) * f),
		Y: int(float32(v.Y) * f),
	}
}

func (v Vector) Len() int {
	return int(math.Sqrt(float64(v.Len2())))
}

func (v Vector) Len2() int {
	return v.X*v.X + v.Y*v.Y
}

func moveTo(start, destination Vector, maxLen int) Vector {
	offset := destination.Sub(start)
	if offset.Len2() <= maxLen*maxLen {
		return destination
	}

	f := float32(maxLen) / float32(offset.Len())
	offset = offset.Scale(f)

	return start.Add(offset)
}

type VectorMap map[Vector]struct{}

func (vl VectorMap) Clone() VectorMap {
	clone := make(VectorMap, len(vl))
	for v := range vl {
		clone[v] = struct{}{}
	}
	return clone
}

var bonusScoreMul []int

func init() {
	fib := make([]int, MaxZombies+1)
	fib[1] = 1
	fib[2] = 2
	for i := 3; i <= MaxZombies; i++ {
		fib[i] = fib[i-2] + fib[i-1]
	}

	bonusScoreMul = make([]int, MaxZombies+1)
	bonusScoreMul[1] = 1
	for i := 2; i <= MaxZombies; i++ {
		bonusScoreMul[i] = bonusScoreMul[i-1] + fib[i]
	}
}

// type Result int
//
// const (
// 	ResultUnknown Result = 0
// 	ResultWin     Result = 1
// 	ResultLose    Result = 2
// )

type State struct {
	player  Vector
	humans  VectorMap
	zombies VectorMap

	initialTarget *Vector
	score         int
	turns         int
	// result        Result
}

func NewState(player Vector, humans, zombies VectorMap) State {
	return State{
		player:  player,
		humans:  humans,
		zombies: zombies,
	}
}

func (s *State) Clone() State {
	return State{
		player:  s.player,
		humans:  s.humans.Clone(),
		zombies: s.zombies.Clone(),

		initialTarget: s.initialTarget,
		score:         s.score,
		turns:         s.turns,
	}
}

func (s *State) moveZombies() {
	moved := make(VectorMap, len(s.zombies))
	for z := range s.zombies {
		closest := s.player
		dist2 := z.Sub(closest).Len2()
		for h := range s.humans {
			hd2 := z.Sub(h).Len2()
			if dist2 > hd2 {
				closest = h
				dist2 = hd2
			}
		}
		nc := moveTo(z, closest, ZombieStep)
		moved[nc] = struct{}{}
	}
	s.zombies = moved
}

func (s *State) movePlayer(destination Vector) {
	s.player = moveTo(s.player, destination, PlayerMaxStep)
}

func (s *State) destroyZombies() int {
	destroyed := make([]Vector, 0, len(s.zombies))
	for z := range s.zombies {
		if s.player.Sub(z).Len2() <= DestroyDistance2 {
			destroyed = append(destroyed, z)
		}
	}
	for _, dz := range destroyed {
		delete(s.zombies, dz)
	}

	// bonus(count(killed zombies))*10*count(alive people)^2
	return bonusScoreMul[len(destroyed)] * 10 * len(s.humans) * len(s.humans)
}

func (s *State) killHumans() {
	for z := range s.zombies {
		delete(s.humans, z)
	}
}

func findOptimalDestination(ctx context.Context, state State) Vector {
	states := []State{state}
	var winState *State
	totalVariants := 0
	depth := 0
timeout:
	for len(states) > 0 {
		nextStates := make([]State, 0, len(states))
		depth++
		for _, s := range states {
			select {
			case <-ctx.Done():
				fmt.Fprintln(os.Stderr, "Timeout")
				states = append(states, nextStates...)
				break timeout
			default:
				newStates := tryVariants(ctx, s)
				for _, s := range newStates {
					s := s
					if len(s.zombies) == 0 {
						if winState == nil || (winState.score < s.score ||
							(winState.score == s.score && winState.turns > s.turns)) {
							winState = &s
						}
					} else {
						nextStates = append(nextStates, s)
					}
				}
				totalVariants += len(newStates)
			}
		}
		states = nextStates
	}

	fmt.Fprintf(os.Stderr, "Total variants: %v; Non winners: %v; Depth: %v; Winner: %v\n",
		totalVariants, len(states), depth, winState != nil,
	)
	if winState != nil {
		states = append(states, *winState)
	}
	var bestDestination Vector
	maxScore := -1
	for _, s := range states {
		if maxScore < s.score {
			maxScore = s.score
			if s.initialTarget == nil {
				fmt.Fprintln(os.Stderr, "Initial target is nil")
				continue
			}
			bestDestination = *s.initialTarget
		}
	}

	return bestDestination
}

func tryVariants(ctx context.Context, state State) []State {
	state.moveZombies()
	nextStates := make([]State, 0, len(state.humans)+len(state.zombies))

	tryTarget := func(destination Vector) {
		stateClone := state.Clone()
		stateClone.movePlayer(destination)
		stateClone.score += stateClone.destroyZombies()
		stateClone.turns++
		stateClone.killHumans()

		if len(stateClone.humans) == 0 {
			return
		}

		if stateClone.initialTarget == nil {
			stateClone.initialTarget = &destination
		}
		nextStates = append(nextStates, stateClone)
	}

	for h := range state.humans {
		select {
		case <-ctx.Done():
			return nextStates
		default:
			tryTarget(h)
		}
	}

	for z := range state.zombies {
		select {
		case <-ctx.Done():
			return nextStates
		default:
			tryTarget(z)
		}
	}

	return nextStates
}
