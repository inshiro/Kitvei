package na.kephas.kitvei.data

/**
 * Repository module for handling data operations.
 */
class SearchRepository private constructor(private val searchDao: SearchDao) {

    fun getVerses(query: String) = searchDao.getVerses(query)

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: SearchRepository? = null

        fun getInstance(searchDao: SearchDao) =
                instance ?: synchronized(this) {
                    instance ?: SearchRepository(searchDao).also { instance = it }
                }
    }
}
