package na.kephas.kitvei.adapter

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.BackgroundColorSpan
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import na.kephas.kitvei.R
import na.kephas.kitvei.data.Bible
import na.kephas.kitvei.prefs
import na.kephas.kitvei.util.*

class MainAdapter(context: Context) : PagedListAdapter<Bible, MainAdapter.ViewHolder>(DIFF_CALLBACK) {

    private var bible: Bible? = null
    private val redLetters: MutableSet<RedLetter> by lazy { RedLetterDatabase.getRedLetters(context) }
    private val TextColor by lazy { ContextCompat.getColor(context, R.color.textColor) }
    private val NumColor by lazy { Color.parseColor("#877f66") }
    private val RedLetterColor by lazy { ContextCompat.getColor(context, R.color.redletter_color_dark) }
    private val HighLightColor by lazy { ContextCompat.getColor(context, R.color.highlight_color_dark) }
    private val HighlightFocusColor by lazy { ContextCompat.getColor(context, R.color.highlight_focus_color) }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.main_rv_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        bible = getItem(position)
        holder.bind(bible)

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
        private val textView: TextView = itemView.findViewById(R.id.mainTextView)

        init {
            textView.setOnClickListener(this)
            textView.typeface = Fonts.GentiumPlus_R

            //if (isRTL && textView.rotationY != 180f)
            //     textView.rotationY = 180f
        }

        override fun onClick(view: View) {
            mClickListener?.onItemClick(view, adapterPosition)
        }

        fun bind(bible: Bible?) {

            if (bible != null) {

                launch(fixedThreadPool) {

                    val nums = SpannableStringBuilder(bible.verseId.toString())
                    nums.apply {
                        setSpan(ForegroundColorSpan(NumColor), 0, nums.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        append("  ")
                        setSpan(RelativeSizeSpan(fontSize / 2f), 0, nums.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    var text = bible.verseText!!

                    // Trim << [TEXT] >>
                    if (text.indexOf('<') == 0) {
                        text = text.substring(text.lastIndexOf('>') + 2)
                    } else if (text.contains('<')) {
                        text = text.substring(0, text.indexOf('<') - 1)
                    }

                    val sText = SpannableStringBuilder(text)

                    // Apply italics
                    val count = text.count("[")
                    for (c in 1..count) {
                        sText.indexOf('[').let {
                            if (it >= 0) {
                                val start = it
                                val end = sText.indexOf(']', it + 1)
                                sText.setSpan(CharacterStyle.wrap(CustomTypefaceSpan(Fonts.GentiumPlus_I)) as CharacterStyle, start, end, 0)
                                sText.delete(it, it + 1)
                                sText.delete(end - 1, end)
                            }
                        }
                    }

                    sText.setSpan(ForegroundColorSpan(TextColor), 0, sText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    sText.setSpan(RelativeSizeSpan(fontSize), 0, sText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

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
                    if (adapterPosition >= 0) {
                        val idx = redLetters.indexOfFirst { (bible.bookId == it.bookId && bible.chapterId == it.chapterId && bible.verseId == it.verseId) }
                        if (idx >= 0) {
                            //d { "redIdx: $foundRedIdx pos: $adapterPosition b: ${bible.bookId}|${bible.bookAbbr} c: ${bible.chapterId} v: ${bible.verseId}" }
                            redLetters.elementAtOrNull(idx)?.run {
                                for (it in positions)
                                    sText.setSpan(CharacterStyle.wrap(ForegroundColorSpan(RedLetterColor) as CharacterStyle), it.first, if (it.second - 1 > sText.length) it.second - 2 else it.second - 1, 0)
                            }
                        }
                    }

                    withContext(UI) {
                        textView.setText(TextUtils.concat("\t\t", nums, sText), TextView.BufferType.SPANNABLE)
                    }
                }

            }
        }
    }

    companion object {
        private var fontSize = prefs.fontSize
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