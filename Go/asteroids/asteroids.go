// https://www.codingame.com/training/easy/asteroids

package main

import (
	"fmt"
	"math"
	"strings"
)

const emptyCell = '.'
const emptyCellCode = byte('.')

type Timestamp = int

type Picture struct {
	pic [][]byte
}

func CreatePicture(rows []string) Picture {
	picture := Picture{}
	for _, row := range rows {
		picture.pic = append(picture.pic, []byte(row))
	}
	return picture
}

func CreateEmptyPicture(width, height int) Picture {
	rows := []string{}
	for i := 0; i < height; i++ {
		rows = append(rows, strings.Repeat(string(emptyCell), width))
	}
	return CreatePicture(rows)
}

func (picture *Picture) Get(row, column int) byte {
	return picture.pic[row][column]
}

func (picture *Picture) Set(row, column int, value byte) {
	if picture.pic[row][column] == emptyCellCode || picture.pic[row][column] > value {
		picture.pic[row][column] = value
	}
}

func (picture *Picture) Build() []string {
	result := []string{}
	for _, row := range picture.pic {
		result = append(result, string(row))
	}
	return result
}

type MapSnaphot struct {
	Picture   Picture
	Timestamp Timestamp
}

type vector2 struct {
	x int
	y int
}

func (vA vector2) sub(vB vector2) vector2 {
	return vector2{
		x: vA.x - vB.x,
		y: vA.y - vB.y,
	}
}

func (vA vector2) add(vB vector2) vector2 {
	return vector2{
		x: vA.x + vB.x,
		y: vA.y + vB.y,
	}
}

func (v vector2) mul(k float64) vector2 {
	return vector2{
		x: int(math.Floor(float64(v.x) * k)),
		y: int(math.Floor(float64(v.y) * k)),
	}
}

type asteroidMovement struct {
	firstCoordinate  *vector2
	secondCoordinate *vector2
}

func (m *asteroidMovement) predict(firstTimestamp, secondTimestamp, targetTimestamp Timestamp) vector2 {
	k := float64(1)
	timeDiff := secondTimestamp - firstTimestamp
	if timeDiff != 0 {
		k = (float64(targetTimestamp) - float64(firstTimestamp)) / float64(timeDiff)
	}

	diff := m.secondCoordinate.sub(*m.firstCoordinate).mul(k)

	return m.firstCoordinate.add(diff)
}

func CalculateMapSnapshot(width int, height int, firstMap MapSnaphot, secondMap MapSnaphot, targetTimestamp Timestamp) MapSnaphot {
	asteroidMap := make(map[byte]*asteroidMovement)
	findAsteroids := func(picture Picture, found func(asteroidMovement *asteroidMovement, coord vector2)) {
		for r := 0; r < height; r++ {
			for c := 0; c < width; c++ {
				cellCode := picture.Get(r, c)
				if cellCode != emptyCellCode {
					asteroid, asteroidExists := asteroidMap[cellCode]
					if !asteroidExists {
						asteroid = &asteroidMovement{}
						asteroidMap[cellCode] = asteroid
					}
					found(asteroid, vector2{x: c, y: r})
				}
			}
		}
	}
	findAsteroids(firstMap.Picture, func(asteroidMovement *asteroidMovement, coord vector2) {
		asteroidMovement.firstCoordinate = &coord
	})
	findAsteroids(secondMap.Picture, func(asteroidMovement *asteroidMovement, coord vector2) {
		asteroidMovement.secondCoordinate = &coord
	})

	picture := CreateEmptyPicture(width, height)

	for asteroid, movement := range asteroidMap {
		coord := movement.predict(firstMap.Timestamp, secondMap.Timestamp, targetTimestamp)
		if coord.x >= 0 && coord.x < width && coord.y >= 0 && coord.y < height {
			picture.Set(coord.y, coord.x, asteroid)
		}
	}

	return MapSnaphot{
		Picture:   picture,
		Timestamp: targetTimestamp,
	}
}

func main() {
	var width, height, firstTimestamp, secondTimestamp, targetTimestamp int
	fmt.Scan(&width, &height, &firstTimestamp, &secondTimestamp, &targetTimestamp)
	// fmt.Printf("%d %d %d %d %d\n", width, height, firstTimestamp, secondTimestamp, targetTimestamp)

	firstMap := []string{}
	secondMap := []string{}
	for i := 0; i < height; i++ {
		var firstPictureRow, secondPictureRow string
		fmt.Scan(&firstPictureRow, &secondPictureRow)
		firstMap = append(firstMap, firstPictureRow)
		secondMap = append(secondMap, secondPictureRow)
		// fmt.Printf("%s %s\n", firstPictureRow, secondPictureRow)
	}

	firstSnapshot := MapSnaphot{
		Picture:   CreatePicture(firstMap),
		Timestamp: firstTimestamp,
	}
	secondSnapshot := MapSnaphot{
		Picture:   CreatePicture(secondMap),
		Timestamp: secondTimestamp,
	}
	predictedMap := CalculateMapSnapshot(width, height, firstSnapshot, secondSnapshot, targetTimestamp)

	rows := predictedMap.Picture.Build()
	for _, row := range rows {
		fmt.Println(row)
	}
}
