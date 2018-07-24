package na.kephas.kitvei

import org.junit.Test

import org.junit.Assert.*
import org.junit.Ignore

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class LogicTest {
    @Ignore
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Ignore
    fun indexTest() {
        val s = "qwe and ope and"
        val i = s.indexOf("and", 4 + 1)
        assertEquals(12, i)
        println("index: $i")
    }

    @Ignore
    fun unicodeTest() {
        println("{1\u200B\u200Bhey}")
    }

    @Test
    fun textTest() {
        val s = "54\t5\t18\tFor the scripture saith, Thou shalt not muzzle the ox that treadeth out the corn. And, {The labourer is worthy of his reward.}"
        val i = s.lastIndexOf('\t')
        val sub = s.substring(i + 1)
        assertEquals("For the scripture saith, Thou shalt not muzzle the ox that treadeth out the corn. And, {The labourer is worthy of his reward.}", sub)
    }
}


