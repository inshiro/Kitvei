package na.kephas.kitvei.data

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query

/**
 * The Data Access Object for the Search class.
 */
@Dao
interface SearchDao {
    @Query("SELECT * FROM Bible WHERE REPLACE(REPLACE(verse_text, '[', ''), ']','') LIKE '%' || :query || '%'")
    fun getVerses(query: String): DataSource.Factory<Int, Bible>
}
