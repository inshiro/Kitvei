package na.kephas.kitvei.util

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.updatePadding


/*
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
}*/


class TextControl : AppCompatTextView {

    private var mAdditionalPadding: Int = 0

    private val additionalPadding: Int
        get() {
            val textSize = textSize

            val textView = TextView(context)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            textView.setLines(1)
            textView.measure(0, 0)
            val measuredHeight = textView.measuredHeight
            if (measuredHeight - textSize > 0) {
                mAdditionalPadding = (measuredHeight - textSize).toInt()
                //Log.v("NoPaddingTextView", "onMeasure: height=$measuredHeight textSize=$textSize mAdditionalPadding=$mAdditionalPadding")
            }
            return mAdditionalPadding
        }

    constructor(context: Context) : super(context) {
        init()
    }


    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        //includeFontPadding = false
        gravity = gravity or Gravity.TOP
        updatePadding(0,0,0,0)
    }

    override fun onDraw(canvas: Canvas) {
        //val yOff = -mAdditionalPadding / 0.85f // 6f
        val yOff = (textSize - lineHeight) / if (text.contains("J")) 0.715f else 0.805f
        canvas.translate(0f, yOff)
        super.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpecc: Int) {
        var heightMeasureSpec = heightMeasureSpecc
        additionalPadding
        val mode = View.MeasureSpec.getMode(heightMeasureSpec)
        if (mode != View.MeasureSpec.EXACTLY) {
            val measureHeight = measureHeight(text.toString(), widthMeasureSpec)
            var height = measureHeight - mAdditionalPadding
            height += paddingTop + paddingBottom
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            if (text.contains("J")) heightMeasureSpec += 75
            heightMeasureSpec += additionalPadding
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun measureHeight(text: String, widthMeasureSpec: Int): Int {
        val textSize = textSize
        val textView = TextView(context)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        textView.text = text
        textView.measure(widthMeasureSpec, 0)
        return textView.measuredHeight
    }
}