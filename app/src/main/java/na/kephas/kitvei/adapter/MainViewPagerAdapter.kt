package na.kephas.kitvei.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.*
import na.kephas.kitvei.R
import na.kephas.kitvei.data.Bible
import na.kephas.kitvei.viewmodels.VerseListViewModel
import na.kephas.kitvei.widget.ScrollingLinearLayoutManager


class MainViewPagerAdapter(private val act: AppCompatActivity, private val vm: VerseListViewModel, private var all: List<Bible>) : PagerAdapter(), MainAdapter.ItemClickListener {
    private val viewPool by lazy(LazyThreadSafetyMode.NONE) { RecyclerView.RecycledViewPool() }
    private lateinit var row: Bible
    private val rvOnly = false

    override fun onItemClick(view: View, position: Int) {
        //view.snackbar("Clicked position: $position")
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(act.baseContext).inflate(R.layout.rv_row, collection, false) as ViewGroup
        collection.addView(layout)
        var mAdapter: MainAdapter? = null
        @Suppress("UNUSED_VARIABLE")
        val recyclerView = layout.findViewById<RecyclerView>(R.id.nestedRecyclerView).apply {
            layoutManager = ScrollingLinearLayoutManager(act, LinearLayoutManager.VERTICAL, false, 700)
            mAdapter = MainAdapter().apply {
                setClickListener(this@MainViewPagerAdapter)
            }
            adapter = mAdapter
            setRecycledViewPool(viewPool)
            overScrollMode = View.OVER_SCROLL_NEVER
            //setWillNotDraw(false)
            tag = "rv$position"

        }

        row = all[position]
        vm.getVerses(row.bookId!!, row.chapterId!!).observe(act, Observer<PagedList<Bible>?> { pagedList ->
            if (pagedList != null) {
                mAdapter?.submitList(pagedList)
            }
        })

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            //var hidden: Boolean = false
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                //super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) { //(!hidden && dy > 0)
                    // Scrolling up
                    act.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE
                    //hidden = true
                } else { //if (hidden && dy < 0)
                    // Scrolling down
                    act.window.decorView.systemUiVisibility = 0
                    //hidden = false
                }
            }
        })

        //System.gc()
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
        view.findViewById<RecyclerView>(R.id.nestedRecyclerView).apply {
            adapter = null
            removeAllViews()
            removeAllViewsInLayout()
        }
        view.clearFindViewByIdCache()
        //Log.d("destroyItem","Called Here")
        //System.gc()
    }

    override fun getCount(): Int {
        return all.size
    }


    override fun isViewFromObject(view: View, `object`: Any): Boolean {

        return view === (`object` as View)
        //return view === `object`
    }
}