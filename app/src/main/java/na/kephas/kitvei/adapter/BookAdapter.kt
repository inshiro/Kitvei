package na.kephas.kitvei.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import na.kephas.kitvei.R

class BookAdapter(val context: Context, var items: List<String>) : androidx.recyclerview.widget.RecyclerView.Adapter<BookAdapter.ViewHolder>(), Filterable {
    private var mClickListener: ItemClickListener? = null
    private var mFilteredList = items
    private var backup = items

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.book_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bookTextView.text = items[position]
    }

    inner class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view), View.OnClickListener {
        internal val bookTextView: TextView = view.findViewById(R.id.book_textView)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            if (mClickListener != null)
                mClickListener!!.onItemClick(view,
                        backup.indexOfFirst { it == bookTextView.text.toString() }
                )

        }
    }

    internal fun getItem(id: Int): String {
        return items[id]
    }

    internal fun setClickListener(itemClickListener: ItemClickListener) {
        this.mClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    // Search implementation: is.gd/m7fvs5
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    mFilteredList = backup
                } else {
                    val filteredList = ArrayList<String>()
                    for (a in backup) {
                        if (a.contains(charString, true)) filteredList.add(a)
                    }
                    mFilteredList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = mFilteredList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                @Suppress("UNCHECKED_CAST")
                mFilteredList = filterResults.values as ArrayList<String>
                items = mFilteredList
                notifyDataSetChanged()
            }
        }
    }
}
