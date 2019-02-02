package na.kephas.kitvei.data

import android.content.Context
import android.content.SharedPreferences

class Prefs (context: Context) {
    private val PREFS_FILENAME = "na.kephas.bible.Prefs"
    private val BOOK = "BOOKID"
    private val CHAPTER = "CHAPTER"
    private val BOOK_LENGTH = "BOOK_LENGTH"
    private val CHAPTER_LENGTH = "CHAPTER_LENGTH"
    private val DAY_MODE = "DAY_MODE"
    private val VP_POSITION = "VP_POSITION"
    private val MAIN_FONT_SIZE = "MAIN_FONT_SIZE"
    private val FONT_SIZE = "FONT_SIZE"
    private val THEME_ID = "THEME_ID"
    private val KJVSTYLE_ID = "KJVSTYLE_ID"
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    var Book: Int
        get() = prefs.getInt(BOOK, 1)
        set(value) = prefs.edit().putInt(BOOK, value).apply()
    var Chapter: Int
        get() = prefs.getInt(CHAPTER, 1)
        set(value) = prefs.edit().putInt(CHAPTER, value).apply()
    var BookLength: Int
        get() = prefs.getInt(BOOK_LENGTH, 50)
        set(value) = prefs.edit().putInt(BOOK_LENGTH, value).apply()
    var ChapterLength: Int
        get() = prefs.getInt(CHAPTER_LENGTH, 31)
        set(value) = prefs.edit().putInt(CHAPTER_LENGTH, value).apply()
    var dayMode: Boolean
        get() = prefs.getBoolean(DAY_MODE, true)
        set(value) = prefs.edit().putBoolean(DAY_MODE, value).apply()
    var VP_Position: Int
        get() = prefs.getInt(VP_POSITION, 0)
        set(value) = prefs.edit().putInt(VP_POSITION, value).apply()
    var textSizeMultiplier: Float
        get() = prefs.getFloat(FONT_SIZE, 1f)
        set(value) = prefs.edit().putFloat(FONT_SIZE, value).apply()
    var mainFontSize: Float
        get() = prefs.getFloat(MAIN_FONT_SIZE, 10f)
        set(value) = prefs.edit().putFloat(MAIN_FONT_SIZE, value).apply()
    var themeId: Int
        get() = prefs.getInt(THEME_ID, 0)
        set(value) = prefs.edit().putInt(THEME_ID, value).apply()
    var kjvStylingPref: Boolean
        get() = prefs.getBoolean(KJVSTYLE_ID, true)
        set(value) = prefs.edit().putBoolean(KJVSTYLE_ID, value).apply()
    /*var shouldDimDarkModeImages: Boolean
        get() = prefs.getFloat(FONT_SIZE, 1f)
        set(value) = prefs.edit().putFloat(FONT_SIZE, value).apply()*/
}