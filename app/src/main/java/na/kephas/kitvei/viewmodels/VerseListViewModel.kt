package na.kephas.kitvei.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import na.kephas.kitvei.data.Bible
import na.kephas.kitvei.data.VerseRepository
import na.kephas.kitvei.util.*

/**
 * The ViewModel for [VerseListFragment].
 */
class VerseListViewModel internal constructor(
        private val verseRepository: VerseRepository
) : ViewModel() {

    fun getRow(position: Int) = runBlocking(IO) {
        verseRepository.getRow(position)
    }

    fun getPagePosition(book: Int, chapter: Int) = runBlocking(IO) {
        verseRepository.getPagePosition(book, chapter)
    }

    fun getPages(): List<Bible> = runBlocking(IO) {
        verseRepository.getPages()
    }

    fun getVersesRaw(book: Int, chapter: Int) = runBlocking(IO) {
        verseRepository.getVersesRaw(book, chapter)
    }

    fun getVerses(book: Int, chapter: Int): LiveData<PagedList<Bible>> = runBlocking(IO) {
            LivePagedListBuilder(verseRepository.getVerses(book, chapter),
                    PagedList.Config.Builder()
                            .setPageSize(PAGE_SIZE)
                            .setPrefetchDistance(PREFETCH_DISTANCE)
                            .setEnablePlaceholders(ENABLE_PLACEHOLDERS)
                            .build())
                    .build()
    }

    companion object {
        private const val PAGE_SIZE = 30
        private const val PREFETCH_DISTANCE = 30
        private const val ENABLE_PLACEHOLDERS = true
    }
}
