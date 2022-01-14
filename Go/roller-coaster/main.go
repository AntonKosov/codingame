package main

import (
	"fmt"
	"os"
)

func main() {
	var capacity, turns, groupsCount int
	fmt.Scan(&capacity, &turns, &groupsCount)
	fmt.Fprintf(os.Stderr, "capacity: %v, turns: %v, groups: %v\n", capacity, turns, groupsCount)

	var groups []int
	for i := 0; i < groupsCount; i++ {
		var groupSize int
		fmt.Scan(&groupSize)
		groups = append(groups, groupSize)
	}

	fmt.Fprintln(os.Stderr, groups)

	fmt.Println(solve(capacity, turns, groups))
}

type groupCache struct {
	len int
	sum int
}

func solve(capacity, turns int, groups []int) int {
	cache := make(map[int]groupCache, len(groups))
	sum := 0
	head := 0
	for t := 0; t < turns; t++ {
		if gc, ok := cache[head]; ok {
			head = (head + gc.len) % len(groups)
			sum += gc.sum
			continue
		}
		startHead := head
		gc := groupCache{}
		for {
			groupSize := groups[head]
			if gc.sum+groupSize > capacity {
				break
			}
			gc.sum += groupSize
			gc.len++
			head = (head + 1) % len(groups)
			if startHead == head {
				break
			}
		}
		cache[startHead] = gc
		sum += gc.sum
	}

	return sum
}
