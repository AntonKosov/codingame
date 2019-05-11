package easy

import java.util.*

/**
 * https://www.codingame.com/ide/puzzle/the-river-ii-
 */
fun main() {
    val input = Scanner(System.`in`)
    val point = input.nextInt()

    System.err.println("$point")

    println(if (isStartPoint(point)) "YES" else "NO")
}

fun isStartPoint(point: Int): Boolean {
    var maximalDelta = 0
    var p = point
    while (p > 0) {
        val lastDigit = p.rem(10)
        p /= 10
        maximalDelta += if (p > 0) 9 else lastDigit
    }

    val minPoint = maxOf(1, point - maximalDelta)

    for (startPoint in minPoint until point)
        if (doRiversMeet(startPoint, point))
            return false

    return true
}

fun doRiversMeet(sourceRiver: Int, targetRiver: Int): Boolean {
    var source = sourceRiver
    while (source < targetRiver) {
        var n = source
        while (n > 0) {
            source += n.rem(10)
            n /= 10
        }
    }

    return source == targetRiver
}