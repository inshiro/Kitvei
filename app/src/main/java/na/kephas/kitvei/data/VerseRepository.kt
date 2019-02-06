package na.kephas.kitvei.data

import androidx.annotation.WorkerThread
import androidx.sqlite.db.SimpleSQLiteQuery
import java.text.FieldPosition

/**
 * Repository module for handling data operations.
 */
class VerseRepository private constructor(private val verseDao: VerseDao) {

    fun getRow(position: Int) = verseDao.getRow(position)
    fun getPagePosition(book: Int, chapter: Int) = verseDao.getPagePosition(book,chapter)

    @WorkerThread
    fun getPages() = verseDao.getPages()

    fun getVersesRaw(book: Int, chapter: Int) = verseDao.getVersesRaw(SimpleSQLiteQuery("SELECT * FROM Bible WHERE book_id=? AND chapter_id=?", arrayOf<Any>(book, chapter)))

//book_id=? AND chapter_id=?
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
