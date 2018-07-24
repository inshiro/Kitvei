package na.kephas.kitvei.adapter

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.PrecomputedTextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.main_rv_item.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withContext
import na.kephas.kitvei.Prefs
import na.kephas.kitvei.R
import na.kephas.kitvei.data.Bible
import na.kephas.kitvei.util.*


class MainAdapter(context: Context) : PagedListAdapter<Bible, MainAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val redLetters by lazy { RedLetters.get() }
    private val TextColor by lazy { AdapterStyle.TextColor }
    private val NumColor by lazy { AdapterStyle.NumColor }
    private val RedLetterColor by lazy { AdapterStyle.RedLetterColor }
    private val HighLightColor by lazy { AdapterStyle.HighLightColor }
    private val HighlightFocusColor by lazy { AdapterStyle.HighlightFocusColor }

    val animationStates: BooleanArray by lazy {
        BooleanArray(currentList!!.size).let {
            if (currentList!!.size < 0) BooleanArray(1) else
            //it[10] = true
                it

        }
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.main_rv_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))

    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id?.toLong() ?: position.toLong()
    }

    private var mClickListener: ItemClickListener? = null

    internal fun setClickListener(itemClickListener: ItemClickListener) {
        this.mClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    fun setFontSize(size: Float) {
        fontSize = size
    }

    fun findInPage(query: String?) {
        findInPageQuery = query
    }

    fun fixMatchesListSize(size: Int) {
        matchesList = IntArray(size)
    }

    fun setCurrentlyHighlighted(index: Int?, element: Int?) {
        currentHighlightIndex = index
        currentHighlightElementIndex = element
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        // private val isRTL: Boolean by lazy { TextUtilsCompat.getLayoutDirectionFromLocale(java.util.Locale.getDefault()) != ViewCompat.LAYOUT_DIRECTION_LTR }
        private val textView = itemView.MainTextView

        init {
            textView.setOnClickListener(this)
            //textView.typeface = Fonts.GentiumPlus_R

            //if (isRTL && textView.rotationY != 180f)
            //     textView.rotationY = 180f
        }

        override fun onClick(view: View) {
            mClickListener?.onItemClick(view, adapterPosition)
        }

        fun bind(row: Bible?) {
            if (row != null && adapterPosition >= 0) {

                @Suppress("DeferredResultUnused")
                async(fixedThreadPool) {


                    /*
                if (adapterPosition <= 7) {
                if (!animationStates[adapterPosition]) {
                    //Log.d("TAG", "Animating item no: $position")
                    animationStates[adapterPosition] = true
                    val animation = AnimationUtils.loadAnimation(textView.context, R.anim.slide_in_right) //android.R.anim.slide_in_left
                    animation.startOffset = (adapterPosition * 50).toLong()
                    textView.startAnimation(animation)

                }
                }*/
                    //textView.precomputeAndSet {
                    val sText = SpannableStringBuilder(row.verseId.toString())

                    sText.apply {
                        setSpan(ForegroundColorSpan(NumColor), 0, sText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        append("  ")
                        setSpan(RelativeSizeSpan(fontSize / 2f), 0, sText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    val numsLen = sText.length

                    /* var text = row.verseText!!

                     // Trim << [TEXT] >>
                     if (text.indexOf('<') == 0) {
                         text = text.substring(text.lastIndexOf('>') + 2)
                     } else if (text.contains('<')) {
                         text = text.substring(0, text.indexOf('<') - 1)
                     }

                     val sText = SpannableStringBuilder(text)*/

                    sText.append(row.verseText!!)

                    sText.indexOf('<').also {
                        if (it == numsLen)
                            sText.delete(numsLen, sText.lastIndexOf('>') + 2)
                        else if (it > numsLen)
                            sText.delete(it - 1, sText.length)
                    }


                    // Apply italics
                    val count = sText.count("[")
                    for (c in 1..count) {
                        sText.indexOf('[').also {
                            if (it >= 0) {
                                val start = it
                                val end = sText.indexOf(']', it + 1)
                                sText.setSpan(CharacterStyle.wrap(CustomTypefaceSpan(Fonts.GentiumPlus_I)) as CharacterStyle, start, end, 0)
                                sText.delete(it, it + 1)
                                sText.delete(end - 1, end)
                            }
                        }
                    }

                    sText.setSpan(ForegroundColorSpan(TextColor), numsLen, sText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    sText.setSpan(RelativeSizeSpan(fontSize), numsLen, sText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    if (findInPageQuery != null)
                        matchesList[adapterPosition] = sText.count(findInPageQuery!!) { start, end, countSoFar ->
                            sText.setSpan(CharacterStyle.wrap(BackgroundColorSpan(
                                    if (currentHighlightElementIndex != null && currentHighlightElementIndex == countSoFar && currentHighlightIndex == adapterPosition)
                                        HighlightFocusColor
                                    else
                                        HighLightColor
                            ) as CharacterStyle), start, end, 0)
                        }

                    // Get the index of correct RedLetter to style the current verse
                    /*val idx = redLetters.indexOfFirst { (row.bookId == it.bookId && row.chapterId == it.chapterId && row.verseId == it.verseId) }
                    if (idx >= 0) {
                        //d { "redIdx: $idx pos: $adapterPosition ${row.bookName} ${row.chapterId}:${row.verseId} ${ redLetters.elementAtOrNull(idx)?.positions}|$sText" }
                        redLetters.elementAtOrNull(idx)?.run {
                            for (pair in positions)
                                sText.setSpan(CharacterStyle.wrap(ForegroundColorSpan(RedLetterColor) as CharacterStyle), pair.first + numsLen,
                                        if (pair.second - 1 + numsLen > sText.length) pair.second - 2 + numsLen else pair.second - 1 + numsLen, 0)
                        }
                    }*/
                    //}

                    sText.insert(0, "\t\t")

                    withContext(UI) {
                        // start precompute
                        val future = PrecomputedTextCompat.getTextFuture(sText, (textView as AppCompatTextView).textMetricsParamsCompat, IO_EXECUTOR)
                        // and pass future to TextView, which awaits result before measuring
                        textView.setTextFuture(future!!)

                        //d {"Setting Text at $adapterPosition"}
                        //TextUtils.concat("\t\t", nums, sText)
                        //textView.setText(TextUtils.concat("\t\t", nums, sText), TextView.BufferType.SPANNABLE)
                    }
                }

            }
        }
    }

    companion object {
        private var fontSize = Prefs.textSizeMultiplier
        private var findInPageQuery: String? = null
        private var matchesList = IntArray(1)
        private var currentHighlightIndex: Int? = null
        private var currentHighlightElementIndex: Int? = null

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Bible>() {
            override fun areItemsTheSame(oldItem: Bible, newItem: Bible): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Bible, newItem: Bible): Boolean = oldItem == newItem
        }
    }

}