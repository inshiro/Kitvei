package na.kephas.kitvei.adapter

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.*
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.main_rv_item.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withContext
import na.kephas.kitvei.Prefs
import na.kephas.kitvei.R
import na.kephas.kitvei.data.Bible
import na.kephas.kitvei.model.Paragraphs
import na.kephas.kitvei.util.*
import na.kephas.kitvei.viewmodels.VerseListViewModel


class MainViewPagerAdapter(private val act: AppCompatActivity, private val vm: VerseListViewModel, private var currentList: List<Bible>) : PagerAdapter() /*, MainAdapter.ItemClickListener */ {
    companion object {
        private var fontSize = Prefs.mainFontSize
        private var showParagraphs = false
        private var newLineEachVerse = true
        private var capitalizeFirstWord = true
        private var showDropCap = true
        private var showLineNumbers = true

    }

    init {
        if (act.resources.getBoolean(R.bool.is_tablet)) {
            showParagraphs = true
            newLineEachVerse = false
            showLineNumbers = true
        }

    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    fun setFontSize(size: Float) {
        fontSize = size
    }

    private val AppCompatTextView.getWidth
        get() = {
            this.measure(0, 0)
            this.measuredWidth
        }()

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(act.applicationContext).inflate(R.layout.main_rv_item, collection, false) as ViewGroup
        collection.addView(layout)

        val row = currentList[position]
        val textView = layout.MainTextView
        val scrollView = layout.MainScrollView.apply {
            tag = "sv$position"
        }
        var dropCapView: TextControl? = null
        if (showDropCap) {
            dropCapView = TextControl(act)
            dropCapView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            textView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            //val params = ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT)
            //params.setMargins(-textView.paddingLeft,0,0,0)
            //dropCapView.layoutParams = params
            val mLayout = FrameLayout(act)
            mLayout.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            scrollView.removeAllViews()
            mLayout.addView(dropCapView, 0)
            mLayout.addView(textView, 1)
            scrollView.addView(mLayout)
            dropCapView.apply {
                tag = "dcv$position"
                typeface = Fonts.GentiumPlus_R
                setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSize * 4.85f)
                //params.setMargins(0,-10,0,0)
                setPadding(0, 0, 12, 0)
                //setIncludeFontPadding(false)

            }
        }

        textView.apply {
            tag = "tv$position"
            typeface = Fonts.GentiumPlus_R
            setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSize)
        }

        @Suppress("DeferredResultUnused")
        async(IO) {
            val sText by lazy { SpannableStringBuilder() }
            val list = vm.getVersesRaw(row.bookId!!, row.chapterId!!)
            var numStart = 0

            list.forEach {
                numStart = sText.length
                val kjv = KJV1611.get()[it.id - 1]
                val s = KJV1611.diffText(it.verseText!!, kjv)
                sText.append(s)
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
                sText.count("[") { i, _, _ ->
                    sText.indexOf('[').also {
                        if (it >= 0) {
                            val start = i
                            val end = sText.indexOf(']', i + 1)
                            sText.setSpan(CharacterStyle.wrap(AdapterStyle.Italics), start, end, 0)
                            sText.delete(i, i + 1)
                            sText.delete(end - 1, end)
                        }
                    }
                }
                val rIdx = RedLetters.get().indexOfFirst { r -> r.startsWith("${it.bookId}\t${it.chapterId}\t${it.verseId}\t") }//RedLetters.getIndexOfFirst { b, c, v, _, _ -> (it.bookId == b && it.chapterId == c && it.verseId == v) }
                if (rIdx >= 0) {
                    RedLetters.getPositions(rIdx) { s, e ->
                        //RedLetters.getPositions(rIdx).forEach { pair ->
                        tryy {
                            val i = if (e > sText.length - numStart) e - (sText.length - numStart) else 0
                            //val e = if (pair.second in sText.length-numStart-5..sText.length-numStart) sText.length else pair.second
                            val start = s - i + numStart
                            val end = e - i + numStart
                            //d { "${row.bookName} ${it.chapterId}:${it.verseId} $pair-$start-$end" }
                            if (start == 0 && it.verseId == 1 && showDropCap) {
                                dropCapView?.post {
                                    dropCapView.setTextColor(AdapterStyle.RedLetterColor)
                                }
                            }
                            sText.setSpan(ForegroundColorSpan(AdapterStyle.RedLetterColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }

                }


                // Apply Line Numbers
                val verseId = "${it.verseId}_"

                if (capitalizeFirstWord) {
                    if (it.verseId != 1 && showLineNumbers) { // This is the only difference
                        sText.insert(numStart, verseId)
                        // Have number connected to word so they don't get separated
                        sText.setSpan(ForegroundColorSpan(AdapterStyle.NumColor), numStart, numStart + verseId.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        sText.setSpan(ForegroundColorSpan(AdapterStyle.Transparent), numStart + verseId.length - 1, numStart + verseId.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        sText.setSpan(RelativeSizeSpan(1f / 1.9f), numStart, numStart + verseId.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    }
                } else {
                    if (showLineNumbers) {
                        sText.insert(numStart, verseId)
                        // Have number connected to word so they don't get separated
                        sText.setSpan(ForegroundColorSpan(AdapterStyle.NumColor), numStart, numStart + verseId.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        sText.setSpan(ForegroundColorSpan(AdapterStyle.Transparent), numStart + verseId.length - 1, numStart + verseId.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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
                            //spanRemover.RemoveOne((textView.text as SpannableString), numStart, sText.length, ForegroundColorSpan::class.java)
                            val ss = textView.text as SpannableString
                            val i0 = ss.indexOf("${it.verseId}").let { i -> if (it.verseId == 1) 0 else i }
                            var i1 = ss.indexOf("${it.verseId!! + 1}").let { idx -> if (idx >= 0) idx else ss.indexOf("\u200B", i0) }
                            if (showParagraphs && !newLineEachVerse) i1--
                            //val dottedUnderlineSpan = DottedUnderlineSpan(-0xff0100, sText.toString())

                            //val n = numStart
                            //val len = sText.length
                            val spans = ss.getSpans(i0, i1, UnderlineSpan::class.java)

                            if (spans.isNotEmpty()) {
                                for (span in spans) {
                                    ss.removeSpan(span)
                                }
                            } else {
                                ss.setSpan(UnderlineSpan(), i0, i1, 0)
                                v?.snackbar("Pressed verse ${it.verseId} numStart: $i0 len: ${i1}")
                            }

                            //textView.futureSet(ss)
                            //ss.removeSpan(ss.getSpans(i0,i1,ForegroundColorSpan::class.java))
                            //Selection.setSelection((textView.text as SpannableString), 0);

                        }

                    }, numStart, sText.length, 0)

                // Seperator
                //if (it.verseId != list.size) sText.append("\u200B ")
                if (it.verseId != 1) {
                    if (newLineEachVerse) sText.insert(numStart, "\u200B\n\t\t")
                    else sText.insert(numStart, "\u200B ")
                }

                if (showParagraphs) {
                    // Apply paragraph breaks
                    val pIdx = Paragraphs.get().indexOfFirst { p -> p == "${it.bookAbbr} ${it.chapterId}:${it.verseId}" }//paragraphList.indexOfFirst { p -> (it.bookAbbr == p.bookAbbr && it.chapterId == p.chapterId && it.verseId == p.verseId) }
                    //d { "${Paragraphs.get()}}" }
                    //d { "${it.bookAbbr}\t${it.chapterId}:${it.verseId}" }
                    if (pIdx >= 0 && it.verseId != 1) {
                        d { "${row.bookName} ${it.chapterId}:${it.verseId} $pIdx" }

                        if (numStart == 0)
                            sText.insert(numStart, "\t\t\t")
                        else if (numStart > 0)
                            sText.insert(numStart, "\n\t\t\t")
                    }
                }
                // Seperator
                // We want space, but we don't want to detect space when finding in page, so we use zero width space character.


            }

            //sText.append("\u200B\n\n\n")
            sText.indexOf('<').let {
                if (it >= 0) {
                    val p0First = it
                    val p0Last = it + 1
                    val p1First = sText.indexOf('>')
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

        }
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return currentList.size
    }


    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === (`object` as View)
    }
}