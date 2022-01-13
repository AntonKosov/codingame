package main

import (
	"fmt"
	"os"
	"sort"
	"strings"
)

func main() {
	field := readField()
	if !field.arrange(0) {
		panic("Solution not found")
	}
	output(field)
}

const (
	emptyCell = '.'
	waterCell = 'X'
	holeCell  = 'H'
)

func readField() *field {
	var width, height int
	fmt.Scan(&width, &height)

	var data []string
	for y := 0; y < height; y++ {
		var row string
		fmt.Scan(&row)
		fmt.Fprintln(os.Stderr, row)
		data = append(data, row)
	}

	return newField(data)
}

func output(s *field) {
	fmt.Fprintln(os.Stderr, "Solution:")
	result := s.grid.buildResult()
	for _, row := range result {
		fmt.Println(row)
	}
}

type vector struct {
	x, y int
}

func sign(v int) int {
	if v == 0 {
		return 0
	}

	if v > 0 {
		return 1
	}

	return -1
}

func abs(v int) int {
	if v >= 0 {
		return v
	}

	return -v
}

func newVector(x, y int) vector {
	return vector{x: x, y: y}
}

func (v vector) distance() int {
	return abs(v.x) + abs(v.y)
}

func (v vector) sub(av vector) vector {
	return vector{x: v.x - av.x, y: v.y - av.y}
}

func (v vector) add(av vector) vector {
	return vector{x: v.x + av.x, y: v.y + av.y}
}

func (v vector) mul(f int) vector {
	return vector{x: v.x * f, y: v.y * f}
}

func (v vector) norm() vector {
	return vector{x: sign(v.x), y: sign(v.y)}
}

type grid struct {
	original [][]rune
	working  [][]rune
}

func (g *grid) height() int {
	return len(g.original)
}

func (g *grid) width() int {
	return len(g.original[0])
}

func (g *grid) restore(c vector) {
	g.working[c.y][c.x] = g.original[c.y][c.x]
}

func (g *grid) addRow(row []rune) {
	g.original = append(g.original, row)
	workingRow := make([]rune, len(row))
	copy(workingRow, row)
	g.working = append(g.working, workingRow)
}

func (g *grid) workingCell(c vector) rune {
	return g.working[c.y][c.x]
}

func (g *grid) buildResult() []string {
	var result []string
	for _, row := range g.working {
		var sb strings.Builder
		for _, v := range row {
			if v == waterCell || v == holeCell {
				v = emptyCell
			}
			sb.WriteRune(v)
		}
		result = append(result, sb.String())
	}

	return result
}

var directions map[vector]rune

func init() {
	directions = map[vector]rune{
		newVector(0, -1): '^',
		newVector(1, 0):  '>',
		newVector(0, 1):  'v',
		newVector(-1, 0): '<',
	}
}

type ball struct {
	position  vector
	turnsLeft int
	holes     map[vector]bool
}

func maxDistance(turnsLeft int) int {
	return turnsLeft * (1 + turnsLeft) / 2
}

type balls []*ball

func (b balls) Len() int { return len(b) }

func (b balls) Swap(i, j int) { b[i], b[j] = b[j], b[i] }

func (b balls) Less(i, j int) bool {
	return len(b[i].holes)-b[i].position.distance() < len(b[j].holes)-b[j].position.distance()
}

type field struct {
	balls balls
	holes map[vector]bool
	grid  *grid
}

var masks [9]map[vector]bool

func fillMask(pos, direction vector, ballNumber int, mask map[vector]bool) {
	currentPosition := pos.add(direction.mul(ballNumber))
	mask[currentPosition] = true
	if ballNumber == 1 {
		return
	}

	oppositeDir := direction.mul(-1)
	for dir := range directions {
		if dir == oppositeDir {
			continue
		}
		fillMask(currentPosition, dir, ballNumber-1, mask)
	}
}

func isReachable(hole vector, ballNumber int) bool {
	index := ballNumber - 1
	if masks[index] == nil {
		mask := map[vector]bool{}
		start := newVector(0, 0)
		for dir := range directions {
			fillMask(start, dir, ballNumber, mask)
		}
		masks[index] = mask
	}

	return masks[index][hole]
}

func newField(data []string) *field {
	f := &field{
		grid:  &grid{},
		holes: map[vector]bool{},
	}

	for y, row := range data {
		runes := []rune(row)
		f.grid.addRow(runes)

		for x, r := range runes {
			if r == holeCell {
				f.holes[newVector(x, y)] = true
			} else if r > '0' && r <= '9' {
				b := ball{
					position:  newVector(x, y),
					turnsLeft: int(r - '0'),
					holes:     map[vector]bool{},
				}
				f.balls = append(f.balls, &b)
			}
		}
	}

	for _, b := range f.balls {
		for hole := range f.holes {
			if isReachable(hole.sub(b.position), b.turnsLeft) {
				b.holes[hole] = true
			}
		}
	}

	sort.Sort(f.balls)

	return f
}

func (f *field) arrange(ballIndex int) bool {
	if ballIndex == len(f.balls) {
		return true
	}

	b := f.balls[ballIndex]
	originalPosition := b.position
	for hole := range b.holes {
		if !f.holes[hole] {
			continue
		}
		for d := range directions {
			target := b.position.add(d.mul(b.turnsLeft))
			if target.x < 0 || target.x >= f.grid.width() || target.y < 0 || target.y >= f.grid.height() {
				continue
			}
			if f.grid.workingCell(target) == waterCell {
				continue
			}
			if b.turnsLeft == 1 && target != hole {
				continue
			}
			if dist := target.sub(hole).distance(); dist > maxDistance(b.turnsLeft-1) {
				continue
			}
			if f.holes[target] && target != hole {
				continue
			}

			if !f.hit(b.position, target, target == hole) {
				continue
			}

			if target == hole {
				f.holes[hole] = false
				if f.arrange(ballIndex + 1) {
					return true
				}
				f.holes[hole] = true
			} else {
				b.turnsLeft--
				b.position = target
				if f.arrange(ballIndex) {
					return true
				}
				b.position = originalPosition
				b.turnsLeft++
			}
			f.rollback(b.position, target)
		}
	}

	return false
}

func (f *field) hit(from, to vector, allowLastHole bool) bool {
	step := to.sub(from).norm()
	dir := directions[step]
	cp := from.sub(step)
	for cp != to {
		cp = cp.add(step)
		c := f.grid.workingCell(cp)
		if c != waterCell && c != emptyCell && cp != from && !(allowLastHole && c == holeCell && cp == to) {
			f.rollback(from, cp.sub(step))
			return false
		}
		f.grid.working[cp.y][cp.x] = dir
	}
	f.grid.restore(cp)

	return true
}

func (f *field) rollback(from, to vector) {
	step := to.sub(from).norm()
	p := from
	for {
		f.grid.restore(p)
		if p == to {
			return
		}
		p = p.add(step)
	}
}
