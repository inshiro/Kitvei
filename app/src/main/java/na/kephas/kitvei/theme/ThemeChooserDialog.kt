package na.kephas.kitvei.theme

import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.style.BackgroundColorSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import na.kephas.kitvei.App
import na.kephas.kitvei.Prefs
import na.kephas.kitvei.R
import na.kephas.kitvei.R.id.buttonDecreaseTextSize
import na.kephas.kitvei.R.id.buttonIncreaseTextSize
import na.kephas.kitvei.activity.FragmentUtil
import na.kephas.kitvei.activity.MainActivity
import na.kephas.kitvei.adapter.MainViewPagerAdapter
import na.kephas.kitvei.page.ExtendedBottomSheetDialogFragment
import na.kephas.kitvei.page.Page
import na.kephas.kitvei.theme.ThemeChooserDialog.Companion.fontSize
import na.kephas.kitvei.util.*
import na.kephas.kitvei.views.DiscreteSeekBar
import java.security.AccessController.getContext

/*
import com.squareup.otto.Subscribe
import org.wikipedia.R
import org.wikipedia.WikipediaApp
import org.wikipedia.activity.FragmentUtil
import org.wikipedia.analytics.AppearanceChangeFunnel
import org.wikipedia.events.WebViewInvalidateEvent
import org.wikipedia.page.ExtendedBottomSheetDialogFragment
import org.wikipedia.settings.Prefs
import org.wikipedia.util.DimenUtil
import org.wikipedia.util.FeedbackUtil
import org.wikipedia.util.ResourceUtil
import org.wikipedia.views.DiscreteSeekBar
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnCheckedChanged
import butterknife.Unbinder*/

class ThemeChooserDialog : ExtendedBottomSheetDialogFragment() {
    /* @BindView(R.id.buttonDecreaseTextSize) internal var buttonDecreaseTextSize:TextView
     @BindView(R.id.buttonIncreaseTextSize) internal var buttonIncreaseTextSize:TextView
     @BindView(R.id.text_size_percent) internal var textSizePercent:TextView
     @BindView(R.id.text_size_seek_bar) internal var textSizeSeekBar:DiscreteSeekBar
     @BindView(R.id.button_theme_light) internal var buttonThemeLight:TextView
     @BindView(R.id.button_theme_dark) internal var buttonThemeDark:TextView
     @BindView(R.id.button_theme_black) internal var buttonThemeBlack:TextView
     @BindView(R.id.button_theme_light_highlight) internal var buttonThemeLightHighlight:View
     @BindView(R.id.button_theme_dark_highlight) internal var buttonThemeDarkHighlight:View
     @BindView(R.id.button_theme_black_highlight) internal var buttonThemeBlackHighlight:View
     @BindView(R.id.theme_chooser_dark_mode_dim_images_switch) internal var dimImagesSwitch:SwitchCompat
     @BindView(R.id.font_change_progress_bar) internal var fontChangeProgressBar:ProgressBar
    */
    private lateinit var buttonDecreaseTextSize: TextView
    private lateinit var buttonIncreaseTextSize: TextView
    private lateinit var textSizePercent: AppCompatTextView
    private lateinit var textSizeSeekBar: DiscreteSeekBar
    private lateinit var buttonThemeLight: TextView
    private lateinit var buttonThemeDark: TextView
    private lateinit var buttonThemeBlack: TextView
    private lateinit var buttonThemeLightHighlight: View
    private lateinit var buttonThemeDarkHighlight: View
    private lateinit var buttonThemeBlackHighlight: View
    private lateinit var dimImagesSwitch: SwitchCompat
    private lateinit var fontChangeProgressBar: ProgressBar
    private var updatingFont = false
    private val app by lazy { App.instance }

    interface Callback {
        fun onToggleDimImages()
        fun onCancel()
    }

    private enum class FontSizeAction {
        INCREASE, DECREASE, RESET
    }

    companion object {
        const val PORTRAIT = 0
        const val LANDSCAPE = 1
        var currentOrientation = PORTRAIT
        var fontSize = Prefs.mainFontSize
    }

    val vp by lazy { (activity as MainActivity).mainViewPager }

    private fun getTextView(page: Int = -1): TextView? {
        return when (page) {
            0 -> vp.findViewWithTag("tv${vp.currentItem - 1}")
            1 -> vp.findViewWithTag("tv${vp.currentItem + 1}")
            else -> vp.findViewWithTag("tv${vp.currentItem}")
        }
    }

    private fun getDropCapView(page: Int = -1): TextControl? {
        return when (page) {
            0 -> vp.findViewWithTag("dcv${vp.currentItem - 1}")
            1 -> vp.findViewWithTag("dcv${vp.currentItem + 1}")
            else -> vp.findViewWithTag("dcv${vp.currentItem}")
        }
    }

    private fun saveFontChanges() {
        if (Prefs.mainFontSize != fontSize) {
            Prefs.mainFontSize = fontSize
            //getRecyclerView(0)?.adapter?.notifyDataSetChanged()
            //getRecyclerView(1)?.adapter?.notifyDataSetChanged()
            val dcv0 = getDropCapView(0)?.apply { setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSize * 4.85f) }
            val dcv1 = getDropCapView(1)?.apply { setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSize * 4.85f) }
            getTextView(0)?.apply {
                setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSize)
                dcv0?.let {
                    val ss = text as Spannable
                    ss.removeSpans(0, ss.length, LettrineLeadingMarginSpan2::class.java)
                    ss.setSpan(LettrineLeadingMarginSpan2(2, dcv0.getWidth), 0, if (Page.showLineNumbers) ss.indexOf("2") - 2 else ss.indexOf("\u200B") - 2, 0)
                }
            }
            getTextView(1)?.apply {
                setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSize)
                dcv1?.let {
                    val ss = text as Spannable
                    ss.removeSpans(0, ss.length, LettrineLeadingMarginSpan2::class.java)
                    ss.setSpan(LettrineLeadingMarginSpan2(2, dcv1.getWidth), 0, if (Page.showLineNumbers) ss.indexOf("2") - 2 else ss.indexOf("\u200B") - 2, 0)
                }
            }


        }

        (activity as MainActivity).apply {
            if (currentOrientation == PORTRAIT)
                if (!isTranslucentNavBar())
                    setTranslucentNavBar()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        if (newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            currentOrientation = LANDSCAPE
        } else if (newConfig?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            currentOrientation = PORTRAIT

        }
    }

    private fun setSeekProgress(size: Float) {
        textSizeSeekBar.post { textSizeSeekBar.progress = size.toInt() } // (fontSize * 10f).toInt() //@ 100
        textSizePercent.futureSet(
                "${(fontSize * 10f).toInt()}%".let {
                    if (it == "100%")
                        "100% (Default)"
                    else
                        it
                }
        )
    }

    private var pTV: TextView? = null
    private var pDCV: TextControl? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dialog_theme_chooser, container, false)
        //unbinder = ButterKnife.bind(this, rootView)

        buttonThemeLight = rootView.findViewById(R.id.button_theme_light)
        buttonThemeLightHighlight = rootView.findViewById(R.id.button_theme_light_highlight)
        buttonThemeBlack = rootView.findViewById(R.id.button_theme_black)
        buttonThemeBlackHighlight = rootView.findViewById(R.id.button_theme_black_highlight)
        buttonThemeDark = rootView.findViewById(R.id.button_theme_dark)
        buttonThemeDarkHighlight = rootView.findViewById(R.id.button_theme_dark_highlight)
        dimImagesSwitch = rootView.findViewById(R.id.theme_chooser_dark_mode_dim_images_switch)
        fontChangeProgressBar = rootView.findViewById(R.id.font_change_progress_bar)
        textSizePercent = rootView.findViewById(R.id.text_size_percent)
        textSizeSeekBar = rootView.findViewById(R.id.text_size_seek_bar)
        buttonDecreaseTextSize = rootView.findViewById(R.id.buttonDecreaseTextSize)
        buttonIncreaseTextSize = rootView.findViewById(R.id.buttonIncreaseTextSize)
        buttonDecreaseTextSize.setOnClickListener(FontSizeButtonListener(FontSizeAction.DECREASE))
        buttonIncreaseTextSize.setOnClickListener(FontSizeButtonListener(FontSizeAction.INCREASE))
        //FeedbackUtil.setToolbarButtonLongPressToast(buttonDecreaseTextSize, buttonIncreaseTextSize)
        buttonThemeLight.setOnClickListener(ThemeButtonListener(Theme.LIGHT))
        buttonThemeDark.setOnClickListener(ThemeButtonListener(Theme.DARK))
        buttonThemeBlack.setOnClickListener(ThemeButtonListener(Theme.BLACK))

        //  progress has something to do with multiplier, that's why its starting at 180%
        //val vpAdapter = vp.adapter as MainViewPagerAdapter
        pTV = getTextView()
        pDCV = getDropCapView()
        textSizeSeekBar.max = 20
        /*textSizeSeekBar.progress = fontSize.toInt() // (fontSize * 10f).toInt() //@ 100
        textSizePercent.futureSet(
                "${(fontSize * 10f).toInt()}%".let {
                    if (it == "100%")
                        "100% (Default)"
                    else
                        it
                }
        )*/
        setSeekProgress(fontSize)
        textSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, value: Int, fromUser: Boolean) {
                if (!fromUser) return
                //val currentMultiplier = Prefs.mainFontSize
                fontSize = value.toFloat()
                updateFont(fontSize)

                /*
                val changed = app.setFontSizeMultiplier(textSizeSeekBar.value)
                if (changed) {
                    updatingFont = true
                    updateFontSize()
                    //funnel.logFontSizeChange(currentMultiplier, Prefs.getTextSizeMultiplier())
                }*/
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        //updateComponents()
        disableBackgroundDim()
        return rootView
    }

    override fun onDismiss(dialog: DialogInterface?) {
        pTV = null
        pDCV = null
        super.onDismiss(dialog)
        saveFontChanges()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val params = (getView()?.parent as? View)?.layoutParams as? CoordinatorLayout.LayoutParams
        val behavior = params?.behavior
        if (behavior != null && behavior is BottomSheetBehavior<*>) {

            getView()?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        getView()?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                    } else {
                        @Suppress("DEPRECATION")
                        getView()?.viewTreeObserver?.removeGlobalOnLayoutListener(this)
                    }
                    behavior.peekHeight = getView()!!.measuredHeight
                    /*(activity as MainActivity).apply {
                        if (isTranslucentNavBar())
                            cancelTranslucentNavBar()
                    }*/
                }
            })
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //app.getBus().register(this)
        //funnel = AppearanceChangeFunnel(app, app.getWikiSite())
    }

    override fun onCancel(dialog: DialogInterface) {
        pTV = null
        pDCV = null
        super.onCancel(dialog)
        if (callback() != null)
            callback()?.onCancel()
    }

    /*@Subscribe fun on(event:WebViewInvalidateEvent) {
        updatingFont = false
        updateComponents()
    }*/
    //@OnCheckedChanged(R.id.theme_chooser_dark_mode_dim_images_switch)

    private fun updateComponents() {
        updateFontSize()
        updateThemeButtons()
        updateDimImagesSwitch()
    }

    private fun updateFontSize() {
        val mult = Prefs.textSizeMultiplier.toInt()
        textSizeSeekBar.value = mult
        val percentStr = getString(R.string.text_size_percent,
                (100 * (1 + mult * DimenUtil.getFloat(R.dimen.textSizeMultiplierFactor))).toInt())
        textSizePercent.text = if (mult == 0)
            getString(R.string.text_size_percent_default, percentStr)
        else
            percentStr
        if (updatingFont)
            fontChangeProgressBar.visibility = View.VISIBLE
        else
            fontChangeProgressBar.visibility = View.GONE
    }

    private fun updateThemeButtons() {
        buttonThemeLightHighlight.visibility = if (app.getCurrentTheme() === Theme.LIGHT) View.VISIBLE else View.GONE
        buttonThemeLight.isClickable = app.getCurrentTheme() !== Theme.LIGHT
        buttonThemeDarkHighlight.visibility = if (app.getCurrentTheme() === Theme.DARK) View.VISIBLE else View.GONE
        buttonThemeDark.isClickable = app.getCurrentTheme() !== Theme.DARK
        buttonThemeBlackHighlight.visibility = if (app.getCurrentTheme() === Theme.BLACK) View.VISIBLE else View.GONE
        buttonThemeBlack.isClickable = app.getCurrentTheme() !== Theme.BLACK
    }

    private fun updateDimImagesSwitch() {
        //dimImagesSwitch.isChecked = Prefs.shouldDimDarkModeImages
        dimImagesSwitch.isEnabled = app.getCurrentTheme().isDark
        dimImagesSwitch.setTextColor(if (dimImagesSwitch.isEnabled)
            ResourceUtil.getThemedColor(getContext()!!, R.attr.section_title_color)
        else
            ContextCompat.getColor(getContext()!!, R.color.black26))
    }


    private fun updateFont(fontSize: Float) {
        updatingFont = true
        fontChangeProgressBar.visibility = View.VISIBLE


        Page.setFontSize(fontSize)
        pDCV?.setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSize * 4.85f)

        // Box area of Initial letter
        val ss = pTV?.text as Spannable
        ss.removeSpans(0, ss.length, LettrineLeadingMarginSpan2::class.java)
        ss.setSpan(LettrineLeadingMarginSpan2(2, pDCV!!.getWidth), 0, if (Page.showLineNumbers) ss.indexOf("2") - 2 else ss.indexOf("\u200B") - 2, 0)

        pTV?.setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSize)

        setSeekProgress(fontSize)

        updatingFont = false
        fontChangeProgressBar.visibility = View.GONE
    }

    private inner class ThemeButtonListener constructor(private val theme: Theme) : View.OnClickListener {
        override fun onClick(v: View?) {
            //v?.toast("This feature is still in development.")
            return
            @Suppress("UNREACHABLE_CODE")
            if (app.getCurrentTheme() !== theme) {
                //funnel.logThemeChange(app.getCurrentTheme(), theme)
                app.setCurrentTheme(theme)
            }
        }
    }

    private inner class FontSizeButtonListener constructor(private val action: FontSizeAction) : View.OnClickListener {
        @Suppress("UNREACHABLE_CODE")

        override fun onClick(v: View?) {
            //v?.toast("This feature is still in development.")
            //return
            val changed: Boolean = when (action) {
                FontSizeAction.INCREASE -> {
                    if (fontSize + 1 <= textSizeSeekBar.max) {
                        fontSize++
                        updateFont(fontSize)
                    }
                    true
                }
                FontSizeAction.DECREASE -> {
                    if (fontSize - 1 >= 0) {
                        fontSize--
                        updateFont(fontSize)
                    }
                    true
                }
                FontSizeAction.RESET -> {
                    fontSize = 10f
                    app.setFontSizeMultiplier(0)
                }
            }
            /*val currentMultiplier = Prefs.textSizeMultiplier
            if (changed) {
                updateFontSize()
                funnel.logFontSizeChange(currentMultiplier, Prefs.getTextSizeMultiplier())
            }*/
        }
    }

    private fun callback(): Callback? {
        return FragmentUtil.getCallback(this, Callback::class.java)
    }
}