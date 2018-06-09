package na.kephas.kitvei.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.cardview.widget.CardView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.card_row.view.*

import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

import na.kephas.kitvei.R
import na.kephas.kitvei.data.Bible
import na.kephas.kitvei.util.Fonts
import na.kephas.kitvei.util.rootParent
import java.util.regex.Pattern

class SearchAdapter(val context: Context) : PagedListAdapter<Bible, SearchAdapter.ViewHolder>(DIFF_CALLBACK) {

    private lateinit var text: String
    private lateinit var sText: SpannableStringBuilder

    fun modify(text: CharSequence, mode: Any, regex: String): SpannableStringBuilder {
        // Faster than from.html
        val pattern = Pattern.compile(regex)
        val ssb = SpannableStringBuilder(text)
        if (pattern != null) {
            val matcher = pattern.matcher(text)
            var matchesSoFar = 0
            while (matcher.find()) {
                val start = matcher.start() - (matchesSoFar * 2)
                val end = matcher.end() - (matchesSoFar * 2)
                ssb.setSpan(mode, start + 1, end - 1, 0)
                ssb.delete(start, start + 1)
                ssb.delete(end - 2, end - 1)
                matchesSoFar++
            }
        }
        return ssb
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.card_row, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        launch(UI, CoroutineStart.DEFAULT, rootParent) {
            val bible: Bible? = getItem(position)
            holder.bind(bible)
        }
    }


    internal fun setClickListener(itemClickListener: ItemClickListener) {
        this.mClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    private var mClickListener: ItemClickListener? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        internal val cardView: CardView = view.card_view
        internal val searchTitle: TextView = view.search_title_name
        internal val searchVerse: TextView = view.search_verse

        init {
            cardView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            mClickListener?.onItemClick(view, adapterPosition)
        }


        fun bind(bible: Bible?) {
            if (bible != null) {

                searchTitle.text = (bible.bookName + " " + bible.chapterId + ":" + bible.verseId)
                searchVerse.text = bible.verseText?.replace('[', '_')?.replace(']', '_')

                text = searchVerse.text.toString()

                if (text.indexOf('<') == 0) {
                    text = text.substring(text.lastIndexOf('>') + 1, text.length).trim()
                } else if (text.contains('<')) {
                    text = text.substring(0, text.indexOf('<')).trim()
                }

                // Setting Verse Style
                if (text.contains('_')) {
                    sText = modify(text, StyleSpan(Typeface.ITALIC), "_(.*?)_")
                    searchVerse.setText(sText, TextView.BufferType.SPANNABLE)

                } else {
                    searchVerse.text = text.replace("_", "")

                }
                searchTitle.typeface = Fonts.Merriweather_Black
                searchVerse.typeface = Fonts.GentiumPlus_R


                //sText.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.textColorPrimary)), 0, sText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {

            }
        }
    }

    companion object {
        /**
         * This diff callback informs the PagedListAdapter how to compute list differences when new
         * PagedLists arrive.
         * <p>
         * When you add a Bible with the 'Add' button, the PagedListAdapter uses diffCallback to
         * detect there's only a single item difference from before, so it only needs to animate and
         * rebind a single view.
         *
         * @see android.support.v7.util.DiffUtil
         */
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Bible>() {
            override fun areItemsTheSame(oldItem: Bible, newItem: Bible): Boolean =
                    oldItem.id == newItem.id

            /**
             * Note that in kotlin, == checking on data classes compares all contents, but in Java,
             * typically you'll implement Object#equals, and use it to compare object contents.
             */
            override fun areContentsTheSame(oldItem: Bible, newItem: Bible): Boolean =
                    oldItem == newItem
        }
    }
}
/*
class BibleViewHolder(parent :ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_row, parent, false)) {

    private val searchVerse = itemView.findViewById<TextView>(R.id.search_verse)
    var Bible : Bible? = null

    /**
     * Items might be null if they are not paged in yet. PagedListAdapter will re-bind the
     * ViewHolder when Item is loaded.
     */
    fun bindTo(Bible : Bible?) {
        this.Bible = Bible
        searchVerse.text = Bible?.verseText
    }
}
        */