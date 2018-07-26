package na.kephas.kitvei.page

import android.graphics.Typeface
import android.text.style.*
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withContext
import na.kephas.kitvei.Prefs
import na.kephas.kitvei.data.Bible
import na.kephas.kitvei.util.*
import na.kephas.kitvei.viewmodels.VerseListViewModel
import android.R.attr.label
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.text.*
import androidx.core.content.ContextCompat.getSystemService
import na.kephas.kitvei.App


object Page {

    private var fontSize = Prefs.mainFontSize
    private var showParagraphs = false
    private var newLineEachVerse = true
    private var capitalizeFirstWord = true
    var showDropCap = true
     var showLineNumbers = true
    var kjvPunctuation = true
    fun setFontSize(size: Float) {
        fontSize = size
    }

    private val clipboard by lazy {  App.instance.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?  }

    fun display(vm: VerseListViewModel, row: Bible, textView: AppCompatTextView, dropCapView: TextControl?) {
        @Suppress("DeferredResultUnused")
        async(IO) {
            d {
                measureTime {
                    val sText by lazy { SpannableStringBuilder() }
                    val list = vm.getVersesRaw(row.bookId!!, row.chapterId!!)

                    var numStart = 0
                    var verse = ""

                    list.forEach {
                        numStart = sText.length
                        verse = if (kjvPunctuation) {
                            val kjv = Formatting.kjvList[it.id - 1]
                            Formatting.diffText(it.verseText!!, kjv)
                        } else it.verseText!!
                        sText.append(verse)

                        /*
                        // Apply Periscope
                        var text = row.verseText!!
                                 // Trim << [TEXT] >>
                                 if (text.indexOf('<') == 0) {
                                     text = text.substring(text.lastIndexOf('>') + 2)
                                 } else if (text.contains('<')) {
                                     text = text.substring(0, text.indexOf('<') - 1)
                                 }
                                 val sText = SpannableStringBuilder(text)


                        sText.indexOf('<').also {
                            if (it == numsLen)
                                sText.delete(numsLen, sText.lastIndexOf('>') + 2)
                            else if (it > numsLen)
                                sText.delete(it - 1, sText.length)
                        }*/

                        // Apply First Word in Chapter Capitalization
                        if (capitalizeFirstWord)
                            if (it.verseId == 1) {
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
                        tryy {
                            val rIdx = Formatting.redLetterList.indexOfFirst { r -> r.startsWith("${it.bookId}\t${it.chapterId}\t${it.verseId}\t") }//RedLetters.getIndexOfFirst { b, c, v, _, _ -> (it.bookId == b && it.chapterId == c && it.verseId == v) }
                            if (rIdx >= 0) {
                                Formatting.redLetterPositions(rIdx) { s, e ->
                                    //RedLetters.getPositions(rIdx).forEach { pair ->
                                    val actualLength = sText.length - numStart
                                    val extraLetters = if (e > actualLength) e - actualLength else 0
                                    //val e = if (pair.second in sText.length-numStart-5..sText.length-numStart) sText.length else pair.second
                                    val start = s - extraLetters + numStart
                                    var end = e - extraLetters + numStart
                                    if (end in actualLength - 2..actualLength) end = sText.length
                                    //d { "${row.bookName} ${it.chapterId}:${it.verseId} $pair-$start-$end" }
                                    if (start <= 0 && it.verseId == 1 && showDropCap) {
                                        dropCapView?.post {
                                            dropCapView.setTextColor(Formatting.RedLetterColor)
                                        }
                                    }
                                    sText.setSpan(ForegroundColorSpan(Formatting.RedLetterColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                }
                            }
                        }


                        // Apply Line Numbers
                        val verseId = "${it.verseId}_"

                        if (capitalizeFirstWord) {
                            if (it.verseId != 1 && showLineNumbers) { // This is the only difference
                                sText.insert(numStart, verseId)
                                // Have number connected to word so they don't get separated
                                sText.setSpan(ForegroundColorSpan(Formatting.NumColor), numStart, numStart + verseId.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                sText.setSpan(ForegroundColorSpan(Formatting.Transparent), numStart + verseId.length - 1, numStart + verseId.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                sText.setSpan(RelativeSizeSpan(1f / 1.9f), numStart, numStart + verseId.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                            }
                        } else {
                            if (showLineNumbers) {
                                sText.insert(numStart, verseId)
                                // Have number connected to word so they don't get separated
                                sText.setSpan(ForegroundColorSpan(Formatting.NumColor), numStart, numStart + verseId.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                sText.setSpan(ForegroundColorSpan(Formatting.Transparent), numStart + verseId.length - 1, numStart + verseId.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                sText.setSpan(RelativeSizeSpan(1f / 1.9f), numStart, numStart + verseId.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                        }

                        //d {"Genesis ${it.chapterId}:${it.verseId} Text: $sText"}
                        if (showLineNumbers)
                            sText.setSpan(object : ClickableSpan() {
                                override fun updateDrawState(ds: TextPaint?) {
                                    //ds?.color = AdapterStyle.TextColor
                                    ds?.isUnderlineText = false
                                }

                                override fun onClick(v: View?) {
                                    //This condition will satisfy only when it is not an autolinked text
                                    //Fired only when you touch the part of the text that is not hyperlinked
                                    val ss = textView.text as SpannableString
                                    val i0 = ss.indexOf("${it.verseId}").let { i -> if (it.verseId == 1) 0 else i }
                                    var i1 = ss.indexOf("${it.verseId!! + 1}").let { idx -> if (idx >= 0) idx else ss.indexOf("\u200B", i0) }
                                    if (showParagraphs && !newLineEachVerse) i1--
                                    val spans = ss.getSpans(i0, i1, UnderlineSpan::class.java)

                                    if (spans.isNotEmpty()) {
                                        for (span in spans) {
                                            ss.removeSpan(span)
                                        }
                                    } else {
                                        ss.setSpan(UnderlineSpan(), i0, i1, 0)
                                        //v?.snackbar("Pressed verse ${it.verseId} numStart: $i0 len: ${i1}")
                                        v?.let { _ ->
                                            Snackbar.make(v, "Selected verse ${it.verseId}", Snackbar.LENGTH_LONG).apply {
                                                setActionTextColor(Formatting.ColorAccent)
                                                setAction("Copy") { _ ->
                                                    var str = ss.substring(i0,i1).replace("\u200B","").replace("\n","").replace("\t","")
                                                            .replace("^[0-9]*".toRegex(),"").replace("_","")
                                                    val firstLetter = if (i0==0 && showDropCap) {
                                                        val onSpace = str.indexOf(" ", i0)
                                                        str = TextUtils.concat(str.substring(i0,onSpace).toLowerCase(), str.substring(onSpace)).toString()
                                                        dropCapView?.text
                                                    }
                                                    else ""
                                                    //it.verseText!!.replace("[", "").replace("]", "").replace("<", "").replace(">", "")
                                                    val clip = ClipData.newPlainText("verse text", TextUtils.concat("${it.bookName} ${it.chapterId}:${it.verseId}\n$firstLetter", str) )
                                                    clipboard?.primaryClip = clip
                                                    v.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                                                }
                                                show()
                                            }
                                        }

                                    }

                                    //textView.futureSet(ss)
                                    //ss.removeSpan(ss.getSpans(i0,i1,ForegroundColorSpan::class.java))
                                    //Selection.setSelection((textView.text as SpannableString), 0);

                                }

                            }, numStart, sText.length, 0)

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
                                d { "${it.bookName} ${it.chapterId}:${it.verseId} $pIdx" }

                                if (numStart == 0)
                                    sText.insert(numStart, "\t\t\t")
                                else if (numStart > 0)
                                    sText.insert(numStart, "\n\t\t\t")
                            }
                        }
                    }

                    //sText.append("\u200B\n\n\n")
                    sText.indexOf('<').let {
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
                    }
                    sText.setSpan(RelativeSizeSpan(1f), 0, sText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    withContext(UI) {

                        if (showDropCap && dropCapView != null) {
                            //val dText = SpannableStringBuilder("${sText[0]}")
                            // dText.setSpan(RelativeSizeSpan(30f), 0, dText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                            dropCapView.setText("${sText[0]}", TextView.BufferType.SPANNABLE)

                            sText.delete(0, 1)

                            sText.setSpan(LettrineLeadingMarginSpan2(2, dropCapView.getWidth), 0, if (showLineNumbers) sText.indexOf("2") - 2 else list[0].verseText!!.length - 2, 0)
                        }
                        textView.setTextViewLinkClickable()
                        textView.futureSet(sText)
                        //textView.movementMethod = LinkMovementMethod.getInstance()
                        //textView.movementMethod = ScrollingMovementMethod.getInstance()
                    }
                }.format(2)
            }
        }


    }
}