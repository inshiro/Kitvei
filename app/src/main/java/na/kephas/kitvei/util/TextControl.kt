package na.kephas.kitvei.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.Gravity
import android.webkit.WebSettings
import androidx.appcompat.widget.AppCompatTextView

class TextControl(context: Context) : AppCompatTextView(context) {

    init {
        includeFontPadding = false
        gravity = gravity or Gravity.TOP
    }


    override fun onDraw(canvas: Canvas) {
        val textPaint = paint

        if (layout == null)
            return

        textPaint.color = currentTextColor //Paint.Color = new Android.Graphics.Color (CurrentTextColor);
        textPaint.drawableState = drawableState

        canvas.save()


        val offset = textSize - lineHeight //WebSettings.TextSize - LineHeight;
        canvas.translate(0f, offset)

        layout.draw(canvas)

        canvas.restore()
    }
}