package easy

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TheRiverTwoTests {

    @Test
    fun river20Test() {
        Assertions.assertEquals(true, isStartPoint(20))
    }

    @Test
    fun river6Test() {
        Assertions.assertEquals(false, isStartPoint(6))
    }

    @Test
    fun river13Test() {
        Assertions.assertEquals(false, isStartPoint(13))
    }
}