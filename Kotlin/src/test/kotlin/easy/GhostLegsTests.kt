package easy

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GhostLegsTests {

    @Test
    fun exampleTest()
    {
        run(
            listOf(
                "A  B  C",
                "|  |  |",
                "|--|  |",
                "|  |--|",
                "|  |--|",
                "|  |  |",
                "1  2  3"
            ),
            "A2\nB1\nC3"
        )
    }

    @Test
    fun smallTest()
    {
        run(
            listOf(
                "A  B  C  D  E",
                "|  |  |  |  |",
                "|  |--|  |  |",
                "|--|  |  |  |",
                "|  |  |--|  |",
                "|  |--|  |--|",
                "|  |  |  |  |",
                "1  2  3  4  5"
            ),
            "A3\nB5\nC1\nD2\nE4"
        )
    }

    companion object {
        private fun run(lines: List<String>, answer: String) {
            val ghostLegs = GhostLegs()

            val firstLine = lines[0]
            for (i in 1 until lines.size - 1)
                ghostLegs.addLine(lines[i])
            val lastLine = lines[lines.size - 1]

            assertEquals(answer, ghostLegs.getAnswer(firstLine, lastLine))
        }
    }
}