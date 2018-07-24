package na.kephas.kitvei.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.SeekBar

import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import na.kephas.kitvei.R


@SuppressLint("AppCompatCustomView")
internal class DiscreteSeekBar : SeekBar {
    private var dMin: Int = 0
    private var tickDrawable: Drawable? = null
    private var centerDrawable: Drawable? = null

    var value: Int
        get() = progress + dMin
        set(value) {
            progress = value - dMin
        }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, @AttrRes defStyleAttr: Int) {
        if (attrs != null) {
            val array = context.obtainStyledAttributes(attrs,
                    R.styleable.DiscreteSeekBar, defStyleAttr, 0)
            dMin = array.getInteger(R.styleable.DiscreteSeekBar_min, 0)
            max = max - dMin
            var id = array.getResourceId(R.styleable.DiscreteSeekBar_tickDrawable, 0)
            if (id != 0) {
                tickDrawable = ContextCompat.getDrawable(context, id)
            }
            id = array.getResourceId(R.styleable.DiscreteSeekBar_centerDrawable, 0)
            if (id != 0) {
                centerDrawable = ContextCompat.getDrawable(context, id)
            }
            array.recycle()
        }
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        val value = value
        if (value >= 0) {
            drawTickMarks(canvas, true, false)
            super.onDraw(canvas)
            drawTickMarks(canvas, false, true)
        } else {
            super.onDraw(canvas)
            drawTickMarks(canvas, true, true)
        }
    }

    fun drawTickMarks(canvas: Canvas, drawCenter: Boolean, drawOther: Boolean) {
        val max = max + dMin
        val value = value
        if (tickDrawable != null) {
            val halfW = if (tickDrawable!!.intrinsicWidth >= 0) tickDrawable!!.intrinsicWidth / 2 else 1
            val halfH = if (tickDrawable!!.intrinsicHeight >= 0) tickDrawable!!.intrinsicHeight / 2 else 1
            tickDrawable!!.setBounds(-halfW, -halfH, halfW, halfH)
        }
        if (centerDrawable != null) {
            val halfW = if (centerDrawable!!.intrinsicWidth >= 0) centerDrawable!!.intrinsicWidth / 2 else 1
            val halfH = if (centerDrawable!!.intrinsicHeight >= 0) centerDrawable!!.intrinsicHeight / 2 else 1
            centerDrawable!!.setBounds(-halfW, -halfH, halfW, halfH)
        }
        val tickSpacing = (width - paddingLeft - paddingRight).toFloat() / (max - dMin).toFloat()
        canvas.save()
        canvas.translate(paddingLeft.toFloat(), (height / 2).toFloat())
        for (i in dMin..max) {
            if (drawOther && tickDrawable != null && i > value) {
                tickDrawable!!.draw(canvas)
            }
            if (drawCenter && i == 0 && centerDrawable != null) {
                centerDrawable!!.draw(canvas)
            }
            canvas.translate(tickSpacing, 0.0f)
        }
        canvas.restore()
    }
}

