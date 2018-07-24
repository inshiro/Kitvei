package na.kephas.kitvei.util

import android.util.TypedValue
import androidx.annotation.DimenRes
import na.kephas.kitvei.App

object DimenUtil {
        private val resources by lazy { App.instance.resources }
        private val displayMetrics by lazy { resources.displayMetrics!! }
        fun getDisplayWidthPx() = displayMetrics.widthPixels

        private fun getValue(@DimenRes id: Int): TypedValue {
                val typedValue = TypedValue()
                resources.getValue(id, typedValue, true)
                return typedValue
        }
        fun getFloat(@DimenRes id: Int): Float {
                return getValue(id).float
        }
}