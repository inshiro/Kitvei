package na.kephas.kitvei.repository

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.sqlite.db.SimpleSQLiteQuery
import na.kephas.kitvei.util.backgroundPool
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.experimental.*

class MyViewModel(app: Application) : AndroidViewModel(app) {
    private val bibleDao: Bible.BibleDao by lazy {  AppDatabase[app].bibleDao()  }
    private var bibleList: LiveData<PagedList<Bible>>? = null
    lateinit var verseList: LiveData<PagedList<Bible>>
    lateinit var bookLength: LiveData<PagedList<Bible>>

    companion object {
        private const val PAGE_SIZE = 30
        private const val PREFETCH_DISTANCE = 30
        private const val ENABLE_PLACEHOLDERS = true
    }

    private lateinit var currentBook: MutableLiveData<Int>
    fun getCurrentBook(): MutableLiveData<Int> {
        if (!this::currentBook.isInitialized) {
            currentBook = MutableLiveData()
            currentBook.postValue(1)
        }
        return currentBook
    }

    private lateinit var currentChapter: MutableLiveData<Int>
    fun getCurrentChapter(): MutableLiveData<Int> {
        if (!this::currentChapter.isInitialized) {
            currentChapter = MutableLiveData()
            currentChapter.postValue(1)
        }
        return currentChapter
    }

    fun getAll(): List<Bible> = runBlocking {
        async(backgroundPool) {
            bibleDao.actualAll()
        }.await()
    }
    fun getBookNames(): List<String> = runBlocking {
        async(backgroundPool) {
            bibleDao.getBooks()
        }.await()
    }
    fun getChapterIds(book: Int = 1): List<Int> = runBlocking {
        async(backgroundPool) {
            bibleDao.getChapterIds(book)
        }.await()
    }
    fun getVerseIds(book: Int = 1, chapter: Int = 1): List<Int> = runBlocking {
        async(backgroundPool) {
            bibleDao.getVerseIds(book, chapter)
        }.await()
    }

   fun getVerseList(book: Int, chapter: Int): LiveData<PagedList<Bible>> = runBlocking {
       async(backgroundPool) {
           verseList = LivePagedListBuilder(bibleDao.getVerses(
                   SimpleSQLiteQuery("SELECT * FROM Bible WHERE book_id=? AND chapter_id=?", arrayOf<Any>(book, chapter))),
                   PagedList.Config.Builder()
                           .setPageSize(PAGE_SIZE)
                           .setPrefetchDistance(PREFETCH_DISTANCE)
                           .setEnablePlaceholders(ENABLE_PLACEHOLDERS)
                           .build())
                   .build()
           verseList
       }.await()
    }

    // RV Cannot be inside RSV https://is.gd/lra3Qy else paging will be on UI thread for some reason
    suspend fun searchList(str: String = ""): LiveData<PagedList<Bible>>? =
            withContext(backgroundPool) {
                bibleList = LivePagedListBuilder(bibleDao.searchVerses(str),
                        PagedList.Config.Builder()
                                .setPageSize(PREFETCH_DISTANCE)
                                .setPrefetchDistance(PREFETCH_DISTANCE)
                                .setEnablePlaceholders(ENABLE_PLACEHOLDERS)
                                .build())
                        .build()
                bibleList
            }


    suspend fun bookLength(book: Int = 1): LiveData<PagedList<Bible>> =
            withContext(backgroundPool) {
                bookLength = LivePagedListBuilder(bibleDao.getBookLength(
                        SimpleSQLiteQuery("SELECT * FROM Bible Where book_id=? group by chapter_id", arrayOf<Any>(book))),
                        PagedList.Config.Builder()
                                .setPageSize(PAGE_SIZE)
                                .setPrefetchDistance(PREFETCH_DISTANCE)
                                .setEnablePlaceholders(ENABLE_PLACEHOLDERS)
                                .build())
                        .build()

                bookLength
            }

}
