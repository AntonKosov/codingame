package main

import (
	"bufio"
	"fmt"
	"os"
)

func main() {
	scanner := bufio.NewScanner(os.Stdin)
	scanner.Buffer(make([]byte, 1000000), 1000000)

	var width int
	scanner.Scan()
	fmt.Sscan(scanner.Text(), &width)

	var height int
	scanner.Scan()
	fmt.Sscan(scanner.Text(), &height)

	m := make([][]bool, height)
	cache := make([][]*int, height)
	for row := 0; row < height; row++ {
		scanner.Scan()
		text := scanner.Text()
		m[row] = make([]bool, width)
		cache[row] = make([]*int, width)
		for col, c := range text {
			m[row][col] = c == 'O'
		}
	}

	var coordinates int
	scanner.Scan()
	fmt.Sscan(scanner.Text(), &coordinates)
	areas := make([]int, 0, coordinates)
	for i := 0; i < coordinates; i++ {
		var x, y int
		scanner.Scan()
		fmt.Sscan(scanner.Text(), &x, &y)
		areas = append(areas, getArea(x, y, m, cache))
	}

	// fmt.Fprintf(os.Stderr, "areas: %v", areas)
	for _, area := range areas {
		fmt.Printf("%v\n", area)
	}
}

func getArea(col, row int, m [][]bool, c [][]*int) int {
	if !m[row][col] {
		return 0
	}

	if c[row][col] == nil {
		a := 0
		c[row][col] = &a
		fillArea(col, row, m, c)
	}

	return *c[row][col]
}

func fillArea(col, row int, m [][]bool, c [][]*int) {
	*c[row][col]++

	fill := func(nextCol, nextRow int) {
		if !m[nextRow][nextCol] || c[nextRow][nextCol] != nil {
			return
		}
		c[nextRow][nextCol] = c[row][col]
		fillArea(nextCol, nextRow, m, c)
	}

	if col > 0 {
		fill(col-1, row)
	}
	if col < len(m[0])-1 {
		fill(col+1, row)
	}
	if row > 0 {
		fill(col, row-1)
	}
	if row < len(m)-1 {
		fill(col, row+1)
	}
}
