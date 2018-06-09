package na.kephas.kitvei.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import na.kephas.kitvei.data.Bible
import na.kephas.kitvei.data.MiniSearchRepository
import na.kephas.kitvei.data.SearchRepository
import na.kephas.kitvei.data.VerseRepository
import na.kephas.kitvei.util.backgroundPool

/**
 * The ViewModel for [VerseListFragment].
 */
class MiniSearchViewModel internal constructor(
        private val miniSearchRepository: MiniSearchRepository
) : ViewModel() {

    fun getBooks(): List<String> = runBlocking {
        async(backgroundPool) {
            miniSearchRepository.getBooks()
        }.await()
    }

    fun getChapters(book: Int = 1): List<Int> = runBlocking {
        async(backgroundPool) {
            miniSearchRepository.getChapters(book)
        }.await()
    }

    fun getVerses(book: Int = 1, chapter: Int = 1): List<Int> = runBlocking {
        async(backgroundPool) {
            miniSearchRepository.getVerses(book, chapter)
        }.await()
    }

}
