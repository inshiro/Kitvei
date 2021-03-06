package na.kephas.kitvei.util

import android.text.Selection
import android.text.Spannable
import android.text.method.BaseMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import android.text.TextPaint
import android.text.style.UpdateAppearance
import android.text.style.CharacterStyle





/**
 * A movement method that traverses links in the text buffer and fires clicks. Unlike
 * [LinkMovementMethod], this will not consume touch events outside [ClickableSpan]s.
 */
class ClickableMovementMethod : BaseMovementMethod() {

    override fun canSelectArbitrarily(): Boolean {
        return false
    }

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {

        val action = event.actionMasked
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {

            var x = event.x.toInt()
            var y = event.y.toInt()
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY

            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())

            val link = buffer.getSpans(off, off, ClickableSpan::class.java)
            if (link.size > 0) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(widget)
                } else {
                    Selection.setSelection(buffer, buffer.getSpanStart(link[0]),
                            buffer.getSpanEnd(link[0]))
                }
                return true
            } else {
                Selection.removeSelection(buffer)
            }
        }

        return false
    }

    override fun initialize(widget: TextView, text: Spannable) {
        Selection.removeSelection(text)
    }

    companion object {

        private var sInstance: ClickableMovementMethod? = null

        val instance: ClickableMovementMethod
            get() {
                if (sInstance == null) {
                    sInstance = ClickableMovementMethod()
                }
                return sInstance!!
            }
    }
}
internal class ColoredUnderlineSpan(private val mColor: Int) : CharacterStyle(), UpdateAppearance {

    override fun updateDrawState(tp: TextPaint) {
        try {
            val method = TextPaint::class.java.getMethod("setUnderlineText",
                    Integer.TYPE,
                    java.lang.Float.TYPE)
            method.invoke(tp, mColor, 1.0f)
        } catch (e: Exception) {
            tp.isUnderlineText = true
        }

    }
}
fun AppCompatTextView.setTextViewLinkClickable() {
   // movementMethod = ClickableMovementMethod.instance
   // movementMethod = ScrollingMovementMethod.getInstance()
    movementMethod = android.text.method.LinkMovementMethod.getInstance()
    highlightColor = android.graphics.Color.TRANSPARENT
    isClickable = false
    isLongClickable = false
}