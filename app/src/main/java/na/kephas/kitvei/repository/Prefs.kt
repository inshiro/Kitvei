package na.kephas.kitvei.repository

import android.content.Context
import android.content.SharedPreferences

class Prefs (context: Context) {
    val PREFS_FILENAME = "na.kephas.bible.prefs"
    val BOOK = "BOOKID"
    val CHAPTER = "CHAPTER"
    val BOOK_LENGTH = "BOOK_LENGTH"
    val CHAPTER_LENGTH = "CHAPTER_LENGTH"
    val DAY_MODE = "DAY_MODE"
    val VP_POSITION = "VP_POSITION"
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
}