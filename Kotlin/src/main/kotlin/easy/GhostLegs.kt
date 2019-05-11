package easy

import java.lang.Exception
import java.util.*

/**
 * https://www.codingame.com/ide/puzzle/ghost-legs
 **/
fun main() {
    val input = Scanner(System.`in`)
    input.nextInt() // width is not needed
    val height = input.nextInt()
    if (input.hasNextLine()) {
        input.nextLine()
    }
    val ghostLegs = GhostLegs()
    val firstLine = input.nextLine()
    for (i in 0 until height - 2) {
        val line = input.nextLine()
        ghostLegs.addLine(line)
        System.err.println(line)
    }
    val lastLine = input.nextLine()

    println(ghostLegs.getAnswer(firstLine, lastLine))
}

class GhostLegs {

/*
    companion object {
        @JvmStatic
        fun easy.main(args: Array<String>) {
        }
    }
*/

    private var mCurrentState: IntArray? = null

    fun addLine(line: String) {
        if (mCurrentState == null)
            mCurrentState = IntArray(1 + line.length / 3) { i -> i }

        for (i in 0 until mCurrentState!!.size - 1) {
            if (line[1 + i * 3] == ' ') continue
            val t = mCurrentState!![i]
            mCurrentState!![i] = mCurrentState!![i + 1]
            mCurrentState!![i + 1] = t
        }
    }

    fun getAnswer(firstLine: String, lastLine: String): String {
        if (mCurrentState == null)
            throw Exception("No data.")

        var result = ""

        for (i in 0 until mCurrentState!!.size) {
            if (result.isNotEmpty())
                result += "\n"
            val answerIndex = mCurrentState!!.indexOf(i)
            result += firstLine[i * 3].toString() + lastLine[answerIndex * 3]
        }

        return result
    }
}
