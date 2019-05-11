package easy

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TheRiverOneTests {

    @Test
    fun exampleTest1() {
        Assertions.assertEquals(47, calculateMeetingPoint(32, 47))
    }

    @Test
    fun exampleTest2() {
        Assertions.assertEquals(519, calculateMeetingPoint(480, 471))
    }
}