package na.kephas.kitvei.util

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.util.regex.Pattern

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

// Returns number of occurrences of substring in String, or additional does something with values.
fun String.occurrences(sub: String, flags: Int = Pattern.LITERAL or Pattern.CASE_INSENSITIVE, removeDelimiter: Boolean = false, additional: (matches: Int, start: Int, end: Int) -> Unit = { _, _, _ -> }): Int {
    val pattern = Pattern.compile(sub, flags) ?: return 0
    val matcher = pattern.matcher(this)
    var matches = 0
    var start: Int
    var end: Int
    //var s = this
    while (matcher.find()) {
        if (removeDelimiter) {
            start = matcher.start() - (matches * 2)
            end = matcher.end() - (matches * 2)

            // Removes delimeter in matched string, replaces all occurrences and applies style span
            additional.invoke(matches, start, end)
            //s = s.replaceRange(start, start + 1, "")
            //s = s.replaceRange(end - 2, end - 1, "")
            matches++
        } else {
            matches++
            start = matcher.start()
            end = matcher.end()
            additional.invoke(matches, start, end)
        }
    }
    return matches
}


fun String.formatText(): String {
    var text = this.replace('[', '_').replace(']', '_')

    if (text.indexOf('<') == 0) {
        text = text.substring(text.lastIndexOf('>') + 1, text.length).trim()
    } else if (text.contains('<')) {
        text = text.substring(0, text.indexOf('<')).trim()
    }

    if (text.contains('_'))
        text = text.replace("_", "")

    return text
}

fun measureTimeMillis(block: () -> Unit): Long {
    val startTime = System.currentTimeMillis()
    block.invoke()
    return System.currentTimeMillis() - startTime
}

fun benchmark(range: IntRange, block: () -> Unit): Double {
    val average = (range).map {
        measureTimeMillis { block() }
    }.average()
    return average
}

// Custom scrolling that waits for layout change
fun RecyclerView.scrollTo(position: Int, smoothScroll: Boolean = false, post: Boolean = false) {
    this.onLayoutChanged(post) {
        this.fling(0, 0)
        if (smoothScroll)
            this.smoothScrollToPosition(position)
        else
            this.scrollToPosition(position)
    }
}

fun RecyclerView.onLayoutChanged(post: Boolean = false, block: () -> Unit) {
    if (post) {
        this.post {
            this.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                override fun onLayoutChange(p0: View?, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int, p6: Int, p7: Int, p8: Int) {
                    this@onLayoutChanged.removeOnLayoutChangeListener(this)
                    block()
                }
            })
        }
    } else {
        this.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(p0: View?, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int, p6: Int, p7: Int, p8: Int) {
                this@onLayoutChanged.removeOnLayoutChangeListener(this)
                block()
            }
        })
    }
}

fun getStatusBarHeight(r: Resources): Int {
    var result = 0
    val resourceId = r.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = r.getDimensionPixelSize(resourceId)
    }
    return result
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
