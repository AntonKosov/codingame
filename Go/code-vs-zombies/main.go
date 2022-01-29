package main

import (
	"context"
	"fmt"
	"math"
	"os"
	"time"
)

const Debug = false

// TODOs:
// * Consider only those states where maximum humans are alive?
// * [Test: Cross] Implement the detection of loosing all humans in the future
// * [Low priority] Hunt zombies even if there is no chance to win
// * Reduce the number of variants
//   * If there are a lot of zombies, try moving by circle (taking into account the field size)
//   * Don't consider saving humans which cannot be saved
// * Collect more score
// * Consider how many people left
// * [Performance] Get rid of copying maps
func main() {
	ctx := context.Background()
	var expectedState *State
	for {
		expectedState = process(ctx, expectedState)
	}
}

func process(ctx context.Context, expectedState *State) *State {
	var x, y int
	fmt.Scan(&x, &y)
	player := NewVector(x, y)

	var humanCount int
	fmt.Scan(&humanCount)
	humans := make(Humans, humanCount)
	for i := 0; i < humanCount; i++ {
		var humanId, humanX, humanY int
		fmt.Scan(&humanId, &humanX, &humanY)
		humans[NewVector(humanX, humanY)] = struct{}{}
	}

	var zombiesCount int
	fmt.Scan(&zombiesCount)
	zombies := make(Zombies, zombiesCount)
	for i := 0; i < zombiesCount; i++ {
		var zombieId, zombieX, zombieY, zombieXNext, zombieYNext int
		fmt.Scan(&zombieId, &zombieX, &zombieY, &zombieXNext, &zombieYNext)
		zombies[NewVector(zombieX, zombieY)]++
	}

	fmt.Fprintf(os.Stderr, "Player: %+v; humans: %v; zombies: %v\n", player, len(humans), len(zombies))
	state := NewState(player, humans, zombies)

	if Debug && expectedState != nil {
		state.ValidateWithExpectedState(expectedState)
	}

	ctx, cancel := context.WithTimeout(ctx, Timeout)
	defer cancel()

	nextState := findOptimalDestination(ctx, state)
	destination := nextState.player //nextState.player.Sub(state.player)

	// fmt.Fprintln(os.Stderr, "Debug messages...")
	fmt.Printf("%v %v\n", destination.X, destination.Y) // Your destination coordinates

	return nextState
}

const (
	Timeout          = 100 * time.Millisecond
	PlayerMaxStep    = 1000
	PlayerMaxStep2   = PlayerMaxStep * PlayerMaxStep
	ZombieStep       = 400
	DestroyDistance  = 2000
	DestroyDistance2 = DestroyDistance * DestroyDistance
	MaxZombies       = 99
	FieldWidth       = 16000
	FieldHeight      = 9000
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
	return Sqrt(v.Len2())
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

type Humans map[Vector]struct{}

func (vl Humans) Clone() Humans {
	clone := make(Humans, len(vl))
	for v := range vl {
		clone[v] = struct{}{}
	}
	return clone
}

type Zombies map[Vector]int

func (z Zombies) Clone() Zombies {
	clone := make(Zombies, len(z))
	for z, c := range z {
		clone[z] = c
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

type State struct {
	player  Vector
	humans  Humans
	zombies Zombies

	firstState       *State
	bestMovements    []Vector
	score            int
	turns            int
	humansMaySurvive int
}

func NewState(player Vector, humans Humans, zombies Zombies) State {
	ns := State{
		player:  player,
		humans:  humans,
		zombies: zombies,
	}
	ns.prepareMovements()
	return ns
}

func (s *State) Clone() State {
	return State{
		player:  s.player,
		humans:  s.humans.Clone(),
		zombies: s.zombies.Clone(),

		firstState: s.firstState,
		score:      s.score,
		turns:      s.turns,
	}
}

func (s *State) moveZombies() {
	moved := make(Zombies, len(s.zombies))
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
		moved[nc]++
	}
	s.zombies = moved
}

func (s *State) safetyDistanceAfterZombiesMove2() map[Vector]int {
	safety := make(map[Vector]int, len(s.humans))
	for h := range s.humans {
		minDist2 := FieldWidth * FieldWidth
		for z := range s.zombies {
			d2 := z.Sub(h).Len2()
			minDist2 = Min(minDist2, d2)
		}
		minDist := Sqrt(minDist2)
		zombieMovements := minDist / ZombieStep
		if minDist%ZombieStep == 0 {
			zombieMovements--
		}
		playerDist := zombieMovements*PlayerMaxStep + DestroyDistance
		safety[h] = playerDist * playerDist
	}

	return safety
}

func (s *State) movePlayer(destination Vector) {
	s.player = moveTo(s.player, destination, PlayerMaxStep)
}

func (s *State) destroyZombies() int {
	count := 0
	destroyed := make([]Vector, 0, len(s.zombies))
	for z, c := range s.zombies {
		if s.player.Sub(z).Len2() <= DestroyDistance2 {
			count += c
			destroyed = append(destroyed, z)
			// fmt.Fprintf(os.Stderr, "Destroyed zombie: dist=%v, player:%+v, z: %+v\n",
			// 	s.player.Sub(z).Len(), s.player, z,
			// )
		}
	}
	for _, dz := range destroyed {
		delete(s.zombies, dz)
	}

	return earnedScore(len(s.humans), count)
}

func earnedScore(humansLeft, zombiesKilled int) int {
	return humansLeft * humansLeft * 10 * bonusScoreMul[zombiesKilled]
}

func (s *State) killHumans() {
	for z := range s.zombies {
		delete(s.humans, z)
	}
}

func findOptimalDestination(ctx context.Context, state State) *State {
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

	if winState != nil {
		states = append(states, *winState)
	}
	var bestState *State
	for _, s := range states {
		s := s
		hms := s.humansMaySurvive
		if bestState != nil && bestState.humansMaySurvive > hms {
			continue
		} else if bestState != nil && bestState.score > s.score {
			continue
		}
		bestState = &s
	}
	fmt.Fprintf(os.Stderr, "Total variants: %v; Non winners: %v; Depth: %v; Survivors: %v, Winner: %v\n",
		totalVariants, len(states), depth, bestState.humansMaySurvive, winState != nil,
	)
	// fmt.Fprintf(os.Stderr, "Player: %+v, zombies: %v\n",
	// 	state.player, len(bestState.zombies),
	// )

	return bestState.firstState
}

const movementAngle = 15

var movements []Vector

func init() {
	movements = make([]Vector, 0, 360/movementAngle+360/movementAngle/2+1)
	zero := NewVector(0, 0)
	movements = append(movements, zero)
	for i := 0; i*movementAngle < 360; i++ {
		angleRad := float64(i*movementAngle) * math.Pi / 180.0
		s := math.Sin(angleRad)
		c := math.Cos(angleRad)
		dir := NewVector(int(c*PlayerMaxStep*10), int(s*PlayerMaxStep*10))
		movements = append(movements, moveTo(zero, dir, PlayerMaxStep))
		if i%2 == 0 {
			movements = append(movements, moveTo(zero, dir, ZombieStep))
		}
	}
}

func (s *State) possibleMovementsTowardCharacters() []Vector {
	movements := make([]Vector, 0, len(s.humans)+len(s.zombies)+1)
	movements = append(movements, s.player)
	for h := range s.humans {
		np := moveTo(s.player, h, PlayerMaxStep)
		movements = append(movements, np)
	}
	for z := range s.zombies {
		np := moveTo(s.player, z, PlayerMaxStep)
		// zombie won't be at this place next turn
		movements = append(movements, np)
	}
	return movements
}

func (s *State) possibleMovementsAround() []Vector {
	movements := make([]Vector, 0, len(s.humans)+len(s.zombies)+1)
	for h := range s.humans {
		np := moveTo(s.player, h, PlayerMaxStep)
		movements = append(movements, np)
	}
	for _, move := range movements {
		movements = append(movements, s.player.Add(move))
	}
	return movements
}

func (s *State) prepareMovements() {
	safetyDistance2 := s.safetyDistanceAfterZombiesMove2()
	draft := make(map[Vector]int, len(movements)+len(safetyDistance2))
	maxSafetyPoints := 0
	survivedHumans := make(map[Vector]struct{}, len(s.humans))
	addPoint := func(pos Vector) {
		if pos.X < 0 || pos.Y < 0 || pos.X >= FieldWidth || pos.Y >= FieldHeight {
			return
		}
		for hPos, sd2 := range safetyDistance2 {
			d2 := pos.Sub(hPos).Len2()
			if d2 > sd2 {
				continue
			}
			points := draft[pos] + 1
			draft[pos] = points
			maxSafetyPoints = Max(maxSafetyPoints, points)
			survivedHumans[hPos] = struct{}{}
		}
	}
	possibleMoves := s.possibleMovementsTowardCharacters()
	for _, p := range possibleMoves {
		addPoint(p)
	}

	moves := make([]Vector, 0, len(draft))
	for p, points := range draft {
		if points == maxSafetyPoints {
			moves = append(moves, p)
		}
	}

	s.humansMaySurvive = len(survivedHumans)

	s.bestMovements = moves
}

func tryVariants(ctx context.Context, state State) []State {
	nextStates := make([]State, 0, len(state.bestMovements))
	state.moveZombies()

	for _, destination := range state.bestMovements {
		select {
		case <-ctx.Done():
			return nextStates
		default:
			stateClone := state.Clone()
			stateClone.movePlayer(destination)
			stateClone.score += stateClone.destroyZombies()
			stateClone.turns++
			stateClone.killHumans()
			stateClone.prepareMovements()
			if stateClone.humansMaySurvive == 0 {
				continue
			}
			if stateClone.firstState == nil {
				stateClone.firstState = &stateClone
			}
			nextStates = append(nextStates, stateClone)
		}
	}

	return nextStates
}

func Min(a, b int) int {
	if a < b {
		return a
	}
	return b
}

func Max(a, b int) int {
	if a > b {
		return a
	}
	return b
}

func Sqrt(v int) int {
	return int(math.Sqrt(float64(v)))
}

func (s *State) ValidateWithExpectedState(e *State) {
	if s.player != e.player {
		panic(fmt.Sprintf("Player position: e=%+v, a=%+v", e.player, s.player))
	}
}
