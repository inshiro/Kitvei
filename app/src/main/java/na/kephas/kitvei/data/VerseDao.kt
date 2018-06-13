package na.kephas.kitvei.data

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery

/**
 * The Data Access Object for the Verse class.
 */
@Dao
interface VerseDao {
    @Query("SELECT * from Bible GROUP BY book_id, chapter_id")
    fun getPages(): List<Bible>

    @RawQuery(observedEntities = arrayOf(Bible::class))
    fun getVersesRaw(query: SupportSQLiteQuery): List<Bible>

    @RawQuery(observedEntities = arrayOf(Bible::class))
    fun getVerses(query: SupportSQLiteQuery): DataSource.Factory<Int, Bible>
}
