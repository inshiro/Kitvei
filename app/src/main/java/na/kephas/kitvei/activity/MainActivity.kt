package na.kephas.kitvei.activity

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager

import androidx.annotation.ColorRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.TextUtilsCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager

import com.flaviofaria.kenburnsview.KenBurnsView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_content.*
import kotlinx.coroutines.experimental.*
import java.util.Locale

import kotlinx.coroutines.experimental.android.UI

import na.kephas.kitvei.DedicatoryActivity
import na.kephas.kitvei.fragment.FragmentBook
import na.kephas.kitvei.fragment.FragmentChapter
import na.kephas.kitvei.fragment.FragmentVerse
import na.kephas.kitvei.HowtoActivity
import na.kephas.kitvei.repository.MyViewModel
import na.kephas.kitvei.R
import na.kephas.kitvei.TranslatorsActivity
import na.kephas.kitvei.adapter.MainAdapter
import na.kephas.kitvei.adapter.SearchViewPagerAdapter
import na.kephas.kitvei.adapter.MainViewPagerAdapter
import na.kephas.kitvei.prefs
import na.kephas.kitvei.repository.AppDatabase
import na.kephas.kitvei.repository.Bible
import na.kephas.kitvei.util.backgroundPool
import na.kephas.kitvei.util.getScreenHeight
import na.kephas.kitvei.util.rootParent

class MainActivity : AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        FragmentBook.Listener,
        FragmentChapter.Listener,
        FragmentVerse.Listener {
    @Suppress("PrivatePropertyName")
    private val TAG by lazy { MainActivity::class.java.simpleName }
    private lateinit var tabLayout: TabLayout
    private lateinit var searchViewPager: ViewPager
    private lateinit var mainViewPager: ViewPager
    private lateinit var searchViewPagerAdapter: SearchViewPagerAdapter
    private lateinit var bookFragment: FragmentBook
    private lateinit var chapterFragment: FragmentChapter
    private lateinit var verseFragment: FragmentVerse
    private lateinit var bible: List<Bible>
    private val imm by lazy(LazyThreadSafetyMode.NONE) { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
    private val isRTL by lazy(LazyThreadSafetyMode.NONE) { TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) != ViewCompat.LAYOUT_DIRECTION_LTR }
    //private val typeface by lazy(LazyThreadSafetyMode.NONE) { Typeface.create("sans-serif", Typeface.NORMAL) }
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) { ViewModelProviders.of(this).get(MyViewModel::class.java) }

    companion object {
        private var row: Bible? = null
        private var hideSearch = false
        private var viewPagerPosition = 0
        private var tBook = 1
        private var tChapter = 1
        private var pos = 0
        private var queryFinished = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val td: ActivityManager.TaskDescription = ActivityManager.TaskDescription("Kitvei HaKodesh")
            setTaskDescription(td)
        }

        launch(backgroundPool, parent = rootParent) {

            // Drawer
            val navHeaderView: View = nav_view.inflateHeaderView(R.layout.nav_header_main)
            val toggle = ActionBarDrawerToggle(this@MainActivity, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
            drawer_layout.addDrawerListener(toggle)

            val coverView: KenBurnsView = navHeaderView.findViewById(R.id.coverView)
            // coverView.setImageResource(R.drawable.cover)

            toggle.syncState()
            nav_view.setNavigationItemSelectedListener(this@MainActivity)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_drag_handle_white_24dp)
            //val image = ContextCompat.getDrawable(applicationContext, R.drawable.cover)

            withContext(UI) {
                Picasso.get().load(R.drawable.cover).into(coverView)
            }
        }

        bible = viewModel.getAll()
        row = bible[viewPagerPosition]

        // Toolbar Title
        toolbarTitle.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.5f)
            setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL)
            setTextColor(ResourcesCompat.getColor(resources, android.R.color.background_light, null))
        }
        blink(toolbarTitle, 4, 1000)

        if (prefs.VP_Position == 0) setMainTitle() // Init
        //toolbarTitle.typeface = typeface

        mainViewPager = findViewById(R.id.mainViewPager)
        mainViewPager.adapter = MainViewPagerAdapter(this, viewModel, bible)
        if (isRTL) mainViewPager.rotationY = 180f

        val onPageListener = object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                viewPagerPosition = position
                row = bible[viewPagerPosition]
                setMainTitle(row?.bookName, row?.chapterId)
            }

        }
        mainViewPager.addOnPageChangeListener(onPageListener)
        mainViewPager.currentItem = prefs.VP_Position

        // Fragment height
        val linearParams = stuffLinearLayout.layoutParams
        linearParams.height = (getScreenHeight() * 0.5f).toInt()
        stuffLinearLayout.layoutParams = linearParams
        stuffLinearLayout.isFocusable = false
        stuffLinearLayout.clearFocus()

        // ViewPager Setup
        searchViewPager = findViewById<View>(R.id.viewpager) as androidx.viewpager.widget.ViewPager
        tabLayout = findViewById<View>(R.id.tablayout) as com.google.android.material.tabs.TabLayout
        searchViewPagerAdapter = SearchViewPagerAdapter(supportFragmentManager)
        searchViewPager.adapter = searchViewPagerAdapter
        tabLayout.setupWithViewPager(searchViewPager)
        searchViewPager.offscreenPageLimit = 2


        // Create an initial view to display; must be a subclass of FrameLayout.
        searchViewPagerAdapter.startUpdate(searchViewPager)
        bookFragment = searchViewPagerAdapter.instantiateItem(searchViewPager, 0) as FragmentBook
        chapterFragment = searchViewPagerAdapter.instantiateItem(searchViewPager, 1) as FragmentChapter
        verseFragment = searchViewPagerAdapter.instantiateItem(searchViewPager, 2) as FragmentVerse
        searchViewPagerAdapter.finishUpdate(searchViewPager)


        // Open SearchView. Prevent keyboard show
        toolbarSearchView.post {
            toolbarSearchView.apply {
                isFocusable = false
                isIconified = false
                clearFocus()
                hideSoftInput(this)
            }
        }

        toolbarTitle.setOnClickListener {
            searchViewPager.currentItem = 0
            toolbarTitle.clearAnimation()
            toolbarTitle.visibility = View.GONE
            toolbarSearchView.visibility = View.VISIBLE
            hideSearch = true
            invalidateOptionsMenu()

            topStuff.alpha = 0f
            topStuff.visibility = View.VISIBLE
            topStuff.animate().alpha(1f)
            topStuff.bringToFront()

            tBook = row?.bookId!!
            tChapter = row?.chapterId!!
            updateHintTemp(tBook, tChapter)
            if (chapterFragment.isAdded) chapterFragment.updateList(tBook)
            if (verseFragment.isAdded) verseFragment.updateList(tBook, tChapter)

        }

        toolbarSearchView.setOnCloseListener {
            finishSearch()
            true
        }
    }

    override fun onStop() {
        super.onStop()
        prefs.VP_Position = viewPagerPosition
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            AppDatabase.destroyInstance()
            toolbarSearchView.setOnCloseListener(null)
            toolbarTitle.setOnClickListener(null)
            nav_view?.setNavigationItemSelectedListener(null)
        }
    }

    fun setMainTitle(bookName: String? = "Genesis", chapterId: Int? = 1) {
        /*titleSpan.apply {
            clear()
            append("$bookName $chapterId")
            setSpan(AbsoluteSizeSpan(20, true), 0, titleSpan.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            //setSpan(Fonts.Merriweather_BoldItalic, 0, titleSpan.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        toolbarTitle.setText(titleSpan, TextView.BufferType.SPANNABLE)*/
        toolbarTitle.text = ("$bookName $chapterId")
    }


    override fun onCompleteFB() {
        toolbarSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (bookFragment.isAdded)
                    bookFragment.filter(newText)
                return true
            }
        })
    }

    override fun onCompleteFC() {}

    override fun onCompleteFV() {}

    private fun getPosition(book: Int = 1, chapter: Int = 1): Int = bible.indexOfFirst { it.bookId == book && it.chapterId == chapter }

    override fun onItemSelectedFB(view: View, position: Int) {
        tBook = position + 1
        tChapter = 1

        if (chapterFragment.isAdded) chapterFragment.updateList(tBook)
        if (verseFragment.isAdded) verseFragment.updateList(tBook)
        searchViewPager.currentItem++
        updateHintTemp(tBook, tChapter)

    }

    override fun onItemSelectedFC(position: Int) {
        tChapter = position + 1

        if (verseFragment.isAdded) verseFragment.updateList(tBook, tChapter)
        searchViewPager.currentItem++
        updateHintTemp(tBook, tChapter)
    }

    override fun onItemSelectedFV(position: Int) {
        mainViewPager.setCurrentItem(getPosition(tBook, tChapter), true).also {
            queryFinished = true
            finishSearch()
        }
        launch(UI) {
            //delay(100)
            val nsv = mainViewPager.findViewWithTag<NestedScrollView>("nsv${mainViewPager.currentItem}")
            val rv = mainViewPager.findViewWithTag<RecyclerView>("rv${mainViewPager.currentItem}")
            rvScrollTo2(rv, nsv, position)
        }

    }

    private fun hideSoftInput(view: View) {
        view.clearFocus()
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun finishSearch() {
        toolbarSearchView.clearFocus()
        //hideSoftInput(toolbarSearchView)
        toolbarTitle.visibility = View.VISIBLE
        toolbarSearchView.visibility = View.GONE
        //toolbarSearchView.setOnQueryTextListener(null)

        topStuff.visibility = View.VISIBLE
        toolbarSearchView.setQuery("", false)

        hideSearch = false
        invalidateOptionsMenu()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            topStuff.animate().alpha(0f).withEndAction {
                topStuff.visibility = View.GONE
            }
        } else
            ViewCompat.animate(topStuff).alpha(0f).withEndAction {
                topStuff.visibility = View.GONE
            }
    }

    private fun updateHintTemp(book: Int, chapter: Int) {
        val row = bible[getPosition(book, chapter)]
        toolbarSearchView.queryHint = "Search ${row.bookName} $chapter..."
    }

    private fun blink(view: View, rep: Int, duration: Long) {
        if ((rep % 2) != 0) throw Exception("Blinking repetition must be EVEN for fade IN/OUT animation.")
        val startBufffer = 500
        async {
            delay(startBufffer)
            withContext(UI) {

                val blinkanimation = AlphaAnimation(1f, 0f) // Change alpha from fully visible to invisible
                blinkanimation.duration = duration // duration
                //blinkanimation.setInterpolator(LinearInterpolator()) // do not alter animation rate
                blinkanimation.repeatCount = rep + 1 // Repeat animation
                blinkanimation.repeatMode = Animation.REVERSE
                view.startAnimation(blinkanimation)
            }

            /*
            delay(startBufffer)
            delay(duration * (rep + 1))
            withContext(UI) {
                //view.visibility = View.GONE
                view.visibility = View.VISIBLE
            }*/
        }

    }

    private var recyclerViewReadyCallback: RecyclerViewReadyCallback? = null

    interface RecyclerViewReadyCallback {
        fun onLayoutReady()
    }

    fun scrollToTop2(nestedScrollView: NestedScrollView) {
        nestedScrollView.post {
            nestedScrollView.apply {
                fling(0)
                smoothScrollTo(0, 0)
            }
        }
    }


    fun rvScrollTo2(recyclerView: RecyclerView, rvNestedScrollView: NestedScrollView, position: Int) {
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (recyclerViewReadyCallback != null) {
                    recyclerViewReadyCallback!!.onLayoutReady()
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    @Suppress("DEPRECATION")
                    recyclerView.viewTreeObserver.removeGlobalOnLayoutListener(this)
                } else
                    recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)

            }
        })
        recyclerViewReadyCallback = object : RecyclerViewReadyCallback {
            override fun onLayoutReady() {
                when (position) {
                    0 -> scrollToTop2(rvNestedScrollView)
                    else -> recyclerView.getChildAt(position)?.y?.toInt()?.let {
                        rvNestedScrollView.post {
                            rvNestedScrollView.apply {
                                //setPadding(this.paddingLeft, this.paddingTop, this.paddingRight, androidx.appcompat.R.attr.actionBarSize) // Only use if toolbar collapses
                                fling(0)
                                smoothScrollTo(0, it)
                                //setPadding(this.paddingLeft, this.paddingTop, this.paddingRight, this.paddingTop)
                            }
                        }
                    }
                }

            }
        }
    }

    private fun tintMenuIcon(item: MenuItem, @ColorRes color: Int) {
        val normalDrawable = item.icon
        val wrapDrawable = DrawableCompat.wrap(normalDrawable)
        DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(this, color))

        item.icon = wrapDrawable
    }

    override fun onBackPressed() {

        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else if (toolbarSearchView.visibility == View.VISIBLE) {
            finishSearch()
        } else {
            super.onBackPressed()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val retValue = super.onCreateOptionsMenu(menu)
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        val searchItem = menu.findItem(R.id.search_menu)
        searchItem?.let {
            tintMenuIcon(searchItem, android.R.color.background_light)
            searchItem.isVisible = !hideSearch // Called when invalidate

        }
        return retValue
    }

    private var fontSize = 1f
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.toggle_night -> {
            try {
                val mA = mainViewPager.findViewWithTag<RecyclerView>("rv${mainViewPager.currentItem}").adapter as MainAdapter
                fontSize = if (fontSize == 1f) 1.2f else 1f
                mA.setFontSize(fontSize)

                mA.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e(TAG, e.message)
            }
            true
        }

        R.id.search_menu -> {
            val intent = Intent(this, SearchActivity::class.java)
            this.startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_epistle_dedicatory -> {
                val intent = Intent(this, DedicatoryActivity::class.java)
                this.startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            }
            R.id.nav_translators_notes -> {
                val intent = Intent(this, TranslatorsActivity::class.java)
                this.startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)


            }
            R.id.nav_howto -> {
                val intent = Intent(this, HowtoActivity::class.java)
                this.startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            }

        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

}

