package main

import (
	"fmt"
)

func main() {
	boostUsed := false
	for {
		// nextCheckpointX: x position of the next check point
		// nextCheckpointY: y position of the next check point
		// nextCheckpointDist: distance to the next checkpoint
		// nextCheckpointAngle: angle between your pod orientation and the direction of the next checkpoint
		var x, y, nextCheckpointX, nextCheckpointY, nextCheckpointDist, nextCheckpointAngle int
		fmt.Scan(&x, &y, &nextCheckpointX, &nextCheckpointY, &nextCheckpointDist, &nextCheckpointAngle)

		var opponentX, opponentY int
		fmt.Scan(&opponentX, &opponentY)

		// fmt.Fprintln(os.Stderr, "Debug messages...")

		// You have to output the target position
		// followed by the power (0 <= thrust <= 100)
		// i.e.: "x y thrust"
		nextCheckpointAngle = abs(nextCheckpointAngle)
		if !boostUsed && nextCheckpointAngle < 15 && nextCheckpointDist > mapWidth/3 {
			fmt.Printf("%d %d %s\n", nextCheckpointX, nextCheckpointY, boostAction)
			boostUsed = true
			continue
		}

		trust := trustMin
		if nextCheckpointAngle < 90 {
			trust = trustMax - trustMax*(abs(nextCheckpointAngle)/90)
		}
		fmt.Printf("%d %d %d\n", nextCheckpointX, nextCheckpointY, trust)
	}
}

const (
	trustMin = 0
	trustMax = 100

	boostAction = "BOOST"

	mapWidth = 1600

// mapHeight = 900
// checkpointRadius = 600
// firstTurnTimeout = time.Millisecond * 1000
// turnTimeout = time.Microsecond * 75
)

func abs(v int) int {
	if v >= 0 {
		return v
	}

	return -v
}

// type vector2 struct {
// 	x, y int
// }

// type pod struct {
// 	position vector2
// }
