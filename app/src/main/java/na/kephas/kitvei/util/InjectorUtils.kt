package na.kephas.kitvei.util

import android.content.Context
import na.kephas.kitvei.data.AppDatabase
import na.kephas.kitvei.data.MiniSearchRepository
import na.kephas.kitvei.data.SearchRepository
import na.kephas.kitvei.data.VerseRepository
import na.kephas.kitvei.viewmodels.MiniSearchViewModelFactory
import na.kephas.kitvei.viewmodels.SearchListViewModelFactory
import na.kephas.kitvei.viewmodels.VerseListViewModelFactory

/**
 * Static methods used to inject classes needed for various Activities and Fragments.
 */
object InjectorUtils {

    private fun getVerseRepository(context: Context): VerseRepository {
        return VerseRepository.getInstance(AppDatabase.getInstance(context).verseDao())
    }

    private fun getSearchRepository(context: Context): SearchRepository {
        return SearchRepository.getInstance(AppDatabase.getInstance(context).searchDao())
    }

    private fun getMiniSearchRepository(context: Context): MiniSearchRepository {
        return MiniSearchRepository.getInstance(AppDatabase.getInstance(context).miniSearchDao())
    }

    fun provideVerseListViewModelFactory(
            context: Context
    ): VerseListViewModelFactory {
        val repository = getVerseRepository(context)
        return VerseListViewModelFactory(repository)
    }

    fun provideSearchListViewModelFactory(
            context: Context
    ): SearchListViewModelFactory {
        val repository = getSearchRepository(context)
        return SearchListViewModelFactory(repository)
    }

    fun provideMiniSearchViewModelFactory(
            context: Context
    ): MiniSearchViewModelFactory {
        val repository = getMiniSearchRepository(context)
        return MiniSearchViewModelFactory(repository)
    }

}
