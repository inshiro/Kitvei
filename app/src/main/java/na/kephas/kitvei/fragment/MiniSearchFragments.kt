package na.kephas.kitvei.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.ArrayList
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager

import na.kephas.kitvei.R
import na.kephas.kitvei.adapter.BookAdapter
import na.kephas.kitvei.adapter.ChapterAdapter
import na.kephas.kitvei.adapter.VerseAdapter
import na.kephas.kitvei.repository.MyViewModel
import na.kephas.kitvei.util.calculateNoOfColumns

class FragmentBook : Fragment(), BookAdapter.ItemClickListener {
    private lateinit var viewPager: ViewPager
    private lateinit var mAdapter: BookAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var list: List<String>
    private lateinit var mCallback: Listener
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) { ViewModelProviders.of(this).get(MyViewModel::class.java) }

    interface Listener {
        fun onItemSelectedFB(view: View, position: Int)
        fun onCompleteFB()
    }

    override fun onAttach(mActivity: Context) {
        super.onAttach(mActivity.applicationContext)
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = activity as Listener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity!!.toString() + " must implement Listener")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            if (!this::list.isInitialized) list = ArrayList()
            list = viewModel.getBookNames()
        } catch (e: Exception) {
            android.util.Log.e("FragmentBook", e.message)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.book_fragment, container, false)
        recyclerView = v.findViewById<RecyclerView>(R.id.recyclerview_fragment).apply {
            setPadding(0, this.paddingTop, 0, this.paddingBottom)
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            adapter = BookAdapter(activity!!.baseContext, list).apply { setClickListener(this@FragmentBook) }
            mAdapter = adapter as BookAdapter
        }

        viewPager = container!!.findViewById(R.id.viewpager)
        mCallback.onCompleteFB()
        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (this::recyclerView.isInitialized) {
            recyclerView.adapter = null
            recyclerView.removeAllViews()
        }
    }

    override fun onItemClick(view: View, position: Int) {
        mCallback.onItemSelectedFB(view, position)
    }

    fun filter(t: String) {
        if (this::mAdapter.isInitialized) mAdapter.filter.filter(t)
    }

}

class FragmentChapter : Fragment(), ChapterAdapter.ItemClickListener {
    private lateinit var list: List<Int>
    private lateinit var mCallback: Listener
    private lateinit var mAdapter: ChapterAdapter
    private lateinit var recyclerView: RecyclerView
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) { ViewModelProviders.of(this).get(MyViewModel::class.java) }

    interface Listener {
        fun onItemSelectedFC(position: Int)
        fun onCompleteFC()
    }

    override fun onAttach(mActivity: Context) {
        super.onAttach(mActivity.applicationContext)
        try {
            mCallback = activity as Listener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity!!.toString() + " must implement Listener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this::list.isInitialized) list = ArrayList()
        list = viewModel.getChapterIds()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.book_fragment, container, false)
        recyclerView = v.findViewById<RecyclerView>(R.id.recyclerview_fragment).apply {
            layoutManager = GridLayoutManager(activity, calculateNoOfColumns(activity!!.baseContext))
            adapter = ChapterAdapter(activity!!.baseContext, list).apply { setClickListener(this@FragmentChapter) }
            mAdapter = adapter as ChapterAdapter
            setHasFixedSize(true)
        }
        mCallback.onCompleteFC()
        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (this::recyclerView.isInitialized) {
            recyclerView.adapter = null
            recyclerView.removeAllViews()
        }
    }

    override fun onItemClick(view: View, position: Int) {
        mCallback.onItemSelectedFC(position)
    }

    fun updateList(book: Int) {
        if (!this::list.isInitialized) list = ArrayList()
        list = viewModel.getChapterIds(book)
        mAdapter.items = list
        recyclerView.adapter?.notifyDataSetChanged()
    }
}

class FragmentVerse : Fragment(), VerseAdapter.ItemClickListener {
    private lateinit var list: List<Int>
    private lateinit var mCallback: Listener
    private lateinit var mAdapter: VerseAdapter
    private lateinit var recyclerView:RecyclerView
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) { ViewModelProviders.of(this).get(MyViewModel::class.java) }

    interface Listener {
        fun onItemSelectedFV(position: Int)
        fun onCompleteFV()
    }

    override fun onAttach(mActivity: Context) {
        super.onAttach(mActivity.applicationContext)
        try {
            mCallback = activity as Listener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity!!.toString() + " must implement Listener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!this::list.isInitialized) list = ArrayList()
        list = viewModel.getVerseIds()
        val v = inflater.inflate(R.layout.book_fragment, container, false)
        recyclerView = v.findViewById<RecyclerView>(R.id.recyclerview_fragment).apply {
            layoutManager = GridLayoutManager(activity, calculateNoOfColumns(activity!!.baseContext))
            adapter = VerseAdapter(activity!!.baseContext, list).apply { setClickListener(this@FragmentVerse) }
            mAdapter = adapter as VerseAdapter
            setHasFixedSize(true)
        }

        mCallback.onCompleteFV()
        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (this::recyclerView.isInitialized) {
            recyclerView.adapter = null
            recyclerView.removeAllViews()
        }
    }

    override fun onItemClick(view: View, position: Int) {
        mCallback.onItemSelectedFV(position)
    }

    fun updateList(book:Int, chapter: Int = 1) {
        if (!this::list.isInitialized) list = ArrayList()
        list = viewModel.getVerseIds(book, chapter)
        mAdapter.items = list
        recyclerView.adapter?.notifyDataSetChanged()
    }

}

