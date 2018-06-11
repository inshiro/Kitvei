package na.kephas.kitvei.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.TextUtilsCompat
import androidx.core.view.ViewCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import na.kephas.kitvei.R
import na.kephas.kitvei.data.Bible
import na.kephas.kitvei.util.Fonts
import java.util.Locale
import java.util.regex.Pattern

class MainAdapter : PagedListAdapter<Bible, MainAdapter.ViewHolder>(DIFF_CALLBACK) {

    private lateinit var text: String
    private lateinit var sText: SpannableStringBuilder
    private lateinit var nums: SpannableStringBuilder
    private var bible: Bible? = null

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

    fun modify(text: CharSequence, mode: Any, regex: String): SpannableStringBuilder {
        val pattern = Pattern.compile(regex)
        val ssb = SpannableStringBuilder(text)
        if (pattern != null) {
            val matcher = pattern.matcher(text)
            var matchesSoFar = 0
            while (matcher.find()) {
                val start = matcher.start() - (matchesSoFar * 2)
                val end = matcher.end() - (matchesSoFar * 2)
                ssb.setSpan(CharacterStyle.wrap(mode as CharacterStyle), start + 1, end - 1, 0)
                ssb.delete(start, start + 1)
                ssb.delete(end - 2, end - 1)
                matchesSoFar++
            }
        }
        return ssb
    }

    fun modify2(text: CharSequence, mode: Any, regex: String): SpannableStringBuilder {
        val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
        val ssb = SpannableStringBuilder(text)
        if (pattern != null) {
            val matcher = pattern.matcher(text)
            var matchesSoFar = 0
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                ssb.setSpan(CharacterStyle.wrap(mode as CharacterStyle), start, end, 0)
                matchesSoFar++
            }
        }
        return ssb
    }

    fun setFontSize(size: Float) {
        fontSize = size
    }

    var findInPageQuery: String? = null
    fun findInPage(query: String?) {
        findInPageQuery = query
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val isRTL: Boolean by lazy { TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) != ViewCompat.LAYOUT_DIRECTION_LTR }
        private val textView: TextView = itemView.findViewById(R.id.mainTextView)

        init {
            textView.setOnClickListener(this)
            textView.typeface = Fonts.GentiumPlus_R

            if (isRTL && textView.rotationY != 180f)
                textView.rotationY = 180f
        }

        override fun onClick(view: View) {
            mClickListener?.onItemClick(view, adapterPosition)
        }

        fun bind(bible: Bible?) {

            if (bible != null) {
                nums = SpannableStringBuilder(bible.verseId.toString())
                nums.apply {
                    setSpan(ForegroundColorSpan(Color.parseColor("#877f66")), 0, nums.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    append(" ")
                    setSpan(RelativeSizeSpan(0.55f), 0, nums.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                text = bible.verseText!!.replace('[', '_').replace(']', '_')

                if (text.indexOf('<') == 0) {
                    text = text.substring(text.lastIndexOf('>') + 1, text.length).trim()
                } else if (text.contains('<')) {
                    text = text.substring(0, text.indexOf('<')).trim()
                }

                if (text.contains('_')) {
                    sText = modify(text, StyleSpan(Typeface.ITALIC), "_(.*?)_")
                } else {
                    sText = SpannableStringBuilder()
                    sText.append(text.replace("_", ""))
                }

                sText.setSpan(ForegroundColorSpan(Color.parseColor("#414141")), 0, sText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                if (fontSize > -1f)
                    sText.setSpan(RelativeSizeSpan(fontSize), 0, sText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                if (findInPageQuery != null)
                    sText = modify2(sText, BackgroundColorSpan(ContextCompat.getColor(itemView.context, R.color.highlight_color_dark)), "($findInPageQuery)")

                textView.setText(TextUtils.concat("\t\t", nums, sText), TextView.BufferType.SPANNABLE)
                //textView.setTextColor(Color.parseColor("#414141"))

            }

        }
    }

    companion object {
        private var fontSize = -1f
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Bible>() {
            override fun areItemsTheSame(oldItem: Bible, newItem: Bible): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Bible, newItem: Bible): Boolean = oldItem == newItem
        }
    }

}