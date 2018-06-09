package na.kephas.kitvei.activity

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.search_activity.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import na.kephas.kitvei.R
import na.kephas.kitvei.adapter.SearchAdapter
import na.kephas.kitvei.data.Bible
import na.kephas.kitvei.util.InjectorUtils
import na.kephas.kitvei.util.snackbar
import na.kephas.kitvei.viewmodels.SearchListViewModel

class SearchActivity : AppCompatActivity(), SearchAdapter.ItemClickListener {
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {

        val factory = InjectorUtils.provideSearchListViewModelFactory(this)
        ViewModelProviders.of(this, factory)
                .get(SearchListViewModel::class.java)
        //ViewModelProviders.of(this).getInstance(MyViewModel::class.java)
    }
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: SearchAdapter
    private lateinit var mLayoutManager: StaggeredGridLayoutManager

    companion object {
        const val LIST_STATE_KEY = "recycler_list_state"
        var listState: Parcelable? = null
    }

    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)
        // Save list state
        listState = mLayoutManager.onSaveInstanceState()
        state.putParcelable(LIST_STATE_KEY, listState)
    }

    override fun onRestoreInstanceState(state: Bundle?) {
        super.onRestoreInstanceState(state)
        // Retrieve list state and list/item positions
        if (state != null)
            listState = state.getParcelable(LIST_STATE_KEY)
    }

    override fun onResume() {
        super.onResume()
        if (listState != null)
            mLayoutManager.onRestoreInstanceState(listState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_activity)
        setSupportActionBar(search_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        mRecyclerView = findViewById(R.id.search_recyclerview)
        mLayoutManager = StaggeredGridLayoutManager(
                if (resources.getBoolean(R.bool.is_tablet)) 3 else 2
                , StaggeredGridLayoutManager.VERTICAL)
        mRecyclerView.layoutManager = mLayoutManager
        mAdapter = SearchAdapter(this)
        mAdapter.setClickListener(this) //SearchAdapter.ItemClickListener
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.adapter = mAdapter

        //viewModel.bibleList.observe(this, { pagedList -> mAdapter.setList(pagedList) })
        //viewModel.bibleList.observe(this, pagedList -> mAdapter.submitList(pagedList))
        //viewModel.bibleList.observe(this, Observer<PagedList<Bible>?>(mAdapter::submitList))
        //viewModel.bibleList.observe(this, { pagedList-> mAdapter.submitList(pagedList) })
        //viewModel.bibleList.observe(this, Observer<PagedList<Bible>?>(mAdapter::submitList))

        var queryTextChangedJob: Job? = null
        var validQuery = ""
        val DEBOUNCE_MS = 300
        val regex = Regex("""^([a-zA-Z,.;:()'? ]+)( [1-9]\d{0,2}+:[1-9]\d{0,2}+)*$""")
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                //if (newText != "" && newText != "0" &&  newText == newText.replace("[^A-Za-z0-9,.:;() ]".toRegex(), "")) {
                //if (newText != "" && newText != "0") {

                queryTextChangedJob?.let {
                    if (!it.isCancelled)
                        it.cancel()
                }

                if (regex.matches(newText)) {
                    queryTextChangedJob = launch(UI) {
                        delay(DEBOUNCE_MS)
                        viewModel.getVerses(newText).observe(this@SearchActivity, Observer<PagedList<Bible>?> { pagedList ->
                            mAdapter.submitList(pagedList)
                            //mAdapter::submitList
                            if (validQuery != newText.trim()) {
                                pagedList?.size?.let {
                                    if (it >= 0) {
                                        if (it == 1)
                                            searchView.snackbar("Found $it result")

                                        else
                                            searchView.snackbar("Found $it results")
                                    }
                                }
                            }
                            validQuery = newText.trim()
                        })
                        /*if (!newText.contains(' '))
                            cursor = db!!.search(arrayOf("book_name", "chapter_id", "verse_id", "verse_text"), "verse_text LIKE '%${newText.trim().replace("'", "''")}%'")
                        else
                            cursor = db!!.search(arrayOf("book_name", "chapter_id", "verse_id", "verse_text"), "REPLACE(REPLACE(verse_text, '[', ''), ']','') LIKE '%${newText.trim().replace("'", "''")}%'")

                        */
                    }

                }
                return true
            }
        })

    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            searchView.setOnQueryTextListener(null)
            if (this::mRecyclerView.isInitialized) {
                mRecyclerView.adapter = null
                mRecyclerView.clearOnScrollListeners()
                mRecyclerView.removeAllViews()
            }
        }
        hideSoftInput(searchView)

    }

    override fun onItemClick(view: View, position: Int) {
        //view.snackbar("Clicked position: $position")
    }

    val imm by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
    private fun hideSoftInput(view: View) {
        view.clearFocus()
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}