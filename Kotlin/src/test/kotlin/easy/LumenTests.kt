package easy

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class LumenTests {

    @Test
    fun exampleTest() {
        run(
            3,
            listOf(
                "X X X X X",
                "X C X X X",
                "X X X X X",
                "X X X X X",
                "X X X X X"
            ),
            9
        )
    }

    @Test
    fun theyHaveAGrateHall() {
        run(
            2,
            listOf(
                "X X C C C C X X X X C C X X X C X X X X X C X X X",
                "C X X X X X X X X X X X X X X C X X X X X X X X X",
                "X X X X X X X X X X X X C X X X X C X X X C C X X",
                "C X X X X C X X X X X X X X X X X X X C X X X X X",
                "X X C X C X X X X X X X X X C X X C X X X C X X X",
                "X X X X X C X X X X X X X C X C X X X X X X X X X",
                "C X X X X C C X X X X X X X X X C X X X X X X C X",
                "C X C C X X C X X C X X C C X X C X X X X X X X X",
                "X X X X X X X X X X X X X X X X X C X X X X X C X",
                "X C X X X X X X X X X X X X X X X X X C C X X X X",
                "C X X X X X X C X C X X X X C X X X X X C X X X X",
                "X C X X C X X X X X X X X X C X X X X X C X X X C",
                "X X X X X X X X X C X X C X X X X X X X X X C X X",
                "X X C X C X X X X X C X X X X X X C C X X X C X C",
                "C X C C X X X X X X X X X X C X X X X X C X X X X",
                "C X X C X C X X C C X X X C C X X X X X X X C C X",
                "C X X X X X X X X X X X X X X X X X X C X X X X X",
                "X X X X X X X C X X X X X X C X X X X X X X C X X",
                "X C X C X X C X X X X X C X X X X X X X C C X C X",
                "X X X C X X C C C X X C X X X X X C X X X X X X X",
                "X C X X X X X X X X X X X C X X X X X X X X X X X",
                "X X X C C X X C X C X X X X X X X X X C C X X X X",
                "X X C X X X X C X C C X X X X X C X X X X C C X X",
                "X X X X C X X X X X C X X X X X C X X X C X X C X",
                "X X X C X X C X X X X X X X X X X X C X X X X X X"
            ),
            90
        )
    }

    companion object {
        private fun run(light: Int, lines: List<String>, answer: Int) {
            val lumen = Lumen(lines.size, light)

            for (i in 0 until lines.size)
                lumen.addLine(lines[i])

            Assertions.assertEquals(answer, lumen.leftDarkSpots)
        }
    }
}