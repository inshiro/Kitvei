package na.kephas.kitvei.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.text.Spannable
import android.text.style.BackgroundColorSpan
import android.text.style.CharacterStyle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ScrollView
import androidx.annotation.ColorRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.flaviofaria.kenburnsview.KenBurnsView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_content.*
import kotlinx.coroutines.*
import na.kephas.kitvei.*
import na.kephas.kitvei.adapter.MainViewPagerAdapter
import na.kephas.kitvei.adapter.MiniSearchViewPagerAdapter
import na.kephas.kitvei.data.AppDatabase
import na.kephas.kitvei.data.Bible
import na.kephas.kitvei.fragment.BottomSheetFragment
import na.kephas.kitvei.fragment.FragmentBook
import na.kephas.kitvei.fragment.FragmentChapter
import na.kephas.kitvei.fragment.FragmentVerse
import na.kephas.kitvei.page.Formatting
import na.kephas.kitvei.page.Page
import na.kephas.kitvei.theme.ThemeChooserDialog
import na.kephas.kitvei.util.*
import na.kephas.kitvei.viewmodels.VerseListViewModel
import java.lang.reflect.Field
import kotlin.properties.Delegates

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
    private val bottomSheetFragment by lazy { BottomSheetFragment() }
    private val themeChooserDialog by lazy { ThemeChooserDialog() }
    private val viewModel by lazy(LazyThreadSafetyMode.NONE) {
        val factory = InjectorUtils.provideVerseListViewModelFactory(this)
        ViewModelProviders.of(this, factory)
                .get(VerseListViewModel::class.java)
        //ViewModelProviders.of(this).getInstance(MyViewModel::class.java)
        // use requireContext() on a fragment?

    }
    //private val bible: List<Bible> by lazy(LazyThreadSafetyMode.NONE) {
    //     viewModel.getPages()
    // }
    private lateinit var bible: List<Bible>
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private val miniSearchListener: SearchView.OnQueryTextListener  by lazy {
        object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                @Suppress("SENSELESS_COMPARISON")
                if (!bookFragment.isAdded && bookFragment != null)
                    bookFragment = FragmentBook()
                bookFragment.filter(newText)
                return true
            }
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
    private var findInPageMenu = false
    private var matchesList: MutableList<Int> = mutableListOf()
    private var findInPageMatch = 1
    private var matchesCount = 0
    private lateinit var disposable: Any

    companion object {
        private var row: Bible? = null
        private var hideSearch = false
        private var viewPagerPosition = 0
        private var tBook = 1
        private var tChapter = 1
        private var queryFinished = false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setHomeButtonEnabled(true)

        val navHeaderView: View = nav_view.inflateHeaderView(R.layout.nav_header_main)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        val coverView: KenBurnsView = navHeaderView.findViewById(R.id.coverView)
        Picasso.get().load(R.drawable.cover).into(coverView)

        viewPagerPosition = Prefs.VP_Position

        viewModel.list.observe(this, Observer {
            if (it.isNotEmpty()) {
                bible = it
                row = bible[viewPagerPosition]

                // Toolbar Title
                toolbarTitle.setTextColor(ResourcesCompat.getColor(resources, android.R.color.background_light, null))
                toolbarTitle.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL)
                toolbarTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.5f)
                blink(toolbarTitle, 4, 1000)

                if (Prefs.VP_Position == 0) toolbarTitle.futureSet("Genesis 1") //Init

                mainViewPager = findViewById(R.id.mainViewPager)
                mainViewPager.adapter = MainViewPagerAdapter(this, viewModel, bible)

                val onPageListener = object : ViewPager.OnPageChangeListener {
                    override fun onPageScrollStateChanged(state: Int) {}

                    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                    override fun onPageSelected(position: Int) {

                        viewPagerPosition = position
                        row = bible[viewPagerPosition]//viewModel.getRow(position)
                        toolbarTitle.futureSet("${row?.bookName} ${row?.chapterId}")
                        if (toolbarSearchView.visibility == View.VISIBLE && findInPageMenu)
                            closeFindInPageSearch()

                    }

                }
                mainViewPager.addOnPageChangeListener(onPageListener)
                mainViewPager.currentItem = Prefs.VP_Position


                // Fragment height
                val linearParams = stuffLinearLayout.layoutParams
                linearParams.height = (getScreenHeight() * 0.5f).toInt()
                stuffLinearLayout.layoutParams = linearParams
                stuffLinearLayout.isFocusable = false
                stuffLinearLayout.clearFocus()

                // SearchViewPager Setup
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


        })

    }

    override fun onStart() {
        super.onStart()
        viewModel.getPages2()
    }

    override fun onPause() {
        super.onPause()
        Prefs.VP_Position = viewPagerPosition
        if (isFinishing) {
            AppDatabase.destroyInstance()
            toolbarSearchView.setOnCloseListener(null)
            toolbarTitle.setOnClickListener(null)
            nav_view?.setNavigationItemSelectedListener(null)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        // Do not touch NavBar if BottomSheet is shown
        if (bottomSheetFragment.isAdded && bottomSheetFragment.dialog != null && bottomSheetFragment.dialog.isShowing)
            return
        if (newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (isTranslucentNavBar())
                cancelTranslucentNavBar()
        } else if (newConfig?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (!isTranslucentNavBar())
                setTranslucentNavBar()
        }
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
        menu.findItem(R.id.settings_menu)?.subMenu?.let {
            it.findItem(R.id.kjv_styling_menu).isChecked = Page.kjvStyling
            it.findItem(R.id.drop_cap_menu).isChecked = Page.showDropCap
            it.findItem(R.id.pbreak_menu).isChecked = Page.showParagraphs
            it.findItem(R.id.red_letter_menu).isChecked = Page.showRedLetters
            it.findItem(R.id.verse_numbers_menu).isChecked = Page.showVerseNumbers
            it.findItem(R.id.seperate_verses_menu).isChecked = Page.newLineEachVerse
            //it.findItem(R.id.subject_headings_menu).isChecked = Page.showHeadings
            //it.findItem(R.id.subject_footings_menu).isChecked = Page.showFootings
        }
        return retValue
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search_menu -> {
                val intent = Intent(this, SearchActivity::class.java)
                this.startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            R.id.find_in_page_menu -> {
                showFindInPageSearch()
            }
            /*R.id.font_and_theme_menu -> {
                if (isTranslucentNavBar())
                    cancelTranslucentNavBar()
                bottomSheetFragment.show(supportFragmentManager, "TAG")
            }
                R.id.settings_menu -> {
                }*/
            R.id.font_and_theme_menu -> {
                if (isTranslucentNavBar())
                    cancelTranslucentNavBar()
                themeChooserDialog.show(supportFragmentManager, "tcd")
            }
            R.id.kjv_styling_menu, R.id.drop_cap_menu, R.id.pbreak_menu, R.id.red_letter_menu, R.id.verse_numbers_menu, R.id.seperate_verses_menu /*, R.id.subject_headings_menu, R.id.subject_footings_menu*/ -> {
                // Keep options menu open
                item.isChecked = !item.isChecked
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                item.actionView = View(this);
                item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                    override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                        return false
                    }

                    override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                        return false
                    }
                })
                when (item.itemId) {
                    R.id.kjv_styling_menu -> Page.kjvStyling = item.isChecked
                    R.id.drop_cap_menu -> Page.showDropCap = item.isChecked
                    R.id.pbreak_menu -> Page.showParagraphs = item.isChecked
                    R.id.red_letter_menu -> Page.showRedLetters = item.isChecked
                    R.id.verse_numbers_menu -> Page.showVerseNumbers = item.isChecked
                    R.id.seperate_verses_menu -> Page.newLineEachVerse = item.isChecked
                    //R.id.subject_headings_menu -> Page.showHeadings = item.isChecked
                    //R.id.subject_footings_menu -> Page.showFootings = item.isChecked
                }
                mainViewPager.adapter?.notifyDataSetChanged()
                return false
                //return false
            }

            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCompleteFB() {
        toolbarSearchView.setOnQueryTextListener(miniSearchListener)
    }

    override fun onCompleteFC() {}

    override fun onCompleteFV() {}

    override fun onItemSelectedFB(view: View, position: Int) {
        tBook = position + 1
        tChapter = 1

        if (chapterFragment.isAdded) chapterFragment.updateList(tBook)
        if (verseFragment.isAdded) verseFragment.updateList(tBook)
        searchViewPager.currentItem++
        updateHintTemp(tBook, tChapter)

        hideSoftInput(toolbarSearchView)
    }

    override fun onItemSelectedFC(position: Int) {
        tChapter = position + 1

        if (verseFragment.isAdded) verseFragment.updateList(tBook, tChapter)
        searchViewPager.currentItem++
        updateHintTemp(tBook, tChapter)
    }

    override fun onItemSelectedFV(position: Int) {
        //val ma = (mainViewPager.adapter as MainViewPagerAdapter)
        val cPosition = getPosition(tBook, tChapter)
        val newPage = mainViewPager.currentItem != cPosition
        queryFinished = true
        finishSearch()
        val MAX_SETTLE_DURATION = 600L
        mainViewPager.setCurrentItem(cPosition, true)
        @Suppress("DeferredResultUnused")
        GlobalScope.async {
            // Scrolling will fail only if there's lag. Perhaps the first 2 scrolls to warm up.
            val sv = mainViewPager.findViewWithTag<ScrollView>("sv${mainViewPager.currentItem}")
            sv.findViewWithTag<AppCompatTextView>("tv${mainViewPager.currentItem}")?.let { tv ->
                if (newPage)
                    while (tv.text.isBlank()) delay(100)
                val idx = if (Page.showVerseNumbers) tv.text.indexOf("${position + 1}") else 0
                if (position == 0)
                    sv.post { sv.smoothScrollTo(0, sv.top - sv.paddingTop) }
                else {
                    tv.post {
                        if (idx >= 0 && tv.layout != null)
                            sv.post { sv.smoothScrollTo(0, tv.layout.getLineTop(tv.layout.getLineForOffset(idx))) }
                    }
                }
            }
        }
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
        /*(currentPageView.adapter as MainAdapter).apply {
            findInPage(null)
            setCurrentlyHighlighted(null, null)
            notifyDataSetChanged()
        }*/
        toolbarSearchView.setOnQueryTextListener(null)
        toolbarSearchView.setOnQueryTextListener(miniSearchListener)
        findInPageMenu = false
    }

    private fun View.vibrate() {
        this.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
    }

    private fun showFindInPageSearch() {
        val regex = Regex("""([a-zA-Z,.;:()'? ]+)""")
        val versesRaw = viewModel.getVersesRaw(row!!.bookId!!, row!!.chapterId!!)
        findInPageMenu = true
        matchesList.reset(versesRaw.size)

        // Find in page views
        showSearch()
        //setClickListener(fipUpButton, currentPageView)
        //setClickListener(fipDownButton, currentPageView)
        val sv = mainViewPager.findViewWithTag<ScrollView>("sv${mainViewPager.currentItem}")
        val tv = mainViewPager.findViewWithTag<AppCompatTextView>("tv${mainViewPager.currentItem}")
        val dcv = mainViewPager.findViewWithTag<AppCompatTextView>("dcv${mainViewPager.currentItem}")
        var sc = 0
        var idx = 0
        // val fm = tv.paint.fontMetrics

        var currentText = ""
        val ignCase = true

        var pressed = 0
        val ss = tv.text as Spannable
        val dcvs = dcv?.text as? Spannable

        //val textBounds = Rect()
        //tv.paint.getTextBounds(ss.toString(), 0, 5, textBounds)
        //lineHeight = textBounds.height()//tv.paint.measureText(ss, 0, 5).toInt()//spToPx(tv.textSize)//(lineHeight * 0.35f).toInt()

        //https://is.gd/oJLdB1
        fipUpButton.setOnClickListener {
            sv.post {
                //sc += tv.lineHeight

                if (idx <= 0) {
                    // No match
                    //sv.smoothScrollTo(0, 0)
                    //sv.fullScroll(View.FOCUS_UP)

                    //sv.smoothScrollTo(0, sv.top - sv.paddingTop)
                } else {
                    if (findInPageMatch == 1) findInPageMatch = matchesCount
                    else findInPageMatch--
                    if (pressed != 0) {
                        ss.setSpan(CharacterStyle.wrap(BackgroundColorSpan(Formatting.HighLightColor)), idx, idx + currentText.length, 0)
                        idx = tv.text.lastIndexOf(currentText, idx - 1, ignoreCase = ignCase).let {
                            if (it < 0) {
                                pressed = 0
                                tv.text.lastIndexOf(currentText, ignoreCase = ignCase)
                            } else
                                it
                        }
                        sc = tv.layout.getLineForOffset(idx)
                    }
                    sv.smoothScrollTo(0, tv.layout.getLineTop(sc))
                    ss.setSpan(CharacterStyle.wrap(BackgroundColorSpan(Formatting.HighlightFocusColor)), idx, idx + currentText.length, 0)
                    ++pressed
                    //fipCountText.text = ("$findInPageMatch/$matchesCount")
                    fipCountText.futureSet("$findInPageMatch/$matchesCount")

                }
                //d { "[UP] sc: $sc id: $idx " }

            }
        }
        fipDownButton.setOnClickListener {
            sv.post {
                //sc += tv.lineHeight
                if (idx < 0) {
                    // No match
                    //sv.smoothScrollTo(0, 0)
                    //sv.fullScroll(View.FOCUS_UP)
                    //sv.smoothScrollTo(0, sv.top - sv.paddingTop)
                } else {
                    if (findInPageMatch == matchesCount) findInPageMatch = 1
                    else findInPageMatch++
                    if (pressed != 0) {
                        ss.setSpan(CharacterStyle.wrap(BackgroundColorSpan(Formatting.HighLightColor)), idx, idx + currentText.length, 0)
                        idx = tv.text.indexOf(currentText, idx + 1, ignoreCase = ignCase).let {
                            if (it < 0) {
                                pressed = 0
                                tv.text.indexOf(currentText, ignoreCase = ignCase)
                            } else
                                it
                        }
                        sc = tv.layout.getLineForOffset(idx)
                    }
                    sv.smoothScrollTo(0, tv.layout.getLineTop(sc))
                    ss.setSpan(CharacterStyle.wrap(BackgroundColorSpan(Formatting.HighlightFocusColor)), idx, idx + currentText.length, 0)
                    ++pressed
                    //d { "[DOWN] sc: $sc id: $idx lineHeight: $lineHeight y: $y" }
                    //fipCountText.text = ("$findInPageMatch/$matchesCount")
                    fipCountText.futureSet("$findInPageMatch/$matchesCount")

                }
            }
        }
        toggleSearchViewCloseButton(true)
        actionBarDrawerToggle.isDrawerIndicatorEnabled = false
        findInSearchContainer.visibility = View.VISIBLE

        toolbarSearchView.isIconified = false
        toolbarSearchView.queryHint = getString(R.string.find_in_page)
        toolbarSearchView.setOnQueryTextListener(null)

        var tempString = ""

        val spanRemover = StyleSpanRemover()
        toolbarSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                //matchesList.reset()
                if (tempString.isBlank()) tempString = newText
                currentText = newText
                findInPageMatch = 0
                pressed = 0
                idx = 0
                sc = 0
                idx = tv.text.indexOf(newText, ignoreCase = ignCase)
                sc = tv.layout.getLineForOffset(idx)
                if (newText.isBlank()) {
                    fipCountText.text = null
                    spanRemover.RemoveOne(ss, 0, ss.length, BackgroundColorSpan::class.java)
                    if (Page.showDropCap)
                        spanRemover.RemoveOne(dcvs!!, 0, ss.length, BackgroundColorSpan::class.java)
                } else if (!regex.matches(newText)) {
                    fipCountText.futureSet("0/0")
                    ContextCompat.getColor(fipCountText.context, R.color.search_not_found).let {
                        if (fipCountText.currentTextColor != it)
                            fipCountText.setTextColor(it)
                    }

                    if (newText != tempString.dropLast(1)) // Don't vibrate on backspace
                        toolbarSearchView.vibrate() //(60)
                    tempString = newText
                } else {

                    spanRemover.RemoveOne(ss, 0, ss.length, BackgroundColorSpan::class.java)
                    if (Page.showDropCap)
                        spanRemover.RemoveOne(dcvs!!, 0, ss.length, BackgroundColorSpan::class.java)


                    matchesCount = tv.text.count(newText, ignoreCase = ignCase) { s, e, _ ->
                        ss.setSpan(CharacterStyle.wrap(BackgroundColorSpan(Formatting.HighLightColor)), s, e, 0)
                    }
                    if (matchesCount > 0) {
                        Formatting.WhiteColor.let {
                            if (fipCountText.currentTextColor != it)
                                fipCountText.setTextColor(it)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                            fipDownButton.callOnClick()
                        else
                            fipDownButton.performClick()
                    } else {
                        fipCountText.futureSet("0/0")
                        Formatting.SearchNotFoundColor.let {
                            if (fipCountText.currentTextColor != it)
                                fipCountText.setTextColor(it)
                        }
                        if (newText != tempString.dropLast(1)) // Don't vibrate on backspace
                            toolbarSearchView.vibrate() //(60)
                        tempString = newText
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

    private fun getPosition(book: Int = 1, chapter: Int = 1): Int = bible.indexOfFirst { it.bookId == book && it.chapterId == chapter } //viewModel.getPagePosition(book, chapter)

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

        val row = bible[getPosition(book, chapter)] // val row = bible[getPosition(book, chapter)]
        toolbarSearchView.queryHint = "Search ${row.bookName} $chapter..."
    }

    private fun blink(view: View, rep: Int, duration: Long) {
        if ((rep % 2) != 0) throw Exception("Blinking repetition must be EVEN for fade IN/OUT animation.")
        val startBufffer = 500.toLong()
        @Suppress("DeferredResultUnused")
        async(IO) {
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

    private fun tintMenuIcon(item: MenuItem, @ColorRes color: Int) {
        val normalDrawable = item.icon
        val wrapDrawable = DrawableCompat.wrap(normalDrawable)
        DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(this, color))

        item.icon = wrapDrawable
    }

}

