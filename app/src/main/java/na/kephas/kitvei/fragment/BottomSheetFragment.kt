package na.kephas.kitvei.fragment


import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.SeekBar
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import na.kephas.kitvei.R
import na.kephas.kitvei.activity.MainActivity
import na.kephas.kitvei.adapter.MainAdapter
import na.kephas.kitvei.prefs


class BottomSheetFragment : BottomSheetDialogFragment() {

    val vp by lazy { (activity as MainActivity).mainViewPager }
    var fontSize = prefs.fontSize

    companion object {
        const val PORTRAIT = 0
        const val LANDSCAPE = 1
        var currentOrientation = PORTRAIT

    }

    private fun getRecyclerView(page: Int = -1): RecyclerView? {
        return when (page) {
            0 -> vp.findViewWithTag("rv${vp.currentItem - 1}")
            1 -> vp.findViewWithTag("rv${vp.currentItem + 1}")
            else -> vp.findViewWithTag("rv${vp.currentItem}")
        }
    }

    private fun saveFontChanges() {
        if (prefs.fontSize != fontSize) {
            prefs.fontSize = fontSize
            getRecyclerView(0)?.adapter?.notifyDataSetChanged()
            getRecyclerView(1)?.adapter?.notifyDataSetChanged()
        }

        (activity as MainActivity).apply {
            if (currentOrientation == PORTRAIT)
                if (!isTranslucentNavBar())
                    setTranslucentNavBar()
        }
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        saveFontChanges()

    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        if (newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            currentOrientation = LANDSCAPE
        } else if (newConfig?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            currentOrientation = PORTRAIT

        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog.setOnShowListener {
            dialog.window.setDimAmount(0f)


            val params = (getView()?.parent as View).layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior

            val rv = getRecyclerView()

            if (behavior != null && behavior is BottomSheetBehavior<*>) {


                behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {

                        when (newState) {
                            BottomSheetBehavior.STATE_DRAGGING -> {
                            }
                            BottomSheetBehavior.STATE_SETTLING -> {
                            }
                            BottomSheetBehavior.STATE_EXPANDED -> {
                            }
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                            }
                            BottomSheetBehavior.STATE_HIDDEN -> {
                                dismiss()
                                saveFontChanges()
                            }
                        }
                        //view.snackbar("Bottom Sheet State Changed to: $state")

                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                })

                // Expand completely
                getView()?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            getView()?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                        } else {
                            @Suppress("DEPRECATION")
                            getView()?.viewTreeObserver?.removeGlobalOnLayoutListener(this)
                        }
                        behavior.peekHeight = getView()!!.measuredHeight
                        (activity as MainActivity).apply {
                            if (isTranslucentNavBar())
                                cancelTranslucentNavBar()
                        }
                    }
                })

            }

            val seekBar = view.findViewById<SeekBar>(R.id.seekBar)
            val fontSizeTextView = view.findViewById<TextView>(R.id.fontSizeTextView)
            seekBar.max = 20
            seekBar.progress = (fontSize * 10f).toInt()
            fontSizeTextView.text = ("${(fontSize * 100f).toInt()}%")
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    fontSize = p1 / 10f
                    fontSizeTextView.text = ("${(fontSize * 100f).toInt()}%")
                    (rv?.adapter as MainAdapter).apply {
                        setFontSize(fontSize)
                        notifyDataSetChanged()
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }

            })
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        currentOrientation = if (activity!!.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) PORTRAIT else LANDSCAPE
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet, container, false)
    }
}
