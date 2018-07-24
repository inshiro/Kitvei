package na.kephas.kitvei.util

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import na.kephas.kitvei.App
import na.kephas.kitvei.R

object AdapterStyle {

    val Italics by lazy { CustomTypefaceSpan(Fonts.GentiumPlus_I) }
    val TextColor by lazy { ContextCompat.getColor(App.instance.applicationContext, R.color.textColor) }
    val NumColor by lazy { Color.parseColor("#877f66") }
    val WhiteColor by lazy { Color.parseColor("#ffffff") }
    val Transparent by lazy { Color.TRANSPARENT }
    val RedLetterColor by lazy { ContextCompat.getColor(App.instance.applicationContext, R.color.redletter_color_dark) }
    val HighLightColor by lazy { ContextCompat.getColor(App.instance.applicationContext, R.color.highlight_color_dark) }
    val HighlightFocusColor by lazy { ContextCompat.getColor(App.instance.applicationContext, R.color.highlight_focus_color) }
    val SearchNotFoundColor by lazy { ContextCompat.getColor(App.instance.applicationContext, R.color.search_not_found) }


}