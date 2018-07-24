package na.kephas.kitvei.model

import na.kephas.kitvei.App
import na.kephas.kitvei.util.RedLetter
import na.kephas.kitvei.util.d
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

data class Paragraph(val bookAbbr: CharSequence, val chapterId: Int, val verseId: Int)

object Paragraphs {
    private const val PATH = "databases/ParagraphList.txt"
    private var list: List<CharSequence>? = null
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