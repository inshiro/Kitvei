package na.kephas.kitvei.util

import android.content.Context
import android.graphics.Canvas
import android.text.TextPaint
import android.util.AttributeSet
import android.view.Gravity

import androidx.appcompat.widget.AppCompatTextView

@Suppress("UNUSED_PARAMETER")
class TopAlignedTextView @JvmOverloads constructor(context: Context, defStyle: Int = 0) : AppCompatTextView(context) {

    init {
        includeFontPadding = false //remove the font padding
        gravity = gravity or Gravity.TOP
    }

    override fun onDraw(canvas: Canvas) {
        val textPaint = paint
        textPaint.color = currentTextColor
        textPaint.drawableState = drawableState
        canvas.save()

        //remove extra font padding
        val yOffset = height - baseline
        canvas.translate(0f, (-yOffset / 2).toFloat())

        if (layout != null) {
            layout.draw(canvas)
        }
        canvas.restore()
    }
}