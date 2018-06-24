package na.kephas.kitvei.activity

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
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
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.flaviofaria.kenburnsview.KenBurnsView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_content.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import na.kephas.kitvei.*
import na.kephas.kitvei.adapter.MainAdapter
import na.kephas.kitvei.adapter.MainViewPagerAdapter
import na.kephas.kitvei.adapter.MiniSearchViewPagerAdapter
import na.kephas.kitvei.data.AppDatabase
import na.kephas.kitvei.data.Bible
import na.kephas.kitvei.fragment.BottomSheetFragment
import na.kephas.kitvei.fragment.FragmentBook
import na.kephas.kitvei.fragment.FragmentChapter
import na.kephas.kitvei.fragment.FragmentVerse
import na.kephas.kitvei.util.*
import na.kephas.kitvei.viewmodels.VerseListViewModel
import java.lang.reflect.Field

class MainActivity : AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        FragmentBook.Listener,
        FragmentChapter.Listener,
        FragmentVerse.Listener {
    @Suppress("PrivatePropertyName")
    private val TAG by lazy { MainActivity::class.java.simpleName }
    private lateinit var tabLayout: TabLayout
    private lateinit var searchViewPager: ViewPager
    lateinit var mainViewPager: ViewPager
    private lateinit var miniSearchViewPagerAdapter: MiniSearchViewPagerAdapter
    private lateinit var bookFragment: FragmentBook
    private lateinit var chapterFragment: FragmentChapter
    private lateinit var verseFragment: FragmentVerse
    private val imm by lazy(LazyThreadSafetyMode.NONE) { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
    private val isRTL by lazy(LazyThreadSafetyMode.NONE) { TextUtilsCompat.getLayoutDirectionFromLocale(java.util.Locale.getDefault()) != ViewCompat.LAYOUT_DIRECTION_LTR }
    //private val typeface by lazy(LazyThreadSafetyMode.NONE) { Typeface.create("sans-serif", Typeface.NORMAL) }
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {

        val factory = InjectorUtils.provideVerseListViewModelFactory(this)
        ViewModelProviders.of(this, factory)
                .get(VerseListViewModel::class.java)
        //ViewModelProviders.of(this).getInstance(MyViewModel::class.java)
        // TODO requireContext() on a fragment

    }
    private val bible: List<Bible> by lazy(LazyThreadSafetyMode.NONE) {
        runBlocking {
            withContext(backgroundPool) {
                viewModel.getPages()
            }
        }
    }
    private val activityManager by lazy(LazyThreadSafetyMode.NONE) { baseContext.getSystemService<ActivityManager>() }
    private lateinit var bottomSheetFragment: BottomSheetFragment

    companion object {
        private var row: Bible? = null
        private var hideSearch = false
        private var viewPagerPosition = 0
        private var tBook = 1
        private var tChapter = 1
        private var pos = 0
        private var queryFinished = false
    }

    fun isTranslucentNavBar(): Boolean {
        val flags = window.attributes.flags
        if ((flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION) == WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            return true
        return false
    }

    fun cancelTranslucentNavBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
    }

    fun setTranslucentNavBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        if (!this::bottomSheetFragment.isInitialized)
            bottomSheetFragment = BottomSheetFragment()

        // Do not touch NavBar if BottomSheet is shown
        if (bottomSheetFragment.isAdded)
            (bottomSheetFragment != null && bottomSheetFragment.dialog != null
                    && bottomSheetFragment.dialog.isShowing).let { if (it) return }

        if (newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (isTranslucentNavBar())
                cancelTranslucentNavBar()
        } else if (newConfig?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (!isTranslucentNavBar())
                setTranslucentNavBar()
        }
    }

    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (savedInstanceState == null)
            bottomSheetFragment = BottomSheetFragment()

        launch(backgroundPool, parent = rootParent) {

            // Drawer
            val navHeaderView: View = nav_view.inflateHeaderView(R.layout.nav_header_main)
            actionBarDrawerToggle = ActionBarDrawerToggle(this@MainActivity, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
            drawer_layout.addDrawerListener(actionBarDrawerToggle)

            val coverView: KenBurnsView = navHeaderView.findViewById(R.id.coverView)
            // coverView.setImageResource(R.drawable.cover)

            actionBarDrawerToggle.syncState()
            nav_view.setNavigationItemSelectedListener(this@MainActivity)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setHomeButtonEnabled(true)
            //supportActionBar?.setDisplayHomeAsUpEnabled(true)
            //supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_drag_handle_white_24dp)
            //val image = ContextCompat.getDrawable(applicationContext, R.drawable.cover)

            withContext(UI) {
                Picasso.get().load(R.drawable.cover).into(coverView)
            }
        }

        row = bible[viewPagerPosition]

        // Toolbar Title
        toolbarTitle.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.5f)
            setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL)
            setTextColor(ResourcesCompat.getColor(resources, android.R.color.background_light, null))
        }
        blink(toolbarTitle, 4, 1000)

        if (prefs.VP_Position == 0) setMainTitle() // Init
        //toolbarTitle.typeface = Fonts.Merriweather_Black

        mainViewPager = findViewById(R.id.mainViewPager)
        mainViewPager.tag = "MainVP"

        // Prefer higher quality images unless we're on a low RAM device
        //mainViewPager.offscreenPageLimit = if (ActivityManagerCompat.isLowRamDevice(activityManager)) 1 else 2
        mainViewPager.adapter = MainViewPagerAdapter(this, viewModel, bible)
        /*if (isRTL) {
            mainViewPager.rotationY = 180f
            (mainViewPager.adapter as? MainViewPagerAdapter)?.notifyDataSetChanged()
        }*/

        val onPageListener = object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                viewPagerPosition = position
                row = bible[viewPagerPosition]
                setMainTitle(row?.bookName, row?.chapterId)
                if (toolbarSearchView.visibility == View.VISIBLE)
                    if (findInPageMenu)
                        closeFindInPageSearch()

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
        miniSearchViewPagerAdapter = MiniSearchViewPagerAdapter(supportFragmentManager)
        searchViewPager.adapter = miniSearchViewPagerAdapter
        tabLayout.setupWithViewPager(searchViewPager)
        searchViewPager.offscreenPageLimit = 2


        // Create an initial view to display; must be a subclass of FrameLayout.
        miniSearchViewPagerAdapter.startUpdate(searchViewPager)
        bookFragment = miniSearchViewPagerAdapter.instantiateItem(searchViewPager, 0) as FragmentBook
        chapterFragment = miniSearchViewPagerAdapter.instantiateItem(searchViewPager, 1) as FragmentChapter
        verseFragment = miniSearchViewPagerAdapter.instantiateItem(searchViewPager, 2) as FragmentVerse
        miniSearchViewPagerAdapter.finishUpdate(searchViewPager)


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

            showSearch()

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


        fipCloseButton.setOnClickListener {
            if (findInPageMenu)
                closeFindInPageSearch()
        }

        toolbarSearchView.setOnCloseListener {
            if (findInPageMenu)
                closeFindInPageSearch()
            else
                finishSearch()
            true
        }

    }


    private val closeButton: ImageView? by lazy {
        var button: ImageView? = null
        try {
            val searchField: Field = SearchView::class.java.getDeclaredField("mCloseButton")
            searchField.isAccessible = true
            button = searchField.get(toolbarSearchView) as ImageView
        } catch (e: Exception) {
            Log.e(TAG, "Error finding close button", e)
        }
        button
    }

    private fun toggleSearchViewCloseButton(close: Boolean) {
        closeButton?.let {
            it.isEnabled = !close
            it.setImageDrawable(if (close) null else ContextCompat.getDrawable(this, R.drawable.ic_close_white_24dp))
        }
    }

    private fun closeFindInPageSearch() {
        toggleSearchViewCloseButton(false)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = true
        toolbarSearchView.clearFocus()
        //hideSoftInput(toolbarSearchView)
        findInSearchContainer.visibility = View.GONE
        toolbarTitle.visibility = View.VISIBLE
        toolbarSearchView.visibility = View.GONE
        toolbarSearchView.setQuery("", false)
        hideSearch = false
        invalidateOptionsMenu()
        val rv = mainViewPager.findViewWithTag<RecyclerView>(0)
        (rv.adapter as MainAdapter).apply {
            findInPage(null)
            setCurrentlyHighlighted(null, null)
            notifyDataSetChanged()
        }
        toolbarSearchView.setOnQueryTextListener(null)
        toolbarSearchView.setOnQueryTextListener(miniSearchListener)
        findInPageMenu = false
    }

    private var findInPageMenu = false
    private var matchesList: MutableList<Int> = mutableListOf()
    private var findInPageMatch = 1
    private var fipIndex = 0
    private var fipElement = 1
    fun resetFIPS() {
        findInPageMatch = 1
        fipIndex = 0
        fipElement = 1

    }

    private fun setClickListener(button: ImageButton, recyclerView: RecyclerView) {


        button.setOnClickListener {
            if (!findInPageMenu)
                return@setOnClickListener
            val sum = matchesList.sum()
            if (sum < 1)
                return@setOnClickListener
            run loop@{

                if (button == fipUpButton) {
                    //if (findInPageMatch == 1) findInPageMatch = count
                    //findInPageMatch--
                    if (findInPageMatch == 1) {
                        findInPageMatch = sum
                        val index = matchesList.indexOfLast { it != 0 }
                        val value = matchesList[index]
                        recyclerView.fling(0, 0)
                        recyclerView.scrollToPosition(index)
                        (recyclerView.adapter as MainAdapter).apply {
                            setCurrentlyHighlighted(index, value)
                            notifyDataSetChanged()
                        }
                        //d(TAG) { "Called scrolled down index: $index fipValue: $value findInPageMatch: $findInPageMatch" }

                        //resetFIPS()
                        return@loop

                    } else {
                        var count = 1
                        --findInPageMatch // Does most of the work
                        matchesList.next { index, value ->
                            if (count == findInPageMatch) {
                                recyclerView.post {
                                    recyclerView.fling(0, 0)
                                    recyclerView.smoothScrollToPosition(index)
                                    d(TAG) { "Called scrolled up index: $index fipValue: $value findInPageMatch: $findInPageMatch" }

                                    (recyclerView.adapter as MainAdapter).apply {
                                        setCurrentlyHighlighted(index, value)
                                        notifyDataSetChanged()
                                    }
                                }
                                return@loop
                            }
                            ++count
                        }
                    }
                } else if (button == fipDownButton) {
                    if (findInPageMatch == sum) {
                        matchesList.next(true) { index, value ->
                            if (value == 1) {
                                recyclerView.fling(0, 0)
                                recyclerView.scrollToPosition(index)
                                (recyclerView.adapter as MainAdapter).apply {
                                    setCurrentlyHighlighted(index, 1)
                                    notifyDataSetChanged()
                                }
                                //d(TAG) { "Called scrolled down index: $index fipValue: $value findInPageMatch: $findInPageMatch" }

                                //resetFIPS()
                                findInPageMatch = 1
                                return@loop
                            }
                        }
                    } else {
                        var count = 1
                        ++findInPageMatch // Does most of the work
                        matchesList.next { index, value ->
                            if (count == findInPageMatch) {
                                recyclerView.post {
                                    recyclerView.fling(0, 0)
                                    recyclerView.smoothScrollToPosition(index)
                                    //d(TAG) { "Called scrolled down index: $index fipValue: $value findInPageMatch: $findInPageMatch" }

                                    (recyclerView.adapter as MainAdapter).apply {
                                        setCurrentlyHighlighted(index, value)
                                        notifyDataSetChanged()
                                    }
                                }
                                return@loop
                            }
                            ++count
                        }
                    }
                }
            }

            fipCountText.text = ("$findInPageMatch/$sum")

        }

    }

    private val vbs by lazy { getSystemService(VIBRATOR_SERVICE) as Vibrator }
    private fun vibrate(duration: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vbs.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            @Suppress("DEPRECATION")
            vbs.vibrate(duration)
        }
    }

    private fun showFindInPageSearch() {
        val regex = Regex("""([a-zA-Z,.;:()'? ]+)""")
        val versesRaw = viewModel.getVersesRaw(row!!.bookId!!, row!!.chapterId!!)
        val rv = mainViewPager.findViewWithTag<RecyclerView>("rv${mainViewPager.currentItem}")
        findInPageMenu = true
        matchesList.reset(versesRaw.size)

        // Find in page views
        showSearch()
        setClickListener(fipUpButton, rv)
        setClickListener(fipDownButton, rv)
        toggleSearchViewCloseButton(true)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = false
        findInSearchContainer.visibility = View.VISIBLE

        toolbarSearchView.isIconified = false
        toolbarSearchView.queryHint = getString(R.string.find_in_page)
        toolbarSearchView.setOnQueryTextListener(null)

        var tempString = ""

        toolbarSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                matchesList.reset()
                if (tempString.isEmpty()) tempString = newText
                rv.post {
                    (rv.adapter as MainAdapter).let {
                        if (newText == "") {
                            fipCountText.text = null
                            it.findInPage(null)
                        } else if (!regex.matches(newText)) {
                            fipCountText.text = "0/0"
                            ContextCompat.getColor(fipCountText.context, R.color.search_not_found).let {
                                if (fipCountText.currentTextColor != it)
                                    fipCountText.setTextColor(it)
                            }

                            if (newText != tempString.dropLast(1)) // Don't vibrate on backspace
                                vibrate(60)
                            tempString = newText
                        } else if (regex.matches(newText)) {

                            // Populate matches in each index
                            for (index in 0 until versesRaw.size)
                                versesRaw[index].verseText!!.formatText().occurrences(newText) { matches, _, _ -> matchesList[index] = matches }
                            val matchCount = matchesList.sum()

                            resetFIPS()
                            findInPageMatch = if (matchCount == 0) {
                                ContextCompat.getColor(fipCountText.context, R.color.search_not_found).let {
                                    if (fipCountText.currentTextColor != it)
                                        fipCountText.setTextColor(it)
                                }
                                if (newText != tempString.dropLast(1)) // Don't vibrate on backspace
                                    vibrate(60)
                                tempString = newText

                                0
                            } else {
                                ContextCompat.getColor(fipCountText.context, R.color.white).let {
                                    if (fipCountText.currentTextColor != it)
                                        fipCountText.setTextColor(it)
                                }
                                1
                            }
                            it.findInPage(newText)
                            it.fixMatchesListSize(it.currentList!!.size)
                            fipCountText.text = ("$findInPageMatch/$matchCount")
                            //d(TAG) { "[new] Text: $newText Matches found: $matchCount" }

                            matchesList.next(true) { index, value ->
                                if (value == 1) {
                                    rv.scrollToPosition(index)
                                    it.setCurrentlyHighlighted(index, 1)
                                }
                            }

                            //rv.onLayoutChanged {
                            //    d(TAG) { "[prev] Text: $newText Matches found: ${it.getCurrentListSize()}" }
                            //}

                        }

                        it.notifyDataSetChanged()
                    }

                }
                return true
            }
        })

    }

    private fun showSearch() {
        toolbarTitle.clearAnimation()
        toolbarTitle.visibility = View.GONE
        toolbarSearchView.visibility = View.VISIBLE
        hideSearch = true
        invalidateOptionsMenu()

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


    private val miniSearchListener: SearchView.OnQueryTextListener  by lazy {
        object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (bookFragment.isAdded)
                    bookFragment.filter(newText)
                return true
            }
        }
    }

    override fun onCompleteFB() {
        toolbarSearchView.setOnQueryTextListener(miniSearchListener)
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
        var delay = false
        getPosition(tBook, tChapter).let {
            if (mainViewPager.currentItem != it) delay = true
            mainViewPager.setCurrentItem(it, true)
        }

        queryFinished = true
        finishSearch()

        tryy {
            mainViewPager.findViewWithTag<RecyclerView>("rv${mainViewPager.currentItem}").run {
                fling(0, 0)
                if (delay) {
                    launch {
                        while ((layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() < 0 && (layoutManager as LinearLayoutManager).findLastVisibleItemPosition() < 0) {
                            delay(20)
                        }
                        delay(80)
                        smoothScrollToPosition(position)
                    }

                } else {
                    smoothScrollToPosition(position)
                }
            }
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

    /*
    private var recyclerViewReadyCallback: RecyclerViewReadyCallback? = null

    interface RecyclerViewReadyCallback {
        fun onLayoutReady()
    }*/


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
            if (findInPageMenu)
                closeFindInPageSearch()
            else
                finishSearch()
        } else {
            super.onBackPressed()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val retValue = super.onCreateOptionsMenu(menu)
        if (hideSearch)
            return false
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

        R.id.search_menu -> {
            val intent = Intent(this, SearchActivity::class.java)
            this.startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            true
        }
        R.id.find_in_page_menu -> {
            showFindInPageSearch()
            true
        }
        R.id.font_and_theme_menu -> {
            bottomSheetFragment.show(supportFragmentManager, "TAG")
            /*
            try {
                fontSize = if (fontSize == 1f) 1.2f else 1f
                (mainViewPager.findViewWithTag<RecyclerView>("rv${mainViewPager.currentItem}").adapter as MainAdapter).apply {
                    setFontSize(fontSize)
                }
                mainViewPager.adapter?.notifyDataSetChanged()
                /*
                if (viewPagerPosition != 0)
                    (mainViewPager.findViewWithTag<RecyclerView>("rv${mainViewPager.currentItem - 1}").adapter as MainAdapter).apply {
                        setFontSize(fontSize)
                        notifyDataSetChanged()
                    }
                (mainViewPager.findViewWithTag<RecyclerView>("rv${mainViewPager.currentItem}").adapter as MainAdapter).apply {
                    setFontSize(fontSize)
                    notifyDataSetChanged()
                }
                (mainViewPager.findViewWithTag<RecyclerView>("rv${mainViewPager.currentItem + 1}").adapter as MainAdapter).apply {
                    setFontSize(fontSize)
                    notifyDataSetChanged()
                }*/
            } catch (e: Exception) {
                Log.e(TAG, e.message)
            }*/
            true
        }

        R.id.settings_menu -> {
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var intent: Intent? = null
        when (item.itemId) {
            R.id.nav_epistle_dedicatory -> {
                intent = Intent(this, DedicatoryActivity::class.java)
            }
            R.id.nav_translators_notes -> {
                intent = Intent(this, TranslatorsActivity::class.java)
            }
            R.id.nav_howto -> {
                intent = Intent(this, HowtoActivity::class.java)
            }
        }
        this.startActivity(intent)
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

}

