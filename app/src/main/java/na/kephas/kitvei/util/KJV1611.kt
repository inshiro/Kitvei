package na.kephas.kitvei.util

import android.util.Log
import na.kephas.kitvei.App
import name.fraser.neil.plaintext.diff_match_patch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object KJV1611 {

    private const val PATH = "databases/KJV1611.txt"
    private var list: List<CharSequence>? = null
    private val dmp by lazy { diff_match_patch() }
    private val punct by lazy { "[.,;:]".toRegex() /* "\\p{P}".toRegex() */ }
    private val excludePunct by lazy { "[^\\p{P}]".toRegex() /* "\\p{P}".toRegex() */ }

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
        for (idx in l.indices) {
            if (l[idx].text!!.contains(punct) && l[idx].text!!.length > 1) {

                // If {P} in DELETE, we want to remove all {P} in it, leaving only the letters
                if (l[idx].operation == diff_match_patch.Operation.DELETE)
                    l[idx] = diff_match_patch.Diff(l[idx].operation, l[idx].text!!.replace(punct, ""))

                // If {P} in INSERT, we want to remove all letters in it, leaving only the {P}
                else if (l[idx].operation == diff_match_patch.Operation.INSERT)
                    l[idx] = diff_match_patch.Diff(l[idx].operation, l[idx].text!!.replace(excludePunct, ""))
            }

            // Swap DELETES and INSERTS on non {P}
            if (!l[idx].text!!.contains(punct)) {
                if (l[idx].operation == diff_match_patch.Operation.DELETE)
                    l[idx] = diff_match_patch.Diff(diff_match_patch.Operation.INSERT, l[idx].text)
                else if (l[idx].operation == diff_match_patch.Operation.INSERT)
                    l[idx] = diff_match_patch.Diff(diff_match_patch.Operation.DELETE, l[idx].text)
            }
        }
        //l pn dmp.diff_prettyHtml(l)
        return dmp.diff_text2(l)
    }
}