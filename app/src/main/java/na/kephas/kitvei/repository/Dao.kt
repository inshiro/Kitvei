package na.kephas.kitvei.repository

import androidx.paging.DataSource
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.room.RawQuery

@Entity(tableName = "Bible")
data class Bible (

        @ColumnInfo(name = "book_id") var bookId: Int?=1,
        @ColumnInfo(name = "book_abbr") var bookAbbr: String?="",
        @ColumnInfo(name = "book_name") var bookName: String?="",
        @ColumnInfo(name = "chapter_id") var chapterId: Int?=1,
        @ColumnInfo(name = "verse_id") var verseId: Int?=1,
        @ColumnInfo(name = "verse_text") var verseText: String? = "",
        @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "id") var id: Int,
        @ColumnInfo(name = "section") var section: String? = ""


) {

    @Dao
    interface BibleDao {

        @Query("SELECT * from Bible GROUP BY book_id, chapter_id")
        fun actualAll(): List<Bible>


        @Query("SELECT * FROM Bible WHERE REPLACE(REPLACE(verse_text, '[', ''), ']','') LIKE '%' || :search || '%'")
        fun searchVerses(search: String?): DataSource.Factory<Int, Bible>

        @Query("SELECT DISTINCT book_name from Bible")
        fun getBooks(): List<String>

        @Query("SELECT DISTINCT chapter_id from Bible WHERE book_id = :book")
        fun getChapterIds(book: Int): List<Int>

        @Query("SELECT DISTINCT verse_id from Bible WHERE book_id = :book AND chapter_id = :chapter")
        fun getVerseIds(book: Int, chapter: Int): List<Int>

        @RawQuery(observedEntities = arrayOf(Bible::class))
        fun getVerses(query: SupportSQLiteQuery): DataSource.Factory<Int, Bible>

        @RawQuery(observedEntities = arrayOf(Bible::class))
        fun getBookLength(query: SupportSQLiteQuery):  DataSource.Factory<Int, Bible>

    }

}