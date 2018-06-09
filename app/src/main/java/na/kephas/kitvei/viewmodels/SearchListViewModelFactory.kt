package na.kephas.kitvei.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import na.kephas.kitvei.data.SearchRepository
import na.kephas.kitvei.data.VerseRepository


/**
 * Factory for creating a [SearchListViewModel] with a constructor that takes a [SearchRepository].
 */
class SearchListViewModelFactory(
        private val repository: SearchRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = SearchListViewModel(repository) as T

}
