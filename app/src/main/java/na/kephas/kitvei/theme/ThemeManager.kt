package na.kephas.kitvei.theme

import android.content.Context
import android.widget.ImageView

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import na.kephas.kitvei.R


/**
 * Theme manager.
 *
 * This class is used to manage theme information.
 *
 */

class ThemeManager private constructor(context: Context) {

    var isLightTheme: Boolean = false
        private set

    init {
        val sharedPreferences = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE)
        this.isLightTheme = sharedPreferences.getBoolean(KEY_LIGHT_THEME, true)
    }

    fun setLightTheme(context: Context, lightTheme: Boolean) {
        this.isLightTheme = lightTheme
        val editor = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
        editor.putBoolean(KEY_LIGHT_THEME, lightTheme)
        editor.apply()
    }

    companion object {

        private var instance: ThemeManager? = null

        fun getInstance(context: Context): ThemeManager {
            if (instance == null) {
                synchronized(ThemeManager::class.java) {
                    if (instance == null) {
                        instance = ThemeManager(context)
                    }
                }
            }
            return instance!!
        }

        private val PREFERENCE_NAME = "mysplash_theme_manager"
        private val KEY_LIGHT_THEME = "light_theme"

        @ColorInt
        fun getPrimaryColor(context: Context): Int {
            val a = context.obtainStyledAttributes(intArrayOf(R.attr.colorPrimary))
            val color = a.getColor(0, ContextCompat.getColor(context, R.color.colorPrimary_light))
            a.recycle()
            return color
        }

        @ColorInt
        fun getPrimaryDarkColor(context: Context): Int {
            val a = context.obtainStyledAttributes(intArrayOf(R.attr.colorPrimaryDark))
            val color = a.getColor(0, ContextCompat.getColor(context, R.color.colorPrimaryDark_light))
            a.recycle()
            return color
        }

        @ColorInt
        fun getRootColor(context: Context): Int {
            val a = context.obtainStyledAttributes(R.styleable.ThemeColor)
            val color = a.getColor(
                    R.styleable.ThemeColor_root_color,
                    ContextCompat.getColor(context, R.color.colorRoot_light))
            a.recycle()
            return color
        }

        @ColorInt
        fun getLineColor(context: Context): Int {
            val a = context.obtainStyledAttributes(R.styleable.ThemeColor)
            val color = a.getColor(
                    R.styleable.ThemeColor_line_color,
                    ContextCompat.getColor(context, R.color.colorLine_light))
            a.recycle()
            return color
        }

        @ColorInt
        fun getTitleColor(context: Context): Int {
            val a = context.obtainStyledAttributes(R.styleable.ThemeColor)
            val color = a.getColor(
                    R.styleable.ThemeColor_title_color,
                    ContextCompat.getColor(context, R.color.colorTextTitle_light))
            a.recycle()
            return color
        }

        @ColorInt
        fun getSubtitleColor(context: Context): Int {
            val a = context.obtainStyledAttributes(R.styleable.ThemeColor)
            val color = a.getColor(
                    R.styleable.ThemeColor_subtitle_color,
                    ContextCompat.getColor(context, R.color.colorTextSubtitle_light))
            a.recycle()
            return color
        }

        @ColorInt
        fun getContentColor(context: Context): Int {
            val a = context.obtainStyledAttributes(R.styleable.ThemeColor)
            val color = a.getColor(
                    R.styleable.ThemeColor_content_color,
                    ContextCompat.getColor(context, R.color.colorTextContent_light))
            a.recycle()
            return color
        }

        fun setNavigationIcon(toolbar: Toolbar,
                              @DrawableRes lightResId: Int, @DrawableRes darkResId: Int) {
            if (getInstance(toolbar.context).isLightTheme) {
                toolbar.setNavigationIcon(lightResId)
            } else {
                toolbar.setNavigationIcon(darkResId)
            }
        }

        fun inflateMenu(toolbar: Toolbar,
                        @MenuRes lightResId: Int, @MenuRes darkResId: Int) {
            if (getInstance(toolbar.context).isLightTheme) {
                toolbar.inflateMenu(lightResId)
            } else {
                toolbar.inflateMenu(darkResId)
            }
        }

        fun setImageResource(imageView: ImageView,
                             @DrawableRes lightResId: Int, @DrawableRes darkResId: Int) {
            if (getInstance(imageView.context).isLightTheme) {
                imageView.setImageResource(lightResId)
            } else {
                imageView.setImageResource(darkResId)
            }
        }
    }
}