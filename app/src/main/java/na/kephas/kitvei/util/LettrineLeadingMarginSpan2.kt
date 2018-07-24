package na.kephas.kitvei.util

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan


class LettrineLeadingMarginSpan2(private var lines: Int, private var margin: Int) : LeadingMarginSpan.LeadingMarginSpan2 {


    override fun getLeadingMargin(first: Boolean): Int {
        return if (first) margin else 0
    }

    override fun getLeadingMarginLineCount(): Int {
        return lines
    }

    override fun drawLeadingMargin(c: Canvas, p: Paint, x: Int, dir: Int,
                                   top: Int, baseline: Int, bottom: Int, text: CharSequence,
                                   start: Int, end: Int, first: Boolean, layout: Layout) {
    }

}