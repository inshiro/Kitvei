package na.kephas.kitvei.util

import na.kephas.kitvei.App
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

data class RedLetter(val bookId: Int, val chapterId: Int, val verseId: Int, var positions: MutableSet<Pair<Int, Int>>)

private fun StringBuilder.clear() {
    setLength(0)
}

object RedLetters {
    private const val PATH = "databases/RedLetters.txt"
    private var list: List<CharSequence>? = null

    fun getPositions(index: Int, block: (start:Int, end:Int) -> Unit) {
        if (list == null)
            get()
        val it = list!![index]
        val text = it.substring(it.lastIndexOf('\t') + 1)

        var e = 0
        text.count("{") { s, _, _ ->
            e = s
            e = text.indexOf('}', e + 1)-1
            if (e >= 0)
                block(s,e)//p.add(Pair(s, e))//if (block(b, c, v, s, e)) return count

        }
    }

    fun get(): List<CharSequence> {
        if (list != null) return list!!

        var reader: BufferedReader? = null
        var l: List<CharSequence>? = null
        try {
            reader = BufferedReader(InputStreamReader(App.instance.baseContext.assets.open(PATH), "UTF-8"))
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

        list = l
        return list!!
    }

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