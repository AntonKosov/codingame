package easy

import java.util.*

/**
 * https://www.codingame.com/ide/puzzle/the-river-i-
 **/
fun main() {
    val input = Scanner(System.`in`)
    val river1 = input.nextLong()
    val river2 = input.nextLong()

    System.err.println("Rivers: $river1, $river2")

    println(calculateMeetingPoint(river1, river2))
}

fun calculateMeetingPoint(river1Start: Long, river2Start: Long) : Long {
    val sumOfDigits = fun(number: Long) : Long {
        var result = 0L
        var n = number
        while (n > 0) {
            result += n.rem(10)
            n /= 10
        }

        return result
    }

    var river1 = river1Start
    var river2 = river2Start
    while (river1 != river2) {
        if (river1 > river2)
            river2 += sumOfDigits(river2)
        else
            river1 += sumOfDigits(river1)
    }

    return river1
}