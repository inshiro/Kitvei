package na.kephas.kitvei.util

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

data class RedLetter(val bookId: Int, val chapterId: Int, val verseId: Int, var positions: MutableSet<Pair<Int, Int>>)

object RedLetterDatabase {
    private const val PATH = "databases/RedLetters.txt"
    private val list: MutableSet<RedLetter>? = null
    fun getRedLetters(context: Context): MutableSet<RedLetter> {
        if (list != null) return list

        var reader: BufferedReader? = null
        var l: List<String>? = null
        try {
            reader = BufferedReader(
                    InputStreamReader(context.assets.open(PATH), "UTF-8"))

            l = reader.readLines()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        val redLetters = mutableSetOf<RedLetter>()

        var count = 0

        var tabPos1: Int
        var tabPos2: Int
        var tabPos3: Int

        var b: String
        var c: String
        var v: String

        var text: String

        l?.forEach {
            count++

            tabPos1 = it.indexOf('\t')
            tabPos2 = it.indexOf('\t', tabPos1 + 1)
            tabPos3 = it.indexOf('\t', tabPos2 + 1)

            b = it.substring(0, tabPos1)
            c = it.substring(tabPos1 + 1, tabPos2)
            v = it.substring(tabPos2 + 1, tabPos3)

            text = it.substring(tabPos3 + 1)


            text = text.styleBetween("{", "", "}", "") { s, e ->
                // If same Triple, add pair else add set
                redLetters.elementAtOrNull(count - 1)?.run {
                    if (bookId == b.toInt() && chapterId == c.toInt() && verseId == v.toInt())
                        redLetters.add(RedLetter(
                                b.toInt(), c.toInt(), v.toInt()
                                , { this.positions.add(Pair(s, e)); this.positions }()
                        )).also { redLetters.remove(this) } // Remove duplicates
                }
                        ?: redLetters.add(RedLetter(b.toInt(), c.toInt(), v.toInt(), mutableSetOf(Pair(s, e))))
            }
            // p { "${count}|b: \"$b\" c: \"$c\" v: \"$v\" t: \"$text\"" }
            // p { "verse\t$b\t$c\t$v\t$text" }


        }

        return redLetters
    }

}

inline fun <T : RedLetter, M : MutableSet<T>, R> M.forEachPosition(startPos: Int = 0, action: (b: Int, c: Int, v: Int, start: Int, end: Int) -> R) {
    for (idx in startPos until this.size) {
        this.elementAtOrNull(idx)?.let { m ->
            m.positions.forEach {
                action(m.bookId, m.chapterId, m.verseId, it.first, it.second).also { if (it is Boolean && it) return }
            }
        }
    }
}


/**
 * Count Occurrences of a String in a String
 */

fun String.count(sub: String): Int {
    var count = 0
    var startIdx = 0
    while ({ indexOf(sub, startIdx).also { startIdx = it + 1 } }() >= 0) {
        count++
    }
    return count
}


/**
 * Styles text between 2 substrings and removes the sub after
 */
fun String.insert(position: Int, str: String): String = StringBuffer(this).insert(position, str).toString()

inline fun String.styleBetween(s1: String, re1: String, s2: String = "", re2: String = "", action: (s: Int, e: Int) -> Unit = { _, _ -> }): String {

    @Suppress("NAME_SHADOWING")
    var s2 = s2
    if (s2.isEmpty()) s2 = s1

    if (!(this.contains(s1) && this.contains(s2))) return this
    var str = this

    val count = this.count(s1)

    var idx1: Int
    var idx2: Int
    (1..count).forEach {
        idx1 = str.indexOf(s1)
        idx2 = str.indexOf(s2, idx1 + 1)

        action(idx1, idx2)


        //str = str.insert(idx2, re2).insert(idx1, re1)

        //str = str.replaceFirst(s1,"").replaceFirst(s2,"")
        str = str.replaceFirst(s1, re1).replaceFirst(s2, re2)

    }

    return str
}