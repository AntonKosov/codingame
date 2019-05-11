package easy

import java.util.*

/**
 * https://www.codingame.com/ide/puzzle/lumen
 **/
fun main() {
    val input = Scanner(System.`in`)
    val side = input.nextInt()
    val light = input.nextInt()
    System.err.println(light);
    if (input.hasNextLine()) {
        input.nextLine()
    }

    val lumen = Lumen(side, light)
    for (i in 0 until side) {
        val line = input.nextLine()
        System.err.println(line)
        lumen.addLine(line)
    }

    println(lumen.leftDarkSpots)
}

class Lumen(side: Int, light: Int) {
    private val mSide = side
    private val mLight = light
    private val mDarkSpots = Array(side) { Array(side) { true } }

    private var mLeftDarkSpots = side * side
    private var mCurrentLineIndex = 0

    fun addLine(line: String) {
        for (i in 0 until mSide) {
            val symbol = line[i * 2]
            if (symbol == 'X') continue

            val cMin = maxOf(0, i - mLight + 1)
            val rMin = maxOf(0, mCurrentLineIndex - mLight + 1)
            val cMax = minOf(mSide - 1, i + mLight - 1)
            val rMax = minOf(mSide - 1, mCurrentLineIndex + mLight - 1)

            for (r in rMin..rMax)
                for (c in cMin..cMax)
                    if (mDarkSpots[r][c]) {
                        mDarkSpots[r][c] = false
                        mLeftDarkSpots--
                    }
        }

        mCurrentLineIndex++
    }

    val leftDarkSpots: Int
        get() = mLeftDarkSpots
}