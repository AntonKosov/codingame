package main

import (
	"testing"

	. "github.com/onsi/ginkgo"
	. "github.com/onsi/gomega"
)

func TestAsteroids(t *testing.T) {
	RegisterFailHandler(Fail)
	RunSpecs(t, "the asteroid package")
}

var _ = Describe("the asteroid package", func() {
	When("There are empty maps", func() {
		firstSnapshot := MapSnaphot{
			Picture: CreatePicture([]string{
				"...",
				"...",
			}),
			Timestamp: 5,
		}
		secondSnapshot := MapSnaphot{
			Picture: CreatePicture([]string{
				"...",
				"...",
			}),
			Timestamp: 7,
		}

		targetSnapshot := CalculateMapSnapshot(3, 2, firstSnapshot, secondSnapshot, 10)

		It("should return correct timestamp", func() {
			Expect(targetSnapshot.Timestamp).Should(Equal(10))
		})

		It("should return an empty map", func() {
			picture := targetSnapshot.Picture
			Expect(picture.Build()).Should(Equal([]string{
				"...",
				"...",
			}))
		})
	})

	When("there is one asteroid", func() {
		firstSnapshot := MapSnaphot{
			Picture: CreatePicture([]string{
				"A...",
			}),
			Timestamp: 3,
		}
		secondSnapshot := MapSnaphot{
			Picture: CreatePicture([]string{
				".A..",
			}),
			Timestamp: 5,
		}

		targetSnapshot := CalculateMapSnapshot(4, 1, firstSnapshot, secondSnapshot, 9)

		It("should have right picture", func() {
			Expect(targetSnapshot.Picture.Build()).Should(Equal([]string{
				"...A",
			}))
		})
	})

	When("there are asteroids in all directions", func() {
		firstSnapshot := MapSnaphot{
			Picture: CreatePicture([]string{
				"AC....D",
				".......",
				".......",
				"E......",
				".......",
				".......",
				"F......",
				"......B",
			}),
			Timestamp: 3,
		}
		secondSnapshot := MapSnaphot{
			Picture: CreatePicture([]string{
				"..C..D.",
				"A......",
				"E......",
				".......",
				".......",
				"..F.B..",
				".......",
				".......",
			}),
			Timestamp: 5,
		}

		targetSnapshot := CalculateMapSnapshot(7, 8, firstSnapshot, secondSnapshot, 9)

		It("should have right timestamp", func() {
			Expect(targetSnapshot.Timestamp).Should(Equal(9))
		})

		It("should have right picture", func() {
			Expect(targetSnapshot.Picture.Build()).Should(Equal([]string{
				"E..DC..",
				"B......",
				".......",
				"A.....F",
				".......",
				".......",
				".......",
				".......",
			}))
		})
	})

	When("there is a static asteroid", func() {
		firstSnapshot := MapSnaphot{
			Picture: CreatePicture([]string{
				"A.",
				"..",
			}),
			Timestamp: 1,
		}
		secondSnapshot := MapSnaphot{
			Picture: CreatePicture([]string{
				"A.",
				"..",
			}),
			Timestamp: 4,
		}

		targetSnapshot := CalculateMapSnapshot(2, 2, firstSnapshot, secondSnapshot, 10)

		It("should have right timestamp", func() {
			Expect(targetSnapshot.Timestamp).Should(Equal(10))
		})

		It("should have right picture", func() {
			Expect(targetSnapshot.Picture.Build()).Should(Equal([]string{
				"A.",
				"..",
			}))
		})
	})

	When("Greater delta test", func() {
		firstSnapshot := MapSnaphot{
			Picture: CreatePicture([]string{
				"A.....",
				"......",
				"......",
				"......",
				"......",
				"......",
			}),
			Timestamp: 1,
		}
		secondSnapshot := MapSnaphot{
			Picture: CreatePicture([]string{
				"....A.",
				"......",
				"......",
				"......",
				"......",
				"......",
			}),
			Timestamp: 5,
		}

		targetSnapshot := CalculateMapSnapshot(6, 6, firstSnapshot, secondSnapshot, 6)

		It("should have right picture", func() {
			Expect(targetSnapshot.Picture.Build()).Should(Equal([]string{
				".....A",
				"......",
				"......",
				"......",
				"......",
				"......",
			}))
		})
	})

	When("Depth test", func() {
		firstSnapshot := MapSnaphot{
			Picture: CreatePicture([]string{
				"..H...",
				"......",
				"E...G.",
				"......",
				"..F...",
				"......",
			}),
			Timestamp: 1,
		}
		secondSnapshot := MapSnaphot{
			Picture: CreatePicture([]string{
				"......",
				"..H...",
				".E.G..",
				"..F...",
				"......",
				"......",
			}),
			Timestamp: 6,
		}

		targetSnapshot := CalculateMapSnapshot(6, 6, firstSnapshot, secondSnapshot, 11)

		It("should have right picture", func() {
			Expect(targetSnapshot.Picture.Build()).Should(Equal([]string{
				"......",
				"......",
				"..E...",
				"......",
				"......",
				"......",
			}))
		})
	})

	When("Out of bounds", func() {
		firstSnapshot := MapSnaphot{
			Picture: CreatePicture([]string{
				"A.E",
				"B..",
				"D.C",
			}),
			Timestamp: 1,
		}
		secondSnapshot := MapSnaphot{
			Picture: CreatePicture([]string{
				"DA.",
				"..B",
				"C.E",
			}),
			Timestamp: 2,
		}

		targetSnapshot := CalculateMapSnapshot(3, 3, firstSnapshot, secondSnapshot, 3)

		It("should have right picture", func() {
			Expect(targetSnapshot.Picture.Build()).Should(Equal([]string{
				"..A",
				"...",
				"...",
			}))
		})
	})
})
