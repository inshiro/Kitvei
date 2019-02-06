package na.kephas.kitvei.viewmodels

import androidx.annotation.UiThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.*
import na.kephas.kitvei.data.Bible
import na.kephas.kitvei.data.VerseRepository
import na.kephas.kitvei.util.*

object Coroutines {
    fun io(work: suspend (() -> Unit)): Job =
            CoroutineScope(Dispatchers.IO).launch {
                work()
            }

    fun <T : Any> ioThenMain(work: suspend (() -> T?), callback: ((T?) -> Unit)): Job =
            CoroutineScope(Dispatchers.Main).launch {
                val data = CoroutineScope(Dispatchers.IO).async rt@{
                    return@rt work()
                }.await()
                callback(data)
            }

}

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

    fun getPages(): List<Bible> {
        val pages = verseRepository.getPages()
        return pages
    }


    private val _list = MutableLiveData<List<Bible>>()
    val list: LiveData<List<Bible>> get() = _list

    // Use livedata and viewmodel to observe when you get the data, it updates the observer.
    // When called this method runs in the background and when it's done it updates the value.
    // When the value is updated, the observer reacts to it.
    @UiThread
    fun getPages2(): LiveData<List<Bible>>  {
        Coroutines.ioThenMain({
            verseRepository.getPages()
        }) {
            _list.value = it
        }
        return list
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
