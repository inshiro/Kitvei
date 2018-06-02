package na.kephas.kitvei.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.chapter_item.view.chapter_textView
import na.kephas.kitvei.R

class ChapterAdapter(val context: Context, var items: List<Int>) : androidx.recyclerview.widget.RecyclerView.Adapter<ChapterAdapter.ViewHolder>() {
    private var mClickListener: ItemClickListener? = null
    // total number of cells
    override fun getItemCount(): Int {
        return items.size
    }

    // inflates the cell layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.chapter_item, parent, false))
    }

    // binds the data to the textview in each cell
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.chapterTextView.text = items[position].toString()
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view), View.OnClickListener {
        internal val chapterTextView: TextView = view.chapter_textView

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            if (mClickListener != null)
                mClickListener!!.onItemClick(view, adapterPosition)
        }
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    // convenience method for getting data at click position
    internal fun getItem(id: Int): String {
        return items[id].toString()
    }

    // allows clicks events to be caught
    internal fun setClickListener(itemClickListener: ItemClickListener) {
        this.mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }
}