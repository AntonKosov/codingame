package easy

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EnigmaMachineTests {

    @Test
    fun encodeExampleTest() {
        val enigmaMachine = EnigmaMachine(
            4,
            listOf(
                "BDFHJLCPRTXVZNYEIWGAKMUSQO",
                "AJDKSIRUXBLHWTMCQGZNPYFVOE",
                "EKMFLGDQVZNTOWYHXUSPAIBRCJ"
            )
        )

        Assertions.assertEquals(
            "KQF",
            enigmaMachine.encrypt("AAA")
        )
    }

    @Test
    fun encode23Test() {
        val enigmaMachine = EnigmaMachine(
            7,
            listOf(
                "BDFHJLCPRTXVZNYEIWGAKMUSQO",
                "AJDKSIRUXBLHWTMCQGZNPYFVOE",
                "EKMFLGDQVZNTOWYHXUSPAIBRCJ"
            )
        )

        Assertions.assertEquals(
            "ALWAURKQEQQWLRAWZHUYKVN",
            enigmaMachine.encrypt("WEATHERREPORTWINDYTODAY")
        )
    }

    @Test
    fun myEncodeTest() {
        val enigmaMachine = EnigmaMachine(
            0,
            listOf(
              //"ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                "BDFHJLCPRTXVZNYEIWGAKMUSQO",
                "AJDKSIRUXBLHWTMCQGZNPYFVOE",
                "EKMFLGDQVZNTOWYHXUSPAIBRCJ"
            )
        )

        Assertions.assertEquals(
            "ZV",
            enigmaMachine.encrypt("AB")
        )
    }

    @Test
    fun decodeExampleTest() {
        val enigmaMachine = EnigmaMachine(
            4,
            listOf(
                "BDFHJLCPRTXVZNYEIWGAKMUSQO",
                "AJDKSIRUXBLHWTMCQGZNPYFVOE",
                "EKMFLGDQVZNTOWYHXUSPAIBRCJ"
            )
        )

        Assertions.assertEquals(
            "AAA",
            enigmaMachine.decrypt("KQF")
        )
    }

    @Test
    fun decode21Test() {
        val enigmaMachine = EnigmaMachine(
            9,
            listOf(
                "BDFHJLCPRTXVZNYEIWGAKMUSQO",
                "AJDKSIRUXBLHWTMCQGZNPYFVOE",
                "EKMFLGDQVZNTOWYHXUSPAIBRCJ"
            )
        )

        Assertions.assertEquals(
            "EVERYONEISWELCOMEHERE",
            enigmaMachine.decrypt("PQSACVVTOISXFXCIAMQEM")
        )
    }
}