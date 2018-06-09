package na.kephas.kitvei.data

/**
 * Repository module for handling data operations.
 */
class MiniSearchRepository private constructor(private val miniSearchDao: MiniSearchDao) {

    fun getBooks() = miniSearchDao.getBooks()

    fun getChapters(book: Int) = miniSearchDao.getChapters(book)

    fun getVerses(book: Int, chapter: Int) = miniSearchDao.getVerses(book, chapter)

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: MiniSearchRepository? = null

        fun getInstance(miniSearchDao: MiniSearchDao) =
                instance ?: synchronized(this) {
                    instance ?: MiniSearchRepository(miniSearchDao).also { instance = it }
                }
    }
}
