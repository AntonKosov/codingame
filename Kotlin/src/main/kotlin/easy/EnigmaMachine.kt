package easy

import java.lang.StringBuilder
import java.util.*

/**
 * https://www.codingame.com/ide/puzzle/encryptiondecryption-of-enigma-machine
 **/
fun main() {
    val input = Scanner(System.`in`)
    val operation = input.nextLine()
    val pseudoRandomNumber = input.nextInt()
    if (input.hasNextLine()) input.nextLine()
    val rotors = (0 until 3).map { input.nextLine() }
    val enigmaMachine = EnigmaMachine(pseudoRandomNumber, rotors)
    val message = input.nextLine()

    System.err.println(
        "Operation: $operation\n" +
                "Number: $pseudoRandomNumber\n" +
                "Rotors: $rotors\n" +
                "Message: $message\n"
    )

    println(if (operation == "DECODE") enigmaMachine.decrypt(message) else enigmaMachine.encrypt(message))
}

class EnigmaMachine(randomNumber: Int, rotors: List<String>) {

    private val mRandomNumber = randomNumber
    private val mRotors = rotors

    fun decrypt(message: String): String {
        var result = message
        for (rotorIndex in mRotors.size - 1 downTo 0) {
            val rotor = mRotors[rotorIndex]
            val rotorMap = mutableMapOf<Char, Int>()
            for (i in 0 until rotor.length)
                rotorMap[rotor[i]] = i
            val nextResult = StringBuffer(result.length)
            for (i in 0 until result.length)
                nextResult.append(ALPHABET[rotorMap[result[i]]!!])
            result = nextResult.toString()
        }

        result = shiftMessageBack(result)
        return result
    }

    fun encrypt(message: String): String {
        var result = shiftMessage(message)
        for (rotor in mRotors) {
            val nextResult = StringBuffer(result.length)
            for (i in 0 until result.length)
                nextResult.append(rotor[result[i] - A_LETTER])
            result = nextResult.toString()
        }

        return result
    }

    private fun shiftMessage(sourceMessage: String): String {
        val result = StringBuilder(sourceMessage.length)
        for (i in 0 until sourceMessage.length)
            result.append(ALPHABET[(mRandomNumber + (sourceMessage[i] - A_LETTER) + i).rem(ALPHABET.length)])
        return result.toString()
    }

    private fun shiftMessageBack(sourceMessage: String): String {
        val result = StringBuilder(sourceMessage.length)
        for (i in 0 until sourceMessage.length) {
            var index = (sourceMessage[i] - A_LETTER - mRandomNumber - i).rem(ALPHABET.length)
            if (index < 0)
                index += ALPHABET.length
            result.append(ALPHABET[index])
        }
        return result.toString()
    }

    companion object {
        private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val A_LETTER = 'A'
    }
}