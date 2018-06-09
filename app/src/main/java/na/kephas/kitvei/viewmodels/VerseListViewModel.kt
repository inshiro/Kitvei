package na.kephas.kitvei.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import na.kephas.kitvei.data.Bible
import na.kephas.kitvei.data.VerseRepository
import na.kephas.kitvei.util.backgroundPool

/**
 * The ViewModel for [VerseListFragment].
 */
class VerseListViewModel internal constructor(
        private val verseRepository: VerseRepository
) : ViewModel() {

    fun getPages(): List<Bible> = runBlocking {
        async(backgroundPool) {
            verseRepository.getPages()
        }.await()
    }

    fun getVerses(book: Int, chapter: Int): LiveData<PagedList<Bible>> = runBlocking {
        async(backgroundPool) {
            LivePagedListBuilder(verseRepository.getVerses(book, chapter),
                    PagedList.Config.Builder()
                            .setPageSize(PAGE_SIZE)
                            .setPrefetchDistance(PREFETCH_DISTANCE)
                            .setEnablePlaceholders(ENABLE_PLACEHOLDERS)
                            .build())
                    .build()
        }.await()
    }

    companion object {
        private const val PAGE_SIZE = 30
        private const val PREFETCH_DISTANCE = 30
        private const val ENABLE_PLACEHOLDERS = true
    }
}
