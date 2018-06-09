package na.kephas.kitvei.data

import androidx.sqlite.db.SimpleSQLiteQuery

/**
 * Repository module for handling data operations.
 */
class VerseRepository private constructor(private val verseDao: VerseDao) {

    fun getPages() = verseDao.getPages()

    fun getVerses(book: Int, chapter: Int) =
            verseDao.getVerses(SimpleSQLiteQuery("SELECT * FROM Bible WHERE book_id=? AND chapter_id=?", arrayOf<Any>(book, chapter)))

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: VerseRepository? = null

        fun getInstance(verseDao: VerseDao) =
                instance ?: synchronized(this) {
                    instance ?: VerseRepository(verseDao).also { instance = it }
                }
    }
}
