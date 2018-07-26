package na.kephas.kitvei.page

import android.graphics.Color
import androidx.core.content.ContextCompat
import na.kephas.kitvei.App
import na.kephas.kitvei.R
import na.kephas.kitvei.page.Formatting.redLetterList
import na.kephas.kitvei.util.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object Formatting {
    private const val KJV_PATH = "databases/KJV1611.txt"
    private const val RED_LETTER_PATH = "databases/RedLetters.txt"
    private const val PARAGRAPH_PATH = "databases/ParagraphList.txt"
    private val dmp by lazy { diff_match_patch() }
    private val punct by lazy { "[.,;:]".toRegex() }
    private val excludePunct by lazy { "[^\\p{P}]".toRegex() }

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

    fun getList(path: String): List<CharSequence> {
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

    private fun diff_match_patch.Diff.swap(): diff_match_patch.Diff {
        return when (operation) {
            diff_match_patch.Operation.DELETE -> diff_match_patch.Diff(diff_match_patch.Operation.INSERT, text)
            diff_match_patch.Operation.INSERT -> diff_match_patch.Diff(diff_match_patch.Operation.DELETE, text)
            else -> this
        }
    }

    fun diffText(pce: String, kjv: CharSequence): String {

        val diffList = dmp.diff_main(pce, kjv)
        dmp.diff_cleanupSemanticLossless(diffList)
        // CAPITALIZE
        for (idx in diffList.indices) {
            if (idx + 1 < diffList.size && diffList[idx].text.equals(diffList[idx + 1].text, ignoreCase = true)) {
                diffList[idx] = diff_match_patch.Diff(diffList[idx].operation, diffList[idx + 1].text)
            }
        }

        // Filter to only get Original PCE and new punctuation
        val l = diffList.filter {
            it.operation == diff_match_patch.Operation.EQUAL ||
                    it.operation == diff_match_patch.Operation.DELETE || // Keep Original PCE Text
                    it.text!!.contains(punct)
        }.toMutableList()


        if (pce.contains("O give thanks unto the")) {
            d { dmp.diff_text2(l) }
            d { dmp.diff_prettyHtml(l) }
        }
        var cappedWord = false
        for (idx in l.indices) {

            // Skip the pair of two words that are same in text, case insensitive, e.g KING; | King,
            if (cappedWord) {
                cappedWord = false
                continue
            }
            if (idx + 1 < l.size && l[idx].text!!.replace(punct, "").equals(l[idx + 1].text!!.replace(punct, ""), ignoreCase = true)) {
                cappedWord = true
                continue
            }
            if (l[idx].text!!.contains(punct)) {

                // Cleanup punctuation
                if (l[idx].text!!.length > 1) {

                        // If {P} in DELETE, we want to remove all {P} in it, leaving only the letters
                        if (l[idx].operation == diff_match_patch.Operation.DELETE)
                            l[idx] = diff_match_patch.Diff(l[idx].operation, l[idx].text!!.replace(punct, ""))

                        // If {P} in INSERT, we want to remove all letters in it, leaving only the {P}
                        else if (l[idx].operation == diff_match_patch.Operation.INSERT)
                            l[idx] = diff_match_patch.Diff(l[idx].operation, l[idx].text!!.replace(excludePunct, ""))


                    if (pce.contains("O give thanks unto the"))
                        d { "Inside punct Swapped now: ${l[idx]}" }

                }

                // Swap on isolated punctuation to retain, e.g Paul,
                /*if (idx + 1 < l.size)
                    if (l[idx+1].operation == l[idx].operation || l[idx + 1].operation == diff_match_patch.Operation.EQUAL)
                        l[idx] = l[idx].swap()
                    else if (idx == l.size-1 && l[idx].text!!.length == 1)
                        l[idx] = l[idx].swap()*/
            }

            // Swap DELETES and INSERTS on non {P}
            else {
                l[idx] = l[idx].swap()
            }
        }
        return dmp.diff_text2(l)
    }

    fun redLetterPositions(index: Int, block: (start: Int, end: Int) -> Unit) {

        val it = redLetterList[index]
        val text = it.substring(it.lastIndexOf('\t') + 1)

        var e = 0
        text.count("{") { s, _, _ ->
            e = s
            e = text.indexOf('}', e + 1) - 1
            if (e >= 0)
                block(s, e)//p.add(Pair(s, e))//if (block(b, c, v, s, e)) return count

        }
    }

}