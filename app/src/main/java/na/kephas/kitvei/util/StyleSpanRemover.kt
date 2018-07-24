package na.kephas.kitvei.util
// https://is.gd/nzAmBQ
import android.text.Spannable
import android.text.style.CharacterStyle
import android.text.style.MetricAffectingSpan
import android.text.style.StyleSpan

import java.util.ArrayList

class StyleSpanRemover {

    fun RemoveOne(spannable: Spannable,
                  startSelection: Int, endSelection: Int, style: Class<*>) {

        val spansParts = getSpanParts(spannable, startSelection, endSelection)
        removeOneSpan(spannable, startSelection, endSelection, style)
        restoreSpans(spannable, spansParts)
    }

    fun RemoveStyle(spannable: Spannable,
                    startSelection: Int, endSelection: Int, styleToRemove: Int) {

        val spansParts = getSpanParts(spannable, startSelection, endSelection)
        removeStyleSpan(spannable, startSelection, endSelection, styleToRemove)
        restoreSpans(spannable, spansParts)
    }

    fun RemoveAll(spannable: Spannable, startSelection: Int, endSelection: Int) {

        val spansParts = getSpanParts(spannable, startSelection, endSelection)
        removeAllSpans(spannable, startSelection, endSelection)
        restoreSpans(spannable, spansParts)
    }

    private fun restoreSpans(spannable: Spannable, spansParts: ArrayList<SpanParts>) {

        for (spanParts in spansParts) {
            if (spanParts.part1.canAplly())
                spannable.setSpan(spanParts.part1.span, spanParts.part1.start,
                        spanParts.part1.end, spanParts.span_flag)
            if (spanParts.part2.canAplly())
                spannable.setSpan(spanParts.part2.span, spanParts.part2.start,
                        spanParts.part2.end, spanParts.span_flag)
        }
    }

    protected fun removeAllSpans(spannable: Spannable, startSelection: Int, endSelection: Int) {

        val spansToRemove = spannable.getSpans(startSelection, endSelection, Any::class.java)
        for (span in spansToRemove) {
            if (span is CharacterStyle)
                spannable.removeSpan(span)
        }
    }

    protected fun removeOneSpan(spannable: Spannable, startSelection: Int, endSelection: Int,
                                style: Class<*>) {

        val spansToRemove = spannable.getSpans(startSelection, endSelection, CharacterStyle::class.java)
        for (span in spansToRemove) {
            if (span.underlying.javaClass == style)
                spannable.removeSpan(span)
        }
    }

    protected fun removeStyleSpan(spannable: Spannable, startSelection: Int,
                                  endSelection: Int, styleToRemove: Int) {

        val spans = spannable.getSpans(startSelection, endSelection, MetricAffectingSpan::class.java)
        for (span in spans) {
            var stylesApplied = 0
            val stylesToApply: Int
            val spanStart: Int
            val spanEnd: Int
            val spanFlag: Int
            val spanUnd = span.underlying
            if (spanUnd is StyleSpan) {
                spanFlag = spannable.getSpanFlags(spanUnd)
                stylesApplied = spanUnd.style
                stylesToApply = stylesApplied and styleToRemove.inv()

                spanStart = spannable.getSpanStart(span)
                spanEnd = spannable.getSpanEnd(span)
                if (spanEnd >= 0 && spanStart >= 0) {
                    spannable.removeSpan(span)
                    spannable.setSpan(StyleSpan(stylesToApply), spanStart, spanEnd, spanFlag)
                }
            }
        }
    }

    private fun getSpanParts(spannable: Spannable,
                             startSelection: Int, endSelection: Int): ArrayList<SpanParts> {

        val spansParts = ArrayList<SpanParts>()
        val spans = spannable.getSpans(startSelection, endSelection, Any::class.java)
        for (span in spans) {
            if (span is CharacterStyle) {
                val spanParts = SpanParts()
                val spanStart = spannable.getSpanStart(span)
                val spanEnd = spannable.getSpanEnd(span)
                if (spanStart == startSelection && spanEnd == endSelection) continue
                spanParts.span_flag = spannable.getSpanFlags(span)
                spanParts.part1.span = CharacterStyle.wrap(span)
                spanParts.part1.start = spanStart
                spanParts.part1.end = startSelection
                spanParts.part2.span = CharacterStyle.wrap(span)
                spanParts.part2.start = endSelection
                spanParts.part2.end = spanEnd
                spansParts.add(spanParts)
            }
        }
        return spansParts
    }

    private inner class SpanParts internal constructor() {
        internal var span_flag: Int = 0
        internal var part1: Part
        internal var part2: Part

        init {
            part1 = Part()
            part2 = Part()
        }
    }

    private inner class Part {
        internal var span: CharacterStyle? = null
        internal var start: Int = 0
        internal var end: Int = 0
        internal fun canAplly(): Boolean {
            return start < end
        }
    }
}