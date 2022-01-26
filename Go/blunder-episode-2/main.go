package main

import (
	"bufio"
	"fmt"
	"os"
	"strconv"
	"strings"
)

func main() {
	scanner := bufio.NewScanner(os.Stdin)
	scanner.Buffer(make([]byte, 1000000), 1000000)

	var roomsCount int
	scanner.Scan()
	fmt.Sscan(scanner.Text(), &roomsCount)

	in := make([]string, 0, roomsCount)

	for i := 0; i < roomsCount; i++ {
		scanner.Scan()
		line := scanner.Text()
		in = append(in, line)
		fmt.Fprintln(os.Stderr, line)
	}

	input := NewInput(in)

	fmt.Println(input.Solve())
}

type Room struct {
	MaxSum  int
	Sum     int
	HasExit bool
	Visited bool
}

type Input struct {
	connections [][]int
	rooms       []Room
}

func NewInput(in []string) *Input {
	roomsCount := len(in)
	input := &Input{
		connections: make([][]int, roomsCount),
		rooms:       make([]Room, roomsCount),
	}

	for _, line := range in {
		parts := strings.Split(line, " ")
		roomIndex, _ := strconv.Atoi(parts[0])
		sum, _ := strconv.Atoi(parts[1])
		r := &input.rooms[roomIndex]
		r.Sum = sum
		for ri := 2; ri <= 3; ri++ {
			v := parts[ri]
			if v == "E" {
				r.HasExit = true
				continue
			}
			cr, _ := strconv.Atoi(v)
			input.connections[roomIndex] = append(input.connections[roomIndex], cr)
			input.connections[cr] = append(input.connections[cr], roomIndex)
		}
	}

	return input
}

func (in *Input) Solve() int {
	toVisit := map[int]struct{}{0: {}}
	in.rooms[0].MaxSum = in.rooms[0].Sum
	maxSum := in.rooms[0].MaxSum

	for len(toVisit) > 0 {
		currentRoomIndex := -1
		for ri := range toVisit {
			if currentRoomIndex < 0 || in.rooms[currentRoomIndex].MaxSum > in.rooms[ri].MaxSum {
				currentRoomIndex = ri
			}
		}
		delete(toVisit, currentRoomIndex)
		currentRoom := &in.rooms[currentRoomIndex]
		for _, cri := range in.connections[currentRoomIndex] {
			cr := &in.rooms[cri]
			if cr.Visited {
				continue
			}
			cr.MaxSum = max(cr.MaxSum, currentRoom.MaxSum+cr.Sum)
			toVisit[cri] = struct{}{}
		}

		currentRoom.Visited = true
		if currentRoom.HasExit {
			maxSum = max(maxSum, currentRoom.MaxSum)
		}
	}

	return maxSum
}

func max(a, b int) int {
	if a > b {
		return a
	}
	return b
}
