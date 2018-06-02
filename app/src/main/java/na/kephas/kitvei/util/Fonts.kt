package na.kephas.kitvei.util

import android.graphics.Typeface
import na.kephas.kitvei.App

object Fonts {
    private val fontAssets by lazy { App.instance.assets }

    private const val Merriweather_Black_Path = "fonts/Merriweather-Black.otf"
    val Merriweather_Black: Typeface by lazy { Typeface.createFromAsset(fontAssets, Merriweather_Black_Path) }

    private const val GentiumPlus_R_Path = "fonts/GentiumPlus-R.otf"
    val GentiumPlus_R: Typeface by lazy { Typeface.createFromAsset(fontAssets, GentiumPlus_R_Path) }
}