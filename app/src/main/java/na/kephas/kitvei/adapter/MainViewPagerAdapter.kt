package na.kephas.kitvei.adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import na.kephas.kitvei.R
import na.kephas.kitvei.repository.Bible
import na.kephas.kitvei.repository.MyViewModel
import kotlinx.android.synthetic.clearFindViewByIdCache
import na.kephas.kitvei.widget.ScrollingLinearLayoutManager


class MainViewPagerAdapter(private val act: AppCompatActivity, private val vm: MyViewModel, private var all: List<Bible>) : PagerAdapter(), MainAdapter.ItemClickListener {
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
            layoutManager = ScrollingLinearLayoutManager(act, LinearLayoutManager.VERTICAL, false, 1000)
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
        vm.getVerseList(row.bookId!!, row.chapterId!!).observe(act, Observer<PagedList<Bible>?> { pagedList ->
            mAdapter?.submitList(pagedList)
            //Log.d("instantiateItem", "current-position: $position LOADING chapter: ${position+1} size: ${pagedList?.size} ")
        })

        /*
        @Suppress("UNUSED_VARIABLE")
        val nestedScrollView = layout.findViewById<NestedScrollView>(R.id.parentNestedScrollView).apply {
            setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                if (scrollY > oldScrollY) {
                    act.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE // Scrolling up

                }
                else {
                    act.window.decorView.systemUiVisibility = 0 // Scrolling down
                }
            })
            tag = "nsv$position"
        }
        */

        /*
        if (rvOnly) {
            recyclerView.clearOnScrollListeners()

            // Add overscroll
            val overScrollListener = object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> {
                            if (!recyclerView.canScrollVertically(-1)) {
                                val e = MotionEvent.obtain(
                                        System.currentTimeMillis(),
                                        System.currentTimeMillis() + 100,
                                        MotionEvent.ACTION_UP,
                                        100f, 100f, 0)
                                recyclerView.dispatchTouchEvent(e)
                            } else if (!recyclerView.canScrollVertically(1)) {
                                val e = MotionEvent.obtain(
                                        System.currentTimeMillis(),
                                        System.currentTimeMillis() + 100,
                                        MotionEvent.ACTION_UP,
                                        100f, 100f, 0)
                                recyclerView.dispatchTouchEvent(e)
                            }
                        }
                    }
                    super.onScrollStateChanged(recyclerView, newState)
                }
            }

            recyclerView.addOnScrollListener(overScrollListener)
            }
*/

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