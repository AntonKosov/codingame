package main

import (
	"fmt"
	"os"
)

func main() {
	for {
		fmt.Fprintln(os.Stderr, "Debug messages...")
		// x: x position of your pod
		// y: y position of your pod
		// nextCheckpointX: x position of the next check point
		// nextCheckpointY: y position of the next check point
		var x, y, nextCheckpointX, nextCheckpointY int
		fmt.Scan(&x, &y, &nextCheckpointX, &nextCheckpointY)

		fmt.Fprintln(os.Stderr, "Debug messages...")
		// fmt.Fprintln(os.Stderr, "Debug messages...")

		// Edit this line to output the target position
		// and thrust (0 <= thrust <= 100)
		// i.e.: "x y thrust"
		fmt.Printf("%d %d 80\n", nextCheckpointX, nextCheckpointY)
	}
}

const (
// trustMin  = 0
// trustMax  = 100
// mapWidth  = 1600
// mapHeight = 900
// checkpointRadius = 600
// firstTurnTimeout = time.Millisecond * 1000
// turnTimeout = time.Microsecond * 75
)

// type vector2 struct {
// 	x, y int
// }
//
// type pod struct {
// 	position vector2
// }
