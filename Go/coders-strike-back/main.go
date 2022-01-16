package main

import (
	"fmt"
	"os"
	"strconv"
)

func main() {
	state := NewState()
	for {
		// nextCheckpointX: x position of the next check point
		// nextCheckpointY: y position of the next check point
		// nextCheckpointDist: distance to the next checkpoint
		// nextCheckpointAngle: angle between your pod orientation and the direction of the next checkpoint
		var x, y, nextCheckpointX, nextCheckpointY, nextCheckpointDist, nextCheckpointAngle int
		fmt.Scan(&x, &y, &nextCheckpointX, &nextCheckpointY, &nextCheckpointDist, &nextCheckpointAngle)

		var opponentX, opponentY int
		fmt.Scan(&opponentX, &opponentY)

		pod := NewVector(x, y)
		checkpoint := NewVector(nextCheckpointX, nextCheckpointY)

		var command Command
		command, state = state.nextTurn(pod, checkpoint, nextCheckpointAngle, nextCheckpointDist)

		fmt.Fprintf(os.Stderr, "Pod: %+v; CP: %+v; A: %d; Command: %+v\n", pod, checkpoint, nextCheckpointAngle, command)
		fmt.Printf("%d %d %s\n", command.target.X, command.target.Y, command.action)
	}
}

const (
	TrustMin = 0
	TrustMax = 100

	BoostAction = "BOOST"

	MapWidth = 1600

	// mapHeight = 900
	// checkpointRadius = 600
	// podRadius = 400
	// firstTurnTimeout = time.Millisecond * 1000
	// turnTimeout = time.Microsecond * 75
	// maxRotationAngle = 18
	// boostPower = 650
)

func Abs(v int) int {
	if v >= 0 {
		return v
	}

	return -v
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

func (v Vector) Len2() int {
	return v.X*v.X + v.Y*v.Y
}

func (v Vector) RotateLeft90() Vector {
	return Vector{X: -v.Y, Y: v.X}
}

type Command struct {
	target Vector
	action string
}

// type Pod struct {
// 	position Vector
// }

// type State struct {
// 	turn int
// 	// pod       Pod
// 	dir Vector
// }
//
// func NewState() State {
// 	s := State{
// 		dir: NewVector(0, -10000),
// 	}
//
// 	return s
// }

// func (s State) nextTurn(podPosition, checkpoint Vector, angle, distance int) (Command, State) {
// 	nextState := s
// 	nextState.turn++
// 	if s.turn < 10 {
// 		// nextState.dir = nextState.dir.rotateLeft90()
// 		command := Command{
// 			target: NewVector(podPosition.X, 20000),
// 			// target: podPosition.Add(nextState.dir),
// 			action: "0",
// 		}
// 		return command, nextState
// 	}
//
// 	if s.turn == 10 {
// 		command := Command{
// 			target: NewVector(podPosition.X, 20000),
// 			action: BoostAction,
// 		}
// 		return command, nextState
// 	}
//
// 	if s.turn < 14 {
// 		command := Command{
// 			target: NewVector(podPosition.X, 20000),
// 			action: "100",
// 		}
//
// 		return command, nextState
// 	}
//
// 	command := Command{
// 		target: NewVector(-20000, podPosition.Y),
// 		action: "100",
// 	}
//
// 	return command, nextState
// }

type State struct {
	boostUsed  bool
	prevPodPos Vector
}

func NewState() State {
	s := State{}

	return s
}

func (s State) nextTurn(podPosition, checkpoint Vector, angle, distance int) (Command, State) {
	angle = Abs(angle)
	nextState := s
	nextState.prevPodPos = podPosition
	command := Command{
		target: checkpoint,
	}
	speed := podPosition.Sub(s.prevPodPos)
	if !s.boostUsed && distance > 7000 && angle < 10 {
		nextState.boostUsed = true
		command.action = BoostAction
	} else {
		trust := TrustMin
		if angle < 80 && !(distance < 2000 && speed.Len2() > 1000*1000) {
			trust = TrustMax - TrustMax*(Abs(angle)/90)
		}
		command.action = strconv.Itoa(trust)
	}

	return command, nextState
}
