package na.kephas.kitvei.util

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.View
import com.google.android.material.snackbar.Snackbar
/*
import android.graphics.drawable.Drawable
import android.os.Looper
import android.util.DisplayMetrics
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import na.kephas.kitvei.R
*/

fun calculateNoOfColumns(context: Context): Int {
    val displayMetrics = context.resources.displayMetrics
    val dpWidth = displayMetrics.widthPixels / displayMetrics.density
    return (dpWidth / 50).toInt() // Width of layout item
}

fun View.snackbar(str: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, str, duration).apply {
        //config()
        show()
    }
}

fun getScreenHeight(): Int = Resources.getSystem().displayMetrics.heightPixels
fun String.toSpanned(): Spanned {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        return Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT)
    } else {
        @Suppress("DEPRECATION")
        return Html.fromHtml(this)
    }
}

/*
var snackbarDrawable: Drawable? = null
private fun getSnackbarDrawable(context: Context): Drawable {
    if (snackbarDrawable == null)
        snackbarDrawable = ContextCompat.getDrawable(context, R.drawable.bg_snackbar)
    return snackbarDrawable!!
}

var snackbarParams: ViewGroup.MarginLayoutParams? = null
fun Snackbar.config() {
    if(snackbarParams== null) {
        snackbarParams = this.view.layoutParams as ViewGroup.MarginLayoutParams
        snackbarParams!!.setMargins(12, 12, 12, 14)
    }
    this.view.layoutParams = snackbarParams

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        this.view.elevation = 6f
    } else {
        ViewCompat.setElevation(this.view, 6f)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        this.view.background = getSnackbarDrawable(this.context)
    } else {
        @Suppress("DEPRECATION")
        this.view.setBackgroundDrawable(getSnackbarDrawable(this.context))
    }

}
fun getScreenWidth(): Int = Resources.getSystem().displayMetrics.widthPixels
fun getLeftRegion(): Float = getScreenWidth() * 0.33f - 70
fun getRightRegion(): Float = getScreenWidth() * 0.66f + 70
fun String.fixItalics(): String = this.replace("[", "<i>").replace("]", "</i>")
fun convertPixelsToDp(px: Float): Float {
    //val resources = context.getResources()
    val metrics = Resources.getSystem().displayMetrics
    return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun getScreenWidthDp(): Float {
    //Resources.getSystem().getDisplayMetrics().widthPixels
    //val displayMetrics = resources.displayMetrics
    val displayMetrics = Resources.getSystem().displayMetrics
    return displayMetrics.widthPixels / displayMetrics.density
}

fun getScreenHeightDp(): Float {
    //Resources.getSystem().getDisplayMetrics().widthPixels
    //val displayMetrics = resources.displayMetrics
    val displayMetrics = Resources.getSystem().getDisplayMetrics()
    return displayMetrics.widthPixels / displayMetrics.density
}

fun setTranslationZ(view: View, translationZ: Float) {
    //setTranslationZ(mRecyclerView, 0.0f)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        ViewCompat.setTranslationZ(view, translationZ)
    } else if (translationZ != 0f) {
        view.bringToFront()
        if (view.parent != null) {
            (view.parent as View).invalidate()
        }
    }
}

fun isOnUiThread(): Boolean = Thread.currentThread() === Looper.getMainLooper().thread
*/
