package na.kephas.kitvei.page

import android.graphics.Color
import androidx.core.content.ContextCompat
import na.kephas.kitvei.App
import na.kephas.kitvei.R
import na.kephas.kitvei.util.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.LinkedList
import java.util.regex.Pattern

object Formatting {
    private const val KJV_PATH = "databases/KJV1611.txt"
    private const val RED_LETTER_PATH = "databases/RedLetters.txt"
    private const val PARAGRAPH_PATH = "databases/ParagraphList.txt"

    val Italics by lazy { CustomTypefaceSpan(Fonts.GentiumPlus_I) }
    val TextColor by lazy { ContextCompat.getColor(App.instance.applicationContext, R.color.textColor) }
    val TextColorPrimary by lazy { ContextCompat.getColor(App.instance.applicationContext, R.color.textColorPrimary) }
    val NumColor by lazy { Color.parseColor("#877f66") }
    val WhiteColor by lazy { Color.parseColor("#ffffff") }
    val Transparent by lazy { Color.TRANSPARENT }
    val RedLetterColor by lazy { ContextCompat.getColor(App.instance.applicationContext, R.color.redletter_color_dark) }
    val HighLightColor by lazy { ContextCompat.getColor(App.instance.applicationContext, R.color.highlight_color_dark) }
    val HighlightFocusColor by lazy { ContextCompat.getColor(App.instance.applicationContext, R.color.highlight_focus_color) }
    val SearchNotFoundColor by lazy { ContextCompat.getColor(App.instance.applicationContext, R.color.search_not_found) }
    val ColorAccent by lazy { ContextCompat.getColor(App.instance.applicationContext, R.color.colorAccent) }
    //val typeface by lazy(LazyThreadSafetyMode.NONE) { Typeface.create("sans-serif", Typeface.NORMAL) }

    private fun getList(path: String): List<CharSequence> {
        var reader: BufferedReader? = null
        var l: List<CharSequence>? = null
        try {
            reader = BufferedReader(InputStreamReader(App.instance.baseContext.assets.open(path), "UTF-8"))
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
        return l!!
    }

    val kjvList: List<CharSequence> by lazy { getList(KJV_PATH) }
    val paragraphList: List<CharSequence> by lazy { getList(PARAGRAPH_PATH) }
    val redLetterList: List<CharSequence> by lazy { getList(RED_LETTER_PATH) }
    private val dmp by lazy { diff_match_patch() }
    private val punct by lazy { "[.,;:?]".toRegex() }
    //private val excludePunct by lazy { "[^\\p{P}]".toRegex() }
    private val andPattern by lazy { Pattern.compile("(?:&|and)", Pattern.CASE_INSENSITIVE).toRegex() }

    fun diffText(pce: String, kjv: CharSequence): String {
        val list = dmp.diff_main(pce, kjv)
        dmp.diff_cleanupSemanticLossless(list)
        val diffList = LinkedList<diff_match_patch.Diff>()

        var cappedWord = false
        list.forEachIndexed { idx, diff ->

            // Skip the pair of two words
            if (cappedWord) {
                cappedWord = false
                diffList.add(diff)
                return@forEachIndexed // Add new text, then skip
            }

            // Capitalisations diffs  e.g KING; / King, | "and, &" diffs | punctuation diffs e.g fair; / faire,
            if (idx + 1 < list.size) {
                if (diff.text!!.replace(punct, "").trim().equals(list[idx + 1].text!!.replace(punct, "").trim(), ignoreCase = true)
                        || diff.text!!.contains(andPattern) && list[idx + 1].text!!.contains(andPattern)
                        || diff.text!!.contains(punct) && list[idx + 1].text!!.contains(punct)
                ) {
                    cappedWord = true
                    return@forEachIndexed // Skip
                }
            }

            // Swap delete with insert to keep original text
            if (diff.operation == diff_match_patch.Operation.DELETE)
                diffList.add(diff_match_patch.Diff(diff_match_patch.Operation.INSERT, diff.text))

            // Filter to only get original text and new punctuation
            else if (diff.operation == diff_match_patch.Operation.EQUAL || diff.text!!.contains(punct))
                diffList.add(diff)
        }
        return dmp.diff_text2(diffList)
    }

    fun redLetterPositions(index: Int, block: (start: Int, end: Int) -> Unit) {

        val it = redLetterList[index]
        val text = it.substring(it.lastIndexOf('\t') + 1)

        var e = 0
        text.count("{") { s, _, _ ->
            e = s
            e = text.indexOf('}', e + 1) - 1
            if (e >= 0)
                block(s, e)

        }
    }

}