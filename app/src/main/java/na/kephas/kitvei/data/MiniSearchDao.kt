package na.kephas.kitvei.data

import androidx.room.Dao
import androidx.room.Query

/**
 * The Data Access Object for the MiniSearch class.
 */
@Dao
interface MiniSearchDao {
    @Query("SELECT DISTINCT book_name from Bible")
    fun getBooks(): List<String>

    @Query("SELECT DISTINCT chapter_id from Bible WHERE book_id = :book")
    fun getChapters(book: Int): List<Int>

    @Query("SELECT DISTINCT verse_id from Bible WHERE book_id = :book AND chapter_id = :chapter")
    fun getVerses(book: Int, chapter: Int): List<Int>
}
