package na.kephas.kitvei.page

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.AudioManager
import android.text.*
import android.text.style.*
import android.util.TypedValue
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import na.kephas.kitvei.App
import na.kephas.kitvei.Prefs
import na.kephas.kitvei.data.Bible
import na.kephas.kitvei.util.*
import na.kephas.kitvei.viewmodels.Coroutines
import na.kephas.kitvei.viewmodels.VerseListViewModel


object Page {
    var showDropCap: Boolean
        get() = Prefs.dropCapPref
        set(value) {
            Prefs.dropCapPref = value
        }

    var showParagraphs: Boolean
        get() = Prefs.pBreakPref
        set(value) {
            Prefs.pBreakPref = value
        }

    var showRedLetters: Boolean
        get() = Prefs.redLetterPref
        set(value) {
            Prefs.redLetterPref = value
        }

    var showVerseNumbers: Boolean
        get() = Prefs.verseNumberPref
        set(value) {
            Prefs.verseNumberPref = value
        }

    var newLineEachVerse: Boolean
        get() = Prefs.seperateVersePref
        set(value) {
            Prefs.seperateVersePref = value
        }

    /*var showHeadings: Boolean
        get() = Prefs.headingsPref
        set(value) {
            Prefs.headingsPref = value
        }

    var showFootings: Boolean
        get() = Prefs.footingsPref
        set(value) {
            Prefs.footingsPref = value
        }*/

    var kjvStyling: Boolean
        get() = Prefs.kjvStylingPref
        set(value) {
            Prefs.kjvStylingPref = value
        }

    var textSize: Float
        get() = Prefs.mainFontSize
        set(value) {
            Prefs.mainFontSize = value
        }

    private val clipboard by lazy { App.instance.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager? }
    private val audioManager by lazy { App.instance.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager? }


    fun display(vm: VerseListViewModel, row: Bible, textView: AppCompatTextView, dropCapView: TextControl?) {

        val sText by lazy { SpannableStringBuilder() }
        val selectedList by lazy { mutableListOf<Int>() }
        val selectedListVerses by lazy { mutableListOf<Pair<Int, Int>>() }

        var numStart = 0
        var verse = ""
        var cleanVerse = ""
        var verseId = ""


        Coroutines.ioThenMain({
            // Process each verse
            val list = vm.getVersesRaw(row.bookId!!, row.chapterId!!)
            list.forEach {
                numStart = sText.length

                // KJV Styling
                verse = if (kjvStyling) {
                    val kjv = Formatting.kjvList[it.id - 1]
                    Formatting.diffText(it.verseText!!, kjv)
                } else it.verseText!!

                // Periscope (Headers/Footers)
                tryy {
                    if (it.verseId!! == 1 && verse[0] == '<') {
                        val endBoldPosition = verse.replace("<", "").indexOf('>')
                        sText.append(verse.replace("<", "").replace(">", ""))
                        sText.setSpan(CharacterStyle.wrap(StyleSpan(android.graphics.Typeface.BOLD)), 0, endBoldPosition, 0)
                        sText.insert(endBoldPosition + 1, "\n")
                    } else if (it.verseId!! == list.size && it.verseText!!.contains("<")) {//(it.id == 31102) { // Last verse in the Bible
                        val idx = sText.lastIndex
                        sText.append(verse)
                        val startBoldPosition = sText.indexOf('<', idx)
                        sText.delete(startBoldPosition, startBoldPosition + 2)
                        sText.delete(sText.indexOf('>', idx), sText.length)
                        sText.setSpan(CharacterStyle.wrap(StyleSpan(android.graphics.Typeface.BOLD)), startBoldPosition, sText.lastIndex, 0)
                        sText.insert(startBoldPosition - 1, "\n")
                    } else
                        sText.append(verse)
                }

                // First Word Capitalization for Drop Cap
                if (showDropCap && it.verseId == 1) {
                    val firstSpace = sText.indexOf(' ')
                    val capitalized = sText.substring(0, firstSpace).toUpperCase()
                    if (firstSpace >= 0)
                        sText.delete(0, firstSpace).insert(0, capitalized)
                }

                // Italics
                tryy {
                    sText.count('[') { start, _, _ ->
                        if (start >= 0) {
                            val end = sText.indexOf(']', start)
                            sText.delete(start, start + 1)
                            sText.delete(end - 1, end)
                            sText.setSpan(CharacterStyle.wrap(Formatting.Italics), start, if (end <= sText.length) end else sText.length, 0)
                        }
                    }
                }

                cleanVerse = sText.substring(numStart, sText.length)

                // Red Letters
                if (showRedLetters)
                    tryy {
                        val rIdx = Formatting.redLetterList.indexOfFirst { r -> r.startsWith("${it.bookId}\t${it.chapterId}\t${it.verseId}\t") }//RedLetters.getIndexOfFirst { b, c, v, _, _ -> (it.bookId == b && it.chapterId == c && it.verseId == v) }
                        if (rIdx >= 0) {
                            Formatting.redLetterPositions(rIdx) { s, e ->
                                //RedLetters.getPositions(rIdx).forEach { pair ->
                                val actualLength = sText.length - numStart
                                val extraLetters = if (e > actualLength) e - actualLength else 0
                                //val e = if (pair.second in sText.length-numStart-5..sText.length-numStart) sText.length else pair.second
                                var start = s - extraLetters + numStart
                                var end = e - extraLetters + numStart
                                if (end in actualLength - 2..actualLength) end = sText.length
                                //d { "${row.bookName} ${it.chapterId}:${it.verseId} $pair-$start-$end" }
                                if (start <= 0 && it.verseId == 1 && showDropCap) {
                                    dropCapView?.post {
                                        dropCapView.setTextColor(Formatting.RedLetterColor)
                                    }
                                }

                                if (kjvStyling) {
                                    if (verse.length - it.verseText!!.length > 0)
                                        end += verse.length - it.verseText!!.length
                                    else
                                        end += it.verseText!!.length - verse.length
                                    if (showDropCap && it.verseId == 1)
                                        end--

                                    // Rev 3:1
                                    if (showDropCap && it.id == 30748) {
                                        start = 0
                                        end--
                                    }

                                }
                                if (start < 0) start = 0
                                if (end > sText.lastIndex) end = sText.length
                                sText.setSpan(ForegroundColorSpan(Formatting.RedLetterColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                        }
                    }


                // Line Numbers
                if (showVerseNumbers) {
                    verseId = "${it.verseId}_"
                    if (!(it.verseId == 1 && showDropCap)) { // Do not add a number if it's the 1st verse and if we show Drop Cap

                        // Show line numbers
                        sText.insert(numStart, verseId)
                        sText.setSpan(ForegroundColorSpan(Formatting.NumColor), numStart, numStart + verseId.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        sText.setSpan(ForegroundColorSpan(Formatting.Transparent), numStart + verseId.length - 1, numStart + verseId.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        sText.setSpan(RelativeSizeSpan(1f / 1.9f), numStart, numStart + verseId.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    }
                }

                // Separator
                if (it.verseId != 1) {
                    // Use zero width space character to have undetectable space.
                    if (newLineEachVerse) sText.insert(numStart, "\u200B\n\t\t")
                    else sText.insert(numStart, "\u200B ")
                }

                // Paragraphs
                if (showParagraphs) {
                    // Apply paragraph breaks
                    val pIdx = Formatting.paragraphList.indexOfFirst { p -> p == "${it.bookAbbr} ${it.chapterId}:${it.verseId}" }//paragraphList.indexOfFirst { p -> (it.bookAbbr == p.bookAbbr && it.chapterId == p.chapterId && it.verseId == p.verseId) }
                    if (pIdx >= 0 && it.verseId != 1) {
                        //d { "${it.bookName} ${it.chapterId}:${it.verseId} $pIdx" }

                        if (numStart == 0)
                            sText.insert(numStart, "\t\t\t")
                        else if (numStart > 0)
                            sText.insert(numStart, "\n\t\t\t")
                    }
                }

                // Copying function
                val i0 = sText.indexOf(cleanVerse, if (numStart!=0) numStart-3 else 0).let { si ->
                    if (si < 0) numStart else {
                        if (showDropCap && it.verseId != 1) si - 1 else si
                    }
                }
                val i1 = sText.length + if (it.verseId!! == list.size) -1 else 0
                sText.setSpan(object : ClickableSpan() {

                    override fun updateDrawState(ds: TextPaint) {
                        // When using a clickable span, like a link, the text changes to be colored and underlined.
                        //ds.color = Formatting.TextColor

                        // Initial state of text, underlined or not.
                        ds.isUnderlineText = false
                    }

                    override fun onClick(v: View) {
                        audioManager?.playSoundEffect(android.view.SoundEffectConstants.CLICK)

                        //This condition will satisfy only when it is not an autolinked text
                        //Fired only when you touch the part of the text that is not hyperlinked
                        //val ss = textView.text as SpannableString
                        //val ss = textView.text as Spannable
                        val ss = textView.text as Spannable
                        //if (showParagraphs && !newLineEachVerse) i1--
                        val spans = ss.getSpans(i0, i1, BackgroundColorSpan::class.java)

                        if (spans.isNotEmpty()) {
                            for (span in spans) {
                                ss.removeSpan(span)
                            }

                            tryy {
                                selectedList.indexOf(it.verseId!!).let { sli -> if (sli >= 0) selectedList.removeAt(sli) }
                                selectedListVerses.indexOf(Pair(i0, i1)).let { sliv -> if (sliv >= 0) selectedListVerses.removeAt(sliv) }
                            }
                            if (showDropCap && it.verseId!! == 1)
                                (dropCapView?.text as Spannable).removeSpans(0, 1, BackgroundColorSpan::class.java)
                        } else {
                            selectedList.add(it.verseId!!)
                            selectedListVerses.add(Pair(i0, i1))
                            //BackgroundColorSpan(Color.BLUE)

                            ss.setSpan(BackgroundColorSpan(Formatting.DefaultSelectColor), i0, i1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                            //ss.setSpan(UnderlineSpan(), i0, i1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)


                            //v?.snackbar("Pressed verse ${it.verseId} numStart: $i0 len: ${i1}")


                            if (showDropCap && it.verseId!! == 1)
                                (dropCapView?.text as Spannable).setSpan(BackgroundColorSpan(Formatting.DefaultSelectColor), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                            // Sort consecutive numbers of selected verses to cleanly display: 1, 4-5, 7
                            val groupedVersesList = selectedList.groupConsecutive()
                            var groupedVerses = ""
                            groupedVersesList.forEachIndexed { idx, it ->
                                if (it.size > 1)
                                    groupedVerses += "${it.min()}-${it.max()}"
                                else
                                    groupedVerses += it[0]
                                if (groupedVerses.isNotEmpty() && idx != groupedVersesList.size - 1)
                                    groupedVerses += ", "
                            }


                            selectedListVerses.sortBy { slv -> slv.first }
                            Snackbar.make(v,
                                    if (selectedList.size > 1) "Selected ${it.bookName} ${it.chapterId!!}:$groupedVerses" else "Selected ${it.bookName} ${it.chapterId!!}:${it.verseId!!}"
                                    , Snackbar.LENGTH_LONG).apply {
                                setActionTextColor(Formatting.ColorAccent)
                                setAction("Copy") { _ ->
                                    val text = StringBuilder("")

                                    var c = 0
                                    groupedVersesList.forEachIndexed { index, lst ->
                                        for (id in lst.min()!!..lst.max()!!) {
                                            //if (groupedVersesList.size == 1) text.append("$id ")
                                            if (selectedListVerses[c].first == 0 && showDropCap) {
                                                val d = textView.text.substring(selectedListVerses[c].first, selectedListVerses[c].second)
                                                val di = d.indexOf(" ")
                                                val newD = d.substring(0, di).toLowerCase() + d.substring(di, d.length)
                                                text.append("$id ${dropCapView!!.text}$newD")
                                            } else
                                                text.append("$id " + textView.text.substring(selectedListVerses[c].first, selectedListVerses[c].second))
                                            text.append("\n")
                                            c++
                                        }
                                    }

                                    // Remove newline
                                    if (text.last() == '\n') text.delete(text.length - 1, text.length)

                                    // Title
                                    text.insert(0, "${it.bookName} ${it.chapterId}:$groupedVerses\n")
                                    val clip = ClipData.newPlainText("Verse text", text.toString())
                                    clipboard?.primaryClip = clip

                                    // Remove spans
                                    val spans2 = (textView.text as Spannable).getSpans(0, textView.text.length, BackgroundColorSpan::class.java)
                                    for (span in spans2) { ss.removeSpan(span) }
                                    selectedList.clear()
                                    selectedListVerses.clear()
                                    if (showDropCap)
                                        (dropCapView?.text as Spannable).removeSpans(0, 1, BackgroundColorSpan::class.java)
                                }
                                show()
                            }

                        }

                        //textView.futureSet(ss)
                        //ss.removeSpan(ss.getSpans(i0,i1,ForegroundColorSpan::class.java))
                        //Selection.setSelection((textView.text as SpannableString), 0);
                    }

                }, i0, i1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            sText

        }) {
            // Set Drop Cap first
            if (showDropCap) {
                dropCapView?.setText("${sText[0]}", TextView.BufferType.SPANNABLE)
                sText.delete(0, 1)
            }

            // Finally set text
            textView.setTextViewLinkClickable()
            textView.futureSet(it!!)

            // Post processing for Drop Cap
            if (showDropCap && dropCapView != null) {
                dropCapView.visibility = View.INVISIBLE
                textView.visibility = View.INVISIBLE
                textView.post {
                    val ss = textView.text as Spannable
                    var end = 0
                    val whiteSpace = ss.indexOf("\u200B")
                    val lo = textView.layout?.getLineForOffset(whiteSpace)
                    end = if (whiteSpace > 0) {
                        if (lo == 0) ss.indexOf("\u200B", whiteSpace + 1) else whiteSpace
                    } else {
                        ss.length
                    }
                    ss.removeSpans(0, ss.length, LettrineLeadingMarginSpan2::class.java)
                    Coroutines.ioThenMain({
                        while (dropCapView.width <= 0) delay(100)
                        dropCapView.width
                    }) { dWidth ->
                        if (dWidth != null && dWidth > 0) {
                            ss.setSpan(LettrineLeadingMarginSpan2(2, dWidth), 0, end, 0)

                            textView.let { itv ->
                                // Increase the text size and bring it back to normal to get rid of text clipping.
                                itv.setTextSize(TypedValue.COMPLEX_UNIT_PT, textSize + 1f)
                                itv.setTextSize(TypedValue.COMPLEX_UNIT_PT, textSize)
                                itv.post {
                                    textView.layout?.let { tvl ->
                                        end = if (whiteSpace > 0) {
                                            if (tvl.getLineForOffset(whiteSpace) == 0) ss.indexOf("\u200B", whiteSpace + 1) else whiteSpace
                                        } else {
                                            ss.length
                                        }
                                        ss.removeSpans(0, ss.length, LettrineLeadingMarginSpan2::class.java)
                                        ss.setSpan(LettrineLeadingMarginSpan2(2, dWidth), 0, end, 0)
                                    }
                                    dropCapView.visibility = View.VISIBLE
                                    textView.visibility = View.VISIBLE
                                }
                            }

                        }
                    }

                }
            }


        }

    }
}