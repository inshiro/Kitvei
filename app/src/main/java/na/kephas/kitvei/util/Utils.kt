package na.kephas.kitvei.util

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.PrecomputedTextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.experimental.async
import java.lang.ref.WeakReference
import java.util.regex.Pattern

inline fun d(tag: String = "TAG", msg: () -> String) {
    Log.d(tag, msg())
}

infix fun <T> T.ld(msg: String) {
    Log.d("TAG", msg)
}

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

fun View.toast(str: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this.context, str, duration).show()
}

fun AppCompatTextView.futureSet(charSequence: CharSequence) {
    val future = PrecomputedTextCompat.getTextFuture(charSequence, this.textMetricsParamsCompat, IO_EXECUTOR)
    this.setTextFuture(future!!)
}

fun SpannableStringBuilder.properSubstring(start: Int, end: Int = this.length) {
    apply {
        if (end > 0)
            delete(end, length)
        if (start > 0)

            delete(0, start)
    }
}

/**
 * Returns a SpannableStringBuilder to a String as a variable instead of method
 */
val SpannableStringBuilder.toString: String
    get() = this.toString()

/**
 * Count Occurrences of a String in a String
 */
inline fun CharSequence.count(sub: String, ignoreCase: Boolean = false, action: (start: Int, end: Int, countSoFar: Int) -> Unit = { _, _, _ -> }): Int {
    var count = 0
    var startIdx = 0
    while ({ indexOf(sub, startIdx, ignoreCase = ignoreCase).also { startIdx = it + 1 } }() >= 0) {
        count++
        action(startIdx - 1, startIdx - 1 + sub.length, count)
    }
    return count
}

inline fun CharSequence.count(sub: Char, action: (start: Int, end: Int, countSoFar: Int) -> Unit = { _, _, _ -> }): Int {
    var count = 0
    var startIdx = 0
    while ({ indexOf(sub, startIdx).also { startIdx = it + 1 } }() >= 0) {
        count++
        action(startIdx - 1, startIdx, count)
    }
    return count
}

/**
 * Removes all spans given a class
 */
fun <T> Spannable.removeSpans(start: Int, end: Int, aClass: Class<T>) {
    val spans = getSpans(start, end, aClass)
    if (spans.isNotEmpty()) {
        for (span in spans) {
            removeSpan(span)
        }
    }
}

val AppCompatTextView.getWidth
    get() = {
        this.measure(0, 0)
        this.measuredWidth
    }()

// Returns number of occurrences of substring in String, or additional does something with values.
inline fun String.occurrences(sub: String, flags: Int = Pattern.LITERAL or Pattern.CASE_INSENSITIVE, removeDelimiter: Boolean = false, additional: (matches: Int, start: Int, end: Int) -> Unit = { _, _, _ -> }): Int {
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

fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)
inline fun measureTime(block: () -> Unit): Double {
    val startTime = System.currentTimeMillis()
    block.invoke()
    return (System.currentTimeMillis() - startTime) / 1000.0
}


inline fun measureTimeMillis(block: () -> Unit): Long {
    val startTime = System.currentTimeMillis()
    block.invoke()
    return System.currentTimeMillis() - startTime
}

inline fun benchmark(range: IntRange, block: () -> Unit): Double {
    return (range).map {
        measureTimeMillis { block() }
    }.average()
}

// Custom index of first
inline fun <T> Iterable<T>.each(startIndex: Int? = null, block: (T) -> Boolean): Int {
    var index = 0
    if (startIndex != null) {
        for (element in this) {
            if (index >= startIndex) {
                if (block(element)) return index
            }
            index++
        }
    } else {
        for (element in this) {
            if (block(element)) return index
            index++
        }
    }
    return -1
}

inline fun TextView.precomputeAndSet(crossinline block: (TextView) -> Any) {
    // UI thread
    val params: PrecomputedTextCompat.Params = (this as AppCompatTextView).textMetricsParamsCompat
    val ref = WeakReference(this)
    @Suppress("DeferredResultUnused")
    async(fixedThreadPool) {
        // background thread
        val text = PrecomputedTextCompat.create(block(this@precomputeAndSet) as CharSequence, params)
        ref.get()?.post {
            // UI thread
            val textViewRef = ref.get()
            textViewRef?.setText(text, TextView.BufferType.SPANNABLE)
        }
    }
}

var recyclerViewReadyCallback: RecyclerViewReadyCallBack? = null

interface RecyclerViewReadyCallBack {
    fun onLayoutReady()
}

fun RecyclerView.onLayoutReady(action: () -> Unit) {
    recyclerViewReadyCallback = object : RecyclerViewReadyCallBack {
        override fun onLayoutReady() {
            action()
        }
    }
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            recyclerViewReadyCallback?.onLayoutReady()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            } else {
                @Suppress("DEPRECATION")
                viewTreeObserver.removeGlobalOnLayoutListener(this)
            }
        }
    })
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

val AppCompatActivity.isTranslucentNavBar: () -> Boolean
    get() = {
        val flags = this.window.attributes.flags
        if ((flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION) == WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            true
        false
    }

inline fun <T> silentTry(block: () -> T) {
    try {
        block()
    } catch (e: Exception) {
    }
}

inline fun <T> tryy(block: () -> T) {
    try {
        block()
    } catch (e: Exception) {
        Log.wtf("TAG", e)
        //throw RuntimeException(e)//Log.d("TAG", e.message)//e.printStackTrace()
    }
}

/**
 *
 * Map Extensions
 *
 */
// onEachValue

infix fun <T> T.eq(other: T): Boolean = this == other

infix fun <T> T.neq(other: T): Boolean = this != other

inline fun <K : Comparable<K>, V, M : Map<out K, V>, T> M.onEachValue(action: (k: K, v: Int) -> T): M {
    return apply {
        for (element in this)
            (element.value as Int).let {
                ((if (it < 1) it else 1)..it)
                        .forEach { v ->
                            action(element.key, v).also {
                                if (it is Boolean && it)
                                    return this
                            }
                        }
            }
    }
}

// sum
val <K, V, M : Map<out K, V>> M.sum: Int
    get() = this.asIterable().sumBy { it.value as Int }

// onMax
inline fun <K : Comparable<K>, V, M : Map<out K, V>, T> M.onMax(action: (k: K, v: V) -> T): Map<K, V> {
    maxBy { it.key }?.let {
        return mapOf(it.toPair())
                .also {
                    with(it.entries.first()) { action(key, value) }
                }
    } ?: return mapOf()
}
// onMin

inline fun <K : Comparable<K>, V, M : Map<out K, V>, T> M.onMin(action: (k: K, v: V) -> T): Map<K, V> {
    minBy { it.key }?.let {
        return mapOf(it.toPair())
                .also {
                    with(it.entries.first()) { action(key, value) }
                }
    } ?: return mapOf()
}

/**
 * MutableList Extension Functions
 */
val MutableList<Int>.sum: Int
    get() = this.filter { it != 0 }.sumBy { it }

fun <T> MutableList<T>.count(predicate: () -> Boolean): Int {
    var count = 0

    for (element in this) {
        if (predicate()) {
            count += 1
        }
    }

    return count
}

fun MutableList<Int>.reset(size: Int = this.size): Boolean {
    if (size < 1)
        return false
    // If size is user set, clear list
    if (size != this.size)
        this.clear()
    for (a in 0 until size)
        if (size != this.size)
            this.add(0)
        else
            if (this[a] != 0)
                this[a] = 0
    return true
}


inline fun MutableList<Int>.next(end: Boolean = false, block: (index: Int, value: Int) -> Unit) {
    this.forEachIndexed { index, mValue ->
        if (mValue != 0) {
            (1..mValue).forEach { value ->
                block(index, value)
                if (end) return
            }
        }
    }
}
/**
 * =====================================================================
 */

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
