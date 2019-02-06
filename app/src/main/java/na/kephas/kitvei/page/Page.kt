package na.kephas.kitvei.page

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.AudioManager
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.*
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.withContext
import na.kephas.kitvei.App
import na.kephas.kitvei.Prefs
import na.kephas.kitvei.data.Bible
import na.kephas.kitvei.util.*
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

    var showHeadings: Boolean
        get() = Prefs.headingsPref
        set(value) {
            Prefs.headingsPref = value
        }

    var showFootings: Boolean
        get() = Prefs.footingsPref
        set(value) {
            Prefs.footingsPref = value
        }

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

    @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
    fun display(vm: VerseListViewModel, row: Bible, textView: AppCompatTextView, dropCapView: TextControl?) {
        @Suppress("DeferredResultUnused")
        async(IO) {
            val runTime = measureTime {
                val sText by lazy { SpannableStringBuilder() }
                val list = vm.getVersesRaw(row.bookId!!, row.chapterId!!)
                val selectedList = mutableListOf<Int>()

                var numStart = 0
                var verse = ""

                list.forEach {
                    numStart = sText.length

                    verse = if (kjvStyling) {
                        val kjv = Formatting.kjvList[it.id - 1]
                        Formatting.diffText(it.verseText!!, kjv)
                    } else it.verseText!!

                    // Apply Periscope (Headers)
                    //sText.append(verse.replace("<","").replace(">",""))
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


                    // Apply First Word in Chapter Capitalization
                    if (showDropCap && it.verseId == 1) {
                        val firstSpace = sText.indexOf(' ')
                        val capitalized = sText.substring(0, firstSpace).toUpperCase()
                        if (firstSpace >= 0)
                            sText.delete(0, firstSpace).insert(0, capitalized)
                    }


                    // Apply italics
                    silentTry {
                        sText.count('[') { start, _, _ ->
                            if (start >= 0) {
                                val end = sText.indexOf(']', start)
                                sText.delete(start, start + 1)
                                sText.delete(end - 1, end)
                                sText.setSpan(CharacterStyle.wrap(Formatting.Italics), start, end, 0)
                            }
                        }
                    }

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


                    // Apply Line Numbers
                    val verseId = "${it.verseId}_"

                    if (showDropCap) {
                        if (it.verseId != 1 && showVerseNumbers) { // This is the only difference
                            sText.insert(numStart, verseId)
                            // Have number connected to word so they don't get separated
                            sText.setSpan(ForegroundColorSpan(Formatting.NumColor), numStart, numStart + verseId.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            sText.setSpan(ForegroundColorSpan(Formatting.Transparent), numStart + verseId.length - 1, numStart + verseId.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            sText.setSpan(RelativeSizeSpan(1f / 1.9f), numStart, numStart + verseId.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                        }
                    } else {
                        if (showVerseNumbers) {
                            sText.insert(numStart, verseId)
                            // Have number connected to word so they don't get separated
                            sText.setSpan(ForegroundColorSpan(Formatting.NumColor), numStart, numStart + verseId.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            sText.setSpan(ForegroundColorSpan(Formatting.Transparent), numStart + verseId.length - 1, numStart + verseId.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            sText.setSpan(RelativeSizeSpan(1f / 1.9f), numStart, numStart + verseId.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }


                    // Seperator
                    // We want space, but we don't want to detect space when finding in page, so we use zero width space character.
                    //if (it.verseId != list.size) sText.append("\u200B ")
                    if (it.verseId != 1) {
                        if (newLineEachVerse) sText.insert(numStart, "\u200B\n\t\t")
                        else sText.insert(numStart, "\u200B ")
                    }

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


                    var i0 = if (it.verseId!! == 1) 2 else numStart + verseId.length + if (newLineEachVerse) 3 else 2
                    if (showDropCap && newLineEachVerse && it.verseId!! == 1) i0 = 0
                    // if(it.verseId!! == 0 ) 0 else if (it.verseId!! >10) 5 else 4
                    var i1 = sText.length + if (newLineEachVerse) -1 else 0
                    /*
                    val i0 = sText.indexOf("${it.verseId}").let { i -> if (it.verseId == 1) 0 else i }
                    var i1 = sText.indexOf("${it.verseId!! + 1}").let { idx ->
                        if (idx >= 0)
                        idx
                    else
                        sText.indexOf("\u200B", i0).let { end ->
                            if (end >=0 ) end else sText.length-1
                        }
                    }*/

                    //d {"Genesis ${it.chapterId}:${it.verseId} Text: $sText"}

                    if (showVerseNumbers)
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
                                val ss = textView.text as Spannable

                                if (showParagraphs && !newLineEachVerse) i1--
                                val spans = ss.getSpans(i0, i1, BackgroundColorSpan::class.java)

                                if (spans.isNotEmpty()) {
                                    for (span in spans) {
                                        ss.removeSpan(span)
                                    }

                                    tryy {
                                        selectedList.indexOf(it.verseId!!).let { sli -> if (sli >= 0) selectedList.removeAt(sli) }
                                    }
                                    if (showDropCap && it.verseId!! == 1)
                                        (dropCapView?.text as Spannable).removeSpans(0, 1, BackgroundColorSpan::class.java)
                                } else {
                                    selectedList.add(it.verseId!!)
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


                                    Snackbar.make(v,
                                            if (selectedList.size > 1) "Selected ${it.bookName} ${it.chapterId!!}:$groupedVerses" else "Selected ${it.bookName} ${it.chapterId!!}:${it.verseId!!}"
                                            , Snackbar.LENGTH_LONG).apply {
                                        setActionTextColor(Formatting.ColorAccent)
                                        setAction("Copy") { _ ->
                                            val text = StringBuilder("")

                                            /*var str = ss.substring(i0, i1).replace("\u200B", "").replace("\n", "").replace("\t", "")
                                                    .replace("^[0-9]*".toRegex(), "").replace("_", "")
                                            val firstLetter = if (i0 == 0 && showDropCap) {
                                                val onSpace = str.indexOf(" ", i0)
                                                str = TextUtils.concat(str.substring(i0, onSpace).toLowerCase(), str.substring(onSpace)).toString()
                                                dropCapView?.text
                                            } else ""
                                            //it.verseText!!.replace("[", "").replace("]", "").replace("<", "").replace(">", "")
                                            val clip = ClipData.newPlainText("Verse text", TextUtils.concat("${it.bookName} ${it.chapterId}:$groupedVerses\n$firstLetter", str))
                                            clipboard?.primaryClip = clip*/

                                            // Get all verses (NOTE: This heavily depends on line-numbers)
                                            groupedVersesList.forEachIndexed { idx, gList ->
                                                if (gList.size == 1) { // If there's only one verse in the list
                                                    if (gList[0] == 1) { // If our verseID is the first verse in the chapter
                                                        if (showDropCap) {
                                                            text.append("1 ")
                                                            text.append(dropCapView!!.text)
                                                            text.append(ss.subSequence(0, ss.indexOf(" ")).toString().toLowerCase())
                                                            text.append("${ss.subSequence(ss.indexOf(" "), ss.indexOf('2', 0))}")
                                                        } else {
                                                            text.append("${ss.subSequence(0, ss.indexOf('2', 0))}")
                                                        }
                                                    } else if (gList[0] == list.size) { // If our verseID is the last verse in the chapter
                                                        text.append("${ss.subSequence(ss.indexOf("${gList[0]}", 0), ss.lastIndex)}")
                                                    } else {
                                                        val ii = ss.indexOf("${gList[0]}", 0)
                                                        text.append("${ss.subSequence(ii,
                                                                ss.indexOf("${gList[0] + 1}", ii))}")
                                                    }
                                                } else {
                                                    var ig = if (gList.min() == 1) 0 else ss.indexOf("${gList.min()}", 0)

                                                    if (showDropCap && gList.min() == 1) {
                                                        text.append("1 ")
                                                        text.append(dropCapView!!.text)
                                                        text.append(ss.subSequence(0, ss.indexOf(" ")).toString().toLowerCase())
                                                        ig = ss.indexOf(" ")
                                                    }
                                                    // If our verseID is the last verse in the chapter
                                                    text.append("${ss.subSequence(ig,
                                                            if (gList.max() == list.size)
                                                                ss.lastIndex
                                                            else
                                                                ss.indexOf("${gList.max()!! + 1}", ig)

                                                    )}")
                                                }
                                                // Add a newline except at the end of foreach loop
                                                //if (idx != gList.lastIndex) text.append("\n")
                                            }
                                            // Title
                                            // Post processing for periscopes
                                            var newText = text.replace("\u200B".toRegex(), "").replace("\t", "").replace("_", " ")
                                            if (selectedList.contains(1) && list[0].verseText!!.contains('<')) {
                                                newText = newText.removeRange(2..newText.indexOf("\n"))

                                            } else if (selectedList.contains(list.size) && it.verseText!!.contains("<")) { //(list[list.lastIndex].id == 31102) {
                                                newText = newText.removeRange(newText.indexOf("\n", newText.indexOf("${list.size}"))..newText.lastIndex)
                                            }

                                            // If selected only one verse, no need to copy verse number
                                            if (selectedList.size == 1) newText = newText.removeRange(0, newText.indexOf(" ") + 1)

                                            // Remove newline
                                            if (newText.last() == '\n') newText = newText.removeRange(newText.lastIndex - 1, newText.lastIndex)

                                            //text.insert(0,"${it.bookName} ${it.chapterId}:$groupedVerses\n")
                                            newText = "${it.bookName} ${it.chapterId}:$groupedVerses\n" + newText
                                            val clip = ClipData.newPlainText("Verse text", newText)
                                            clipboard?.primaryClip = clip


                                        }
                                        show()
                                    }

                                }

                                //textView.futureSet(ss)
                                //ss.removeSpan(ss.getSpans(i0,i1,ForegroundColorSpan::class.java))
                                //Selection.setSelection((textView.text as SpannableString), 0);
                            }

                        }, i0, i1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    // d {"numstart:$numStart sLen:${sText.length} text:${sText.substring(numStart,sText.length)}"}

                }

                //sText.append("\u200B\n\n\n")
                /*sText.indexOf('<').let {
                    if (it >= 0) {
                        val p0First = it
                        val p0Last = it + 1
                        val p1First = sText.indexOf('>', p0Last)
                        val p1Last = p1First + 1
                        sText.setSpan(StyleSpan(Typeface.BOLD), p0Last, p1First, 0)
                        sText.delete(p1First, p1Last + 2)
                        sText.delete(p0First, p0Last + 1)
                        if (it in 0..5)
                            sText.insert(p1First - 1, "\n")
                        else if (it in sText.length - 20..sText.length)
                            sText.insert(p0First, "\n")
                    }
                }*/
                sText.setSpan(RelativeSizeSpan(1f), 0, sText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                withContext(UI) {

                    // Order of code here is important
                    if (showDropCap && dropCapView != null) {
                        dropCapView.setText("${sText[0]}", TextView.BufferType.SPANNABLE)
                        sText.delete(0, 1)

                    }
                    textView.setTextViewLinkClickable()
                    textView.futureSet(sText)

                    if (showDropCap && dropCapView != null) {
                        dropCapView.visibility = View.GONE
                        textView.visibility = View.GONE
                        textView.post {
                            val ss = textView.text as Spannable
                            var end = 0
                            val whiteSpace = ss.indexOf("\u200B")

                            end = if (whiteSpace > 0) {
                                if (textView.layout?.getLineForOffset(whiteSpace) == 0) ss.indexOf("\u200B", whiteSpace + 1) else whiteSpace
                            } else {
                                ss.length
                            }
                            ss.removeSpans(0, ss.length, LettrineLeadingMarginSpan2::class.java)
                            ss.setSpan(LettrineLeadingMarginSpan2(2, dropCapView.getWidth), 0, end, 0)
                            dropCapView.visibility = View.VISIBLE
                            textView.visibility = View.VISIBLE

                            // Increase the text size and bring it back to normal to get rid of text clipping.
                            textView.let { itv ->
                                itv.setTextSize(TypedValue.COMPLEX_UNIT_PT, textSize + 1f)
                                itv.setTextSize(TypedValue.COMPLEX_UNIT_PT, textSize)
                            }
                        }
                    }
                }
            }.format(2)
            d { runTime }
        }


    }
}