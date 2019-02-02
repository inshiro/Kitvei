package na.kephas.kitvei.page

import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup

import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import java.util.Objects

import na.kephas.kitvei.R
import na.kephas.kitvei.util.DimenUtil

/**
 * Descendant of BottomSheetDialogFragment that adds a few features and conveniences.
 */
open class ExtendedBottomSheetDialogFragment : BottomSheetDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        setWindowLayout()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        setWindowLayout()
    }

    protected fun disableBackgroundDim() {
        dialog.window!!.setDimAmount(0f)
    }

    private fun setWindowLayout() {
        if (dialog != null) {
           dialog.window?.setLayout(dialogWidthPx(), ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    private fun dialogWidthPx(): Int {
        return Math.min(DimenUtil.getDisplayWidthPx(),
                resources.getDimension(R.dimen.bottomSheetMaxWidth).toInt())
    }
}
