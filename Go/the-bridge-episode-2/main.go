package main

import (
	"fmt"
	"os"
)

const lanes = 4

type road [lanes][]bool

func (r road) len() int {
	return len(r[0])
}

func newRoad(l [lanes]string) road {
	r := road{}
	for i, lane := range l {
		pl := make([]bool, len(lane))
		for j, r := range lane {
			pl[j] = r == '.'
		}
		r[i] = pl
	}

	return r
}

func main() {
	var bikes int
	fmt.Scan(&bikes)
	var mustSurvive int
	fmt.Scan(&mustSurvive)

	roadLines := [lanes]string{}
	for i := 0; i < lanes; i++ {
		var l string
		fmt.Scan(&l)
		fmt.Fprintln(os.Stderr, l)
		roadLines[i] = l
	}
	r := newRoad(roadLines)

	var commands []string
	for {
		var speed int
		fmt.Scan(&speed)

		b := [lanes]bike{}
		distance := 0
		for i := 0; i < bikes; i++ {
			var dist, lane, activated int
			fmt.Scan(&dist, &lane, &activated)
			b[i].lane = lane
			b[i].activated = activated == 1
			distance = dist
		}
		if commands == nil {
			s := newState(0, speed, "", b, distance, r.len())
			commands, _ = nextCommand(r, s, mustSurvive)
		}
		// fmt.Fprintln(os.Stderr, "Debug messages...")

		// A single line containing one of 6 keywords: SPEED, SLOW, JUMP, WAIT, UP, DOWN.
		fmt.Println(commands[len(commands)-1])
		commands = commands[:len(commands)-1]
	}
}

func nextCommand(r road, currentState state, mustSurvive int) ([]string, state) {
	handled := map[state]*state{}
	states := map[state]struct{}{currentState: {}}
	var bestOption *state

	for len(states) > 0 {
		if bestOption == nil {
			for s := range states {
				s := s
				if bestOption == nil || bestOption.score < s.score {
					bestOption = &s
				}
			}
		}

		options := make([]state, 0, 6)
		appendState(&options, r, mustSurvive, bestOption.speedCommand)
		appendState(&options, r, mustSurvive, bestOption.waitCommand)
		appendState(&options, r, mustSurvive, bestOption.jumpCommand)
		appendState(&options, r, mustSurvive, bestOption.slowCommand)
		appendState(&options, r, mustSurvive, bestOption.upCommand)
		appendState(&options, r, mustSurvive, bestOption.downCommand)

		delete(states, *bestOption)
		var nextBestOption *state
		for _, o := range options {
			o := o
			states[o] = struct{}{}
			handled[o] = bestOption
			if o.score > bestOption.score && (nextBestOption == nil || nextBestOption.score < o.score) {
				nextBestOption = &o
			}
		}
		bestOption = nextBestOption
		if bestOption != nil && bestOption.isFinished {
			break
		}
	}

	if bestOption == nil {
		panic("Solution not found")
	}

	commands := []string{
		"SPEED",
		bestOption.command,
	}
	for {
		parent := *handled[*bestOption]
		if handled[parent] == nil {
			return commands, *bestOption
		}

		commands = append(commands, parent.command)
		bestOption = &parent
	}
}

type bike struct {
	lane      int
	activated bool
}

type state struct {
	turn     int
	speed    int
	command  string
	bikes    [lanes]bike
	distance int

	isFinished bool
	survived   int
	score      int
}

func newState(turn int, speed int, command string, bikes [lanes]bike, distance, roadLength int) state {
	score := distance*10 - turn
	survived := 0
	for _, b := range bikes {
		if b.activated {
			score += 10000
			survived++
		}
	}

	return state{
		command:    command,
		turn:       turn,
		speed:      speed,
		bikes:      bikes,
		distance:   distance,
		isFinished: distance == roadLength,
		survived:   survived,
		score:      score,
	}
}

func appendState(states *[]state, r road, mustSurvive int, f func(r road) (state, bool)) {
	ns, ok := f(r)
	if !ok || ns.survived < mustSurvive {
		return
	}

	*states = append(*states, ns)
}

func (s state) move(r road, laneOffset int, speedOffset int, command string, isJump bool, speedRequired bool) (state, bool) {
	if speedRequired && s.speed == 0 {
		return state{}, false
	}
	speed := s.speed + speedOffset
	distance := distance(r.len(), s.distance, speed)
	bikes := s.bikes
	for i := 0; i < len(bikes); i++ {
		b := &bikes[i]
		if !b.activated {
			continue
		}
		nl := b.lane + laneOffset
		if nl < 0 || nl >= lanes {
			return state{}, false
		}
		if isJump {
			if distance < r.len() && !r[nl][distance] {
				b.activated = false
				continue
			}
		} else if holes(r, s.distance, distance, b.lane, nl) {
			b.activated = false
			continue
		}
		b.lane = nl
	}

	return newState(s.turn+1, speed, command, bikes, distance, r.len()), true
}

func (s state) upCommand(r road) (state, bool) {
	return s.move(r, -1, 0, "UP", false, true)
}

func (s state) downCommand(r road) (state, bool) {
	return s.move(r, 1, 0, "DOWN", false, true)
}

func (s state) waitCommand(r road) (state, bool) {
	return s.move(r, 0, 0, "WAIT", false, true)
}

func (s state) speedCommand(r road) (state, bool) {
	return s.move(r, 0, 1, "SPEED", false, false)
}

func (s state) slowCommand(r road) (state, bool) {
	return s.move(r, 0, -1, "SLOW", false, true)
}

func (s state) jumpCommand(r road) (state, bool) {
	return s.move(r, 0, 0, "JUMP", true, true)
}

func distance(roadLength int, cd int, speed int) int {
	d := cd + speed
	if d > roadLength {
		d = roadLength
	}

	return d
}

func holes(r road, distanceFrom, distanceTo, lineFrom, lineTo int) bool {
	lMin, lMax := lineFrom, lineTo
	if lMin > lMax {
		lMin, lMax = lMax, lMin
	}
	for d := distanceFrom + 1; d <= distanceTo; d++ {
		for l := lMin; l <= lMax; l++ {
			if lMin != lMax && d == distanceTo && l == lineFrom {
				continue
			}
			if d < r.len() && !r[l][d] {
				return true
			}
		}
	}

	return false
}
