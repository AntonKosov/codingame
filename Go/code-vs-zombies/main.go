package main

import (
	"context"
	"fmt"
	"math"
	"os"
	"time"
)

const Debug = false

// const Debug = true

// TODOs:
// * The simulation is slightly wrong!
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
	var nextState *State
	for {
		nextState = process(ctx, nextState)
	}
}

func process(ctx context.Context, nextState *State) *State {
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

	// fmt.Fprintf(os.Stderr, "Player: %+v; humans: %v; zombies: %v\n", player, len(humans), len(zombies))
	currentState := nextState
	if currentState == nil {
		currentState = NewState(player, humans, zombies)
	} else if Debug {
		actualState := NewState(player, humans, zombies)
		actualState.ValidateWithExpectedState(currentState)
	}

	ctx, cancel := context.WithTimeout(ctx, Timeout)
	defer cancel()

	nextState = findNextState(ctx, currentState)
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

type NextStates struct {
	states         map[Vector]*State // player's position -> State
	bestChildState *State
	isWinner       bool
}

func (ns *NextStates) saveBestChildState(state *State) {
	if ns.bestChildState == nil {
		ns.bestChildState = state
	}
	if ns.bestChildState.Compare(state) >= 0 {
		return
	}
	ns.bestChildState = state
	for pos, s := range ns.states {
		if s.MaxPossibleScore() < state.score {
			delete(ns.states, pos)
		}
	}
	ns.isWinner = state.IsWinner() && len(ns.states) == 1
	if state.parent != nil {
		state.parent.nextStates.saveBestChildState(state)
	}
}

func (ns *NextStates) AddState(state *State) {
	if ns.bestChildState == nil || state.MaxPossibleScore() >= ns.bestChildState.score {
		ns.states[state.player] = state
		ns.saveBestChildState(state)
	}
}

func (ns *NextStates) IsAnalyzed() bool {
	return ns.bestChildState != nil
}

func (ns *NextStates) PickStateToAnalyze() *State {
	var chosenState *State
	for _, s := range ns.states {
		if !s.nextStates.IsAnalyzed() {
			return s
		}
		if s.nextStates.isWinner {
			continue
		}
		if chosenState == nil || chosenState.Compare(s) < 0 {
			s := s
			chosenState = s
		}
	}

	return chosenState
}

type State struct {
	player  Vector
	humans  Humans
	zombies Zombies

	parent *State

	nextStates         NextStates
	turns              int
	score              int
	maxAdditionalScore int
}

func NewState(player Vector, humans Humans, zombies Zombies) *State {
	return &State{
		player:  player,
		humans:  humans,
		zombies: zombies,
		nextStates: NextStates{
			states: make(map[Vector]*State),
		},
	}
}

func (s *State) IsWinner() bool {
	return len(s.zombies) == 0
}

func (s *State) MaxPossibleScore() int {
	return s.score + s.maxAdditionalScore
}

func (s *State) Compare(as *State) int {
	maxPossibleScoreDiff := s.MaxPossibleScore() - as.MaxPossibleScore()
	if maxPossibleScoreDiff != 0 {
		return maxPossibleScoreDiff
	}
	scoreDiff := s.score - as.score
	if scoreDiff != 0 {
		return scoreDiff
	}
	turnsDiff := s.turns - as.turns
	if turnsDiff != 0 {
		return turnsDiff
	}
	w := 0
	if s.IsWinner() {
		w++
	}
	if as.IsWinner() {
		w--
	}
	return w
}

func (s *State) Clone() *State {
	cs := NewState(s.player, s.humans.Clone(), s.zombies.Clone())
	cs.score = s.score
	return cs
}

func (s *State) AddNextState(ns *State) {
	ns.turns = s.turns + 1
	ns.parent = s
	s.nextStates.AddState(ns)
}

func (s *State) moveZombies() {
	//TODO Zombies should cache their targets and check before moving:
	// * if the player is closer
	// * if the human is still there
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

func safetyDistanceAfterZombiesMove2(state *State) map[Vector]int {
	safety := make(map[Vector]int, len(state.humans))
	for h := range state.humans {
		minDist2 := FieldWidth * FieldWidth
		for z := range state.zombies {
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

	return Score(len(s.humans), count)
}

func (s *State) killHumans() {
	for z := range s.zombies {
		delete(s.humans, z)
	}
}

func (s *State) EstimateBestState() bool {
	for s.nextStates.IsAnalyzed() {
		s = s.nextStates.PickStateToAnalyze()
		if s == nil {
			return false
		}
	}
	s.Estimate()
	return true
}

func (s *State) DetachBestMovement() *State {
	bc := s.nextStates.bestChildState
	b := bc
	for b.parent != s {
		b = b.parent
	}
	b.parent = nil
	fmt.Fprintf(os.Stderr, "Best child: turns=%v, score=%v, max_pos_score=%v, winner=%v\n",
		bc.turns, bc.score, bc.MaxPossibleScore(), bc.IsWinner(),
	)
	return b
}

// TODO add context?
func (s *State) Estimate() {
	nextMovements := prepareMovements(s)
	s.moveZombies()

	for pos, survivors := range nextMovements {
		nextState := s.Clone()
		nextState.movePlayer(pos)
		nextState.score += nextState.destroyZombies()
		nextState.killHumans()
		nextState.maxAdditionalScore = Score(survivors, len(nextState.zombies))
		s.AddNextState(nextState)
	}
}

func findNextState(ctx context.Context, state *State) *State {
	estimations := 0
stop:
	for {
		select {
		case <-ctx.Done():
			fmt.Fprintln(os.Stderr, "Timeout")
			break stop
		default:
			if !state.EstimateBestState() {
				fmt.Fprintln(os.Stderr, "No need to analyze")
				break stop
			}
			estimations++
		}
	}

	fmt.Fprintf(os.Stderr, "Estimations: %v\n", estimations)
	return state.DetachBestMovement()
}

func possibleMovementsAround(s *State) map[Vector]struct{} {
	possibleMovements := make(map[Vector]struct{}, len(movements)+1)
	// possibleMovements := make(map[Vector]struct{}, len(s.humans)+len(movements)+1)
	possibleMovements[s.player] = struct{}{}
	add := func(pos Vector) {
		if pos.X < 0 || pos.Y < 0 || pos.X >= FieldWidth || pos.Y >= FieldHeight {
			return
		}
		possibleMovements[pos] = struct{}{}
	}
	// for h := range s.humans {
	// 	np := moveTo(s.player, h, PlayerMaxStep)
	// 	add(np)
	// }
	for _, move := range movements {
		add(s.player.Add(move))
	}
	return possibleMovements
}

func prepareMovements(state *State) map[Vector]int { // Position -> the number of survivors
	safetyDistance2 := safetyDistanceAfterZombiesMove2(state)
	possibleMoves := possibleMovementsAround(state)
	nextMovements := make(map[Vector]int, len(possibleMoves))
	for pos := range possibleMoves {
		maySurvive := 0
		for hPos, sd2 := range safetyDistance2 {
			d2 := pos.Sub(hPos).Len2()
			if d2 <= sd2 {
				maySurvive++
			}
		}
		if maySurvive > 0 {
			nextMovements[pos] = maySurvive
		}
	}
	return nextMovements
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

	if len(s.humans) != len(e.humans) {
		panic(fmt.Sprintf("Different number of humans: e=%v, a=%v", len(s.humans), len(e.humans)))
	}

	for h := range s.humans {
		if _, ok := e.humans[h]; !ok {
			panic(fmt.Sprintf("Human not found at %+v", h))
		}
	}

	if len(s.zombies) != len(e.zombies) {
		panic(fmt.Sprintf("Different number of zombies: e=%v, a=%v", len(s.zombies), len(e.zombies)))
	}

	for z := range s.zombies {
		if _, ok := e.zombies[z]; !ok {
			panic(fmt.Sprintf("Zombie not found at %+v, actual zombies: %+v, expected zombies: %+v",
				z, s.zombies, e.zombies,
			))
		}
	}
}

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

func (v Vector) Scale(f float64) Vector {
	const eps = 0.00000000001
	ex := eps
	if v.X < 0 {
		ex *= -1
	}
	ey := eps
	if v.Y < 0 {
		ey *= -1
	}
	x := float64(v.X)*f + ex
	y := float64(v.Y)*f + ey
	return NewVector(int(x), int(y))
}

func (v Vector) Len() int {
	return Sqrt(v.Len2())
}

func (v Vector) Len2() int {
	return v.X*v.X + v.Y*v.Y
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

func Score(humansLeft, zombiesKilled int) int {
	return humansLeft * humansLeft * 10 * bonusScoreMul[zombiesKilled]
}

func moveTo(start, destination Vector, maxDistance int) Vector {
	offset := destination.Sub(start)
	if offset.Len2() <= maxDistance*maxDistance {
		return destination
	}

	f := float64(maxDistance) / math.Sqrt(float64(offset.Len2()))
	offset = offset.Scale(f)

	return start.Add(offset)
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
